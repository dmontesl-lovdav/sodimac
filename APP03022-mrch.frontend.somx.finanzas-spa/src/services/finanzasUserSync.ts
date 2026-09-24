import { createApiClient } from "@/services/ApiClient";
import ConfigurationBuilder from "@/configuration/ConfigurationBuilder";
import { localHomeStore } from "@/store/localStore";
import { fetchCatalogDetails, fetchProviders } from "@/utils/utils";
import { getCurrentUserKey } from "@/shared/security/currentUserKey";
import {
  ACCESS_DENIED_MESSAGE_KEY,
  LOCAL_PERFIL_CANDIDATES,
  LOCAL_ROL_CANDIDATES,
  MACRO_ROL_PERFIL_CANDIDATES,
  MACRO_ROL_ROL_CANDIDATES,
  assignmentsMatchExpected,
  axiosErrorMessage,
  catalogMessageText,
  extractMacroRoles,
  extractMxTaxIds,
  findConversionRows,
  idsMissingFrom,
  idsNotInExpected,
  isoDateOffset,
  isUsableCatalogMessage,
  loadCatalogByCandidates,
  matchSupplierByRfc,
  parseUserCatalogAssignments,
  resolveDeniedWarning,
  resolveLocalCatalogIds,
  unwrapData,
  writeSessionSupplier,
  type SessionSupplier,
  type UserAssignments,
} from "./finanzasUserSync.helpers";

export type FinanzasUserSyncResult =
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
};

const utilUrl = process.env.CATALOGS_API_URL;
const api = createApiClient({ baseUrl: utilUrl });

let inFlight: Promise<FinanzasUserSyncResult> | null = null;

