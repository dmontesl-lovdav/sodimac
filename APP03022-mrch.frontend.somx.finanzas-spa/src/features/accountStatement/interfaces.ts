export type AccountStatementStatus =
    | 'Generado'
    | 'Publicado'
    | 'Revisado'
    | 'Rechazado'
    | 'Reprocesado';

export interface AccountStatementRecord {
    accountStatementUuid: string;
    vendorNumber: string;
    vendorName: string;
    year: number;
    month: number;
    statusLabel: AccountStatementStatus;
    processedAt: string;
    reviewedAt: string;
}

export interface AccountStatementFilters {
    providerId?: string;
    year: number;
    month: number | 'all';
}

export interface PagedAccountStatementResult {
    items: AccountStatementRecord[];
    totalItems: number;
    totalPages: number;
    currentPage: number;
}

export interface ProviderOption {
    value: string;
    label: string;
}

