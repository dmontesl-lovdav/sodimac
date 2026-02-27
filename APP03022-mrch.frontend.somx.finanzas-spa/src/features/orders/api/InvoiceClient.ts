import { ApiClient } from "@/services/ApiClient";
import { Invoice } from "../interfaces";

export class InvoiceClient extends ApiClient {

    private DEFAULT_ROUTE = "/fiscal";

    public constructor() {
        super(process.env.API_FISCAL_URL || "");
    }

    public async validateInvoice(invoice: File): Promise<any> {
        return this.executeRequestBinary(invoice, `${this.DEFAULT_ROUTE}/xml/process/file`);
    };

    public async getCreditNotesByUuid(uuid: string): Promise<any> {
        return this.execute(`${this.DEFAULT_ROUTE}/xml/process/file`, 'get');
    };

    public async create(invoice: File): Promise<any> {
        return this.executeRequestBinary(invoice, `/invoices/register`);
    };

    public async update(uuid: string, payload: any): Promise<any> {
       return this.execute<Invoice>(`/invoices`, "put", payload);
    };

    public async getXmlDocument(uuid: string): Promise<any> {
        return this.fetchDocument(`invoices/${uuid}/xml`);
    };

}