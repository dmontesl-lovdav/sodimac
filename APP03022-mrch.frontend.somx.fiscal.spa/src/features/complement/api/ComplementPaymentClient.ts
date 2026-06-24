import type { AxiosRequestConfig } from "axios";
import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import type { ComplementPaymentFilters, ComplementPaymentResponse, RelatedInvoice } from "../interfaces";

/** Respuesta paginada de facturas relacionadas a un complemento (según consumo en `ComplementRelatedInvoices`). */
export type RelatedDocumentsResponse = {
  content?: RelatedInvoice[];
  totalElements?: number;
  totalPages?: number;
  page?: number;
};

const buildSearchParams = (filters: ComplementPaymentFilters): URLSearchParams => {
  const params = new URLSearchParams();
  if (filters.uuid) params.append("paymentsUuid", filters.uuid);
  if (filters.serie) params.append("series", filters.serie);
  if (filters.folio) params.append("folio", filters.folio);
  if (filters.numeroProveedor?.trim()) params.append("numeroProveedor", filters.numeroProveedor.trim());
  if (filters.rfcEmisor) params.append("rfcEmisor", filters.rfcEmisor);
  if (filters.rfcReceptor) params.append("rfcReceptor", filters.rfcReceptor);
  if (filters.fechaPagoInicio) params.append("fechaPagoInicio", filters.fechaPagoInicio);
  if (filters.fechaPagoFin) params.append("fechaPagoFin", filters.fechaPagoFin);
  if (filters.fechaEmisionInicio) params.append("fechaEmisionInicio", filters.fechaEmisionInicio);
  if (filters.fechaEmisionFin) params.append("fechaEmisionFin", filters.fechaEmisionFin);
  if (filters.status) params.append("status", filters.status);
  params.append("page", String(filters.page ?? 0));
  params.append("size", String(filters.size ?? 10));
  return params;
};

const textResponse: AxiosRequestConfig = { responseType: "text" };
const blobResponse: AxiosRequestConfig = { responseType: "blob" };

export const createComplementPaymentClient = (api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getUser: (): string | null => getUserIdFromStore(),

    getComplementPayments: (filters: ComplementPaymentFilters): Promise<ComplementPaymentResponse> => {
      const path = `fiscal/complementos-pago/buscar?${buildSearchParams(filters).toString()}`;
      return client.request<ComplementPaymentResponse>(path, "get");
    },

    getRelatedInvoices: (paymentUUID: string) =>
      client.request<RelatedDocumentsResponse>(`related-documents/by-payment/${paymentUUID}`, "get"),

    getXmlDocument: async (uuid: string): Promise<{ data: string }> => {
      const data = await client.request<string>(`invoices/${uuid}/xml`, "get", undefined, textResponse);
      return { data };
    },

    getPdfDocument: (uuid: string) =>
      client.request<Blob>(`pdf/from-uuid/${uuid}?inline=true`, "get", undefined, blobResponse),

    publishComplement: (formData: FormData) =>
      client.request<{ success?: boolean; message?: string }>(
        "fiscal/complementos-pago/registrar",
        "post",
        formData
      ),

    relateComplement: (transactionId: string, idPago: string) =>
      client.request<{ success?: boolean; message?: string }>("fiscal/complementos-pago/relacionar", "post", {
        idTransaccion: transactionId,
        idPago,
      }),
  };
};
