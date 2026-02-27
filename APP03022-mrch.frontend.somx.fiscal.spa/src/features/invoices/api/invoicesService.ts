import {
    InvoiceRecord,
    CreditNoteRecord,
    PagedResult,
    InvoiceSearchParams,
    ExportRequest,
    VALIDATION_MESSAGES
} from '../interfaces';

const mockInvoices: InvoiceRecord[] = [
    {
        id: 1,
        serie: 'A',
        folio: '1001',
        subtotal: 45000.00,
        total: 52200.00,
        purchaseOrder: 'OC-2025-001',
        reception: 'REC-2025-001',
        uuid: 'A1B2C3D4-E5F6-4789-A1B2-C3D4E5F6G7H8',
        creditNotesCount: 1,
        providerId: '393264',
        providerName: 'PLASTITRIM, S.A. DE C.V.',
        issueDate: '15/09/2025',
        receptionDate: '15/09/2025 10:30:00',
        sendDate: '15/09/2025 11:00:00',
        statusId: 1,
        status: 'Activa'
    },
    {
        id: 2,
        serie: 'A',
        folio: '1002',
        subtotal: 30000.00,
        total: 34800.00,
        purchaseOrder: 'OC-2025-002',
        reception: 'REC-2025-002',
        uuid: 'B2C3D4E5-F6G7-4890-B2C3-D4E5F6G7H8I9',
        creditNotesCount: 0,
        providerId: '393264',
        providerName: 'PLASTITRIM, S.A. DE C.V.',
        issueDate: '16/09/2025',
        receptionDate: '16/09/2025 14:15:00',
        sendDate: '16/09/2025 15:30:00',
        statusId: 2,
        status: 'Procesada'
    },
    {
        id: 3,
        serie: 'B',
        folio: '2001',
        subtotal: 15000.00,
        total: 17400.00,
        purchaseOrder: 'OC-2025-003',
        reception: 'REC-2025-003',
        uuid: 'C3D4E5F6-G7H8-4901-C3D4-E5F6G7H8I9J0',
        creditNotesCount: 2,
        providerId: '394201',
        providerName: 'COMERCIALIZADORA ABC S.A. DE C.V.',
        issueDate: '17/09/2025',
        receptionDate: '17/09/2025 09:00:00',
        sendDate: '17/09/2025 09:45:00',
        statusId: 1,
        status: 'Activa'
    }
];

const mockCreditNotes: CreditNoteRecord[] = [
    {
        id: 1,
        serie: 'NC',
        folio: '3001',
        subtotal: 5000.00,
        total: 5800.00,
        reason: 'Devolución de mercancía',
        uuid: 'D4E5F6G7-H8I9-4012-D4E5-F6G7H8I9J0K1',
        issueDate: '18/09/2025',
        receptionDate: '18/09/2025 10:00:00',
        sendDate: '18/09/2025 10:30:00',
        invoiceSerie: 'A',
        invoiceFolio: '1001',
        invoiceUuid: 'A1B2C3D4-E5F6-4789-A1B2-C3D4E5F6G7H8'
    },
    {
        id: 2,
        serie: 'NC',
        folio: '3002',
        subtotal: 2000.00,
        total: 2320.00,
        reason: 'Descuento comercial',
        uuid: 'E5F6G7H8-I9J0-4123-E5F6-G7H8I9J0K1L2',
        issueDate: '19/09/2025',
        receptionDate: '19/09/2025 11:00:00',
        sendDate: '19/09/2025 11:45:00',
        invoiceSerie: 'B',
        invoiceFolio: '2001',
        invoiceUuid: 'C3D4E5F6-G7H8-4901-C3D4-E5F6G7H8I9J0'
    }
];

