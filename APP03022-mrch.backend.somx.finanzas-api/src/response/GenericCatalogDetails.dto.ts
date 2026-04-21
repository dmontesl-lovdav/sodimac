export interface GenericCatalogDetails {
    key: string,
    value: string;
    color: string;
    externalKey: string;
    internalStatus: number;
    description: string;
};

export interface Supplier {
    supplierNumber: number;
    rfc: string;
    businessName: string;
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