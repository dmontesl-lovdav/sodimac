export type SapDocumentListItem = {
  sapDocumentUuid?: string;
  documentNumber?: string | null;
  docSap?: string | null;
  message?: string | null;
  createdAt?: string | Date | null;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

export function unwrapSapDocumentList(payload: unknown): SapDocumentListItem[] {
  if (Array.isArray(payload)) {
    return payload as SapDocumentListItem[];
  }
  if (isRecord(payload) && Array.isArray(payload.data)) {
    return payload.data as SapDocumentListItem[];
  }
  return [];
}

export function toAccountingDetailRow(item: SapDocumentListItem) {
  return {
    documentNumber: String(item.documentNumber ?? "").trim(),
    sapDocument: String(item.docSap ?? "").trim(),
    sapMessage: String(item.message ?? "").trim(),
    accountingDate: item.createdAt ? String(item.createdAt) : "",
  };
}
