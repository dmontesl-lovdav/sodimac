import type { PublishQuery } from "./types";

function param(params: URLSearchParams, ...keys: string[]): string {
  for (const key of keys) {
    const value = params.get(key)?.trim();
    if (value) return value;
  }
  return "";
}

export function parsePublishQuery(search: string): PublishQuery {
  const params = new URLSearchParams(search);
  return {
    supplierNumber: param(params, "supplierNumber", "numeroProveedor"),
    documentNumber: param(params, "documentNumber", "numeroDocumento"),
    documentReference: param(params, "documentReference", "referenciaDocumento", "referenceNumber"),
    tipoRebate: param(params, "tipoRebate"),
    sapDocument: param(params, "sapDocument"),
    amount: param(params, "amount"),
    periodId: param(params, "periodId", "periodName"),
    postingDate: param(params, "postingDate"),
    dueDate: param(params, "dueDate"),
    vendorName: param(params, "vendorName", "nombreProveedor"),
  };
}

export function isCommercialDiscountFlow(query: PublishQuery): boolean {
  return query.documentNumber.trim() !== "";
}

export function displayOrDash(value: string | undefined | null): string {
  const trimmed = value?.trim();
  return trimmed ? trimmed : "--";
}
