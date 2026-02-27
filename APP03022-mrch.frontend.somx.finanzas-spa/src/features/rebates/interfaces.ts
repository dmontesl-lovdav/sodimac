export interface SearchParams {
    vendorId?: string;
    sapDocument?: string;
    documentNumber?: string;
    dateStart?: string;
    dateEnd?: string;
    rebateTypeId?: number;
    statusId?: number;
    page?: number;
    size?: number;
}

export interface DiscountRecord {
    id: number;
    vendorNumber: string;
    vendorName: string;
    documentNumber: string;
    referenceDocument: string;
    rebateType: string;
    sapDocument: string;
    amount: number;
    periodId: number;
    periodName: string;
    status: string;
    applyDate: string;
    expirationDate: string;
}

export interface PagedResult<T> {
    items: T[];
    totalItems: number;
    totalPages: number;
    page: number;
}
