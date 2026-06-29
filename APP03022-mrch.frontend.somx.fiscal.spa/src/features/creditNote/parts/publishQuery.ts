import type { PublishQuery } from "./types";

export function parsePublishQuery(search: string): PublishQuery {
  const params = new URLSearchParams(search);
  return {
    supplierNumber: params.get("supplierNumber") ?? "",
    documentNumber: params.get("documentNumber") ?? "",
  };
}

export function isCommercialDiscountFlow(query: PublishQuery): boolean {
  return query.documentNumber.trim() !== "";
}
