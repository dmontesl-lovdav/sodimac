import { createApiClient } from "@/services/apiClient";
import { localHomeStore } from "@/store/localStore";
import { getCurrentUserCatalogKey } from "@/features/security/utils/currentUserCatalogKey";
import {
  ACCESS_DENIED_MESSAGE_KEY,
  MACRO_ROL_PERFIL_CATALOG,
  MACRO_ROL_ROL_CATALOG,
  assignmentsMatchExpected,
  axiosErrorMessage,
  conversionValueIds,
  deniedWarning,
  extractDetails,
  extractMacroRoles,
  findConversionRows,
  idsMissingFrom,
  idsNotInExpected,
  isLocalDeployment,
  isoDateOffset,
  parseUserCatalogAssignments,
  unwrapData,
  type UserAssignments,
} from "./utilityUserSync.helpers";

export type UtilityUserSyncResult =
  | { status: "skipped" }
  | { status: "configured" }
  | { status: "assigned" }
  | { status: "reassigned" }
  | { status: "denied"; messageKey: string; message: string; deniedKind: "profile" | "role" }
  | { status: "error"; message: string };

interface UserCatalogRow {
  id?: number;
  username?: string;
  email?: string | null;
}

interface AssignmentLists {
  assigned?: Array<{ id?: number }>;
}

const EMPTY_ASSIGNMENTS: UserAssignments = {
  profileIds: [],
  roleIds: [],
  multipleProfiles: false,
  profileIsFromFront: null,
  frontRoleIds: [],
};

const utilUrl =
  process.env.CATALOGS_API_URL ??
  process.env.API_URL ??
  process.env.API_BASE_URL;
const api = createApiClient({ baseUrl: utilUrl });

let inFlight: Promise<UtilityUserSyncResult> | null = null;

export function resetUtilityUserSyncForTests(): void {
  inFlight = null;
}

function readTokenDecoded(): Record<string, unknown> {
  try {
    const decoded = (localHomeStore.getState() as {
      authentication?: { tokenDecoded?: Record<string, unknown> };
    })?.authentication?.tokenDecoded;
    return decoded && typeof decoded === "object" ? decoded : {};
  } catch {
    return {};
  }
}

async function fetchConversionCatalog(code: string): Promise<unknown> {
  try {
    return await api.request(`catalog/${code}`, "get");
  } catch {
    return null;
  }
}

async function registerUtilityUser(): Promise<void> {
  await api.request("security/user-utility", "post", {});
}

async function findCatalogUserId(userKey: string, email?: string): Promise<number | null> {
  const params: Record<string, string | number> = {
    startDate: "2000-01-01",
    endDate: isoDateOffset(1),
    entityId: userKey,
    limit: 20,
    status: 1,
  };
  if (email) params.email = email;

  const body = await api.request<unknown>("security/user-catalog", "get", undefined, { params });
  const data = unwrapData<{ items?: UserCatalogRow[] }>(body);
  const items = Array.isArray(data?.items) ? data.items : [];
  const wantedKey = userKey.trim().toLowerCase();
  const wantedEmail = String(email ?? "").trim().toLowerCase();

  const match =
    items.find((row) => String(row.username ?? "").trim().toLowerCase() === wantedKey) ??
    items.find((row) => String(row.email ?? "").trim().toLowerCase() === wantedEmail) ??
    items.find((row) => Number(row.id) > 0);

  const id = Number(match?.id);
  return Number.isInteger(id) && id > 0 ? id : null;
}

function deniedResult(kind: "profile" | "role"): UtilityUserSyncResult {
  return {
    status: "denied",
    messageKey: ACCESS_DENIED_MESSAGE_KEY,
    deniedKind: kind,
    message: deniedWarning(kind),
  };
}

async function readAssignedUserIds(path: string): Promise<number[]> {
  const body = await api.request<unknown>(path, "get");
  const lists = unwrapData<AssignmentLists>(body);
  return (lists?.assigned ?? [])
    .map((row) => Number(row.id))
    .filter((id) => Number.isInteger(id) && id > 0);
}

async function saveAssignedUserIds(path: string, selectedIds: number[]): Promise<void> {
  await api.request(path, "put", { selectedIds, isFromFront: 1 });
}

function profileUsersPath(profileId: number): string {
  return `security/profiles/${profileId}/users`;
}

function roleUsersPath(roleId: number): string {
  return `security/roles/${roleId}/users`;
}

