
export interface PaymentSearchParams {
    providerId?: string;          
    paymentNumber?: string;        
    statusId?: number;           
    startDate: string;            
    endDate: string;              
    page?: number;
    size?: number;
}

export interface PaymentRecord {
    id?: string | number;          
    providerNumber: string;        
    providerName: string;        
    paymentNumber: string;        
    receptionNumber: string;     
    guideNumber: string;         
    invoiceNumber: string;       
    currency: string;            
    amount: number;             
    bulkQty: number;             
    palletQty: number;          
    totalQty: number;            
    paymentDate: string;        
    issueDate: string;         
    status: string;            
    statusId: number;           
    serie?: string;             
    folio?: string;              
    uuid?: string;               
    reportIds?: number[];        
}

export interface PaymentStatus {
    id: number;
    description: string;
}

export const PAYMENT_STATUSES: PaymentStatus[] = [
    { id: 0, description: 'Pendiente de complemento' },
    { id: 1, description: 'Complemento relacionado' },
    { id: 2, description: 'Pago cancelado' }
];

export interface PagedResult<T> {
    items: T[];
    totalItems: number;
    totalPages: number;
    currentPage: number;
}

export interface PaymentDetail extends PaymentRecord {
    documents: PaymentDocument[];
    complementInfo?: ComplementInfo;
    paymentYear?: string;
}

export interface PaymentDocument {
    id: string;
    documentNumber: string;
    documentType: string;
    reference?: string;
    documentDate: string;
    accountingDate?: string;
    dueDate: string;
    currency: string;
    amount: number;
    serie?: string;
    folio?: string;
    uuid?: string;
    status: string;
}

export interface ComplementInfo {
    serie: string;
    folio: string;
    uuid: string;
    uploadDate: string;
    status: string;
}

export interface ExportOptions {
    format: 'csv' | 'xlsx';
    includeHeaders: boolean;
    columns?: string[];
}

export interface UserRole {
    isAdmin: boolean;
    providerId?: string;
    providerName?: string;
}

