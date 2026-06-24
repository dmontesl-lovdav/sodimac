import { createApiClient } from "@/services/ApiClient";
import { Invoice } from "../interfaces";

const fiscalApi = createApiClient({baseUrl: process.env.FISCAL_API_URL || ""});

export type RegisterInvoiceOptions = {
    idTransaccion?: string;
    receptionId?: string;
    supplierNumber?: string | number;
    purchaseOrderNumber?: string;
};

function appendFormField(form: FormData, key: string, value: string | number | undefined | null): void {
    if (value == null) return;
    const normalized = String(value).trim();
    if (!normalized) return;
    form.append(key, normalized);
}

export const InvoiceClient = {
    async validateInvoice(invoice: File): Promise<any> {
        const form = new FormData();
        form.append("file", invoice);
        return fiscalApi.request<any>("fiscal/xml/process/file", "post", form);
    },

    async getCreditNotesByUuid(uuid: string): Promise<any> {
        return fiscalApi.request<any>(`fiscal/xml/process/file`, "get");
    },

    async getInvoicesByUuid(uuid: string): Promise<any> {
        return fiscalApi.request<any>(`fiscal/search`, "post", {
            invoiceUuid: uuid,
        });
    },

    async create(invoice: File, pdf: File | null, options?: RegisterInvoiceOptions): Promise<any> {
        const form = new FormData();
        form.append("file", invoice);
        if(pdf) {
            form.append("pdfFile", pdf);
        }
        appendFormField(form, "idTransaccion", options?.idTransaccion);
        appendFormField(form, "receptionId", options?.receptionId);
        appendFormField(form, "supplierNumber", options?.supplierNumber);
        appendFormField(form, "purchaseOrderNumber", options?.purchaseOrderNumber);

        return fiscalApi.request<any>("invoices/register", "post", form);
    },

    async update(uuid: string, payload: any): Promise<any> {
        return fiscalApi.request<Invoice>("invoices", "put", payload);
    },

    async getXmlDocument(uuid: string): Promise<Blob> {
        return fiscalApi.request<Blob>(
            `invoices/${uuid}/xml`,
            "get",
            undefined,
            { responseType: "blob" }
        );
    },
};
