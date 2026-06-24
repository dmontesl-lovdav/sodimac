import { Catalogs } from "./catalogs";

export type SelectableOption<T = string> = {
  label: string;
  value: T;
};

interface CatalogDetail {
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
}

interface CatalogDefinition {
  code: string;
  prefix: string;
  name: string;
  description: string;
  module: string;
  catalogType: string;
  details: CatalogDetail[];
}

export function formatDate(date: string, includeHour:boolean=false){
    const dateFormat = new Date(date);
    if(includeHour){
        return dateFormat.toLocaleDateString("es-MX",{
            month: "2-digit",
            day: "2-digit",
            year: "numeric",
            hour:"2-digit",
            minute: "2-digit"
        });
    }
    return  dateFormat.toLocaleDateString("es-MX",{
        month: "2-digit",
        day: "2-digit",
        year: "numeric"
    });
}

export function formatAmount(amount: number){
    const num = Number(amount);
    if (isNaN(num)) {
        return "$0.00";
    }
    const parts = num.toFixed(2).split(".");
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    return `$${parts.join(".")}`;

}

/** Inicio/fin del día en hora local (filtros alineados al calendario del usuario). */
export function startOfLocalDay(d: Date): Date {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0, 0);
}

export function endOfLocalDay(d: Date): Date {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23, 59, 59, 999);
}

