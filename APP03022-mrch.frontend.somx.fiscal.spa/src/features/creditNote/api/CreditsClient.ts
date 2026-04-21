import { createApiClient, type ApiClient } from "@/services/ApiClient";

export const createCreditsClient = <T = unknown>(api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    cancelCreditNote: (uuid: string, numeroProveedor: string) =>
      client.execute("invoices", "put", {
        uuid,
        numeroProveedor,
        estatus: 0,
        idUsuarioActualizacion: client.getUserId(),
      }),
    
    getCreditNotes: (filters: unknown) => client.execute<T>("invoices/search", "post", filters),

    getXmlDocument: (uuid: string) => client.fetchDocument(`invoices/${uuid}/xml`),

    getPdfDocument: (uuid: string) =>
      client.fetchDocument(`pdf/from-uuid/${uuid}?inline=true`),
  };
};
