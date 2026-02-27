export const ReceiptTypeOptions = [
    { value: 1, type: "DAD", label: "Despacho a domicilio" },
    { value: 2, type: "ABASTO", label: "Envio a tiendas desde CEDIS" },
    { value: 3, type: "TRANSFERENCIA", label: "Envio entre tiendas y CEDIS" },
    { value: 4, type: "ODBMS", label: "Mercancia" },
    { value: 5, type: "SERVICIOS", label: "Servicios profesionales" },
    { value: 6, type: "INSUMOS", label: "Insumos para tienda u ODA" },
];

export interface ShippingGuideFilter {
    id?: string,
    guideNumber?: string;
    vendorNumber?: string;
    sourceId?: string,
    truckPlate?: string;
    trailerPlate?: string;
    deliveryType?: string,
    from?: string,
    to?: string,
}

export interface ShippingGuide {
    shippingGuideId: string;
    guideNumber: string;

    vendorNumber: number;
    truckPlate: string;
    trailerPlate?: string | null;

    sourceId: number;
    deliveryType: number;

    status: number;
    comments?: string | null;

    deliveryDate: string;
    shippingDate: string;
    createdBy: string;
    createdAt: string;
    updatedBy?: string | null;
    updatedAt?: string | null;
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
