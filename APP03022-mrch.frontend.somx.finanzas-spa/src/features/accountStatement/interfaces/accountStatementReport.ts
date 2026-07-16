export type CatalogStatusItem = {
    key: string;
    description: string;
    color: string | null;
    sortOrder?: number;
};

/** Catálogo de estatus de OC/recepción (`tenant_finance.status_catalog`). */
export type PurchaseOrderStatusItem = {
    status: number;
    name: string;
    description: string | null;
};

export type AccountStatementReportPayload = {
    meta: {
        accountStatementUuid: string;
        vendorNumber: number;
        year: number;
        month: number;
        version: number;
        initialBalance: number | null;
        finalBalance: number | null;
    };
    issuer: {
        name: string;
        address: string;
        rfc: string;
        phone: string;
        email: string;
    };
    vendor: {
        vendorNumber: number;
        vendorName: string;
        providerRfc: string;
        providerCp: string;
        providerAddress: string;
        providerContact: string;
        providerEmail: string;
    };
    dates: {
        issueDate: string | null;
        periodStart: string | null;
        periodEnd: string | null;
        generatedAt: string;
    };
    totals: {
        totalOC: number;
        totalFacturasPendientes: number;
        totalFacturasPagadas: number;
        totalDescuentos: number;
        totalNotasCredito: number;
        saldoPendiente: number;
        counts: {
            purchaseOrders: number;
            facturas: number;
            payments: number;
            rebates: number;
            notasCredito: number;
        };
    };
    purchaseOrders: Record<string, unknown>[];
    receptions: Record<string, unknown>[];
    payments: Record<string, unknown>[];
    rebates: Record<string, unknown>[];
    facturas: Record<string, unknown>[];
    notasCredito: Record<string, unknown>[];
    catalogs: {
        purchaseOrderStatus?: PurchaseOrderStatusItem[];
        invoiceStatus: CatalogStatusItem[];
        paymentStatus: CatalogStatusItem[];
        creditNoteStatus: CatalogStatusItem[];
    };
};
