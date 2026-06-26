export interface MigoDocument {
    migoDocumentId: string;
    folio: string;
    fileName: string;
    totalRecords: number;
    numeroOc: number;
    numeroRecepcion: number;
    montoOc: number;
    numeroRechazoOc: number;
    status: number;
    rejectionReason?: string;
    publishedAt: string;
    authorizedAt?: string;
    fechaFlujo?: string;
    createdBy?: number;
    createdAt: string;
    updatedBy?: number;
    updatedAt?: string;
}

export interface MigoReception {
    migoReceptionId: string;
    migoDocumentId: string;
    nroOc: number;
    nroRecepcion: number;
    sucursal: number;
    nroGuia?: string;
    origen?: string;
    fechaRecepcion: string;
    importeSinImpuesto: number;
    sku?: string;
    descripcionSku?: string;
    cantidad: number;
    importeUnitario: number;
    importeSinImpuestoDet: number;
    montoOc?: number;
    isValid: boolean;
    validationError?: string;
    rowNumber: number;
    numeroProveedor?: string | null;
    vendorName?: string;
    emailFinancial?: string;
}

export interface MigoDocumentPage {
    content: MigoDocument[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
    numberOfElements: number;
}

export interface MigoReceptionPage {
    content: MigoReception[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
    numberOfElements: number;
}

export interface MigoSearchFilters {
    publishedAtStart?: string;
    publishedAtEnd?: string;
    fileName?: string;
    pageNumber: number;
    pageSize: number;
}

export const MIGO_STATUS_MAP: Record<number, { label: string; color: string }> = {
    9: { label: 'Publicado', color: '#2563eb' },
    0: { label: 'Autorizado', color: '#16a34a' },
    8: { label: 'Rechazado', color: '#dc2626' },
};
