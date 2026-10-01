import { createApiClient } from "@/services/apiClient";
import { createAuditLogsService } from "./auditLogsService";

const api = createApiClient();
const client = createAuditLogsService(api);

const AUDIT_MODULES: Record<string, string> = {
    '1': 'API_FINANZAS',
    '2': 'API_FISCAL',
    '5': 'API_UTIL',
};

function buildParams(filters: any = {}, page: number = 1, size: number = 10) {
    const params: any = {};

    if (filters.startDate)
        params.fechaInicio = new Date(filters.startDate).toISOString();
    if (filters.endDate)
        params.fechaFin = new Date(filters.endDate).toISOString();

    if (filters.idAplicativo) params.idAplicativo = String(filters.idAplicativo).trim();
    if (filters.tipoEvento) params.tipoEvento = filters.tipoEvento;
    if (filters.codigoError) params.codigoError = String(filters.codigoError).trim();
    if (filters.idTransaccion) params.idTransaccion = String(filters.idTransaccion).trim();
    if (filters.modulo) {
        const modulo = String(filters.modulo).trim();
        params.modulo = AUDIT_MODULES[modulo] ?? modulo;
    }
    if (filters.search) params.search = String(filters.search).trim();

    if (filters.ids && Array.isArray(filters.ids) && filters.ids.length > 0) {
        params.ids = filters.ids;
    }

    params.page = page;
    params.limit = size;

    return params;
}

export const listAuditLogs = (filters: any = {}, page: number = 1, size: number = 10) => {
    return client.listAuditLogs(buildParams(filters, page, size));
};

export const getAuditLogDetail = (id: string) => {
    return client.getAuditLogDetail(id);
};

export const getCatalogDetails = (code: string) => {
    return client.getCatalogDetailsByCode(code);
};

export const exportAuditLogsCsv = (filters: any = {}) => {
    // La exportación descarga todos los registros filtrados; no se pagina.
    const params = buildParams(filters);
    delete params.page;
    delete params.limit;
    return client.exportAuditLogsCsv(params);
};

export const getAuditLogApplications = (modulo?: string) => {
    const value = String(modulo ?? '').trim();
    const mappedModulo = value
        ? AUDIT_MODULES[value] ?? value
        : undefined;

    return client.getAuditLogApplications(mappedModulo);
};