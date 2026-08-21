import type { AxiosRequestConfig } from "axios";
import { createApiClient, type ApiClient } from "@/services/ApiClient";
import { getUserIdFromStore } from "@/utils/getUserIdFromStore";
import type {
  ComplementPayment,
  ComplementPaymentFilters,
  ComplementPaymentResponse,
  RelatedInvoice,
} from "../interfaces";

/** Respuesta paginada de facturas relacionadas a un complemento. */
export type RelatedDocumentsResponse = {
  content?: RelatedInvoice[];
  totalElements?: number;
  totalPages?: number;
  page?: number;
  number?: number;
};

const buildSearchParams = (filters: ComplementPaymentFilters): URLSearchParams => {
  const params = new URLSearchParams();
  if (filters.uuid?.trim()) params.append("paymentsUuid", filters.uuid.trim());
  if (filters.serie?.trim()) params.append("serie", filters.serie.trim());
  if (filters.folio?.trim()) params.append("folio", filters.folio.trim());
  if (filters.numeroProveedor?.trim()) {
    params.append("numeroProveedor", filters.numeroProveedor.trim());
  }
  if (filters.tipoProveedor?.trim()) {
    params.append("tipoProveedor", filters.tipoProveedor.trim());
  }
  if (filters.rfcEmisor?.trim()) params.append("rfcEmisor", filters.rfcEmisor.trim());
  if (filters.rfcReceptor?.trim()) params.append("rfcReceptor", filters.rfcReceptor.trim());
  if (filters.fechaPagoInicio) params.append("fechaPagoInicio", filters.fechaPagoInicio);
  if (filters.fechaPagoFin) params.append("fechaPagoFin", filters.fechaPagoFin);
  if (filters.fechaRegistroInicio) {
    params.append("fechaRegistroInicio", filters.fechaRegistroInicio);
  }
  if (filters.fechaRegistroFin) {
    params.append("fechaRegistroFin", filters.fechaRegistroFin);
  }
  if (filters.status != null && String(filters.status).trim() !== "") {
    params.append("status", String(filters.status).trim());
  }
  params.append("page", String(filters.page ?? 0));
  params.append("size", String(filters.size ?? 10));
  return params;
};

const blobResponse: AxiosRequestConfig = { responseType: "blob" };

const apiBase = () =>
  (process.env.REACT_APP_API_BASE_URL ?? process.env.API_BASE_URL ?? "").replace(/\/$/, "");

export const createComplementPaymentClient = (api?: ApiClient) => {
  const client = api ?? createApiClient();

  return {
    getUser: (): string | null => getUserIdFromStore(),

    getComplementPayments: (
      filters: ComplementPaymentFilters
    ): Promise<ComplementPaymentResponse> => {
      const path = `fiscal/complementos-pago/buscar?${buildSearchParams(filters).toString()}`;
      return client.request<ComplementPaymentResponse>(path, "get");
    },

    /** Obtiene un complemento por paymentsUuid (PK de la ruta de detalle). */
    getComplementByPaymentsUuid: async (
      paymentsUuid: string
    ): Promise<ComplementPayment | null> => {
      const result = await client.request<ComplementPaymentResponse>(
        `fiscal/complementos-pago/buscar?paymentsUuid=${encodeURIComponent(paymentsUuid)}&page=0&size=1`,
        "get"
      );
      return result?.content?.[0] ?? null;
    },

    getRelatedInvoices: (paymentUUID: string) =>
      client.request<RelatedDocumentsResponse>(
        `related-documents/by-payment/${paymentUUID}`,
        "get"
      ),

    /** XML del GET buscar; si no viene, GET /invoices/{uuid}/xml */
    getXmlFromContent: (xmlContent: string | null | undefined): Promise<{ data: string }> => {
      const data = typeof xmlContent === "string" ? xmlContent.trim() : "";
      if (!data) {
        return Promise.reject(new Error("XML no disponible en el registro"));
      }
      return Promise.resolve({ data });
    },

    /** XML de factura/NC o complemento: GET /invoices/{uuid}/xml (texto plano). */
    getXmlDocument: async (fiscalUuid: string): Promise<{ data: string }> => {
      const data = await client.request<string>(
        `invoices/${encodeURIComponent(fiscalUuid)}/xml`,
        "get",
        undefined,
        {
          responseType: "text",
          headers: {
            Accept: "application/xml, text/xml, text/plain, */*",
          },
        }
      );
      const xml = typeof data === "string" ? data.trim() : "";
      if (!xml) {
        return Promise.reject(new Error("XML no disponible para este documento"));
      }
      return { data: xml };
    },

    /** PDF factura/NC/complemento: GET /invoices/{uuid}/pdf */
    getInvoicePdfUrl: (invoiceUuid: string) =>
      `${apiBase()}/invoices/${encodeURIComponent(invoiceUuid)}/pdf`,

    getPdfDocument: (uuid: string) =>
      client.request<Blob>(
        `invoices/${encodeURIComponent(uuid)}/pdf`,
        "get",
        undefined,
        {
          ...blobResponse,
          headers: {
            Accept: "application/pdf, application/octet-stream, */*",
          },
        }
      ),

    publishComplement: (formData: FormData) =>
      client.request<{ success?: boolean; message?: string }>(
        "fiscal/complementos-pago/registrar",
        "post",
        formData
      ),

    relateComplement: (transactionId: string, idPago: string) =>
      client.request<{ success?: boolean; message?: string }>(
        "fiscal/complementos-pago/relacionar",
        "post",
        {
          idTransaccion: transactionId,
          idPago,
        }
      ),
  };
};
