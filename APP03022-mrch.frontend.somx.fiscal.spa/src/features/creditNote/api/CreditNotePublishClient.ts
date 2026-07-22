import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import type { PublishCreditNoteResponse } from "../parts/types";

const fiscalApi = createApiClient({
  baseUrl: process.env.FISCAL_API_URL ?? "",
});

export const createCreditNotePublishClient = (api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getUser: () => getUserIdFromStore(),

    validateXml: (file: File) => {
      const form = new FormData();
      form.append("file", file);
      return client.request<unknown>("fiscal/xml/process/file", "post", form);
    },

    publishCreditNote: (formData: FormData) =>
      client.request<PublishCreditNoteResponse>("invoices/register", "post", formData),
  };
};
