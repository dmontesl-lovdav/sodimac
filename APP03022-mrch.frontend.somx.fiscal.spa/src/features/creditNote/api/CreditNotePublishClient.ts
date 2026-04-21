import { createApiClient, type ApiClient } from "@/services/ApiClient";
import type { CreditNotePublishApiPayload } from "../utils/publishCreditNoteResponse";

export const createCreditNotePublishClient = (api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getUser: () => client.getUserId(),

    publishCreditNote: (formData: FormData) =>
      client.execute<CreditNotePublishApiPayload>(
        "invoices/register",
        "post",
        formData,
      ),
  };
};

