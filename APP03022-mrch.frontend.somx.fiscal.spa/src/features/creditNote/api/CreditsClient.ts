import type { AxiosRequestConfig } from "axios";
import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import {
  CREDIT_NOTE_STATUS_PENDIENTE_CONTABILIZACION,
  type CreditNoteFilters,
} from "../interfaces";

const blobResponse: AxiosRequestConfig = { responseType: "blob" };

export const createCreditsClient = <T = unknown>(api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getUser: () => getUserIdFromStore(),

    //Usar esta función para obtener el id del usuario actualización: getUserIdFromStore() ?? "1",
    cancelCreditNote: (uuid: string, numeroProveedor: string) =>
      client.request("invoices", "put", {
        uuid,
        numeroProveedor,
        estatus: 0,
        idUsuarioActualizacion: 1,
      }),

    reprocessCreditNote: (uuid: string, numeroProveedor: string) =>
      client.request("invoices", "put", {
        uuid,
        numeroProveedor,
        estatus: CREDIT_NOTE_STATUS_PENDIENTE_CONTABILIZACION,
        idUsuarioActualizacion: getUserIdFromStore() ?? "1",
      }),

    getCreditNotes: (filters: CreditNoteFilters & { tipoDocumento?: string }) =>
      client.request<T>("invoices/search", "post", filters),

    getXmlDocument: (xmlContent: string | null | undefined): Promise<{ data: string }> => {
      const data = typeof xmlContent === "string" ? xmlContent.trim() : "";
      if (!data) {
        return Promise.reject(new Error("XML no disponible en el registro"));
      }
      return Promise.resolve({ data });
    },

    getPdfDocument: (uuid: string) =>
      client.request<Blob>(`pdf/from-uuid/${uuid}?inline=true`, "get", undefined, blobResponse),
  };
};
