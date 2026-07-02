/** Estatus: rechazo contable (solo en este estado se permite reprocesar) */
export const INVOICE_STATUS_RECHAZO_CONTABLE = 11;
/** Estatus: pendiente de contabilización (resultado del reproceso) */
export const INVOICE_STATUS_PENDIENTE_CONTABILIZACION = 3;
/** Estatus: disponibles para cancelación */
export const INVOICE_PENDIENTE_ADDENDA = 1;
export const INVOICE_RECIBIDO_PARCIAL = 2;
export const INVOICE_PROCESS_SENDED = 3;
export const INVOICE_ERROR_DATA = 6;
export const INVOICE_WRONG_DATA = 16;

export interface InvoiceFilters {
  rfcEmisor: string;
  fechaInicioRecepcion: string;
  fechaFinalRecepcion: string;
  tipoDocumento: string;
  page: number,
  size: number,
  estatus?: number | undefined;
  serie: string;
  uuid: string;
  folio: string;
  idProveedor: string;
  tipoProveedor?: string;
  paymentUUID?: string;
}

export const EMPTY_INVOICE: InvoiceFilters = {
  rfcEmisor: "",
  fechaInicioRecepcion: "",
  fechaFinalRecepcion: "",
  tipoDocumento: "",
  page: 0,
  size: 10,
  serie: "",
  uuid: "",
  folio: "",
  idProveedor: "",
  tipoProveedor: "",
}

export interface Invoice {
  invoiceUuid: string;
  fiscalUuid: string | null;
  documentType: string | null;
  series: string | null;
  folio: string | null;
  version: number | null;
  issueDate: string | null;            
  certificationDate: string | null;    
  total: number | null;
  subtotal: number | null;
  discount: number | null;
  currency: string | null;
  exchangeRate: number | null;
  paymentMethod: string | null;
  paymentForm: string | null;
  paymentConditions: string | null;
  placeOfIssue: string | null;
  estatus: number | null;
  //Legacy status, se mantiene para no afectar la funcionalidad actual, pero se planea eliminar en el futuro
  status: number | undefined | null;
  statusName: string | null;
  supplierName: string | null;
  createdAt: string | null;
  updatedAt: string | null;
  emisorRfc: string | null;
  emisorName: string | null;
  emisorTaxRegime: string | null;
  receptorRfc: string | null;
  receptorName: string | null;
  receptorTaxRegime: string | null;
  hasAddenda: boolean | null;
  addendaUuid: string | null;
  addendaType: string | null;
  addendaTypeName: string | null;
  noOrdenCompra: string | null;
  noRecepcion: string | null;
  numeroProveedor: string | null;
  tipoProveedorDescripcion: string | null;
  tipoProveedor: string | null;
  guiaEntrega: string | null;
  xmlContent: string | null;
  notasCreditoRelacionadas: any[];
}
