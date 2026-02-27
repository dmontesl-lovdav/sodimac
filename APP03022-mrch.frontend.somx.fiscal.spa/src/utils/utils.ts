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

const escapeCSVValues = (values: string[] | number[]) => {
    const escaped = values.map(item=>{
        const str = String(item).replace(/"/g, '""'); 
        return `"${str}"`; 
    })
    return escaped
};


export function exportToCSV(headers: any[], rows: any[], filename:string){
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

export function  toNumber(val: unknown, def = 0): number {
  if (val === null || val === undefined) return def;
  const n = typeof val === 'number' ? val : Number(val);
  return Number.isFinite(n) ? n : def;
};

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