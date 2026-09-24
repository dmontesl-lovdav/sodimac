export const MACRO_ROL_PERFIL_CANDIDATES = [
  "CatMacroRolPerfil",
  "CATMACROROLPERFIL",
];

export const MACRO_ROL_ROL_CANDIDATES = [
  "CatMacroRolRolUsuario",
  "CATMACROROLROLUSUARIO",
];

export const LOCAL_PERFIL_CANDIDATES = ["CatPerfil"];
export const LOCAL_ROL_CANDIDATES = ["CatRol"];
export const ACCESS_DENIED_MESSAGE_KEY = "WRN7038";
export const WRN7038_PROFILE_FALLBACK =
  "El perfil asignado a su usuario no se encuentra configurado para acceder al Módulo Financiero. Favor de contactar al usuario administrador para validar su configuración y permisos de acceso.";
export const WRN7038_ROLE_FALLBACK =
  "El rol asignado a su usuario no se encuentra configurado para acceder al Módulo Financiero. Favor de contactar al usuario administrador para validar su configuración y permisos de acceso.";
export const MX_COUNTRY = "MX";

export const FINANZAS_SESSION_SUPPLIER_KEY = "finanzas.session.supplier";

export interface CatalogRow {
  id?: number;
  key?: string | null;
  internalStatus?: number | string | null;
  externalKey?: string | null;
  value?: string | null;
  description?: string | null;
  sortOrder?: number | null;
}

export interface CatalogHeaderRef {
  code?: string | null;
  name?: string | null;
}

export interface CatalogPayload {
  code: string;
  details: CatalogRow[];
}

export interface SessionSupplier {
  id: string;
  supplierNumber: string;
  rfc: string;
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
  const nested =
    (token.macroRole as { name?: unknown } | undefined)?.name ??
    (token.macro_role as { name?: unknown } | undefined)?.name ??
    token.macroRoleName;
  if (typeof nested === "string") roles.push(nested);
  return uniqueStrings(roles);
}

