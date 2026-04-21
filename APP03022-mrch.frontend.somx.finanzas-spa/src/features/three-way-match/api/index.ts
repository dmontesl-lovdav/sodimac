// src/features/three-way-match/api/index.ts
import { createApiClient } from "@/services/ApiClient";
import { createThreeWayMatchService } from "./threeWayMatchService";

const api = createApiClient();
const client = createThreeWayMatchService(api);

function buildParams(filters: any = {}, page: number = 1, size: number = 10) {
    const params: any = {};

    if (filters.dateType) params.tipoFecha = filters.dateType;
    if (filters.startDate) params.fechaInicio = new Date(filters.startDate).toISOString();
    if (filters.endDate) params.fechaFin = new Date(filters.endDate).toISOString();

    if (filters.supplier !== undefined && filters.supplier !== null && filters.supplier !== "")
        params.numeroProveedor = Number(filters.supplier);

    if (filters.po) params.ordenCompra = filters.po;
    if (filters.reception) params.recepcion = filters.reception;

    params.page = page;
    params.limit = size;

    return params;
}

export const searchThreeWayMatch = (filters: any = {}, page: number = 1, size: number = 10) => {
    return client.searchThreeWayMatch(buildParams(filters, page, size));
};

export const exportThreeWayMatchCsv = (filters: any = {}, page: number = 1, size: number = 10) => {
    return client.exportThreeWayMatchCsv(buildParams(filters, page, size));
};

export const exportThreeWayMatchXlsx = (filters: any = {}, page: number = 1, size: number = 10) => {
    return client.exportThreeWayMatchXlsx(buildParams(filters, page, size));
};