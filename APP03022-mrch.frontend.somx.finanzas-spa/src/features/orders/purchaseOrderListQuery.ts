import type { OrdersFilters } from "./interfaces";
import { normalizeReceptionTypeQueryValue } from "./receptionTypeQuery";

export function buildPurchaseOrdersQuery(criteria: OrdersFilters): URLSearchParams {
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
    const receptionTypeId = normalizeReceptionTypeQueryValue(criteria.receptionTypeId);
    if (receptionTypeId) {
        params.set("receptionTypeId", receptionTypeId);
    }
    if (criteria.receptionNumber != null && String(criteria.receptionNumber).trim() !== "") {
        params.set("receptionNumber", String(criteria.receptionNumber).trim());
    }
    return params;
}
