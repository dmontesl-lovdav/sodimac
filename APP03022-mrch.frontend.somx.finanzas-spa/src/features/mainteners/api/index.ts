import { createApiClient } from "@/services/ApiClient";
import { createHealthcheckService } from "./healthcheckService";

const api = createApiClient();
const client = createHealthcheckService(api);

export const getHealthcheck = () => {
    return client.getHealthcheck();
};