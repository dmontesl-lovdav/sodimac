import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import type { PublishCreditNoteResponse } from "../parts/types";

function resolveFinanzasApiBaseUrl(): string {
  return (
    process.env.FINANZAS_API_URL ??
    process.env.API_FINANZAS_URL ??
    ""
  );
}

export const createCreditNotePublishClient = (api?: ApiClient) => {
  const client = api ?? createApiClient();
  const finanzasClient = createApiClient({
    baseUrl: resolveFinanzasApiBaseUrl(),
  });

  return {
    getUser: () => getUserIdFromStore(),

    validateXml: (file: File) => {
      const form = new FormData();
      form.append("file", file);
      return client.request<unknown>("fiscal/xml/process/file", "post", form);
    },

    publishCreditNote: (formData: FormData) =>
      client.request<PublishCreditNoteResponse>("invoices/register", "post", formData),

    /** PUT /rebates/:id — actualiza status del descuento comercial (1 → 2). */
    updateRebateStatus: (rebateId: string, status: number) => {
      const userId = Number(getUserIdFromStore());
      const body: { status: number; updatedBy?: number } = { status };
      if (Number.isFinite(userId)) {
        body.updatedBy = userId;
      }
      return finanzasClient.request<unknown>(
        `rebates/${encodeURIComponent(rebateId)}`,
        "put",
        body
      );
    },
  };
};
