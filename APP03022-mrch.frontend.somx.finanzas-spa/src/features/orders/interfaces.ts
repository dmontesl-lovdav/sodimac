import { ShippingGuide } from "../shippingGuides/interfaces";

export type ReceptionStatus = 0 | 1 | 2 | 3 | 4;

export const ReceptionStatusOptions = [
    { value: 0, type: "done", label: "Disponible" },
    { value: 1, type: "done", label: "Consumida" },
    { value: 2, type: "done", label: "Manual" },
    { value: 3, type: "error", label: "Cancelada" },
    { value: 4, type: "error", label: "Borrado" },
];

//TODO: catálogo de proveedores dinámicos
export const ProviderOptions = [
    { value: 0, label: "Proveedor 1" },
    { value: 1, label: "Proveedor 2" },
];

export type TableItem = Item & { id: string | number };

export const EMPTY_ORDER: Order = {
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
  purchaseOrderId?: string | undefined;
  orderNumber?: string | undefined;
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