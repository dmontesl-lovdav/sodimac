import { ApiClient } from "@/services/ApiClient";
export class CreditsClient extends ApiClient {

    public constructor() {
        super();
    }

    public async getCreditNotes(filters: any): Promise<any> {
        return this.execute(`invoices/search`, 'post', filters);
    };

    public async getXmlDocument(uuid: string): Promise<any> {
        return this.fetchDocument(`invoices/${uuid}/xml`);
    };

    public async getPdfDocument(uuid: string): Promise<any> {
        return this.fetchDocument(`pdf/from-uuid/${uuid}?inline=true`);
    };
}