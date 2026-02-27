export const CreditsStatusOptions = [
    { value: 0, type: "done", label: "Disponible" },
    { value: 2, type: "done", label: "Manual" },
    { value: 3, type: "error", label: "Cancelada" },
    { value: 4, type: "error", label: "Borrado" },
];

export const CreditsMockProviderOptions = [
    { value: "JOH120507FU9", label: "Proveedor 1" },
    { value: "JOH120507F00", label: "Proveedor 2" },
];

export interface CreditNoteFilters {
  rfcEmisor: string;
  fechaInicioRecepcion: string;
  fechaFinalRecepcion: string;
  tipoDocumento: string;
  page: number,
  size: number,
  status?: string | undefined;
  serie: string;
  uuid: string;
  folio: string;
}

type CreditNoteFiltersRequired = { key: keyof CreditNoteFilters; label: string };

export const REQUIRED_CREDIT_NOTE_KEYS : CreditNoteFiltersRequired[] = [
  {"key": "fechaInicioRecepcion", "label": "Fecha de inicio de recepción"},
  {"key": "fechaFinalRecepcion", "label": "Fecha final de recepción"}
];

export const EMPTY_CREDIT_NOTE:CreditNoteFilters = {
  rfcEmisor: "",
  fechaInicioRecepcion: "2025-12-01",
  fechaFinalRecepcion: "2025-12-31",
  tipoDocumento: "",
  page: 0,
  size: 10,
  serie: "",
  uuid: "",
  folio: ""
}

export interface CreditNote {
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
  status: number | null;
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
