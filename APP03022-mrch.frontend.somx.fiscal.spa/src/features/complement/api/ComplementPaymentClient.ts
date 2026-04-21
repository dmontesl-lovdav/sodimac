import { createApiClient, type ApiClient } from "@/services/ApiClient";
import type { ComplementPaymentFilters, ComplementPaymentResponse } from "../interfaces";

const buildSearchParams = (filters: ComplementPaymentFilters): URLSearchParams => {
  const params = new URLSearchParams();
  if (filters.uuid) params.append("paymentsUuid", filters.uuid);
  if (filters.serie) params.append("series", filters.serie);
  if (filters.folio) params.append("folio", filters.folio);
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

export const createComplementPaymentClient = (api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getUser : () => client.getUserId(),

    getComplementPayments: (filters: ComplementPaymentFilters): Promise<ComplementPaymentResponse> => {
      const path = `fiscal/complementos-pago/buscar?${buildSearchParams(filters).toString()}`;
      return client.execute<ComplementPaymentResponse>(path, "get");
    },

    getRelatedInvoices: (paymentUUID: string) => client.execute<any>("related-documents/by-payment/"+paymentUUID, "get"),


    getXmlDocument: (uuid: string) => client.fetchDocument(`invoices/${uuid}/xml`),

    getPdfDocument: (uuid: string) =>
      client.fetchDocument(`pdf/from-uuid/${uuid}?inline=true`),

    publishComplement: (formData: FormData) =>
      client.execute<{ success?: boolean; message?: string }>("fiscal/complementos-pago/registrar", "post", formData),

    relateComplement: (transactionId: string, idPago: string) =>
      client.execute<{ success?: boolean; message?: string }>("fiscal/complementos-pago/relacionar", "post", {
        idTransaccion: transactionId,
        idPago,
      }),
  };
};
