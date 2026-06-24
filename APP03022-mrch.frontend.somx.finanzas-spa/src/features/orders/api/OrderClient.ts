import { createApiClient } from "@/services/ApiClient";
import type { OrdersFilters } from "../interfaces";
import { Order, Reception, ReceptionAxios, ReceptionAxiosSingle } from "../interfaces";

const api = createApiClient({
    baseUrl: process.env.API_BASE_URL || "",
});

const DEFAULT_ROUTE = "purchase-orders";

export const OrderClient = {
    async get(criteria: OrdersFilters): Promise<ReceptionAxios> {
        const params = new URLSearchParams();
        params.set("purchaseOrderDateAtInitial", criteria.purchaseOrderDateAtInitial);
        params.set("purchaseOrderDateAtEnd", criteria.purchaseOrderDateAtEnd);
        params.set("pageNumber", String(criteria.pageNumber));
        params.set("pageSize", String(criteria.pageSize));

        if (criteria.supplierNumber != null && !Number.isNaN(Number(criteria.supplierNumber))) {
            params.set("supplierNumber", String(criteria.supplierNumber));
        }
        if (criteria.orderNumber != null && String(criteria.orderNumber).trim() !== "") {
            params.set("orderNumber", String(criteria.orderNumber).trim());
        }
        if (criteria.status != null && !Number.isNaN(Number(criteria.status))) {
            params.set("status", String(criteria.status));
        }
        if (criteria.originId != null && String(criteria.originId).trim() !== "") {
            params.set("originId", String(criteria.originId).trim());
        }

        const qs = params.toString();

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
