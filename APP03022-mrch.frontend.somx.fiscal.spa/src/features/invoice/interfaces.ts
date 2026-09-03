/** CatEstatusFactura.value — Rechazo Contable (EFA0017). */
export const INVOICE_STATUS_RECHAZO_CONTABLE = 16;

/** Reproceso contable activo: 6, 9, 10, 13, 16. */
export const StatusReprocesoContable = [6, 9, 10, 13, 16];

/** Cancelar factura activo: 2 Recibido Parcial, 3 En proceso de envío. */
export const StatusCancelarFactura = [2, 3];

/** Destino del PUT de reproceso (CatEstatusFactura.value 3 = En proceso de envío). */
export const INVOICE_STATUS_PENDIENTE_CONTABILIZACION = 3;
/** value 1 = No válido fiscal */
export const INVOICE_PENDIENTE_ADDENDA = 1;
/** value 2 = Recibido Parcial */
export const INVOICE_RECIBIDO_PARCIAL = 2;
/** value 3 = En proceso de envío */
export const INVOICE_PROCESS_SENDED = 3;
/** value 6 = Error en el desglose xml */
export const INVOICE_ERROR_DATA = 6;
/** value 20 = Cancelada — ninguna acción del grid */
export const INVOICE_STATUS_CANCELADA = 20;

export interface InvoiceFilters {
  rfcEmisor: string;
  fechaInicioRecepcion: string;
  fechaFinalRecepcion: string;
  tipoDocumento: string;
  page: number,
  size: number,
  estatus?: number;
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
