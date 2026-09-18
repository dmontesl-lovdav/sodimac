import { createApiClient } from "@/services/ApiClient";
import type { OrdersFilters } from "../interfaces";
import { Order, Reception, ReceptionAxios, ReceptionAxiosSingle } from "../interfaces";
import { buildPurchaseOrdersQuery } from "../purchaseOrderListQuery";

const api = createApiClient({
    baseUrl: process.env.API_BASE_URL ?? "",
});

const DEFAULT_ROUTE = "purchase-orders";

export const OrderClient = {
    async get(criteria: OrdersFilters): Promise<ReceptionAxios> {
        const qs = buildPurchaseOrdersQuery(criteria).toString();

        return api.request<ReceptionAxios>(
            `${DEFAULT_ROUTE}?${qs}`,
            "get"
        );
    },

    async getByUuid(uuid: string): Promise<Order> {
        return api.request<Order>(
            `${DEFAULT_ROUTE}/${uuid}`,
            "get"
        );
    },

    async getReceptionByUuid(uuid: string): Promise<ReceptionAxiosSingle> {
        return api.request<ReceptionAxiosSingle>(
            `${DEFAULT_ROUTE}/reception/${uuid}`,
            "get"
        );
    },

    async updateReceptionManual(payload: {
        supplierNumber: number;
        orderNumber: string;
        receptionNumber: string;
        status: number;
        comments: string;
        uuid?: string;
    }): Promise<Reception> {
        return api.request<Reception>(
            `${DEFAULT_ROUTE}/updateReception`,
            "patch",
            payload
        );
    },

    async updateReceptionStatus(
        uuid: string,
        order: Partial<Reception>
    ): Promise<Reception> {
        return api.request<Reception>(
            `${DEFAULT_ROUTE}/reception/${uuid}`,
            "patch",
            order
        );
    },

    async updateByUuid(uuid: string, order: Order): Promise<Order> {
        return api.request<Order>(
            `${DEFAULT_ROUTE}/${uuid}`,
            "patch",
            order
        );
    },
};
