
export interface InvoiceSearchParams {
    startDate: string;              
    endDate: string;                
    providerId?: string;            
    serie?: string;                 
    folio?: string;                
    statusId?: number;              
    page?: number;
    size?: number;
}

export interface InvoiceRecord {
    id?: string | number;
    serie: string;
    folio: string;
    subtotal: number;
    total: number;
    purchaseOrder: string;
    reception: string;
    uuid: string;
    creditNotesCount: number;
    providerId: string;
    providerName: string;
    issueDate: string;              
    receptionDate: string;          
    sendDate: string;               
    statusId: number;
    status: string;
}

export interface CreditNoteRecord {
    id?: string | number;
    serie: string;
    folio: string;
    subtotal: number;
    total: number;
    reason: string;
    uuid: string;
    issueDate: string;
    receptionDate: string;
    sendDate: string;
    invoiceSerie: string;
    invoiceFolio: string;
    invoiceUuid: string;
}

export interface InvoiceStatus {
    id: number;
    description: string;
}

export const INVOICE_STATUSES: InvoiceStatus[] = [
    { id: 1, description: 'Activa' },
    { id: 2, description: 'Procesada' },
    { id: 3, description: 'Cancelada' }
];

export interface PagedResult<T> {
    items: T[];
    totalItems: number;
    totalPages: number;
    currentPage: number;
}

export interface ExportRequest {
    invoiceIds?: string[];
    searchParams?: InvoiceSearchParams;
    format: 'xlsx' | 'xml' | 'pdf';
}

export const VALIDATION_MESSAGES = {
    'INF6000': 'No existe información con los criterios establecidos',
    'WRN7000': 'La fecha inicio no puede ser superior a la fecha final',
    'WRN7006': 'La fecha final no puede ser menor a la fecha inicio',
    'WRN7005': 'El periodo de búsqueda no puede ser superior a 6 meses, favor de validar',
    'INF6001': 'No existe información seleccionada para descargar, favor de validar',
    'SUCCESS_DOWNLOAD': 'Descarga completada exitosamente',
    'SUCCESS_CANCEL': 'Facturas enviadas a cancelación'
};

