export const RebateStatusOptions = [
    { value: 1, type: "warning", label: "Pendiente" },
    { value: 2, type: "done", label: "Aprobado" },
    { value: 3, type: "error", label: "Rechazado" }
];

export const EMPTY_REBATE: Rebate = {
    dueDate: "",
    postingDate: "",
    supplierNumber: 0,
    documentNumber: "",
    documentReference: "",
    sapDocument: "",
    periodId: 0,
    stampedRebate: {
        stampedRebateUuid: "",
        documentNumber: "",
       referenceNumber: "",
        status: 0,
        invoiceFiscalUuid: "",
        createdBy: "",
        createdAt: ""
    },
    originId: 0,
    amount: 0,
    status: 1,
    rebateId: "",
    createdBy: "",
    createdAt: ""
}

export interface RebateAxios {
    "message": string,
    "errorCode": number,
    "code": string,
    "httpStatus": number,
    "success": boolean,
    "detailError": string,
    "timeStamp": number,
    "trace_id": string,
    "data": any[]

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

export interface StampedRebate {
      stampedRebateUuid: string,
      documentNumber: string,
      referenceNumber: string,
      status: number,
      invoiceFiscalUuid: string,
      createdBy: string,
      createdAt: string
    }

/** Fila de catálogo BFF (`details` en CatTipoRebate, CatTipoProveedor, etc.). */
export interface CatalogDetailRow {
    key?: string;
    internalStatus?: number;
    externalKey?: string | null;
    value?: string;
    description?: string;
}

export interface Rebate {
    rebateId: string,
    documentNumber: string,
    documentReference: string,
    /** Alias que puede enviar la API en lugar de `documentReference`. */
    referenceNumber?: string,
    sapDocument: string,
    supplierNumber: number,
    /** Alias API (`vendorNumber`). */
    vendorNumber?: number,
    /** Tipo de rebate (`source` en query / catálogo). */
    source?: number,
    /** Si el backend envía nombre de proveedor (recomendado). */
    vendorName?: string,
    supplier?: Supplier,
    amount: number,
    originId: number,
    periodId: number,
    dueDate: string,
    postingDate: string,
    status: number,
    createdBy: string,
    createdAt: string,
    stampedRebate?: StampedRebate | null,
}

export function getRebateReference(r: Rebate): string {
    return (r.referenceNumber ?? r.documentReference ?? "").trim();
}

export function getRebateVendorNumber(r: Rebate): number | undefined {
    const n = r.vendorNumber ?? r.supplierNumber;
    if (n == null || !Number.isFinite(Number(n))) return undefined;
    return Number(n);
}

export function getRebateSourceId(r: Rebate): number | undefined {
    if (r.source != null && Number.isFinite(Number(r.source))) {
        return Number(r.source);
    }
    if (r.originId != null && Number.isFinite(Number(r.originId))) {
        return Number(r.originId);
    }
    return undefined;
}



export interface ProvidersOptions {
    label: string;
    value: string;
}

export interface RebateFilters {
  from: string;
  to: string;
  pageNumber: number;
  pageSize: number;
  supplierNumber?: number;
  status?: number;
  documentNumber?: string;
  sapDocument?: string;
  source?: number;
}
