import { 
    PaymentInfo, 
    ComplementUploadRequest, 
    ComplementUploadResponse,
    LastPublishedComplement,
    VALIDATION_MESSAGES
} from '../interfaces';

const mockPaymentInfo: PaymentInfo = {
    paymentId: '383254',
    paymentNumber: '383254',
    paymentDate: '19/09/2025',
    paymentAmount: 1000.00,
    currency: 'MXN',
    providerNumber: 'SODIMAC, S.A. DE C.V., MX',
    providerName: 'SODIMAC, S.A. DE C.V.',
    invoiceCount: 10,
    creditNoteCount: 4,
    totalDocuments: 14,
    status: 'Pendiente de complemento',
    statusId: 0
};

const mockLastComplement: LastPublishedComplement | null = null; 

export const complementService = {
   
    async getPaymentInfo(paymentId: string): Promise<PaymentInfo> {
        await new Promise((r) => setTimeout(r, 300));
        
        return {
            ...mockPaymentInfo,
            paymentId
        };
    },

    async getLastPublishedComplement(paymentId: string): Promise<LastPublishedComplement | null> {
        await new Promise((r) => setTimeout(r, 300));
        return mockLastComplement;
    },

    async validateXML(xmlFile: File): Promise<{ valid: boolean; message?: string }> {
        await new Promise((r) => setTimeout(r, 500));
        
        const fileName = xmlFile.name.toLowerCase();
        
        if (!fileName.endsWith('.xml')) {
            return {
                valid: false,
                message: 'El archivo debe ser formato XML'
            };
        }

        return {
            valid: true,
            message: 'Complemento de pago válido'
        };
    },

    async validateInvoices(paymentId: string, xmlFile: File): Promise<{ valid: boolean; message?: string }> {
        await new Promise((r) => setTimeout(r, 400));
        
        const hasInvoices = mockPaymentInfo.invoiceCount > 0;
        
        if (!hasInvoices) {
            return {
                valid: false,
                message: VALIDATION_MESSAGES['BUS2006']
            };
        }

        return { valid: true };
    },

    async validateCreditNotes(paymentId: string, xmlFile: File): Promise<{ valid: boolean; message?: string }> {
        await new Promise((r) => setTimeout(r, 400));
        
        return { valid: true };
    },

    async publishComplement(request: ComplementUploadRequest): Promise<ComplementUploadResponse> {
        await new Promise((r) => setTimeout(r, 1000));

        if (!request.xmlFile) {
            return {
                success: false,
                message: VALIDATION_MESSAGES['WRN7004'],
                errors: [{
                    code: 'WRN7004',
                    message: VALIDATION_MESSAGES['WRN7004'],
                    field: 'xml'
                }]
            };
        }

        const xmlValidation = await this.validateXML(request.xmlFile);
        if (!xmlValidation.valid) {
            return {
                success: false,
                message: xmlValidation.message || 'XML inválido',
                errors: [{
                    code: 'INVALID_XML',
                    message: xmlValidation.message || 'XML inválido',
                    field: 'xml'
                }]
            };
        }

        const invoiceValidation = await this.validateInvoices(request.paymentId, request.xmlFile);
        if (!invoiceValidation.valid) {
            return {
                success: false,
                message: invoiceValidation.message || VALIDATION_MESSAGES['BUS2006'],
                errors: [{
                    code: 'BUS2006',
                    message: invoiceValidation.message || VALIDATION_MESSAGES['BUS2006'],
                    field: 'invoices'
                }]
            };
        }

        const creditNoteValidation = await this.validateCreditNotes(request.paymentId, request.xmlFile);
        if (!creditNoteValidation.valid) {
            return {
                success: false,
                message: creditNoteValidation.message || VALIDATION_MESSAGES['BUS2006'],
                errors: [{
                    code: 'BUS2006',
                    message: creditNoteValidation.message || VALIDATION_MESSAGES['BUS2006'],
                    field: 'creditNotes'
                }]
            };
        }

        return {
            success: true,
            message: VALIDATION_MESSAGES['SUCCESS_UPLOAD'],
            complementId: `COMP-${Date.now()}`
        };
    }
};

