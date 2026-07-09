/** Estatus: disponibles para cancelación */
export const CREDIT_NOTE_PENDIENTE_CONTABILIZAR = 2;
export const CREDIT_NOTE_RECHAZO_CONTABLE = 11;
export const CREDIT_NOTE_PROCESS_SENDED = 1;

export interface CreditNoteFilters {
  relatedInvoiceUuid: string;
  rfcEmisor: string;
  fechaInicioRecepcion: string;
  fechaFinalRecepcion: string;
  tipoDocumento: string;
  page: number,
  size: number,
  estatus?: string;
  serie: string;
  uuid: string;
  folio: string;
  idProveedor: string;
}

export const EMPTY_CREDIT_NOTE: CreditNoteFilters = {
  relatedInvoiceUuid: "",
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
}

export interface CreditNote {
  relatedInvoiceUuid: string | number | boolean;
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
  //Legacy status, se mantiene para no afectar la funcionalidad actual, pero se planea eliminar en el futuro
  status: number | undefined | null;
  estatus: number | undefined | null;
  statusName: string;
  supplierName: string | null;
  tipoProveedorDescripcion: string | null;
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
  tipoProveedor: string | null;
  guiaEntrega: string | null;
  xmlContent: string | null;
  notasCreditoRelacionadas: unknown | null;
}


export interface CreditNoteAxios {
    "message": string,
    "errorCode": number,
    "code": string,
    "httpStatus": number,
    "success": boolean,
    "detailError": string,
    "timeStamp": number,
    "trace_id": string,
    "data": {
        "content": any[]
    }

}
