import type { Rebate } from "../interfaces";

/** Parámetros de URL compartidos entre la grilla y `RebateDetailView`. */
export type RebateDetailFromQuery = {
    rebateId: string;
    supplierNumber: string;
    documentNumber: string;
    documentReference: string;
    sapDocument: string;
    postingDate: string;
    dueDate: string;
    tipoRebate: string;
    amount: string;
    originId: string;
    periodId: string;
    status: string;
    vendorName: string;
    createdBy: string;
    createdAt: string;
    stampedRebateUuid: string;
    stampedDocumentNumber: string;
    stampedReferenceNumber: string;
    stampedStatus: string;
    stampedInvoiceFiscalUuid: string;
    stampedCreatedBy: string;
    stampedCreatedAt: string;
};

function setIfPresent(p: URLSearchParams, key: string, value: unknown): void {
    if (value === undefined || value === null || value === "") return;
    p.set(key, String(value));
}

/** Serializa una fila `Rebate` para abrir el detalle en la misma app. */
export function buildRebateDetailSearchParams(r: Rebate): URLSearchParams {
    const p = new URLSearchParams();
    const sr = r.stampedRebate;

    setIfPresent(p, "rebateId", r.rebateId);
    setIfPresent(p, "supplierNumber", r.supplierNumber);
    setIfPresent(p, "documentNumber", r.documentNumber);
    setIfPresent(
        p,
        "documentReference",
        r.documentReference || r.referenceNumber || ""
    );
    setIfPresent(
        p,
        "referenceNumber",
        r.referenceNumber || r.documentReference || ""
    );
    setIfPresent(p, "sapDocument", r.sapDocument);
    setIfPresent(p, "postingDate", r.postingDate);
    setIfPresent(p, "dueDate", r.dueDate);
    setIfPresent(p, "amount", r.amount);
    setIfPresent(p, "originId", r.originId);
    setIfPresent(p, "periodId", r.periodId);
    setIfPresent(p, "status", r.status);
    setIfPresent(
        p,
        "vendorName",
        r.vendorName || r.supplier?.businessName || ""
    );
    setIfPresent(p, "createdBy", r.createdBy);
    setIfPresent(p, "createdAt", r.createdAt);

    if (sr) {
        setIfPresent(p, "sr_uuid", sr.stampedRebateUuid);
        setIfPresent(p, "sr_doc", sr.documentNumber);
        setIfPresent(p, "sr_ref", sr.referenceNumber);
        setIfPresent(p, "sr_status", sr.status);
        setIfPresent(p, "sr_invoice", sr.invoiceFiscalUuid);
        setIfPresent(p, "sr_createdBy", sr.createdBy);
        setIfPresent(p, "sr_createdAt", sr.createdAt);
    }

    return p;
}

export function parseRebateDetailFromSearchParams(
    sp: URLSearchParams
): RebateDetailFromQuery {
    const g = (k: string) => sp.get(k)?.trim() ?? "";
    return {
        rebateId: g("rebateId"),
        supplierNumber: g("supplierNumber"),
        documentNumber: g("documentNumber"),
        documentReference: g("documentReference"),
        sapDocument: g("sapDocument"),
        postingDate: g("postingDate"),
        dueDate: g("dueDate"),
        tipoRebate: g("tipoRebate"),
        amount: g("amount"),
        originId: g("originId"),
        periodId: g("periodId"),
        status: g("status"),
        vendorName: g("vendorName"),
        createdBy: g("createdBy"),
        createdAt: g("createdAt"),
        stampedRebateUuid: g("sr_uuid"),
        stampedDocumentNumber: g("sr_doc"),
        stampedReferenceNumber: g("sr_ref"),
        stampedStatus: g("sr_status"),
        stampedInvoiceFiscalUuid: g("sr_invoice"),
        stampedCreatedBy: g("sr_createdBy"),
        stampedCreatedAt: g("sr_createdAt"),
    };
}
