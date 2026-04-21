import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { INVOICE_STATUS_PENDIENTE_CONTABILIZACION, InvoiceFilters } from "../interfaces";

export const createInvoicesClient = <T = unknown>(api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getInvoices: (filters: InvoiceFilters) => client.execute<T>("invoices/search", "post", {
      ...filters,
      tipoDocumento: "I",
    }),
    
    getXmlDocument: (uuid: string) => client.fetchDocument(`invoices/${uuid}/xml`),

    getPdfDocument: (uuid: string) =>
      client.fetchDocument(`pdf/from-uuid/${uuid}?inline=true`),

    reprocessInvoice: (uuid: string, numeroProveedor: string) =>
      client.execute("invoices", "put", {
        uuid,
        numeroProveedor,
        estatus: INVOICE_STATUS_PENDIENTE_CONTABILIZACION,
        idUsuarioActualizacion: client.getUserId(),
      }),

    cancelInvoice: (uuid: string, numeroProveedor: string) =>
      client.execute("invoices", "put", {
        uuid,
        numeroProveedor,
        estatus: 0,
        idUsuarioActualizacion: client.getUserId(),
      }),
  };
};
