import type { Rebate } from "../interfaces";

/** Alinea alias de campos que envía el BFF con la interfaz `Rebate`. */
export function normalizeRebateRow(raw: Record<string, unknown>): Rebate {
    const stamped = raw.stampedRebate as Rebate["stampedRebate"] | undefined;
    const documentReference = String(
        raw.documentReference ?? raw.referenceNumber ?? ""
    );
    const referenceNumber = String(
        raw.referenceNumber ?? raw.documentReference ?? ""
    );
    const supplierNumber = Number(raw.supplierNumber ?? raw.vendorNumber ?? 0);
    const vendorNumber = Number(raw.vendorNumber ?? raw.supplierNumber ?? 0);
    let sourceRaw: number | undefined;
    if (raw.source != null) {
        sourceRaw = Number(raw.source);
    } else if (raw.originId != null) {
        sourceRaw = Number(raw.originId);
    }

    return {
        ...(raw as unknown as Rebate),
        documentReference,
        referenceNumber: referenceNumber || undefined,
        supplierNumber,
        vendorNumber: Number.isFinite(vendorNumber) ? vendorNumber : undefined,
        source:
            sourceRaw != null && Number.isFinite(sourceRaw)
                ? sourceRaw
                : undefined,
        stampedRebate: stamped ?? null,
    };
}
