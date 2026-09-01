import { exportToCSV, formatDate, formatFilenameTimestamp } from "@/utils/utils";
import type { MigoReception } from "./interfaces";

export interface GroupedMigoReception {
    key: string;
    nroOc: number;
    nroRecepcion: number;
    sucursal: number;
    numeroProveedor: string;
    nroGuia: string;
    origen: string;
    fechaRecepcion: string;
    importeSinImpuesto: number;
    montoOc: number;
    vendorName: string;
    emailFinancial: string;
}

/** Columnas visibles en el grid de recepciones agrupadas de un documento MIGO. */
export const MIGO_GROUPED_RECEPTION_CSV_HEADERS = [
    "Orden Compra",
    "Recepción",
    "Sucursal",
    "Número Proveedor",
    "Nombre Proveedor",
    "Correo electrónico",
    "Guía",
    "Origen",
    "Fecha Recepción",
    "Importe",
    "Monto OC",
] as const;

export function formatMigoCurrency(val: number | undefined | null): string {
    if (val == null || Number.isNaN(Number(val))) return "-";
    return Number(val).toLocaleString("es-MX", { minimumFractionDigits: 2 });
}

export function groupMigoReceptions(
    allReceptions: MigoReception[]
): GroupedMigoReception[] {
    const groups = new Map<string, GroupedMigoReception>();
    for (const r of allReceptions) {
        const key = `${r.nroOc}-${r.nroRecepcion}`;
        if (!groups.has(key)) {
            groups.set(key, {
                key,
                nroOc: r.nroOc,
                nroRecepcion: r.nroRecepcion,
                sucursal: r.sucursal,
                numeroProveedor: (r.numeroProveedor ?? "").toString().trim(),
                nroGuia: r.nroGuia ?? "-",
                origen: r.origen ?? "-",
                fechaRecepcion: r.fechaRecepcion,
                importeSinImpuesto: r.importeSinImpuesto,
                montoOc: r.montoOc ?? 0,
                vendorName: (r.vendorName ?? "").trim(),
                emailFinancial: (r.emailFinancial ?? "").trim(),
            });
        }
    }
    return Array.from(groups.values());
}

export function mapGroupedMigoReceptionToCsvRow(
    r: GroupedMigoReception
): string[] {
    return [
        String(r.nroOc ?? ""),
        String(r.nroRecepcion ?? ""),
        String(r.sucursal ?? ""),
        r.numeroProveedor || "--",
        r.vendorName || "--",
        r.emailFinancial || "--",
        r.nroGuia,
        r.origen,
        formatDate(r.fechaRecepcion),
        formatMigoCurrency(r.importeSinImpuesto),
        formatMigoCurrency(r.montoOc),
    ];
}

export function exportGroupedMigoReceptionsCsv(
    receptions: GroupedMigoReception[],
    fileBaseName: string
): void {
    exportToCSV(
        [...MIGO_GROUPED_RECEPTION_CSV_HEADERS],
        receptions.map(mapGroupedMigoReceptionToCsvRow),
        fileBaseName
    );
}

export function migoReceptionsCsvFileName(documentIdOrFolio: string): string {
    return `recepciones-migo-${documentIdOrFolio}_${formatFilenameTimestamp()}`;
}