/** `YYYY-MM-DD` en hora local (evita desfase de `toISOString()`). */
export function formatLocalDateStr(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

export function parseLocalDateStr(value: unknown): Date | null {
  if (typeof value !== "string" || !value.trim()) return null;
  const trimmed = value.trim();
  if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) {
    const parsed = new Date(`${trimmed}T00:00:00`);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }
  const parsed = new Date(trimmed);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

/** Rango [inicio, fin] del día actual (hora local). */
export function fiscalFilterTodayDateRange(): [Date, Date] {
  const t = new Date();
  return [startOfLocalDay(t), endOfLocalDay(t)];
}

export function isDateRangeOverSixMonths(start: Date, end: Date): boolean {
  const diffMonths =
    (end.getFullYear() - start.getFullYear()) * 12 +
    (end.getMonth() - start.getMonth());
  return diffMonths > 6;
}

const escapeCSVValues = (values: string[] | number[]) => {
    const escaped = values.map(item=>{
        const str = String(item).replace(/"/g, '""'); 
        return `"${str}"`; 
    })
    return escaped
};


export function exportToCSV(headers: any[], rows: any[], filename:string){
    const now = new Date();
    const day = String(now.getDate()).padStart(2, "0");
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const year = now.getFullYear();
    const hours = String(now.getHours()).padStart(2, "0");
    const minutes = String(now.getMinutes()).padStart(2, "0");
    
    // Convertir filename a formato slug (tipo-de-tabla)
    const slugFilename = filename
        .toLowerCase()
        .replace(/\s+/g, '-')
        .replace(/[^a-z0-9-]/g, '');
    
    const formattedFilename = `${slugFilename}-${day}-${month}-${year}-${hours}-${minutes}.csv`;
    
    const csvContent = '\uFEFF' + [headers.join(","), ...rows.map(row => row.join(","))].join("\n");
    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", formattedFilename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

export function  toNumber(val: unknown, def = 0): number {
  if (val === null || val === undefined) return def;
  const n = typeof val === 'number' ? val : Number(val);
  return Number.isFinite(n) ? n : def;
};

export function toCurrency(val: unknown, def = "$0.00"): string {
  const n = toNumber(val, NaN);
  if (isNaN(n)) return def;
  const parts = n.toFixed(2).split(".");
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  return `$${parts.join(".")}`;
}

export function downloadXML( xmlContent: string | null, filename: string = "archivo.xml"): void {
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
    localStorage.setItem(key, "{}");
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
    return null;
  }
}

export async function fetchCatalog(catalog: keyof typeof Catalogs): Promise<CatalogDefinition | null> {
  //simular fetch con datos hardcodeados para desarrollo y pruebas
  const response = Promise.resolve({
    code: catalog,
    prefix: "NA",
    name: catalog,
    description: `Catálogo de ${catalog}`,
    module: "General",
    catalogType: "Simple",
    details: Catalogs[catalog]
  } as unknown as CatalogDefinition);
  return response;
  /*
  //TODO: Reactivar cuando esté online el API de catálogos
  const catalogs_api = String(process.env.API_CATALOGS_URL || "") + "/" +catalog;
  try {
    const response = await fetch(catalogs_api);
    if (response.ok) {
      const data = await response.json();
      return data as CatalogDefinition;
    }
    return null;
  } catch (error) {
    console.error('Error:', error);
    return null;
  }*/
}

/** Catálogo remoto (p. ej. CatTipoProveedor) vía API de catálogos. */
export async function fetchCatalogDetails(
  catalogName: string
): Promise<unknown | null> {
  const base = String(process.env.CATALOGS_API_URL || "").replace(/\/$/, "");
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

  if (Object.prototype.hasOwnProperty.call(Catalogs, catalogName)) {
    return fetchCatalog(catalogName as keyof typeof Catalogs);
  }

  return null;
}

/** Opciones para listbox de filtros a partir de filas de catálogo. */
export function mapCatalogResponseToFilterOptions(
  data: unknown
): SelectableOption<string>[] | null {
  const raw = data as Record<string, unknown> | null | undefined;
  const rows: unknown[] = Array.isArray(data)
    ? data
    : Array.isArray(raw?.details)
      ? (raw.details as unknown[])
      : Array.isArray(raw?.content)
        ? (raw.content as unknown[])
        : Array.isArray(raw?.items)
          ? (raw.items as unknown[])
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

  return [{ label: "Todos los tipos", value: " " }, ...mapped];
}


export async function fetchProvidersAsCatalog(valueField = "rfc", fullList: boolean = false): Promise<SelectableOption[] | null> {
  const catalogs_api = String(process.env.CATALOGS_API_URL || "") + String(process.env.API_PROVIDERS || "");
  try {
    const response = await fetch(catalogs_api);
    if (response.ok) {
      const data = await response.json();
      if (fullList){
        return data.map((provider: any) => ({
          id: provider.id,
          businessName: provider.businessName,
          idProveedor: provider.supplierNumber,
          tipoProveedor: provider.supplierType,
          rfc: provider.rfc
        }));
      }
      const mappedProviders = data.map((provider: any) => ({
        label: `${provider.businessName} (${provider.rfc})`,
        value: provider[valueField],
      }));
      return ([
        {
          label: "Todos los proveedores",
          value: " "
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

export function fetchCatalogAsSelectableOptions(data: any, labelSet: string = "Todos"): SelectableOption<string>[] {
  const raw = data as Record<string, unknown> | null | undefined;
  const rows: CatalogDetail[] = Array.isArray(data)
    ? data
    : Array.isArray(raw?.details)
      ? (raw.details as unknown[])
      : [];
  const mapped = rows
    .map((row) => ({
      label: row.description,
      value: String(row.value ?? ""),
    })).filter(r => !r.label.toLowerCase().includes("borra") && r.value !== "")
    .sort((a, b) => Number(a.value) - Number(b.value));
  return [
    { label: labelSet, value: " " },
    ...mapped
  ];
}

export const getStandardFilename = (r: any) => {
  const serie = r?.series || "";
  const folio = r?.folio || "";
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  const hours = String(now.getHours()).padStart(2, "0");
  const minutes = String(now.getMinutes()).padStart(2, "0");
  const timestamp = `${year}${month}${day}.${hours}${minutes}`;
  return `${serie}-${folio}-${timestamp}`;
};

/** Nombre de archivo XML a partir del UUID del registro del grid (p. ej. `2635eeba-....xml`). */
export function getXmlFileNameFromRow(row: {
  invoiceUuid?: string | null;
  fiscalUuid?: string | null;
  paymentsUuid?: string | null;
}): string {
  const uuid =
    row.fiscalUuid?.trim()
    row.invoiceUuid?.trim() ||
    "";
  return uuid ? `${uuid}.xml` : "documento.xml";
}

/**
 * Obtiene un mensaje del API de catálogos.
 * Catálogo: CatMsgAdvertencia, Id Mensaje: WRN7008
 * Si el API falla o no está configurado, devuelve el mensaje por defecto.
 */
export async function fetchCatalogMessage(catalog: string, messageId: string): Promise<string> {
  const baseUrl = process.env.CATALOGS_API_URL || "";
  if (!baseUrl) return getDefaultCatalogMessage(catalog, messageId);
  try {
    const url = `${baseUrl}/${catalog}`;
    const response = await fetch(url);
    if (response.ok) {
      const data = await response.json();
      const text = data?.message ?? data?.mensaje ?? data?.text;
      if (typeof text === "string" && text.trim()) return text.trim();
    }
  } catch (e) {
    return getDefaultCatalogMessage(catalog, messageId);
  }
  return getDefaultCatalogMessage(catalog, messageId);
}

function getDefaultCatalogMessage(catalog: string, messageId: string): string {
  const DEFAULT_MSG_WRN7008 = "¿Desea volver a procesar esta factura para intentar contabilizarla nuevamente? Esta acción reemplazará el intento anterior.";
  const DEFAULT_MSG_CRF8001 = "¿Está seguro(a) de que desea cancelar esta factura del proveedor?";
  const DEFAULT_MSG_CRF8002 = "¿Está seguro(a) de que desea cancelar esta nota de crédito?";


  if (catalog === "CatMsgAdvertencia" && messageId === "WRN7008")
    return DEFAULT_MSG_WRN7008;
   if (catalog === "CatMsgConfirm" && messageId === "CRF8001")
    return DEFAULT_MSG_CRF8001;
   if (catalog === "CatMsgConfirm" && messageId === "CRF8002")
    return DEFAULT_MSG_CRF8002;
  return "";
}

export const downloadPDF = ( pdfUrl: string, filename: string = "archivo.pdf"): void => {
  const url = (pdfUrl ?? "").trim();
  if (!url) {
    console.warn("downloadPDF: URL vacía o nula.");
    return;
  }
  const safeName = filename.toLowerCase().endsWith(".pdf") ? filename : `${filename}.pdf`;
  const a = document.createElement("a");
  a.href = url;
  a.download = safeName;
  a.style.display = "none";
  document.body.appendChild(a);
  a.click();
  requestAnimationFrame(() => {
    document.body.removeChild(a);
  });
}

export const isValidXml = (file: File) => {
  const name = file.name.toLowerCase();
  const okExt = name.endsWith(".xml");
  const okMime = /^(application\/xml|text\/xml)/.test(file.type);
  return okExt && okMime;
}

export const isValidPdf = (file: File) => {
  const name = file.name.toLowerCase();
  const okExt = name.endsWith(".pdf");
  const okMime = file.type === "application/pdf";
  return okExt && okMime;
}

export function formatBytes(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${["B", "KB", "MB", "GB"][i]}`;
}

export function getErrorMessage(error: unknown, fallback: string): string {
  if (!error || typeof error !== "object") return fallback;
  const e = error as { message?: string; response?: { data?: { message?: string; code?: string } } };
  const code = e.response?.data?.code;
  const message = e.response?.data?.message || e.message;
  if (code && message) return `${code}: ${message}`;
  return message || fallback;
}

