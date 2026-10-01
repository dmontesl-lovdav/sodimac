export const ACCESS_DENIED_MESSAGE_KEY = "WRN7038";
export const WRN7038_PROFILE_FALLBACK =
  "El perfil asignado a su usuario no se encuentra configurado para acceder al Módulo Fiscal. Favor de contactar al usuario administrador para validar su configuración y permisos de acceso.";
export const WRN7038_ROLE_FALLBACK =
  "El rol asignado a su usuario no se encuentra configurado para acceder al Módulo Fiscal. Favor de contactar al usuario administrador para validar su configuración y permisos de acceso.";
export const MACRO_ROL_PERFIL_CATALOG = "CATMACROROLPERFIL";
export const MACRO_ROL_ROL_CATALOG = "CATMACROROLROLUSUARIO";

export interface CatalogRow {
  id?: number;
  key?: string | null;
  externalKey?: string | null;
  value?: string | null;
  description?: string | null;
  sortOrder?: number | null;
}

export interface UserAssignments {
  profileIds: number[];
  roleIds: number[];
  multipleProfiles: boolean;
  profileIsFromFront?: boolean | null;
  frontRoleIds?: number[];
}

export function normalizeLabel(value: string | null | undefined): string {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

export function uniqueStrings(values: Array<string | null | undefined>): string[] {
  const seen = new Set<string>();
  const out: string[] = [];
  for (const raw of values) {
    const value = String(raw ?? "").trim();
    if (!value) continue;
    const key = value.toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(value);
  }
  return out;
}

export function extractMacroRoles(token: Record<string, unknown> | null | undefined): string[] {
  if (!token) return [];
  const roles: string[] = [];
  const realm = token.realm_access;
  if (realm && typeof realm === "object") {
    const list = (realm as { roles?: unknown }).roles;
    if (Array.isArray(list)) {
      for (const role of list) {
        if (typeof role === "string") roles.push(role);
      }
    }
  }
  return uniqueStrings(roles);
}

function asRecord(value: unknown): Record<string, unknown> | null {
  return value && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

export function extractDetails(payload: unknown): CatalogRow[] {
  if (Array.isArray(payload)) {
    return payload.filter((row) => row && typeof row === "object") as CatalogRow[];
  }
  const record = asRecord(payload);
  if (record && Array.isArray(record.details)) {
    return record.details.filter((row) => row && typeof row === "object") as CatalogRow[];
  }
  return [];
}

export function conversionMatchesMacroRole(row: CatalogRow, macroRole: string): boolean {
  const wanted = normalizeLabel(macroRole);
  const externalKey = normalizeLabel(row.externalKey);
  return Boolean(wanted) && Boolean(externalKey) && wanted === externalKey;
}

export function findConversionRows(details: CatalogRow[], macroRoles: string[]): CatalogRow[] {
  const matches: CatalogRow[] = [];
  const seen = new Set<string>();
  for (const role of macroRoles) {
    for (const row of details) {
      if (!conversionMatchesMacroRole(row, role)) continue;
      const key = `${row.id ?? ""}:${row.externalKey ?? ""}:${row.value ?? ""}`;
      if (seen.has(key)) continue;
      seen.add(key);
      matches.push(row);
    }
  }
  return matches.sort((a, b) => Number(a.sortOrder ?? 0) - Number(b.sortOrder ?? 0));
}

export function conversionValueIds(rows: CatalogRow[]): number[] {
  const ids: number[] = [];
  const seen = new Set<number>();
  for (const row of rows) {
    const id = Number(String(row.value ?? "").trim());
    if (!Number.isInteger(id) || id <= 0 || seen.has(id)) continue;
    seen.add(id);
    ids.push(id);
  }
  return ids;
}

export function deniedWarning(kind: "profile" | "role"): string {
  return kind === "role" ? WRN7038_ROLE_FALLBACK : WRN7038_PROFILE_FALLBACK;
}

export function isoDateOffset(days = 0, from = new Date()): string {
  const date = new Date(from.getTime());
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
}

export function unwrapData<T>(body: unknown): T {
  if (body && typeof body === "object" && "data" in body) {
    return (body as { data: T }).data;
  }
  return body as T;
}

function positiveId(value: unknown): number | null {
  const id = Number(value);
  return Number.isInteger(id) && id > 0 ? id : null;
}

function readIsFromFront(value: unknown, defaultValue = true): boolean {
  if (value === false || value === 0 || value === "0" || value === "false") return false;
  if (value === true || value === 1 || value === "1" || value === "true") return true;
  return defaultValue;
}

export function parseUserCatalogAssignments(detail: unknown): UserAssignments {
  const record = asRecord(detail);
  const profile = asRecord(record?.profile);
  const profileId = positiveId(profile?.id);
  const profileIds = profileId ? [profileId] : [];

  const rolesWrap = asRecord(record?.roles);
  const roleRows = Array.isArray(rolesWrap?.items)
    ? rolesWrap.items
    : Array.isArray(record?.roles)
      ? record.roles
      : [];
  const roleIds: number[] = [];
  const frontRoleIds: number[] = [];
  const seen = new Set<number>();
  for (const row of roleRows) {
    const item = asRecord(row);
    const id = positiveId(item?.id);
    if (!id || seen.has(id)) continue;
    seen.add(id);
    roleIds.push(id);
    if (readIsFromFront(item?.isFromFront, true)) frontRoleIds.push(id);
  }

  return {
    profileIds,
    roleIds,
    multipleProfiles: Boolean(record?.multipleProfilesDetected) || profileIds.length > 1,
    profileIsFromFront: profileId ? readIsFromFront(profile?.isFromFront, true) : null,
    frontRoleIds,
  };
}

export function sameNumberSet(left: number[], right: number[]): boolean {
  if (left.length !== right.length) return false;
  const a = [...left].sort((x, y) => x - y);
  const b = [...right].sort((x, y) => x - y);
  return a.every((value, index) => value === b[index]);
}

export function idsNotInExpected(current: number[], expected: number[]): number[] {
  const want = new Set(expected);
  return current.filter((id) => !want.has(id));
}

export function idsMissingFrom(current: number[], expected: number[]): number[] {
  const have = new Set(current);
  return expected.filter((id) => !have.has(id));
}

export function assignmentsMatchExpected(
  current: UserAssignments,
  profileId: number,
  roleIds: number[]
): boolean {
  if (current.multipleProfiles) return false;
  if (!current.profileIds.length) return false;

  const profileFromFront = current.profileIsFromFront !== false;
  if (profileFromFront && current.profileIds[0] !== profileId) return false;

  const have = new Set(current.roleIds);
  if (roleIds.some((id) => !have.has(id))) return false;

  const frontRoles = current.frontRoleIds ?? current.roleIds;
  const expected = new Set(roleIds);
  return !frontRoles.some((id) => !expected.has(id));
}

export function axiosErrorMessage(error: unknown): string | undefined {
  const record = asRecord(error);
  const response = asRecord(record?.response);
  const data = asRecord(response?.data);
  const message = data?.message ?? record?.message;
  return typeof message === "string" && message.trim() ? message.trim() : undefined;
}
