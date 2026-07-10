// src/features/three-way-match/api/index.ts
import { createApiClient } from "@/services/ApiClient";
import { createThreeWayMatchService } from "./threeWayMatchService";

const api = createApiClient();
const client = createThreeWayMatchService(api);

type ThreeWayMatchFilters = {
    dateType?: string;
    startDate?: string;
    endDate?: string;
    supplier?: string;
    supplierType?: string;
    po?: string;
    reception?: string;
};

function buildParams(
    filters: ThreeWayMatchFilters = {},
    page: number = 1,
    size: number = 10
) {
    const params: Record<string, string | number> = {};

    if (filters.dateType) {
        params.tipoFecha = filters.dateType;
    }

    if (filters.startDate) {
        params.fechaInicio = new Date(
            filters.startDate
        ).toISOString();
    }

    if (filters.endDate) {
        params.fechaFin = new Date(
            filters.endDate
        ).toISOString();
    }

    if (
        filters.supplier !== undefined &&
        filters.supplier !== null &&
        filters.supplier.trim() !== ""
    ) {
        params.numeroProveedor = Number(filters.supplier);
    }

    if (
        filters.supplierType !== undefined &&
        filters.supplierType !== null &&
        filters.supplierType.trim() !== ""
    ) {
        params.tipoProveedor = Number(filters.supplierType);
    }

    if (filters.po?.trim()) {
        params.ordenCompra = filters.po.trim();
    }

    if (filters.reception?.trim()) {
        params.recepcion = filters.reception.trim();
    }

    params.page = page;
    params.limit = size;

    return params;
}

export const searchThreeWayMatch = (
    filters: ThreeWayMatchFilters = {},
    page: number = 1,
    size: number = 10
) => {
    return client.searchThreeWayMatch(
        buildParams(filters, page, size)
    );
};

export const exportThreeWayMatchCsv = (
    filters: ThreeWayMatchFilters = {},
    page: number = 1,
    size: number = 10
) => {
    return client.exportThreeWayMatchCsv(
        buildParams(filters, page, size)
    );
};

export const exportThreeWayMatchXlsx = (
    filters: ThreeWayMatchFilters = {},
    page: number = 1,
    size: number = 10
) => {
    return client.exportThreeWayMatchXlsx(
        buildParams(filters, page, size)
    );
};