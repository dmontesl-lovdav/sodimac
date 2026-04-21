import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
const TRACE_EXAMPLE = "a4b6c2b0-1f2b-4a0f-9c3c-9c2f9e7b1a11";

export const auditLogsSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    AuditLog: {
        type: "object",
        properties: {
            activity_logs_uuid: { type: "string", format: "uuid", example: UUID_EXAMPLE },

            trace_front_id: { type: "string", format: "uuid", nullable: true },
            trace_id: { type: "string", format: "uuid", example: TRACE_EXAMPLE },

            duration_ms: { type: "number", example: 123.45 },
            is_error: { type: "boolean", example: false },

            modulo: { type: "string", example: "API_FINANZAS" },
            service_name: { type: "string", example: "finanzas-api" },

            action: { type: "string", nullable: true, example: "GET /shipping-guide" },
            paso: { type: "string", nullable: true, example: "END_GET" },

            message: { type: "string", nullable: true, example: "END_GET" },
            message_detail: { type: "string", nullable: true, example: "END REQUEST" },

            user_id: { type: "string", example: "system" },
            timestamp: { type: "string", format: "date-time", example: "2026-02-21T22:10:00.000Z" },

            details: { type: "object", additionalProperties: true },

            tipo_evento: { type: "string", example: "INFO" },
            codigo_error: { type: "string", nullable: true, example: "400" },
            id_mensaje: { type: "string", nullable: true, example: "ValidationError" },
            log: { type: "string", nullable: true, example: "stack trace..." },
        },
        required: ["activity_logs_uuid", "trace_id", "modulo", "service_name", "timestamp", "tipo_evento"],
    },

    CreateAuditLogDto: {
        type: "object",
        properties: {
            idTransaccion: { type: "string", format: "uuid", example: TRACE_EXAMPLE },
            idAplicativo: { type: "string", example: "finanzas-api" },
            idModulo: { type: "string", example: "API_FINANZAS" },
            paso: { type: "string", example: "CreateShippingGuide" },
            detalle: { type: "string", nullable: true, example: "Descripción corta" },
            fechaHora: { type: "string", format: "date-time", nullable: true, example: "2026-02-21T22:10:00.000Z" },
            tipoEvento: { type: "string", example: "INFO" },
            idUsuario: { type: "string", nullable: true, example: "system" },

            idError: { type: "string", nullable: true, example: "ERR-400" },
            idMensaje: { type: "string", nullable: true, example: "CAT1001" },
            mensaje: { type: "string", nullable: true, example: "Validation failed" },
            log: { type: "string", nullable: true, example: "stack trace..." },
        },
        required: ["idTransaccion", "idAplicativo", "idModulo", "paso", "tipoEvento"],
    },

    ListAuditLogsResponse: {
        type: "object",
        properties: {
            page: { type: "integer", example: 1 },
            limit: { type: "integer", example: 10 },
            total: { type: "integer", example: 152 },
            data: {
                type: "array",
                items: { $ref: "#/components/schemas/AuditLog" },
            },
        },
        required: ["page", "limit", "total", "data"],
    },

    AuditLogsByTransactionResponse: {
        type: "object",
        properties: {
            idTransaccion: { type: "string", format: "uuid", example: TRACE_EXAMPLE },
            total: { type: "integer", example: 3 },
            data: {
                type: "array",
                items: { $ref: "#/components/schemas/AuditLog" },
            },
        },
        required: ["idTransaccion", "total", "data"],
    },
};