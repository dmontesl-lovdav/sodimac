import type { AxiosRequestConfig } from "axios";
import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import { INVOICE_STATUS_PENDIENTE_CONTABILIZACION, InvoiceFilters } from "../interfaces";

const blobResponse: AxiosRequestConfig = { responseType: "blob" };

export const createInvoicesClient = <T = unknown>(api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getInvoices: (filters: InvoiceFilters) =>
      client.request<T>("invoices/search", "post", {
        ...filters,
        tipoDocumento: "I",
      }),

    getXmlDocument: (xmlContent: string | null | undefined): Promise<{ data: string }> => {
      const data = typeof xmlContent === "string" ? xmlContent.trim() : "";
      if (!data) {
        return Promise.reject(new Error("XML no disponible en el registro"));
      }
      return Promise.resolve({ data });
    },

    getPdfDocument: (uuid: string) =>
      client.request<Blob>(`pdf/from-uuid/${uuid}?inline=true`, "get", undefined, blobResponse),

    reprocessInvoice: (uuid: string, numeroProveedor: string) =>
      client.request("invoices", "put", {
        uuid,
        numeroProveedor,
        estatus: INVOICE_STATUS_PENDIENTE_CONTABILIZACION,
        idUsuarioActualizacion: getUserIdFromStore() ?? "1",
      }),

    cancelInvoice: (uuid: string, numeroProveedor: string) =>
      client.request("invoices", "put", {
        uuid,
        numeroProveedor,
        estatus: 20,
        idUsuarioActualizacion: getUserIdFromStore() ?? "1",
      }),
  };
};
