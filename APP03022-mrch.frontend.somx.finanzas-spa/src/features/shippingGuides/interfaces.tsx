export const ReceiptTypeOptions = [
    { value: 1, type: "DAD", label: "Despacho a domicilio" },
    { value: 2, type: "ABASTO", label: "Envio a tiendas desde CEDIS" },
    { value: 3, type: "TRANSFERENCIA", label: "Envio entre tiendas y CEDIS" },
    { value: 4, type: "ODBMS", label: "Mercancia" },
    { value: 5, type: "SERVICIOS", label: "Servicios profesionales" },
    { value: 6, type: "INSUMOS", label: "Insumos para tienda u ODA" },
];

/** POST `shipping-guide/cancel` */
export interface CancelShippingGuidesPayload {
    shippingGuideIds: string[];
    reasonId: number;
    comment: string;
}

/** POST `shipping-guide/status` */
export interface UpdateShippingGuideStatusPayload {
    shippingGuideId: string;
    targetStatus: number;
    reasonId: number;
    series?: string;
    folio?: string;
    uuid?: string;
    comment: string;
}

export interface ShippingGuideFilter {
    id?: string;
    guideNumber?: string;
    vendorNumber?: string;
    status?: number;
    orderNumber?: string;
    sourceId?: string;
    truckPlate?: string;
    trailerPlate?: string;
    deliveryType?: string;
    from?: string;
    to?: string;
}

export interface ShippingGuideCatalogItem {
    key?: string;
    value?: string;
    color?: string;
    externalKey?: string;
    internalStatus?: number;
    description?: string;
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

export interface ShippingGuideDocument {
    shippingGuideDocumentId: string;
    shippingGuideId: string;
    fileName: string;
    fileType: number;
    status: string;
    createdBy: string | null;
    createdAt: string;
    updatedBy: string | null;
    updatedAt: string | null;
}

export interface ShippingGuide {
    shippingGuideDocuments: ShippingGuideDocument[];
    shippingGuideId: string;
    guideNumber: string;

    vendorNumber: number;
    truckPlate: string;
    trailerPlate?: string | null;

    originId: number;
    deliveryType: ShippingGuideCatalogItem;

  /** El BFF puede devolver el catálogo completo o solo el código numérico. */
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
    /** Estatus numérico de la OC vinculada (para mostrar descripción en grid). */
    purchaseOrderStatus?: number | null;

    // compatibilidad temporal por si alguna vista vieja aún usa esto
    sourceId?: number;
}

export interface ShippingGuideDetailPurchaseOrder {
    purchaseOrderId: string;
    orderNumber: string;
    supplierNumber: number;
    /** Nombre comercial del proveedor (detalle API). */
    supplierBusinessName?: string | null;
    originId: number;
    amount: string;
    status: number;
    purchaseOrderDate: string;
    /** Fecha de la recepción asociada a la OC, si existe. */
    receptionDate?: string | null;
    createdBy?: string;
    createdAt?: string;
    updatedBy?: string | null;
    updatedAt?: string | null;
}

export interface ShippingGuidePurchaseOrderLink {
    shippingGuidePurchaseOrderId: string;
    shippingGuideId: string;
    purchaseOrderId: string;
    createdBy?: string;
    createdAt?: string;
    updatedBy?: string | null;
    updatedAt?: string | null;
    purchaseOrder?: ShippingGuideDetailPurchaseOrder | null;
}

/**
 * Detalle GET `shipping-guide/:id`.
 * El backend puede envolver el payload en `{ data: {...}, success }`.
 */
export interface ShippingGuideDetail {
    shippingGuideId: string;
    guideNumber: string;
    vendorNumber: number;
    truckPlate: string;
    trailerPlate?: string | null;
    originId: number;
    deliveryType: number | ShippingGuideCatalogItem;
    status: ShippingGuideCatalogItem;
    comments?: string | null;
    deliveryDate?: string | null;
    shippingDate?: string | null;
    createdBy?: string | null;
    createdAt?: string | null;
    updatedBy?: string | null;
    updatedAt?: string | null;
    isStatusUpdated?: boolean;
    supplier?: ShippingGuideSupplier;
    shippingGuidePurchaseOrders?: ShippingGuidePurchaseOrderLink[];
}

/** Estatus numérico cuando la API devuelve número u objeto catálogo. */
export function getNumericGuideStatus(
    status: ShippingGuideDetail["status"] | ShippingGuide["status"] | undefined
): number | undefined {
    if (status === null || status === undefined) return undefined;
    const s = status as unknown;
    if (typeof s === "number" && Number.isFinite(s)) return s;
    if (typeof s === "object" && s !== null && "internalStatus" in s) {
        const n = Number((s as ShippingGuideCatalogItem).internalStatus);
        return Number.isFinite(n) ? n : undefined;
    }
    return undefined;
}

export function getDeliveryTypeLabel(
    deliveryType: ShippingGuideDetail["deliveryType"]
): string {
    const d = deliveryType as unknown;
    if (typeof d === "number") {
        const opt = ReceiptTypeOptions.find((o) => o.value === d);
        return opt?.label ?? String(d);
    }
    if (d && typeof d === "object" && "description" in d) {
        return (
            (d as ShippingGuideCatalogItem).description ||
            ((d as ShippingGuideCatalogItem).value ?? "N/D")
        );
    }
    return "N/D";
}

export interface ShippingGuideStatusHistory {
    status: number;
    registeredAt: string;
    userId?: string;
    comment?: string | null;
}