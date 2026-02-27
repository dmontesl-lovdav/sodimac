import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { 
    PaymentRecord, 
    PagedResult, 
    PaymentSearchParams,
    PaymentDetail,
    PaymentDocument
} from '../interfaces';

const client = ConfigurationBuilder.client;

// Mock data 
const mockPayments: PaymentRecord[] = [
    {
        id: 1,
        providerNumber: '393264',
        providerName: 'PLASTITRIM, S.A. DE C.V.',
        paymentNumber: '242195', 
        receptionNumber: '242195',
        guideNumber: '9',
        invoiceNumber: '',  
        currency: 'MXN',
        amount: 152000.00,
        bulkQty: 152,
        palletQty: 0,
        totalQty: 0,
        paymentDate: '19-09-2025',
        issueDate: '19-09-2025',
        status: 'Pendiente de complemento',
        statusId: 0,
        serie: '',
        folio: '',
        uuid: '',
        reportIds: [1286]
    },
    {
        id: 2,
        providerNumber: '394201',
        providerName: 'COMERCIALIZADORA ABC S.A. DE C.V.',
        paymentNumber: '242196',  
        receptionNumber: '242196',
        guideNumber: '9',
        invoiceNumber: '', 
        currency: 'MXN',
        amount: 3000.00,
        bulkQty: 3,
        palletQty: 0,
        totalQty: 0,
        paymentDate: '19-09-2025',
        issueDate: '19-09-2025',
        status: 'Pendiente de complemento',
        statusId: 0,
        serie: '',
        folio: '',
        uuid: '',
        reportIds: [40]
    }
];

const mockDocuments: PaymentDocument[] = [
    {
        id: '1',
        documentNumber: 'FAC-2025-001',
        documentType: 'Factura',
        reference: 'REF-001',
        documentDate: '15/09/2025',
        accountingDate: '15/09/2025',
        dueDate: '15/10/2025',
        currency: 'MXN',
        amount: 50000.00,
        serie: 'A',
        folio: '1001',
        uuid: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
        status: 'Activo'
    },
    {
        id: '2',
        documentNumber: 'NC-2025-001',
        documentType: 'Nota de Crédito',
        reference: 'REF-002',
        documentDate: '16/09/2025',
        accountingDate: '16/09/2025',
        dueDate: '16/10/2025',
        currency: 'MXN',
        amount: -5000.00,
        serie: 'NC',
        folio: '2001',
        uuid: 'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
        status: 'Activo'
    },
    {
        id: '3',
        documentNumber: 'FAC-2025-002',
        documentType: 'Factura',
        reference: 'REF-003',
        documentDate: '17/09/2025',
        accountingDate: '17/09/2025',
        dueDate: '17/10/2025',
        currency: 'MXN',
        amount: 107000.00,
        serie: 'A',
        folio: '1002',
        uuid: 'C3D4E5F6-G7H8-I9J0-K1L2-M3N4O5P6Q7R8',
        status: 'Activo'
    }
];

