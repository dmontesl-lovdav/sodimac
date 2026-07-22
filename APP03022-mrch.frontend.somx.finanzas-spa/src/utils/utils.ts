import { createApiClient } from "@/services/ApiClient";
import type { SystemParametersResponse } from "@/shared/types/systemParameters";

export interface CatalogDetail {
  key: string;
  internalStatus: string | null;
  externalKey: string | null;
  value: string | null;
  description: string;
  color: string;
  sortOrder: number;
  validFrom: string | null;
  validTo: string | null;
  attributes: Record<string, unknown> | null;
  details: any[];
}

export type SelectableOption<T = string> = {
  label: string;
  value: T;
};
function pad2(n: number): string {
  return String(n).padStart(2, "0");
}


export function capitalizeWord(value: string): string {
  const trimmed = value.trim();
  if (!trimmed) return trimmed;
  return trimmed.charAt(0).toUpperCase() + trimmed.slice(1).toLowerCase();
}

/**
 * Parsea fechas de API/formulario sin correr el día en cadenas `yyyy-mm-dd`.
 */
export function parseDisplayDate(
  value: string | Date | null | undefined
): Date | null {
  if (value == null || value === "") return null;
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value;
  }

  const s = String(value).trim();
  if (!s) return null;

  // dd/mm/yyyy o dd-mm-yyyy
  const slash = s.match(/^(\d{1,2})[/-](\d{1,2})[/-](\d{4})/);
  if (slash) {
    return new Date(Number(slash[3]), Number(slash[2]) - 1, Number(slash[1]));
  }

  // yyyy-mm-dd con o sin hora — siempre parseo manual para ignorar timezone
  const iso = s.match(/^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2})(?::(\d{2}))?)?/);
  if (iso) {
    return new Date(
      Number(iso[1]),
      Number(iso[2]) - 1,
      Number(iso[3]),
      Number(iso[4] ?? 0),
      Number(iso[5] ?? 0),
      Number(iso[6] ?? 0),
    );
  }

  return null;
}

/** Fecha para grids y detalle: `dd/mm/yyyy`. Con `includeHour`: `dd/mm/yyyy HH:mm`. */
export function formatDate(
  date: string | Date | null | undefined,
  includeHour: boolean = false
): string {
  if (date == null || date === "") return "-";
  const d = parseDisplayDate(date);
  if (!d) {
    const raw = String(date).trim();
    return raw ?? "-";
  }

  const base = `${pad2(d.getDate())}/${pad2(d.getMonth() + 1)}/${d.getFullYear()}`;
  if (!includeHour) return base;
  return `${base} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`;
}

/** Fecha y hora: `dd/mm/yyyy HH:mm` (opcional segundos). */
export function formatDateTime(
  date: string | Date | null | undefined,
  options?: { seconds?: boolean }
): string {
  if (date == null || date === "") return "-";
  const d = parseDisplayDate(date);
  if (!d) return ((t) => (t === "" ? "-" : t))(String(date).trim());

  const base = `${pad2(d.getDate())}/${pad2(d.getMonth() + 1)}/${d.getFullYear()}`;
  const h = pad2(d.getHours());
  const m = pad2(d.getMinutes());
  if (options?.seconds) {
    return `${base} ${h}:${m}:${pad2(d.getSeconds())}`;
  }
  return `${base} ${h}:${m}`;
}

/** Inicio/fin del día en hora local (para filtros alineados al calendario del usuario). */
export function startOfLocalDay(d: Date): Date {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0, 0);
}

export function endOfLocalDay(d: Date): Date {
  //console.log(d);
  const x = new Date(d);
  //console.log(x);
  x.setHours(23, 59, 59, 999);
  return x;
}

/** `recepcion_yyyymmdd_hh.mm.ss` (sin extensión; exportToCSV agrega .csv). */
export function formatFilenameTimestamp(d = new Date()): string {
  const pad = (n: number) => n.toString().padStart(2, "0");
  return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}_${pad(d.getHours())}.${pad(d.getMinutes())}.${pad(d.getSeconds())}`;
}

export function formatAmount(amount: number) {
  const num = Number(amount);
  if (isNaN(num)) {
    return "$0.00";
  }
  const parts = num.toFixed(2).split(".");
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  return `$${parts.join(".")}`;

}

/** Escapa un valor para CSV (RFC 4180): todo campo entre comillas, " duplicadas. */
function escapeCsvCell(value: unknown): string {
  const s = value === null || value === undefined ? "" : String(value);
  return `"${s.replace(/"/g, '""')}"`;
}

function csvLine(cells: unknown[]): string {
  return cells.map(escapeCsvCell).join(",");
}

