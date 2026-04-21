import { datasource } from "@/config/typeorm-datasource.js";

export async function getNextSequenceValue() {
    const sql = `SELECT nextval('tenant_finance.seq_trx_global')::bigint AS value`;
    const rows = await datasource.query(sql);
    return Number(rows?.[0]?.value ?? 0);
}

export async function createOne(args: {
    folioVisible: string;
    uuidInterno: string;
    codigoModulo: string;
    pantallaOrigen: string;
    caso: string;
    idUsuario: string;
    origen: string;
    metadatos?: Record<string, unknown> | null;
}) {
    const sql = `
        INSERT INTO tenant_finance.transaction_ids (
            folio_visible,
            uuid_interno,
            codigo_modulo,
            pantalla_origen,
            caso,
            id_usuario,
            origen,
            estatus,
            metadatos,
            created_at
        )
        VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)
        RETURNING
            transaction_id_uuid,
            folio_visible,
            uuid_interno,
            codigo_modulo,
            pantalla_origen,
            caso,
            id_usuario,
            origen,
            estatus,
            metadatos,
            created_at
    `;

    const values = [
        args.folioVisible,
        args.uuidInterno,
        args.codigoModulo,
        args.pantallaOrigen,
        args.caso,
        args.idUsuario,
        args.origen,
        "GENERATED",
        args.metadatos ?? null,
        new Date(),
    ];

    const rows = await datasource.query(sql, values);
    return rows?.[0] ?? null;
}

export async function findByFolioVisible(folioVisible: string) {
    const sql = `
        SELECT
            transaction_id_uuid,
            folio_visible,
            uuid_interno,
            codigo_modulo,
            pantalla_origen,
            caso,
            id_usuario,
            origen,
            estatus,
            metadatos,
            created_at
        FROM tenant_finance.transaction_ids
        WHERE folio_visible = $1
        LIMIT 1
    `;

    const rows = await datasource.query(sql, [folioVisible]);
    return rows?.[0] ?? null;
}

export async function createErrorLog(args: {
    idUsuario?: string | null;
    origen?: string | null;
    codigoModulo?: string | null;
    pantallaOrigen?: string | null;
    caso?: string | null;
    codigoError: string;
    descripcionError: string;
    folioIntentado?: string | null;
    uuidIntentado?: string | null;
    metadatos?: Record<string, unknown> | null;
}) {
    const sql = `
        INSERT INTO tenant_finance.transaction_id_error_logs (
            id_usuario,
            origen,
            codigo_modulo,
            pantalla_origen,
            caso,
            codigo_error,
            descripcion_error,
            folio_intentado,
            uuid_intentado,
            metadatos,
            created_at
        )
        VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)
        RETURNING transaction_id_error_log_uuid
    `;

    const values = [
        args.idUsuario ?? null,
        args.origen ?? null,
        args.codigoModulo ?? null,
        args.pantallaOrigen ?? null,
        args.caso ?? null,
        args.codigoError,
        args.descripcionError,
        args.folioIntentado ?? null,
        args.uuidIntentado ?? null,
        args.metadatos ?? null,
        new Date(),
    ];

    const rows = await datasource.query(sql, values);
    return rows?.[0] ?? null;
}