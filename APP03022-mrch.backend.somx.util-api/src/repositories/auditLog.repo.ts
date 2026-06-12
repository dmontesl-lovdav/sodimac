import { datasource } from "@/config/typeorm-datasource.js";

type FindWithFiltersArgs = {
    fechaInicio: Date;
    fechaFin: Date;
    ids?: string[];
    idAplicativo?: string;
    tipoEvento?: "ALL" | "ERROR" | "ALERTA" | "INFO";
    codigoError?: string;
    idTransaccion?: string;
    modulo?: string;
    search?: string;
    page: number;
    limit: number;
};

export async function createOne(args: {
    trace_id: string;
    service_name: string;
    modulo: string;
    paso: string;
    detalle?: string | null;
    tipo_evento: "ERROR" | "ALERTA" | "INFO";
    codigo_error?: string | null;
    id_mensaje?: string | null;
    message?: string | null;
    log?: string | null;
    user_id?: string | null;
    timestamp?: Date;
    details?: any;
}) {
    const isError = args.tipo_evento === "ERROR";

    const sql = `
        INSERT INTO core_audit.activity_logs (
            trace_id, trace_front_id, duration_ms, is_error, modulo, service_name, action,
            message, message_detail, user_id, timestamp, details,
            tipo_evento, codigo_error, id_mensaje, paso, log
        )
        VALUES (
            $1, $2, $3, $4, $5, $6, $7,
            $8, $9, $10, $11, $12,
            $13, $14, $15, $16, $17
        )
        RETURNING activity_logs_uuid
    `;

    const values = [
        args.trace_id,
        null,
        0,
        isError,
        args.modulo,
        args.service_name,
        args.paso,
        args.message ?? null,
        args.detalle ?? null,
        args.user_id ?? "system",
        args.timestamp ?? new Date(),
        args.details ?? {},
        args.tipo_evento,
        args.codigo_error ?? null,
        args.id_mensaje ?? null,
        args.paso ?? null,
        args.log ?? null,
    ];

    const rows = await datasource.query(sql, values);
    return rows?.[0] ?? null;
}

function buildWhere(a: FindWithFiltersArgs) {
    const where: string[] = [];
    const values: any[] = [];

    values.push(a.fechaInicio);
    values.push(a.fechaFin);
    where.push(`timestamp >= $1`);
    where.push(`timestamp <= $2`);

    let i = 3;

    if (a.ids?.length) {
        values.push(a.ids);
        where.push(`activity_logs_uuid = ANY($${i++}::uuid[])`);
    }

    if (a.idAplicativo) {
        values.push(a.idAplicativo);
        where.push(`service_name = $${i++}`);
    }

    if (a.modulo) {
        values.push(a.modulo);
        where.push(`modulo = $${i++}`);
    }

    if (a.idTransaccion) {
        values.push(a.idTransaccion);
        where.push(`trace_id = $${i++}`);
    }

    if (a.tipoEvento && a.tipoEvento !== "ALL") {
        values.push(a.tipoEvento);
        where.push(`tipo_evento = $${i++}`);
    }

    if (a.codigoError) {
        values.push(a.codigoError);
        where.push(`codigo_error = $${i++}`);
    }

    if (a.search) {
        values.push(`%${a.search}%`);
        const p = `$${i++}`;
        where.push(
            `(
                message ILIKE ${p}
                OR message_detail ILIKE ${p}
                OR action ILIKE ${p}
                OR service_name ILIKE ${p}
                OR modulo ILIKE ${p}
                OR trace_id::text ILIKE ${p}
                OR user_id::text ILIKE ${p}
                OR codigo_error ILIKE ${p}
                OR id_mensaje ILIKE ${p}
            )`
        );
    }

    return { whereSql: `WHERE ${where.join(" AND ")}`, values, nextIndex: i };
}

export async function findWithFilters(args: FindWithFiltersArgs) {
    const { whereSql, values, nextIndex } = buildWhere(args);
    const offset = (args.page - 1) * args.limit;

    const countSql = `
        SELECT COUNT(*)::int AS total
        FROM core_audit.activity_logs
        ${whereSql}
    `;
    const countRows = await datasource.query(countSql, values);
    const total = Number(countRows?.[0]?.total ?? 0);

    const limitParamIndex = nextIndex;
    const offsetParamIndex = nextIndex + 1;

    const dataSql = `
        SELECT
            activity_logs_uuid,
            trace_front_id,
            trace_id,
            duration_ms,
            is_error,
            modulo,
            service_name,
            action,
            paso,
            message,
            id_mensaje,
            message_detail,
            codigo_error,
            tipo_evento,
            log,
            user_id,
            timestamp,
            details
        FROM core_audit.activity_logs
        ${whereSql}
        ORDER BY timestamp DESC
        LIMIT $${limitParamIndex}
        OFFSET $${offsetParamIndex}
    `;

    const data = await datasource.query(dataSql, [...values, args.limit, offset]);
    return { page: args.page, limit: args.limit, total, data };
}

export async function findManyByIds(ids: string[]) {
    const sql = `
        SELECT
            activity_logs_uuid, trace_front_id, trace_id, duration_ms, is_error,
            modulo, service_name, action, paso, message, id_mensaje,
            message_detail, codigo_error, tipo_evento, log, user_id,
            timestamp, details
        FROM core_audit.activity_logs
        WHERE activity_logs_uuid = ANY($1::uuid[])
        ORDER BY timestamp DESC
    `;
    const rows = await datasource.query(sql, [ids]);
    return rows ?? [];
}

export async function findById(id: string) {
    const sql = `
        SELECT
            activity_logs_uuid, trace_front_id, trace_id, duration_ms, is_error,
            modulo, service_name, action, paso, message, id_mensaje,
            message_detail, codigo_error, tipo_evento, log, user_id,
            timestamp, details
        FROM core_audit.activity_logs
        WHERE activity_logs_uuid = $1
        LIMIT 1
    `;
    const rows = await datasource.query(sql, [id]);
    return rows?.[0] ?? null;
}

export async function findByTransactionId(idTransaccion: string) {
    const sql = `
        SELECT
            activity_logs_uuid, trace_front_id, trace_id, duration_ms, is_error,
            modulo, service_name, action, paso, message, id_mensaje,
            message_detail, codigo_error, tipo_evento, log, user_id,
            timestamp, details
        FROM core_audit.activity_logs
        WHERE trace_id = $1
        ORDER BY timestamp DESC
    `;
    const rows = await datasource.query(sql, [idTransaccion]);
    return rows ?? [];
}
