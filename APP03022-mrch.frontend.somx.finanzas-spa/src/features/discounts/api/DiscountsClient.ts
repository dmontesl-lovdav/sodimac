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

export const DiscountsClient = {
  async get(
    criteria: RebateFilters
  ): Promise<Rebate[]> {
    const params =
      new URLSearchParams();

    if (
      criteria.status != null &&
      Number.isFinite(
        Number(criteria.status)
      )
    ) {
      params.set(
        "status",
        String(criteria.status)
      );
    }

    if (
      criteria.supplierNumber != null &&
      Number.isFinite(
        Number(
          criteria.supplierNumber
        )
      )
    ) {
      params.set(
        "vendorNumber",
        String(
          criteria.supplierNumber
        )
      );
    }

    if (
      criteria.supplierType != null &&
      Number.isFinite(
        Number(
          criteria.supplierType
        )
      )
    ) {
      params.set(
        "supplierType",
        String(
          criteria.supplierType
        )
      );
    }

    if (
      criteria.documentNumber
        ?.trim()
    ) {
      params.set(
        "documentNumber",
        criteria.documentNumber.trim()
      );
    }

    if (
      criteria.sapDocument
        ?.trim()
    ) {
      params.set(
        "sapDocument",
        criteria.sapDocument.trim()
      );
    }

    if (
      criteria.source != null &&
      Number.isFinite(
        Number(criteria.source)
      )
    ) {
      params.set(
        "source",
        String(criteria.source)
      );
    }

    if (criteria.from) {
      params.set(
        "from",
        new Date(
          criteria.from
        ).toISOString()
      );
    }

    if (criteria.to) {
      params.set(
        "to",
        new Date(
          criteria.to
        ).toISOString()
      );
    }

    const limit =
      criteria.pageSize ?? 100;

    /*
     * La UI maneja páginas desde 1,
     * mientras que el backend las
     * maneja desde 0.
     */
    const pageIndex =
      Math.max(
        0,
        (
          criteria.pageNumber ??
          1
        ) - 1
      );

    params.set(
      "limit",
      String(limit)
    );

    params.set(
      "page",
      String(pageIndex)
    );

    const query =
      params.toString();

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