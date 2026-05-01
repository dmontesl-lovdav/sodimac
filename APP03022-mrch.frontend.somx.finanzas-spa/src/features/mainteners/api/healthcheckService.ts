import type { ApiClient } from "@/services/ApiClient";

export interface HealthcheckItem {
    healthcheckUuid: string;
    serviceName: string;
    status: string;
    message?: string | null;
    createdAt: string;
    updatedAt?: string | null;
}

export interface HealthcheckResponse {
    alive: boolean;
    count: number;
    data: HealthcheckItem[];
}

export function createHealthcheckService(api: ApiClient) {
    async function getHealthcheck(): Promise<HealthcheckResponse> {
        return api.request<HealthcheckResponse>("healthcheck", "get");
    }

    return {
        getHealthcheck,
    };
}