async function unlinkUser(path: string, userId: number): Promise<void> {
  const assigned = await readAssignedUserIds(path);
  if (!assigned.includes(userId)) return;
  await saveAssignedUserIds(
    path,
    assigned.filter((id) => id !== userId)
  );
}

async function linkUser(path: string, userId: number): Promise<void> {
  const assigned = await readAssignedUserIds(path);
  if (assigned.includes(userId)) return;
  await saveAssignedUserIds(path, [...assigned, userId]);
}

async function readCurrentAssignments(userId: number): Promise<UserAssignments> {
  try {
    const body = await api.request<unknown>(
      `security/users/${userId}/catalog-detail`,
      "get",
      undefined,
      { params: { sectionSize: 50, rolesPage: 1 } }
    );
    return parseUserCatalogAssignments(unwrapData(body));
  } catch {
    return EMPTY_ASSIGNMENTS;
  }
}

async function applyExpectedAssignments(
  userId: number,
  current: UserAssignments,
  profileId: number,
  roleIds: number[]
): Promise<void> {
  const hasAdminProfile =
    current.profileIds.length > 0 && current.profileIsFromFront === false;
  const profileChanged =
    current.multipleProfiles || !current.profileIds.length || current.profileIds[0] !== profileId;
  if (!hasAdminProfile && profileChanged) {
    await linkUser(profileUsersPath(profileId), userId);
  }

  const frontRoleIds = current.frontRoleIds ?? current.roleIds;
  for (const roleId of idsNotInExpected(frontRoleIds, roleIds)) {
    await unlinkUser(roleUsersPath(roleId), userId);
  }
  for (const roleId of idsMissingFrom(current.roleIds, roleIds)) {
    await linkUser(roleUsersPath(roleId), userId);
  }
}

async function invalidateBackendCache(userKey: string): Promise<void> {
  try {
    await api.request("security/user-details/cache", "delete", undefined, {
      params: { userKey },
    });
  } catch {
    /* la invalidación no debe bloquear el acceso ya asignado */
  }
}

async function runSync(): Promise<UtilityUserSyncResult> {
  if (typeof window === "undefined") return { status: "skipped" };
  if (isLocalDeployment()) return { status: "skipped" };

  const token = readTokenDecoded();
  const userKey = getCurrentUserCatalogKey() || String(token.sub ?? "").trim();
  if (!userKey) return { status: "skipped" };

  try {
    await registerUtilityUser();
  } catch (error) {
    return {
      status: "error",
      message: axiosErrorMessage(error) ?? "No fue posible registrar el usuario en catálogo.",
    };
  }

  try {
    const [perfilConv, rolConv] = await Promise.all([
      fetchConversionCatalog(MACRO_ROL_PERFIL_CATALOG),
      fetchConversionCatalog(MACRO_ROL_ROL_CATALOG),
    ]);

    const macroRoles = extractMacroRoles(token);
    const profileIds = conversionValueIds(
      findConversionRows(extractDetails(perfilConv), macroRoles)
    );
    const roleIds = conversionValueIds(findConversionRows(extractDetails(rolConv), macroRoles));
    const profileId = profileIds[0];
    if (!profileId) return deniedResult("profile");
    if (roleIds.length === 0) return deniedResult("role");

    const email = typeof token.email === "string" ? token.email : undefined;
    const userId = await findCatalogUserId(userKey, email);
    if (!userId) {
      return {
        status: "error",
        message: "No fue posible localizar el usuario en el catálogo de seguridad.",
      };
    }

    const current = await readCurrentAssignments(userId);
    const hadAssignment = current.profileIds.length > 0 || current.roleIds.length > 0;

    if (assignmentsMatchExpected(current, profileId, roleIds)) {
      return { status: "configured" };
    }

    await applyExpectedAssignments(userId, current, profileId, roleIds);
    await invalidateBackendCache(userKey);
    return { status: hadAssignment ? "reassigned" : "assigned" };
  } catch (error) {
    return {
      status: "error",
      message:
        axiosErrorMessage(error) ??
        "No fue posible asignar el perfil o rol local del usuario.",
    };
  }
}

export async function syncUtilityUser(): Promise<UtilityUserSyncResult> {
  if (inFlight) return inFlight;

  inFlight = runSync().finally(() => {
    inFlight = null;
  });

  return inFlight;
}

export function getUtilityUserSyncInFlight(): Promise<UtilityUserSyncResult> | null {
  return inFlight;
}

/** Compatibilidad: la home antigua llamaba este nombre sin esperar resultado. */
export function syncUserToCatalogs(): void {
  void syncUtilityUser();
}
