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

export interface Rebate {
    rebateId: string,
    documentNumber: string,
    documentReference: string,
    sapDocument: string,
    supplierNumber: number,
    amount: number,
    originId: number,
    periodId: number,
    dueDate: string,
    postingDate: string,
    status: number,
    createdBy: string,
    createdAt: string,
    stampedRebate: StampedRebate
}



export interface ProvidersOptions {
    label: string;
    value: string;
}

export interface RebateFilters {
  postingDate: string;
  dueDate: string;
  pageNumber: number,
  pageSize: number,
  supplierNumber?: number | undefined;
  status?: number | undefined;
}
