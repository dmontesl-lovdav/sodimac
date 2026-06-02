export interface GenericCatalogDetails {
    key: string,
    value: string;
    color: string;
    externalKey: string;
    internalStatus: number;
    description: string;
    success: boolean;
};

export interface Supplier {
    supplierNumber: number;
    rfc: string;
    businessName: string;
    emailFinancial: string;
    emailPrincipal?: string;
    emailCommercial?: string;
    supplierType: {
                    id: number;
                    code: string;
                    description: string;
                    };
};

export interface ValidStatus {
    success: boolean;
    valid: boolean;
};