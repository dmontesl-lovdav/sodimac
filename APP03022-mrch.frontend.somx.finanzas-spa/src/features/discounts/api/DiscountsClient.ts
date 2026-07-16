import {
  createApiClient,
} from "@/services/ApiClient";

import type {
  Rebate,
  RebateFilters,
} from "../interfaces";

import {
  normalizeRebateRow,
} from "./normalizeRebate";

const api = createApiClient();

const DEFAULT_ROUTE = "rebates";

function isNotFound(
  error: unknown
): boolean {
  const normalizedError =
    error as {
      response?: {
        status?: number;
      };
    };

  return (
    normalizedError
      ?.response
      ?.status === 404
  );
}

function setIfFinite(
  params: URLSearchParams,
  key: string,
  value: unknown
): void {
  if (value == null) return;
  if (!Number.isFinite(Number(value))) return;
  params.set(key, String(value));
}

function setIfTrimmed(
  params: URLSearchParams,
  key: string,
  value?: string
): void {
  const trimmed = value?.trim();
  if (!trimmed) return;
  params.set(key, trimmed);
}

function buildRebateQuery(
  criteria: RebateFilters
): string {
  const params = new URLSearchParams();

  setIfFinite(params, "status", criteria.status);
  setIfFinite(params, "vendorNumber", criteria.supplierNumber);
  setIfFinite(params, "supplierType", criteria.supplierType);
  setIfTrimmed(params, "documentNumber", criteria.documentNumber);
  setIfTrimmed(params, "sapDocument", criteria.sapDocument);
  setIfFinite(params, "source", criteria.source);

  if (criteria.from) {
    params.set("from", new Date(criteria.from).toISOString());
  }

  if (criteria.to) {
    params.set("to", new Date(criteria.to).toISOString());
  }

  const limit = criteria.pageSize ?? 100;

  /*
   * La UI maneja páginas desde 1,
   * mientras que el backend las
   * maneja desde 0.
   */
  const pageIndex = Math.max(0, (criteria.pageNumber ?? 1) - 1);

  params.set("limit", String(limit));
  params.set("page", String(pageIndex));

  return params.toString();
}

export const DiscountsClient = {
  async get(
    criteria: RebateFilters
  ): Promise<Rebate[]> {
    const query = buildRebateQuery(criteria);

    try {
      const rows =
        await api.request<Rebate[]>(
          query
            ? `${DEFAULT_ROUTE}?${query}`
            : DEFAULT_ROUTE,
          "get"
        );

      if (!Array.isArray(rows)) {
        return [];
      }

      return rows.map(
        (row) =>
          normalizeRebateRow(
            row as unknown as Record<
              string,
              unknown
            >
          )
      );
    } catch (error) {
      if (isNotFound(error)) {
        return [];
      }

      throw error;
    }
  },
};
