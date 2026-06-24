import { ShippingGuide } from "../shippingGuides/interfaces";
import { RECEPTION_STATUS_DICTIONARY, receptionStatusDefinedIds } from "./receptionStatusDictionary";

export type ReceptionStatus = number;

export type ReceptionStatusOption = {
    value: number;
    type: string;
    label: string;
    description: string;
};

/** Opciones de filtro / formulario derivadas del diccionario estático de estatus. */
export const ReceptionStatusOptions: ReceptionStatusOption[] =
    receptionStatusDefinedIds().map((value) => {
        const e = RECEPTION_STATUS_DICTIONARY[value];
        return {
            value,
            type: e.pillType,
            label: e.shortLabel,
            description: e.description,
        };
    });

/** Estatus visibles en filtros (sin borrado lógico). */
export const ReceptionStatusFilterOptions = receptionStatusDefinedIds()
    .filter((value) => value !== 8)
    .map((value) => {
        const e = RECEPTION_STATUS_DICTIONARY[value];
        return {
            value: String(value),
            label: e.description,
            type: e.pillType,
            description: e.description,
        };
    });

/** Estatus permitidos al editar recepción manualmente. */
export const RECEPTION_STATUS_EDIT_IDS = [2, 7, 8] as const;

export const ReceptionStatusEditOptions: ReceptionStatusOption[] =
    RECEPTION_STATUS_EDIT_IDS.map((value) => {
        const e = RECEPTION_STATUS_DICTIONARY[value];
        return {
            value,
            type: e.pillType,
            label: e.shortLabel,
            description: e.description,
        };
    });

//TODO: catálogo de proveedores dinámicos
export const ProviderOptions = [
    { value: 0, label: "Proveedor 1" },
    { value: 1, label: "Proveedor 2" },
];

export type TableItem = Item & { id: string | number };

export const EMPTY_ORDER: Order = {
    shippingGuideNumber: "",
    purchaseOrderId: "",
    orderNumber: "",
    supplierNumber: "",
    vendorName: "",
    receptions: [],
    originId: 0,
    amount: 0,
    purchaseOrderDate: "",
    status: 1,
}

export const EMPTY_SHIPPING: ShippingGuide = {
    shippingGuideId: "",
    guideNumber: "",
    shippingDate: "",
    comments: "",
    sourceId: 0,
    status: 1,
    truckPlate: "",
    trailerPlate: "",
    deliveryDate: "",
    deliveryType: 0,
    createdBy: "",
    createdAt: "",
    updatedBy: "",
    updatedAt: "",
    vendorNumber: 0
}

export const EMPTY_RECEPTION: Reception = {
    receptionId: "0",
    purchaseOrderId: "",
    orderNumber: "",
    supplierNumber: "",
    vendorName: "",
    originId: "0",
    amount: 0,
    purchaseOrderDate: "",
    status: 1,
    receptionDate:"",
    receptionSkus: [],
    order: EMPTY_ORDER,
    shippingGuidePurchaseOrders: [],
    receptionNumber: "",
    comment: ""
}

export const EMPTY_INVOICE: Invoice = {
    invoice_uuid: "",
    document_type: "",
    total: 0,
    subtotal: 0,
    issue_date: "",
    version: 0,
    issuer_uuid: "",
    receiver_uuid: "",
    xml_content: ""
}

export interface ReceptionAxios {
    "message": string,
    "errorCode": number,
    "code": string,
    "httpStatus": number,
    "success": boolean,
    "detailError": string,
    "timeStamp": number,
    "trace_id": string,
    "data": {
        "content": any[]
    }

}

export interface ReceptionAxiosSingle {
    "message": string,
    "errorCode": number,
    "code": string,
    "httpStatus": number,
    "success": boolean,
    "detailError": string,
    "timeStamp": number,
    "trace_id": string,
    "data": any
}


interface SupplierType {
    id: number;
    code: string;
    description: string;
}

interface Supplier {
    supplierNumber: number;
    rfc: string;
    businessName: string;
    supplierType: SupplierType;
    emailFinancial?: string;
}


export interface Reception {
    receptionDate: string;
    orderNumber: string;
    receptionSkus: ReceptionSKU[],
    listAddendum?: any[],
    supplier?: Supplier,
    order: Order,
    shippingGuidePurchaseOrders: ShippingGuide[],
    originId: string;
    /** Nombre amigable (catálogo BFF CatTipoOrigenRecepcionSodimac), junto con originId. */
    originName?: string;
    purchaseOrderDate: string;
    supplierNumber: string;
    vendorName: string;
    receptionId: string,
    receptionNumber?: string,
    destinationId?: number,
    purchaseOrderId?: string,
    amount: number,
    status: number,
    comment: string,
    receivedAt?: string,
    createdBy?: string,
    createdAt?: string,
    updatedBy?: string,
    updatedAt?: string,
}

export interface Order {
    shippingGuideNumber: string,
    supplier?: Supplier,
    purchaseOrderId: string,
    orderNumber: string,
    supplierNumber: string,
    vendorName: string,
    originId: number,
    amount: number,
    purchaseOrderDate: string,
    status: ReceptionStatus,
    receptions?: Reception[]
}

export interface ProvidersOptions {
    label: string;
    value: string;
}

export interface OrdersFilters {
  purchaseOrderDateAtInitial: string;
  purchaseOrderDateAtEnd: string;
  pageNumber: number,
  pageSize: number,
  providerType?: string | number | undefined;
  purchaseOrderId?: string | undefined;
  orderNumber?: string | undefined;
  /** Filtro presentacional: recorte en cliente tras aplanar recepciones (si el API no filtra por recepción). */
  receptionNumber?: string | undefined;
  originId?: string | undefined;
  supplierNumber?: number | undefined;
  status?: number | undefined;
  isStatusUpdated?: boolean | undefined;
}

export interface Addendum {
    addendumUuid: string,
    invoiceUuid: string,
    supplierNumber: string,
    receptionNumber: string,
    createdBy: string,
    createdAt: string,
    updatedBy: string ,
    updatedAt: string,
    invoice: Invoice
}

export interface Invoice {
  invoice_uuid?: string;
  /** Alias camelCase usado en algunas respuestas/adendas. */
  invoiceUuid?: string;
  fiscal_uuid?: string;
  place_of_issue?: string;
  payment_method?: string;
  document_type?: string;
  documentType?: string;
  total: number;
  exchange_rate?: number;
  currency?: string;
  discount?: number;
  subtotal?: number;
  payment_conditions?: string;
  payment_form?: string;
  issue_date?: string;
  certification_date?: string;
  folio?: string;
  series?: string;
  version?: number;
  xml_content: string;
  status?: number;
  issuer_uuid?: string;
  receiver_uuid?: string;
  created_by?: number;
  created_at?: string;
  updated_by?: number;
  certificationDate?: string;
  updated_at?: string;
}


export interface Item {
    sku: string,
    description: string,
    quantity: number,
    price: number,
    amount: number,
}

export interface ItemSummary {
    requestedQuantity: number,
    receivedQuantity: number,
    requestedSkus: number,
    receivedSkus: number,
}

export interface ReceptionSKU {
    createdAt: string;
    createdBy: string | null;
    description: string
    quantity: string;
    receptionId: string;
    receptionSkuId: string;
    sku: string;
    status: string;
    totalCost: string;
    unitCost: string;
    updatedAt: string | null;
    updatedBy: string | null;
    id: string;

}