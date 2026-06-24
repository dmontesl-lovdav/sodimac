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
    const sourceRaw =
        raw.source != null
            ? Number(raw.source)
            : raw.originId != null
              ? Number(raw.originId)
              : undefined;

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