export const paymentsService = {
    async searchPayments(params: PaymentSearchParams): Promise<PagedResult<PaymentRecord>> {
       
        await new Promise((r) => setTimeout(r, 500));

        let filteredPayments = [...mockPayments];
        
        if (params.statusId !== undefined) {
            filteredPayments = filteredPayments.filter(p => p.statusId === params.statusId);
        }

        if (params.paymentNumber) {
            filteredPayments = filteredPayments.filter(p => 
                p.paymentNumber.includes(params.paymentNumber!)
            );
        }

        if (params.providerId) {
            filteredPayments = filteredPayments.filter(p => 
                p.providerNumber === params.providerId
            );
        }

        const page = params.page || 1;
        const size = params.size || 10;
        const startIndex = (page - 1) * size;
        const endIndex = startIndex + size;
        const paginatedItems = filteredPayments.slice(startIndex, endIndex);

        return {
            items: paginatedItems,
            currentPage: page,
            totalItems: filteredPayments.length,
            totalPages: Math.ceil(filteredPayments.length / size)
        };
    },

    async getPaymentDetail(paymentNumber: string): Promise<PaymentDetail> {
        await new Promise((r) => setTimeout(r, 300));
        
        const payment = mockPayments.find(p => p.paymentNumber === paymentNumber);
        if (!payment) {
            throw new Error('Pago no encontrado');
        }

        return {
            ...payment,
            documents: mockDocuments,
            paymentYear: '2025',
            complementInfo: payment.statusId === 1 ? {
                serie: payment.serie || '',
                folio: payment.folio || '',
                uuid: payment.uuid || '',
                uploadDate: '20-09-2025',
                status: 'Activo'
            } : undefined
        };
    },

    async exportPayments(params: PaymentSearchParams, format: 'csv' | 'xlsx' = 'csv', isAdmin: boolean = false): Promise<Blob> {
        const result = await this.searchPayments({ ...params, size: 999999 });
        
        const headers = [
            ...(isAdmin ? ['Número Proveedor', 'Nombre Proveedor'] : []),
            'Número Pago',
            'Moneda',
            'Monto',
            'Fecha Pago',
            'Estatus',
            'Serie',
            'Folio',
            'UUID'
        ];

        const rows = result.items.map(item => [
            ...(isAdmin ? [item.providerNumber, item.providerName] : []),
            item.paymentNumber,
            item.currency,
            item.amount.toString(),
            item.paymentDate,
            item.status,
            item.serie || '',
            item.folio || '',
            item.uuid || ''
        ]);

        const csvContent = [
            headers.join(','),
            ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
        ].join('\n');

        return new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    },

    async uploadPaymentComplement(paymentNumber: string, file: File): Promise<void> {
        await new Promise((r) => setTimeout(r, 1000));
        console.log('Complement uploaded for payment:', paymentNumber, file.name);
    },

    async exportPaymentDetail(paymentNumber: string, isAdmin: boolean = false): Promise<Blob> {
        const detail = await this.getPaymentDetail(paymentNumber);
        
        const headers = [
            ...(isAdmin ? ['Número Proveedor', 'Nombre Proveedor'] : []),
            'Número Pago',
            'Año Pago',
            'Fecha Pago',
            'Monto Pago',
            'Moneda',
            'Número Documento',
            'Referencia',
            'Fecha Documento',
            ...(isAdmin ? ['Fecha Contable'] : []),
            'Fecha Vencimiento',
            'Monto Documento',
            'Serie',
            'Folio',
            'UUID',
            'Estatus'
        ];

        const rows = detail.documents.map(doc => [
            ...(isAdmin ? [detail.providerNumber, detail.providerName] : []),
            detail.paymentNumber,
            detail.paymentYear || '2025',
            detail.paymentDate,
            detail.amount.toString(),
            detail.currency,
            doc.documentNumber,
            doc.reference || '',
            doc.documentDate,
            ...(isAdmin ? [doc.accountingDate || ''] : []),
            doc.dueDate,
            doc.amount.toString(),
            doc.serie || '',
            doc.folio || '',
            doc.uuid || '',
            doc.status
        ]);

        const csvContent = [
            headers.join(','),
            ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
        ].join('\n');

        return new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    },

    async getMessages(): Promise<Record<string, string>> {
        return {
            'INF6000': 'No existe información con los criterios establecidos.',
            'ERR001': 'La fecha final no puede exceder un mes desde la fecha actual.',
            'ERR002': 'El periodo máximo de consulta es de 6 meses.',
            'ERR003': 'Fecha inicio es obligatoria.',
            'ERR004': 'Fecha fin es obligatoria.',
            'WRN7003': 'No es posible publicar el complemento de pago, faltan documentos fiscales por publicar',
            'SUCCESS001': 'Búsqueda realizada exitosamente.',
            'SUCCESS002': 'Exportación completada.',
            'SUCCESS003': 'Complemento cargado exitosamente.'
        };
    }
};
