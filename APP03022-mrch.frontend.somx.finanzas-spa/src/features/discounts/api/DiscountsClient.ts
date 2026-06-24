import { createApiClient } from "@/services/ApiClient";
import { Rebate } from "../interfaces";
import type { RebateFilters } from "../interfaces";
import { normalizeRebateRow } from "./normalizeRebate";

const api = createApiClient();

const DEFAULT_ROUTE = "rebates";

function isNotFound(err: unknown): boolean {
  const e = err as { response?: { status?: number } };
  return e?.response?.status === 404;
}

export const DiscountsClient = {
  async get(criteria: RebateFilters): Promise<Rebate[]> {
    const params = new URLSearchParams();

    if (criteria.status != null && criteria.status !== ("" as unknown as number)) {
      params.set("status", String(criteria.status));
    }
    if (criteria.supplierNumber != null && !Number.isNaN(Number(criteria.supplierNumber))) {
      params.set("vendorNumber", String(criteria.supplierNumber));
    }
    if (criteria.documentNumber?.trim()) {
      params.set("documentNumber", criteria.documentNumber.trim());
    }
    if (criteria.source != null && !Number.isNaN(Number(criteria.source))) {
      params.set("source", String(criteria.source));
    }

    if (criteria.from) {
      params.set("from", new Date(criteria.from).toISOString());
    }
    if (criteria.to) {
      params.set("to", new Date(criteria.to).toISOString());
    }

    const limit = criteria.pageSize ?? 100;
    const pageIdx = Math.max(0, (criteria.pageNumber ?? 1) - 1);
    params.set("limit", String(limit));
    params.set("page", String(pageIdx));

    const query = params.toString();

    try {
      const rows = await api.request<Rebate[]>(
        query ? `${DEFAULT_ROUTE}?${query}` : DEFAULT_ROUTE,
        "get"
      );
      return Array.isArray(rows)
        ? rows.map((row) =>
            normalizeRebateRow(row as Record<string, unknown>)
          )
        : [];
    } catch (err) {
      if (isNotFound(err)) return [];
      throw err;
    }
  },
};
