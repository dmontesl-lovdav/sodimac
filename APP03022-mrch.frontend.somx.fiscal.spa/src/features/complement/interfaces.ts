export interface ComplementPaymentFilters {
  uuid?: string;
  serie?: string;
  folio?: string;
  rfcEmisor?: string;
  numeroProveedor?: string;
  tipoProveedor?: string;
  rfcReceptor?: string;
  fechaPagoInicio?: string;
  fechaPagoFin?: string;
  fechaRegistroInicio?: string;
  fechaRegistroFin?: string;
  fechaEmisionInicio?: string;
  fechaEmisionFin?: string;
  status?: string;
  page?: number;
  size?: number;
}

export interface ErrorResponse {
  response?: {
    data?: { errorCode?: string; message?: string };
  };
}

export const EMPTY_COMPLEMENT_PAYMENT: ComplementPaymentFilters = {
  fechaRegistroInicio: "",
  fechaRegistroFin: "",
  numeroProveedor: "",
  page: 0,
  size: 10,
};

export interface ComplementPayment {
  paymentsUuid: string;
  fiscalUuid: string;
  series: string;
  folio: string;
  subtotalAmount: number;
  totalAmount: number;
  issuerRfc: string;
  issuerName: string;
  receiverRfc: string;
  tipoProveedorDescripcion: string;
  receiverName: string;
  paymentDate: string;
  certificationDate?: string;
  createdAt: string;
  status?: number;
  statusDescription: string;
  relatedDocumentsCount?: number;
  xmlContent?: string | null;
}

export interface RelatedInvoice {
  relatedDocumentUuid: string;
  paymentUuid: string;
  /** invoice_uuid interno (para PDF). */
  documentUuid: string;
  /** Folio fiscal SAT (para mostrar y XML). */
  fiscalUuid?: string;
  amountPaid: number;
  previousBalance: number;
  remainingBalance: number;
  installmentNumber: number;
  series: string;
  folio: string;
  currency: string;
  exchangeRate: number;
}

export interface ComplementPaymentResponse {
  content: ComplementPayment[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface PaymentHeaderData {
  uuid: string;
  rfcProveedor: string;
  idProveedor: string;
  nombreProveedor: string;
  referenciaPago: string;
  anioPagos: string;
  moneda: string;
  monto: string;
  status: string;
  fechaRegistro: string;
}

export interface XmlComplementPreview {
  uuid: string;
  rfcEmisor: string;
  nombreEmisor: string;
  monto: string;
  fechaTimbrado: string;
  serie: string;
  folio: string;
  tipoComprobante: string;
  formaDePagoP: string;
  fechaPago: string;
}

export const EMPTY_HEADER: PaymentHeaderData = {
  uuid: "--",
  rfcProveedor: "--",
  idProveedor: "--",
  nombreProveedor: "--",
  referenciaPago: "--",
  anioPagos: "--",
  moneda: "--",
  monto: "--",
  status: "--",
  fechaRegistro: "--",
};

export type ProviderCatalogItem = {
  id: string;
  idProveedor: string;
  rfc: string;
  businessName?: string;
  tipoProveedor?: {
    id?: string;
  };
};

export type QueryPaymentData = {
  uuid: string;
  referenciaPago: string;
  idProveedor: string;
  moneda: string;
  monto: string;
  fechaRegistro: string;
  anioPagos: string;
  status: string;
};
