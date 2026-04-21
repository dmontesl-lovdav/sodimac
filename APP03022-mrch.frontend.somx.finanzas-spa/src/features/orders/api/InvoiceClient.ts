import { createApiClient } from "@/services/ApiClient";
import { Invoice } from "../interfaces";

const fiscalApi = createApiClient({baseUrl: process.env.FISCAL_API_URL || ""});

const api = createApiClient();

export const InvoiceClient = {
    async validateInvoice(invoice: File): Promise<any> {
        const form = new FormData();
        form.append("file", invoice);
        return fiscalApi.request<any>("fiscal/xml/process/file", "post", form);
    },

    async getCreditNotesByUuid(uuid: string): Promise<any> {
        return fiscalApi.request<any>(`fiscal/xml/process/file`, "get");
    },

    async create(invoice: File): Promise<any> {
        const form = new FormData();
        form.append("file", invoice);
        return api.request<any>("invoices/register", "post", form);
    },

    async update(uuid: string, payload: any): Promise<any> {
        return api.request<Invoice>("invoices", "put", payload);
    },

    async getXmlDocument(uuid: string): Promise<Blob> {
        return api.request<Blob>(
            `invoices/${uuid}/xml`,
            "get",
            undefined,
            { responseType: "blob" }
        );
    },
};
