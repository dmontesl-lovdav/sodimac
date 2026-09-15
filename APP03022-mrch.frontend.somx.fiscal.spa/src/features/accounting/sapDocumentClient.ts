import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { unwrapSapDocumentList, type SapDocumentListItem } from "./accountingApi";

function resolveFinanzasApiBaseUrl(): string {
  return process.env.FINANZAS_API_URL ?? process.env.API_FINANZAS_URL ?? "";
}

export function createSapDocumentClient(api?: ApiClient) {
  const client =
    api ??
    createApiClient({
      baseUrl: resolveFinanzasApiBaseUrl(),
    });

  return {
    listByFiscalUuid: async (fiscalUuid: string): Promise<SapDocumentListItem[]> => {
      const payload = await client.request<unknown>(
        `sap-documents/by-fiscal-uuid/${encodeURIComponent(fiscalUuid)}`,
        "get"
      );
      return unwrapSapDocumentList(payload);
    },
  };
}
