
export interface PaymentInfo {
    paymentId: string;
    paymentNumber: string;
    paymentDate: string;
    paymentAmount: number;
    currency: string;
    providerNumber: string;
    providerName: string;
    invoiceCount: number;
    creditNoteCount: number;
    totalDocuments: number;
    status: string;
    statusId: number;
}

export interface ComplementDocument {
    type: 'xml' | 'pdf';
    file?: File;
    fileName?: string;
    uploadDate?: string;
    status?: string;
}

export interface ComplementUploadRequest {
    paymentId: string;
    xmlFile: File;
    pdfFile?: File;
}

export interface ComplementUploadResponse {
    success: boolean;
    message: string;
    complementId?: string;
    errors?: ValidationError[];
}

export interface ValidationError {
    code: string;
    message: string;
    field?: string;
}

export interface LastPublishedComplement {
    complementId: string;
    fileName: string;
    uploadDate: string;
    status: string;
    xmlUrl?: string;
    pdfUrl?: string;
}

export const VALIDATION_MESSAGES = {
    'WRN7004': 'Se requiere publicar el complemento de pago en formato XML.',
    'BUS2006': 'Las facturas no corresponden al complemento que desea publicar.',
    'SUCCESS_UPLOAD': 'Complemento publicado exitosamente.',
    'ERROR_UPLOAD': 'Error al publicar el complemento. Por favor intente nuevamente.'
};

