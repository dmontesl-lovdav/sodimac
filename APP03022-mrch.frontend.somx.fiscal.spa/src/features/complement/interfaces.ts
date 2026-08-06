export interface ComplementPaymentFilters {
  uuid?: string;
  serie?: string;
  folio?: string;
  rfcEmisor?: string;
  numeroProveedor?: string;
  rfcReceptor?: string;
  fechaPagoInicio?: string;
  fechaPagoFin?: string;
  fechaEmisionInicio?: string;
  fechaEmisionFin?: string;
  status?: string;
  page?: number;
  size?: number;
}

export interface ErrorResponse {
  response?: {
    data?: { errorCode?: string; message?: string }
  }
}

export const EMPTY_COMPLEMENT_PAYMENT: ComplementPaymentFilters = {
  fechaPagoInicio: "",
  fechaPagoFin: "",
  numeroProveedor: "",
  page: 0,
  size: 10,
};

export interface ComplementPayment {
  paymentsUuid: string;
  fiscalUuid: string;
  series: string;
  folio: string;
  subtotal: number;
  totalAmount: number;
  issuerRfc: string;
  issuerName: string;
  receiverRfc: string;
  receiverName: string;
  paymentDate: string;
  createdAt: string;
  statusDescription: string;
  relatedDocumentsCount?: number;
}

export interface RelatedInvoice {
   relatedDocumentUuid: string;
   paymentUuid: string;
   documentUuid: string;
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
  tipoDeComprobante: string;
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