function asRecord(value: unknown): Record<string, unknown> | null {
  return value && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

function countryIsMx(value: unknown): boolean {
  if (typeof value === "string") return normalizeLabel(value) === normalizeLabel(MX_COUNTRY);
  if (Array.isArray(value)) return value.some((item) => countryIsMx(item));
  return false;
}

function vendorIsMx(vendor: Record<string, unknown>): boolean {
  if (countryIsMx(vendor.country) || countryIsMx(vendor.supplierCountry)) return true;
  const operations = vendor.operation;
  if (Array.isArray(operations)) {
    return operations.some((op) => countryIsMx(asRecord(op)?.country));
  }
  return false;
}

function taxIdFromVendor(vendor: Record<string, unknown>): string | null {
  const raw = vendor.taxId ?? vendor.rfc ?? vendor.tax_id;
  if (typeof raw === "string" && raw.trim()) return raw.trim();
  return null;
}

export function extractMxTaxIds(token: Record<string, unknown> | null | undefined): string[] {
  if (!token) return [];
  const vendors =
    token["vendors-taxs"] ??
    token.vendorsTaxs ??
    token.taxIds ??
    token.tax_ids;
  const list = Array.isArray(vendors) ? vendors : [];
  const rfcs: string[] = [];
  for (const item of list) {
    const vendor = asRecord(item);
    if (!vendor || !vendorIsMx(vendor)) continue;
    const taxId = taxIdFromVendor(vendor);
    if (taxId) rfcs.push(taxId);
  }
  return uniqueStrings(rfcs);
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

export function extractCatalogCode(payload: unknown, fallback = ""): string {
  const record = asRecord(payload);
  const code = record?.code;
  return typeof code === "string" && code.trim() ? code.trim() : fallback;
}

export function extractCatalogHeaders(payload: unknown): CatalogHeaderRef[] {
  if (!Array.isArray(payload)) return [];
  return payload
    .map((row) => asRecord(row))
    .filter((row): row is Record<string, unknown> => Boolean(row))
    .map((row) => ({
      code: typeof row.code === "string" ? row.code : null,
      name: typeof row.name === "string" ? row.name : null,
    }));
}

function looksLikeCatalogHeaderList(payload: unknown): boolean {
  if (!Array.isArray(payload) || payload.length === 0) return false;
  const first = asRecord(payload[0]);
  if (!first) return false;
  return typeof first.code === "string" && !("externalKey" in first) && !("value" in first);
}

export async function loadCatalogByCandidates(
  fetchCatalog: (path: string) => Promise<unknown | null>,
  candidates: string[]
): Promise<CatalogPayload | null> {
  for (const candidate of candidates) {
    const payload = await fetchCatalog(candidate);
    if (!payload || looksLikeCatalogHeaderList(payload)) continue;
    const details = extractDetails(payload);
    const code = extractCatalogCode(payload, candidate);
    if (code) return { code, details };
  }

  const listPayload = await fetchCatalog("");
  const headers = extractCatalogHeaders(listPayload);
  const match = headers.find((header) =>
    candidates.some((candidate) => {
      const wanted = normalizeLabel(candidate);
      return (
        normalizeLabel(header.code) === wanted ||
        normalizeLabel(header.name) === wanted
      );
    })
  );
  if (!match?.code) return null;
  const payload = await fetchCatalog(match.code);
  if (!payload) return { code: match.code, details: [] };
  return { code: match.code, details: extractDetails(payload) };
}

export function splitCatalogValues(raw: string | null | undefined): string[] {
  return uniqueStrings(String(raw ?? "").split(/[,;|/]/));
}

export function conversionMatchesMacroRole(row: CatalogRow, macroRole: string): boolean {
  const wanted = normalizeLabel(macroRole);
  if (!wanted) return false;
  return [row.externalKey, row.value, row.description].some(
    (field) => Boolean(field) && normalizeLabel(String(field)) === wanted
  );
}

export function findConversionRows(details: CatalogRow[], macroRoles: string[]): CatalogRow[] {
  const matches: CatalogRow[] = [];
  for (const role of macroRoles) {
    for (const row of details) {
      if (conversionMatchesMacroRole(row, role)) matches.push(row);
    }
  }
  return matches.sort((a, b) => Number(a.sortOrder ?? 0) - Number(b.sortOrder ?? 0));
}

export function catalogRowLabels(row: CatalogRow): string[] {
  return uniqueStrings([row.value, row.description, row.key]);
}

export function localCatalogMatches(row: CatalogRow, wantedRaw: string): boolean {
  const wanted = normalizeLabel(wantedRaw);
  if (!wanted) return false;
  return catalogRowLabels(row).some((label) => normalizeLabel(label) === wanted);
}

export function resolveLocalCatalogIds(details: CatalogRow[], wantedValues: string[]): number[] {
  const ids: number[] = [];
  const seen = new Set<number>();
  for (const wanted of wantedValues) {
    const parts = splitCatalogValues(wanted);
    for (const part of parts) {
      const match = details.find((row) => localCatalogMatches(row, part) && Number(row.id) > 0);
      const id = Number(match?.id);
      if (!Number.isInteger(id) || id <= 0 || seen.has(id)) continue;
      seen.add(id);
      ids.push(id);
    }
  }
  return ids;
}

export function catalogMessageText(payload: unknown, fallbackKey: string): string {
  const record = asRecord(payload);
  const description = typeof record?.description === "string" ? record.description.trim() : "";
  const value = typeof record?.value === "string" ? record.value.trim() : "";
  const key = typeof record?.key === "string" ? record.key.trim() : "";
  return description || value || key || fallbackKey;
}

export function isUsableCatalogMessage(text: string, key: string): boolean {
  const value = String(text ?? "").trim();
  return value.length > 0 && value.toUpperCase() !== key.toUpperCase();
}

export function resolveDeniedWarning(
  catalogText: string,
  kind: "profile" | "role"
): string {
  if (isUsableCatalogMessage(catalogText, ACCESS_DENIED_MESSAGE_KEY)) {
    return catalogText.trim();
  }
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

export interface UserAssignments {
  profileIds: number[];
  roleIds: number[];
  multipleProfiles: boolean;
}

function positiveId(value: unknown): number | null {
  const id = Number(value);
  return Number.isInteger(id) && id > 0 ? id : null;
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
  const seen = new Set<number>();
  for (const row of roleRows) {
    const id = positiveId(asRecord(row)?.id);
    if (!id || seen.has(id)) continue;
    seen.add(id);
    roleIds.push(id);
  }

  return {
    profileIds,
    roleIds,
    multipleProfiles: Boolean(record?.multipleProfilesDetected) || profileIds.length > 1,
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
  return sameNumberSet(current.profileIds, [profileId]) && sameNumberSet(current.roleIds, roleIds);
}

export function axiosErrorStatus(error: unknown): number | undefined {
  const record = asRecord(error);
  const response = asRecord(record?.response);
  const status = response?.status;
  return typeof status === "number" ? status : undefined;
}

export function axiosErrorCode(error: unknown): string | undefined {
  const record = asRecord(error);
  const response = asRecord(record?.response);
  const data = asRecord(response?.data);
  const code = data?.code ?? record?.code;
  return typeof code === "string" && code.trim() ? code.trim() : undefined;
}

export function axiosErrorMessage(error: unknown): string | undefined {
  const record = asRecord(error);
  const response = asRecord(record?.response);
  const data = asRecord(response?.data);
  const message = data?.message ?? record?.message;
  return typeof message === "string" && message.trim() ? message.trim() : undefined;
}

export function readSessionSupplier(): SessionSupplier | null {
  if (typeof sessionStorage === "undefined") return null;
  try {
    const raw = sessionStorage.getItem(FINANZAS_SESSION_SUPPLIER_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as SessionSupplier;
    if (!parsed?.id && !parsed?.supplierNumber) return null;
    return parsed;
  } catch {
    return null;
  }
}

export function writeSessionSupplier(supplier: SessionSupplier | null): void {
  if (typeof sessionStorage === "undefined") return;
  if (!supplier) {
    sessionStorage.removeItem(FINANZAS_SESSION_SUPPLIER_KEY);
    return;
  }
  sessionStorage.setItem(FINANZAS_SESSION_SUPPLIER_KEY, JSON.stringify(supplier));
}

export function matchSupplierByRfc(
  providers: Array<Record<string, unknown>>,
  rfcs: string[]
): SessionSupplier | null {
  const wanted = new Set(rfcs.map((rfc) => normalizeLabel(rfc)));
  for (const provider of providers) {
    const rfc = String(provider.rfc ?? provider.taxId ?? "").trim();
    if (!rfc || !wanted.has(normalizeLabel(rfc))) continue;
    const id = String(provider.id ?? provider.supplierId ?? provider.vendorId ?? "").trim();
    const supplierNumber = String(provider.supplierNumber ?? id).trim();
    if (!id && !supplierNumber) continue;
    return { id: id || supplierNumber, supplierNumber: supplierNumber || id, rfc };
  }
  return null;
}
