import { createApiClient } from "@/services/ApiClient";
import { Rebate } from "../interfaces";

const api = createApiClient();

const DEFAULT_ROUTE = "rebates";

export const DiscountsClient = {

    async get(criteria: any = {}): Promise<Rebate[]> {

        const params = new URLSearchParams();

        if (criteria.purchaseOrderDateAtInitial)
            params.set(
                "purchaseOrderDateAtInitial",
                new Date(criteria.purchaseOrderDateAtInitial).toISOString()
            );

        if (criteria.purchaseOrderDateAtEnd)
            params.set(
                "purchaseOrderDateAtEnd",
                new Date(criteria.purchaseOrderDateAtEnd).toISOString()
            );

        if (criteria.pageNumber !== undefined && criteria.pageNumber !== null)
            params.set("pageNumber", String(criteria.pageNumber));

        if (criteria.pageSize !== undefined && criteria.pageSize !== null)
            params.set("pageSize", String(criteria.pageSize));

        if (criteria.supplierNumber !== undefined && criteria.supplierNumber !== null && criteria.supplierNumber !== "")
            params.set("supplierNumber", String(criteria.supplierNumber));

        if (criteria.status !== undefined && criteria.status !== null && criteria.status !== "")
            params.set("status", String(criteria.status));

        const query = params.toString();

        return api.request<Rebate[]>(
            query ? `${DEFAULT_ROUTE}?${query}` : DEFAULT_ROUTE,
            "get"
        );
    }

};