export const invoicesService = {
    
    async searchInvoices(params: InvoiceSearchParams): Promise<PagedResult<InvoiceRecord>> {
        await new Promise((r) => setTimeout(r, 500));

        let filteredInvoices = [...mockInvoices];

        if (params.serie) {
            filteredInvoices = filteredInvoices.filter(inv =>
                inv.serie.toLowerCase().includes(params.serie!.toLowerCase())
            );
        }

        if (params.folio) {
            filteredInvoices = filteredInvoices.filter(inv =>
                inv.folio.includes(params.folio!)
            );
        }

        if (params.statusId !== undefined) {
            filteredInvoices = filteredInvoices.filter(inv => inv.statusId === params.statusId);
        }

        if (params.providerId) {
            filteredInvoices = filteredInvoices.filter(inv => inv.providerId === params.providerId);
        }

        const page = params.page || 1;
        const size = params.size || 10;
        const startIndex = (page - 1) * size;
        const endIndex = startIndex + size;
        const paginatedItems = filteredInvoices.slice(startIndex, endIndex);

        return {
            items: paginatedItems,
            currentPage: page,
            totalItems: filteredInvoices.length,
            totalPages: Math.ceil(filteredInvoices.length / size)
        };
    },

    async getCreditNotes(invoiceId: string): Promise<CreditNoteRecord[]> {
        await new Promise((r) => setTimeout(r, 300));
        return mockCreditNotes;
    },

    async exportToExcel(params: InvoiceSearchParams, isAdmin: boolean = false): Promise<Blob> {
        const invoicesResult = await this.searchInvoices({ ...params, size: 999999 });
        
        const invoiceHeaders = [
            'Serie',
            'Folio',
            'Subtotal',
            'Total',
            'Orden compra',
            'Recepcion',
            'UUID',
            'Numero NC',
            ...(isAdmin ? ['ID Proveedor', 'Nombre Proveedor'] : []),
            'Fecha emision',
            'Fecha recepcion',
            'Fecha envio'
        ];

        const invoiceRows = invoicesResult.items.map(inv => [
            inv.serie,
            inv.folio,
            inv.subtotal.toFixed(2),
            inv.total.toFixed(2),
            inv.purchaseOrder,
            inv.reception,
            inv.uuid,
            inv.creditNotesCount.toString(),
            ...(isAdmin ? [inv.providerId, inv.providerName] : []),
            inv.issueDate,
            inv.receptionDate,
            inv.sendDate
        ]);

        const creditNoteHeaders = [
            'Serie',
            'Folio',
            'Subtotal',
            'Total',
            'Motivo',
            'UUID',
            'Fecha emision',
            'Fecha recepcion',
            'Fecha envio',
            'Serie Factura',
            'Folio Factura',
            'UUID Factura'
        ];

        const creditNoteRows = mockCreditNotes.map(nc => [
            nc.serie,
            nc.folio,
            nc.subtotal.toFixed(2),
            nc.total.toFixed(2),
            nc.reason,
            nc.uuid,
            nc.issueDate,
            nc.receptionDate,
            nc.sendDate,
            nc.invoiceSerie,
            nc.invoiceFolio,
            nc.invoiceUuid
        ]);

        const invoicesCsv = [
            'HOJA: Facturas',
            invoiceHeaders.join(','),
            ...invoiceRows.map(row => row.map(cell => `"${cell}"`).join(','))
        ].join('\n');

        const creditNotesCsv = [
            '\n\nHOJA: Notas de Credito',
            creditNoteHeaders.join(','),
            ...creditNoteRows.map(row => row.map(cell => `"${cell}"`).join(','))
        ].join('\n');

        const csvContent = invoicesCsv + creditNotesCsv;
        
        const BOM = '\uFEFF';
        return new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
    },

    async downloadXML(invoiceIds: string[]): Promise<Blob> {
        await new Promise((r) => setTimeout(r, 800));
        
        const content = `Simulated ZIP with ${invoiceIds.length} XML files`;
        return new Blob([content], { type: 'application/zip' });
    },

    async downloadPDF(invoiceIds: string[]): Promise<Blob> {
        await new Promise((r) => setTimeout(r, 800));
        
        const content = `Simulated ZIP with ${invoiceIds.length} PDF files`;
        return new Blob([content], { type: 'application/zip' });
    },

    getMessages(): Record<string, string> {
        return VALIDATION_MESSAGES;
    }
};

