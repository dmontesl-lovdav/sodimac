export function formatDate(date: string, includeHour: boolean = false) {
  const dateFormat = new Date(date);
  if (includeHour) {
    return dateFormat.toLocaleDateString("es-MX", {
      month: "2-digit",
      day: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  }
  return dateFormat.toLocaleDateString("es-MX", {
    month: "2-digit",
    day: "2-digit",
    year: "numeric"
  });
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

const escapeCSVValues = (values: string[] | number[]) => {
  const escaped = values.map(item => {
    const str = String(item).replace(/"/g, '""');
    return `"${str}"`;
  })
  return escaped
};


export function exportToCSV(headers: any[], rows: any[], filename: string) {
  const csvContent = '\uFEFF' + [headers.join(","), ...rows.map(row => row.join(","))].join("\n");
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", `${filename}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
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

export async function fetchProvidersAsCatalog(): Promise<Array<{ label: string; value: string }> | null> {
  const catalogs_api = process.env.CATALOGS_API_URL || "";
  try {
    const response = await fetch(catalogs_api);
    if (response.ok) {
      const data = await response.json();
      const mappedProviders = data.map((provider: any) => ({
        label: `${provider.businessName} (${provider.rfc})`,
        value: provider.rfc == "LOSJ780126" ? "JOH120507FU9" : provider.rfc,
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
}