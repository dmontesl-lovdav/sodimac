export interface PaymentSearchParams {
    providerId?: string;
    paymentNumber?: string;
    referenceNumber?: string;
    statusId?: number;
    startDate: string;
    endDate: string;
    page?: number;
    size?: number;
}

export interface PaymentRecord {
    idPago: string;

    // ✅ NUEVO: UUID de cabecera para navegar a detalle paginado (GET header-with-details/:uuid)
    paymentHeaderUuid?: string | null;

    documentNumber: string;
    documentReference: string;
    providerNumber: string;
    providerName: string;
    currency: string;
    amount: number;
    documentType: string;
    sapDocument: string;
    paymentDate: string;
    paymentYear: string;
    status: string;
    statusId: number;
    createdAt: string;
    updatedAt: string;
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
    sapDocument?: string;
    paymentDate?: string;
    status: string;
    createdAt?: string;
    updatedAt?: string;
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