export function resetFinanzasUserSyncForTests(): void {
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

async function loadDeniedMessage(kind: "profile" | "role"): Promise<string> {
  const payload = await fetchCatalogDetails(`message/${ACCESS_DENIED_MESSAGE_KEY}`);
  let text = catalogMessageText(payload, "");
  if (!isUsableCatalogMessage(text, ACCESS_DENIED_MESSAGE_KEY)) {
    try {
      const body = await api.request<unknown>(
        `catalog/message/${ACCESS_DENIED_MESSAGE_KEY}`,
        "get"
      );
      text = catalogMessageText(unwrapData(body) ?? body, "");
    } catch {
      text = "";
    }
  }
  return resolveDeniedWarning(text, kind);
}

async function deniedResult(kind: "profile" | "role"): Promise<FinanzasUserSyncResult> {
  return {
    status: "denied",
    messageKey: ACCESS_DENIED_MESSAGE_KEY,
    deniedKind: kind,
    message: await loadDeniedMessage(kind),
  };
}

async function persistMxSupplier(token: Record<string, unknown>): Promise<SessionSupplier | null> {
  const rfcs = extractMxTaxIds(token);
  if (rfcs.length === 0) {
    writeSessionSupplier(null);
    return null;
  }
  const providers = await fetchProviders();
  const list = Array.isArray(providers)
    ? (providers as Array<Record<string, unknown>>)
    : [];
  const supplier = matchSupplierByRfc(list, rfcs);
  writeSessionSupplier(supplier);
  return supplier;
}

async function readAssignedUserIds(path: string): Promise<number[]> {
  const body = await api.request<unknown>(path, "get");
  const lists = unwrapData<AssignmentLists>(body);
  return (lists?.assigned ?? [])
    .map((row) => Number(row.id))
    .filter((id) => Number.isInteger(id) && id > 0);
}

async function saveAssignedUserIds(path: string, selectedIds: number[]): Promise<void> {
  await api.request(path, "put", { selectedIds });
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
  if (assigned.includes(userId)) {
    await saveAssignedUserIds(path, assigned);
    return;
  }
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

async function unlinkCurrentAssignments(userId: number, current: UserAssignments): Promise<void> {
  for (const profileId of current.profileIds) {
    await unlinkUser(profileUsersPath(profileId), userId);
  }
  let leftover = await readCurrentAssignments(userId);
  for (let i = 0; i < 4 && leftover.profileIds.length; i += 1) {
    await unlinkUser(profileUsersPath(leftover.profileIds[0]!), userId);
    leftover = await readCurrentAssignments(userId);
  }
  for (const roleId of current.roleIds) {
    await unlinkUser(roleUsersPath(roleId), userId);
  }
}

async function applyExpectedAssignments(
  userId: number,
  current: UserAssignments,
  profileId: number,
  roleIds: number[]
): Promise<void> {
  const profileChanged =
    current.multipleProfiles || !current.profileIds.length || current.profileIds[0] !== profileId;
  if (profileChanged) {
    await linkUser(profileUsersPath(profileId), userId);
  }

  for (const roleId of idsNotInExpected(current.roleIds, roleIds)) {
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

async function runSync(): Promise<FinanzasUserSyncResult> {
  if (typeof window === "undefined") return { status: "skipped" };
  if (ConfigurationBuilder.localDeployment) return { status: "skipped" };

  const token = readTokenDecoded();
  const userKey = getCurrentUserKey() || String(token.sub ?? "").trim();
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
    const [perfilConv, rolConv, perfiles, roles] = await Promise.all([
      loadCatalogByCandidates(fetchCatalogDetails, MACRO_ROL_PERFIL_CANDIDATES),
      loadCatalogByCandidates(fetchCatalogDetails, MACRO_ROL_ROL_CANDIDATES),
      loadCatalogByCandidates(fetchCatalogDetails, LOCAL_PERFIL_CANDIDATES),
      loadCatalogByCandidates(fetchCatalogDetails, LOCAL_ROL_CANDIDATES),
    ]);

    const macroRoles = extractMacroRoles(token);
    const profileConversions = findConversionRows(perfilConv?.details ?? [], macroRoles);
    const roleConversions = findConversionRows(rolConv?.details ?? [], macroRoles);

    const email = typeof token.email === "string" ? token.email : undefined;
    const userId = await findCatalogUserId(userKey, email);
    const current = userId ? await readCurrentAssignments(userId) : EMPTY_ASSIGNMENTS;
    const hadAssignment = current.profileIds.length > 0 || current.roleIds.length > 0;

    if (profileConversions.length === 0) {
      if (userId) await unlinkCurrentAssignments(userId, current);
      return deniedResult("profile");
    }
    if (roleConversions.length === 0) {
      if (userId) await unlinkCurrentAssignments(userId, current);
      return deniedResult("role");
    }

    const profileIds = resolveLocalCatalogIds(
      perfiles?.details ?? [],
      profileConversions.map((row) => String(row.value || row.description || ""))
    );
    const roleIds = resolveLocalCatalogIds(
      roles?.details ?? [],
      roleConversions.map((row) => String(row.value || row.description || ""))
    );

    const profileId = profileIds[0];
    if (!profileId) {
      if (userId) await unlinkCurrentAssignments(userId, current);
      return deniedResult("profile");
    }
    if (roleIds.length === 0) {
      if (userId) await unlinkCurrentAssignments(userId, current);
      return deniedResult("role");
    }

    if (!userId) {
      return {
        status: "error",
        message: "No fue posible localizar el usuario en el catálogo de seguridad.",
      };
    }

    if (assignmentsMatchExpected(current, profileId, roleIds)) {
      await persistMxSupplier(token);
      return { status: "configured" };
    }

    await applyExpectedAssignments(userId, current, profileId, roleIds);
    await invalidateBackendCache(userKey);
    await persistMxSupplier(token);
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

export async function syncFinanzasUser(): Promise<FinanzasUserSyncResult> {
  if (inFlight) return inFlight;

  inFlight = runSync().finally(() => {
    inFlight = null;
  });

  return inFlight;
}

export function getFinanzasUserSyncInFlight(): Promise<FinanzasUserSyncResult> | null {
  return inFlight;
}

export { readSessionSupplier } from "./finanzasUserSync.helpers";
