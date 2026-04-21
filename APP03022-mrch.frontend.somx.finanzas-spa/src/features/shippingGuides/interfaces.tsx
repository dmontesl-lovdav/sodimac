export const ReceiptTypeOptions = [
    { value: 1, type: "DAD", label: "Despacho a domicilio" },
    { value: 2, type: "ABASTO", label: "Envio a tiendas desde CEDIS" },
    { value: 3, type: "TRANSFERENCIA", label: "Envio entre tiendas y CEDIS" },
    { value: 4, type: "ODBMS", label: "Mercancia" },
    { value: 5, type: "SERVICIOS", label: "Servicios profesionales" },
    { value: 6, type: "INSUMOS", label: "Insumos para tienda u ODA" },
];

export interface ShippingGuideFilter {
    id?: string;
    guideNumber?: string;
    vendorNumber?: string;
    sourceId?: string;
    truckPlate?: string;
    trailerPlate?: string;
    deliveryType?: string;
    from?: string;
    to?: string;
}

export interface ShippingGuideCatalogItem {
    key: string;
    value: string;
    color: string;
    externalKey: string;
    internalStatus: number;
    description: string;
}

export interface ShippingGuideSupplierType {
    id: number;
    code: string;
    description: string;
}

export interface ShippingGuideSupplier {
    supplierNumber: number;
    rfc: string;
    businessName: string;
    supplierType: ShippingGuideSupplierType;
}

export interface ShippingGuide {
    shippingGuideId: string;
    guideNumber: string;

    vendorNumber: number;
    truckPlate: string;
    trailerPlate?: string | null;

    originId: number;
    deliveryType: ShippingGuideCatalogItem;

    status: ShippingGuideCatalogItem;
    comments?: string | null;

    deliveryDate: string;
    shippingDate: string;
    createdBy: string;
    createdAt: string;
    updatedBy?: string | null;
    updatedAt?: string | null;
    isStatusUpdated?: boolean;

    supplier?: ShippingGuideSupplier;
    tipoProveedor?: ShippingGuideSupplierType;
    OrigenCartaPorte?: ShippingGuideCatalogItem;
    orderNumber?: string | null;

    // compatibilidad temporal por si alguna vista vieja aún usa esto
    sourceId?: number;
}

export interface ShippingGuideDetail {
    shippingGuideId: string;
    originType: string;
    status?: number;
    reasonId: number;
    reasonDescription: string;
    relationDate: string;
    comment: string;
    userId: string;
    userName: string;
}

export interface ShippingGuideStatusHistory {
    status: number;
    registeredAt: string;
    userId?: string;
    comment?: string | null;
}