// ✅ FILE: src/features/audit-logs/api/auditLogsService.ts
import type { ApiClient } from "@/services/ApiClient";

function toQuery(params: any = {}) {
    const sp = new URLSearchParams();

    Object.entries(params).forEach(([k, v]) => {
        if (v === undefined || v === null || v === "") return;

        if (Array.isArray(v)) {
            v.forEach((item) => {
                if (item === undefined || item === null || item === "") return;
                // ✅ ids=<uuid>&ids=<uuid>
                sp.append(k, String(item));
            });
            return;
        }

        sp.append(k, String(v));
    });

    return sp.toString();
}

export function createAuditLogsService(api: ApiClient) {
    async function listAuditLogs(params: {
        idAplicativo?: string;
        tipoEvento?: "ALL" | "ERROR" | "ALERTA" | "INFO";
        codigoError?: string;
        idTransaccion?: string;
        modulo?: string;
        search?: string;
        ids?: string[]; // ✅ NUEVO
        fechaInicio: string | Date;
        fechaFin: string | Date;
        page?: number;
        limit?: number;
    }) {
        const query = toQuery({
            ...params,
            fechaInicio:
                params.fechaInicio instanceof Date
                    ? params.fechaInicio.toISOString()
                    : params.fechaInicio,
            fechaFin:
                params.fechaFin instanceof Date
                    ? params.fechaFin.toISOString()
                    : params.fechaFin,
        });

        return api.request(`audit-logs${query ? `?${query}` : ""}`, "get");
    }

    async function getAuditLogDetail(id: string) {
        return api.request(`audit-logs/${id}`, "get");
    }

    async function exportAuditLogsCsv(params: {
        idAplicativo?: string;
        tipoEvento?: "ALL" | "ERROR" | "ALERTA" | "INFO";
        codigoError?: string;
        idTransaccion?: string;
        modulo?: string;
        search?: string;
        ids?: string[]; // ✅ NUEVO
        fechaInicio: string | Date;
        fechaFin: string | Date;
    }) {
        const query = toQuery({
            ...params,
            fechaInicio:
                params.fechaInicio instanceof Date
                    ? params.fechaInicio.toISOString()
                    : params.fechaInicio,
            fechaFin:
                params.fechaFin instanceof Date
                    ? params.fechaFin.toISOString()
                    : params.fechaFin,
        });

        return api.requestBinary(
            `audit-logs/export/csv${query ? `?${query}` : ""}`,
            "get",
            null,
            "audit-logs.csv"
        );
    }

    return {
        listAuditLogs,
        getAuditLogDetail,
        exportAuditLogsCsv,
    };
}