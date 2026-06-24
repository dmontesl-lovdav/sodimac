import { createApiClient } from "@/services/ApiClient";
const api = createApiClient();


export const HelpClientService = {
    async getHelp(helpId: string): Promise<string> {
        return api.request<string>(
            `help/${helpId}`,
            "get",
            undefined,
            { params: { helpId } }
        );
    },
};

