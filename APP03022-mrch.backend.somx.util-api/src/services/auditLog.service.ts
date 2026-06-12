import * as r from "@/repositories/auditLog.repo.js";
import type { CreateAuditLogDto, ListAuditLogsQuery } from "@/schemas/auditLog.schema.js";
import { GenericException } from "@/exceptions/GenericException.js";

function validateRange(q: ListAuditLogsQuery) {
    if (q.fechaFin < q.fechaInicio) {
        throw new GenericException(400, "fechaFin must be >= fechaInicio");
    }
}

export async function create(dto: CreateAuditLogDto) {
    const row = await r.createOne({
        trace_id: dto.idTransaccion,
        service_name: dto.idAplicativo,
        modulo: dto.idModulo,
        paso: dto.paso,
        detalle: dto.detalle ?? null,
        timestamp: dto.fechaHora ?? new Date(),
        tipo_evento: dto.tipoEvento,
        codigo_error: dto.idError ?? null,
        id_mensaje: dto.idMensaje ?? null,
        message: dto.mensaje ?? dto.detalle ?? dto.paso,
        log: dto.log ?? null,
        user_id: dto.idUsuario ?? "system",
        details: {},
    });

    return row;
}

export async function list(q: ListAuditLogsQuery) {
    validateRange(q);

    return r.findWithFilters({
        fechaInicio: q.fechaInicio,
        fechaFin: q.fechaFin,
        ...(q.ids?.length && { ids: q.ids }),
        ...(q.idAplicativo && { idAplicativo: q.idAplicativo }),
        ...(q.tipoEvento && { tipoEvento: q.tipoEvento }),
        ...(q.codigoError && { codigoError: q.codigoError }),
        ...(q.idTransaccion && { idTransaccion: q.idTransaccion }),
        ...(q.modulo && { modulo: q.modulo }),
        ...(q.search && { search: q.search }),
        page: q.page ?? 1,
        limit: q.limit ?? 10,
    });
}

export async function detail(id: string) {
    const row = await r.findById(id);
    if (!row) throw new GenericException(404, "Audit log not found");
    return row;
}

export async function detailByTransaction(idTransaccion: string) {
    const rows = await r.findByTransactionId(idTransaccion);
    if (!rows?.length) throw new GenericException(404, "Audit logs not found for transaction");
    return {
        idTransaccion,
        total: rows.length,
        data: rows,
    };
}

function escapeCsv(v: any) {
    const s = v === null || v === undefined ? "" : String(v);
    return `"${s.replace(/"/g, '""')}"`;
}

export async function exportCsv(q: ListAuditLogsQuery) {
    validateRange(q);

    let rows: any[] = [];

    if (q.ids?.length) {
        rows = await r.findManyByIds(q.ids);
    } else {
        const result = await r.findWithFilters({
            fechaInicio: q.fechaInicio,
            fechaFin: q.fechaFin,
            ...(q.idAplicativo && { idAplicativo: q.idAplicativo }),
            ...(q.tipoEvento && { tipoEvento: q.tipoEvento }),
            ...(q.codigoError && { codigoError: q.codigoError }),
            ...(q.idTransaccion && { idTransaccion: q.idTransaccion }),
            ...(q.modulo && { modulo: q.modulo }),
            ...(q.search && { search: q.search }),
            page: 1,
            limit: 100000,
        });

        rows = result.data ?? [];
    }

    const headers = [
        "IdLog",
        "IdTransaccion",
        "IdAplicativo",
        "IdModulo",
        "Paso",
        "Detalle",
        "FechaHora",
        "IdMensaje",
        "Mensaje",
        "CodigoError",
        "TipoEvento",
        "Log",
        "IdUsuario",
    ];

    const lines = [
        headers.join(","),
        ...rows.map((x: any) =>
            [
                escapeCsv(x.activity_logs_uuid),
                escapeCsv(x.trace_id),
                escapeCsv(x.service_name),
                escapeCsv(x.modulo),
                escapeCsv(x.paso ?? x.action ?? ""),
                escapeCsv(x.message_detail ?? ""),
                escapeCsv(x.timestamp),
                escapeCsv(x.id_mensaje ?? ""),
                escapeCsv(x.message ?? ""),
                escapeCsv(x.codigo_error ?? ""),
                escapeCsv(x.tipo_evento ?? (x.is_error ? "ERROR" : "INFO")),
                escapeCsv(x.log ?? ""),
                escapeCsv(x.user_id ?? ""),
            ].join(",")
        ),
    ];

    return lines.join("\n");
}