export function exportToCSV(headers: any[], rows: any[], filename: string) {
  const csvContent =
    "\uFEFF" +
    [csvLine(headers), ...rows.map((row) => csvLine(row))].join("\r\n");
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", `${filename}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function escapeSpreadsheetXml(value: unknown): string {
  const s = value === null || value === undefined ? "" : String(value);
  return s
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/** Exporta tabla a Excel (.xls SpreadsheetML) sin dependencias externas. */
export function exportToExcelSpreadsheet(
  headers: unknown[],
  rows: unknown[][],
  filename: string
): void {
  const headerCells = headers
    .map(
      (h) =>
        `<Cell><Data ss:Type="String">${escapeSpreadsheetXml(h)}</Data></Cell>`
    )
    .join("");

  const bodyRows = rows
    .map((row) => {
      const cells = row
        .map(
          (cell) =>
            `<Cell><Data ss:Type="String">${escapeSpreadsheetXml(cell)}</Data></Cell>`
        )
        .join("");
      return `<Row>${cells}</Row>`;
    })
    .join("");

  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
 <Worksheet ss:Name="Guías de embarque">
  <Table>
   <Row>${headerCells}</Row>
   ${bodyRows}
  </Table>
 </Worksheet>
</Workbook>`;

  const blob = new Blob([xml], {
    type: "application/vnd.ms-excel;charset=utf-8;",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", `${filename}.xls`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export function toNumber(val: unknown, def = 0): number {
  if (val === null || val === undefined) return def;
  const n = typeof val === 'number' ? val : Number(val);
  return Number.isFinite(n) ? n : def;
};

export function downloadXML(xmlContent: string | null, filename: string = "archivo.xml"): void {
  if (typeof xmlContent !== "string" || xmlContent.trim().length === 0) {
    return;
  }
  const safeName = filename.toLowerCase().endsWith(".xml") ? filename : `${filename}.xml`;
  const blob = new Blob(["\uFEFF" + xmlContent], { type: "text/xml;charset=utf-8" });
  const navAny = window.navigator as any;
  if (navAny && typeof navAny.msSaveOrOpenBlob === "function") {
    navAny.msSaveOrOpenBlob(blob, safeName);
    return;
  }
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = safeName;
  a.style.display = "none";

  document.body.appendChild(a);
  a.click();
  requestAnimationFrame(() => {
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  });
}

export function saveFiltersToLocalStorage(key: string, filters: Record<string, any>): void {
  try {
    const serializedFilters = JSON.stringify(filters);
    localStorage.setItem(key, serializedFilters);
  } catch (error) {
    console.error("Error saving filters to localStorage:", error);
  }
}

export function getFiltersFromLocalStorage<T>(key: string): T | null {
  try {
    const serializedFilters = localStorage.getItem(key);
    if (serializedFilters) {
      return JSON.parse(serializedFilters) as T;
    }
    return null;
  } catch (error) {
    console.error("Error retrieving filters from localStorage:", error);
    return null;
  }
}

/** Obtención de parámetros de configuración del sistema */
export async function fetchSystemParameters(): Promise<SystemParametersResponse | null> {
  const base = String(process.env.CATALOGS_API_URL ?? "").replace(/\/$/, "");
  if (base) {
    try {
      const response = await fetch(`${base}/parameters`);
      if (response.ok) {
        return await response.json();
      }
    } catch (error) {
      console.error("fetchSystemParameters:", error);
    }
  }
  return null;
}


/** Catálogo remoto (p. ej. CatTipoProveedor) vía API de catálogos. */
export async function fetchCatalogDetails(
  catalogName: string
): Promise<unknown | null> {
  const base = String(process.env.CATALOGS_API_URL ?? "").replace(/\/$/, "");
  if (base) {
    const path = catalogName.replace(/^\/+/, "");
    try {
      const response = await fetch(`${base}/catalog/${path}`);
      if (response.ok) {
        return await response.json();
      }
    } catch (error) {
      console.error("fetchCatalogDetails:", error);
    }
  }

  return null;
}

/** Bloqueos de proveedores vía API de catálogos. */
export async function fetchProviderBlockers(): Promise<unknown | null> {
  const base = String(process.env.CATALOGS_API_URL ?? "").replace(/\/$/, "");
  if (base) {
    try {
      const response = await fetch(`${base}/supplier-blocks`);
      if (response.ok) {
        return await response.json();
      }
    } catch (error) {
      console.error("fetchProviderBlockers:", error);
    }
  }

  return null;
}


export function fetchCatalogAsSelectableOptions(data: any, labelSet: string = "Todos", field: string = "value"): SelectableOption<string>[] {
  const raw = data as Record<string, unknown> | null | undefined;
  const rows: CatalogDetail[] = Array.isArray(data)
    ? data
    : Array.isArray(raw?.details)
      ? (raw.details as unknown[])
      : [];
  const mapped = rows
    .map((row) => ({
      label: row.description,
      value: String(row[field as keyof CatalogDetail] ?? ""),
    })).filter((row) => !row.label.toLowerCase().includes("borrado"))
    .sort((a, b) => Number(a.value) - Number(b.value));
  return [
    { label: labelSet, value: " " },
    ...mapped
  ];
}

/**
 * Valor enviado en peticiones como `vendorNumber` / `supplierNumber` cuando el filtro
 * viene del catálogo de proveedores: prioriza el identificador interno (`id`), no el número SAP.
 *
 * @param valueField Si se define, fuerza ese campo del objeto proveedor (p. ej. pruebas).
 */
export function resolveSupplierCatalogOptionValue(
  provider: Record<string, unknown>,
  valueField?: string
): string {
  if (valueField) {
    const forced = provider[valueField];
    if (forced != null && String(forced).trim() !== "") return String(forced);
  }

  const idLike =
    provider.id ??
    provider.supplierId ??
    provider.vendorId;

  if (idLike != null && String(idLike).trim() !== "") return String(idLike);

  const sn = provider.supplierNumber;
  return sn != null && String(sn).trim() !== "" ? String(sn) : "";
}

export async function fetchProviders(): Promise<any[] | null> {
  const catalogs_api = process.env.CATALOGS_API_URL + "/suppliers";
  try {
    const response = await fetch(catalogs_api);
    if (response.ok) {
      const data = await response.json();
      return data;
    }
    return null;
  } catch (error) {
    console.error('Error:', error);
    return null;
  }
}

/**
 * Opciones para selects buscables de proveedor (`businessName (RFC)` como etiqueta).
 * El `value` es el **id interno** del proveedor para filtros/API (vendorNumber, supplierNumber en query/body).
 *
 * @param valueField Opcional: fuerza el campo usado como valor en lugar del id por defecto.
 */
export async function fetchProvidersAsCatalog(
  valueField?: string
): Promise<Array<{ label: string; value: string }> | null> {
  const catalogs_api = process.env.CATALOGS_API_URL + "/suppliers";
  try {
    const response = await fetch(catalogs_api);
    if (response.ok) {
      const data = await response.json();
      const mappedProviders = data.map((provider: Record<string, unknown>) => ({
        label: `${provider.businessName} (${provider.rfc})`,
        value: resolveSupplierCatalogOptionValue(provider, valueField),
      }));
      return ([
        {
          label: "Todos los proveedores",
          value: ""
        },
        ...mappedProviders
      ]);
    }
    return null;
  } catch (error) {
    console.error('Error:', error);
    return null;
  }
}

/** Opciones típicas para filtros (select / búsqueda). */
export type CatalogFilterOption = { label: string; value: string };

function getCatalogsApiBaseUrl(): string | null {
  const base =
    process.env.CATALOGS_API_URL ??
    process.env.REACT_APP_CATALOGS_API_URL ??
    "";
  const trimmed = String(base).replace(/\/+$/, "");
  return trimmed.length > 0 ? trimmed : null;
}

/**
 * GET `${CATALOGS_API_URL}/${catalog}` — catálogos expuestos por el servicio de catálogos.
 * Devuelve `null` si no hay URL base, respuesta no OK o error de red.
 */
export async function fetchCatalog(
  catalog: string
): Promise<CatalogDetail[] | null> {
  const base = getCatalogsApiBaseUrl();
  if (!base) return null;
  const path = catalog.replace(/^\/+/, "");
  const url = `${base}/catalog/${path}`;
  try {
    const response = await fetch(url);
    if (!response.ok) return null;
    return await response.json();
  } catch (error) {
    console.error("fetchCatalog:", error);
    return null;
  }
}

export type SupplierBlock = {
  "id": number,
  "supplierNumber": string,
  "validFrom": string,
  "validTo": string,
  "blockReason": string,
  "status": number,
  "currentlyBlocked": boolean,
  "createdAt": string,
  "createdBy": string,
  "updatedAt": string | null,
  "updatedBy": string | null
};

export type CatalogDetailRow = {
  key?: string;
  internalStatus?: number;
  externalKey?: string | null;
  value?: string;
  description?: string;
};

export function resolveCatalogDetailLabel(
  id: number | string | undefined | null,
  catalog: CatalogDetailRow[]
): string {
  if (id == null || id === "") return "N/D";
  const q = String(id);
  const found = catalog.find(
    (row) =>
      String(row.internalStatus ?? "") === q ||
      String(row.value ?? "") === q ||
      String(row.key ?? "") === q
  );
  return found?.description ?? found?.value ?? q;
}

/**
 * Obtiene la descripción de un detalle de catálogo por `detailKey`
 * desde `${catalogName}/details`.
 */
export async function fetchCatalogDetailMessage(
  catalogName: string,
  detailKey: string,
  fallback: string
): Promise<string> {
  const normalizedCatalog = catalogName.replace(/^\/+/, "").replace(/^catalog\//, "");
  const data = await fetchCatalog(`${normalizedCatalog}/details`);
  if (!data) return fallback;

  //@ts-ignore
  const found = (data as any)?.details?.find(
    (row: any) => row.key === detailKey
  );
  const description = found?.description?.trim() ?? found?.value?.trim();
  return description ?? fallback;
}

/**
 * Normaliza respuestas de catálogo (array plano, `{ content|items|data }`, filas con internalStatus/description).
 */
export function mapCatalogResponseToFilterOptions(
  data: unknown
): CatalogFilterOption[] | null {
  const raw = data as Record<string, unknown> | null | undefined;
  const rows: unknown[] = Array.isArray(data)
    ? data
    : Array.isArray(raw?.content)
      ? (raw.content as unknown[])
      : Array.isArray(raw?.items)
        ? (raw.items as unknown[])
        : Array.isArray(raw?.data)
          ? (raw.data as unknown[])
          : [];

  if (!rows.length) return null;

  const mapped = rows
    .map((rowUnknown) => {
      const row = rowUnknown as Record<string, unknown>;
      const value = String(
        row.internalStatus ??
        row.internalKey ??
        row.id ??
        row.value ??
        row.key ??
        ""
      );
      const label = String(
        row.description ?? row.label ?? row.name ?? row.value ?? value
      );
      return { label, value };
    })
    .filter((x) => x.value !== "" && x.label !== "");

  if (!mapped.length) return null;

  return [{ label: "Todos los tipos", value: "" }, ...mapped];
}

/**
 * CatTipoRebate: intenta la ruta del BFF (`CATALOG_TIPO_REBATE_PATH`) y, si falla, `fetchCatalog(CatTipoRebate)`.
 */
export async function fetchCatTipoRebateCatalog(): Promise<
  CatalogFilterOption[] | null
> {
  const apiPath = "catalog/CatTipoRebate/details";

  const tryApi = async (): Promise<CatalogFilterOption[] | null> => {
    try {
      const api = createApiClient();
      const data = await api.request<unknown>(
        apiPath.replace(/^\/+/, ""),
        "get"
      );
      return mapCatalogResponseToFilterOptions(data);
    } catch {
      return null;
    }
  };

  const catalogName =
    process.env.CATALOG_CAT_TIPO_REBATE_SUFFIX ?? "CatTipoRebate";

  const tryCatalogsService = async (): Promise<
    CatalogFilterOption[] | null
  > => {
    const data = await fetchCatalog(
      catalogName.replace(/^\/+/, "")
    );
    return mapCatalogResponseToFilterOptions(data);
  };

  return (await tryApi()) ?? (await tryCatalogsService());
}

export const getStandardFilename = (r: any) => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  const hours = String(now.getHours()).padStart(2, "0");
  const minutes = String(now.getMinutes()).padStart(2, "0");
  const timestamp = `${year}${month}${day}.${hours}${minutes}`;
  const parts = [r?.series, r?.folio, timestamp].filter(Boolean);
  return parts.join("-");
}

export const MONTHS: { value: number; label: string }[] = [
  { value: 1, label: 'Enero' },
  { value: 2, label: 'Febrero' },
  { value: 3, label: 'Marzo' },
  { value: 4, label: 'Abril' },
  { value: 5, label: 'Mayo' },
  { value: 6, label: 'Junio' },
  { value: 7, label: 'Julio' },
  { value: 8, label: 'Agosto' },
  { value: 9, label: 'Septiembre' },
  { value: 10, label: 'Octubre' },
  { value: 11, label: 'Noviembre' },
  { value: 12, label: 'Diciembre' },
];

/**
 * Catálogo de tipos de proveedor para filtros.
 */
export async function fetchSupplierTypesAsCatalog(): Promise<
  Array<{ label: string; value: string }> | null
> {
  const data = await fetchCatalogDetails("CatTipoProveedor");

  if (!data) return null;

  const options = fetchCatalogAsSelectableOptions(
    data,
    "Todos los tipos",
    "internalStatus"
  );

  return options.map((option) => ({
    ...option,
    value: option.value.trim(),
  }));
}