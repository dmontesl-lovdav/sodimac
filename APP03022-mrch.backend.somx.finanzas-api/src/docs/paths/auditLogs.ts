import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
const TRACE_EXAMPLE = "a4b6c2b0-1f2b-4a0f-9c3c-9c2f9e7b1a11";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const auditLogsPaths: OpenAPIV3.PathsObject = {
    "/audit-logs": {
        get: {
            tags: ["AuditLogs"],
            summary: "List audit logs (filtered + paginated)",
            parameters: [
                {
                    in: "query",
                    name: "fechaInicio",
                    required: true,
                    description: "Start datetime (required)",
                    schema: { type: "string", format: "date-time", example: "2026-02-01T00:00:00.000Z" },
                },
                {
                    in: "query",
                    name: "fechaFin",
                    required: true,
                    description: "End datetime (required)",
                    schema: { type: "string", format: "date-time", example: "2026-02-21T23:59:59.000Z" },
                },
                {
                    in: "query",
                    name: "idAplicativo",
                    required: false,
                    description: "Service/App name (maps to service_name)",
                    schema: { type: "string", example: "finanzas-api" },
                },
                {
                    in: "query",
                    name: "modulo",
                    required: false,
                    description: "Module (maps to modulo)",
                    schema: { type: "string", example: "API_FINANZAS" },
                },
                {
                    in: "query",
                    name: "idTransaccion",
                    required: false,
                    description: "Transaction id (trace_id)",
                    schema: { type: "string", pattern: UUID_PATTERN, example: TRACE_EXAMPLE },
                },
                {
                    in: "query",
                    name: "tipoEvento",
                    required: false,
                    description: "Event type (ALL|ERROR|ALERTA|INFO)",
                    schema: { type: "string", enum: ["ALL", "ERROR", "ALERTA", "INFO"], example: "ALL" },
                },
                {
                    in: "query",
                    name: "codigoError",
                    required: false,
                    description: "Error code (codigo_error)",
                    schema: { type: "string", example: "400" },
                },
                {
                    in: "query",
                    name: "search",
                    required: false,
                    description: "Free text search (message, detail, action, etc.)",
                    schema: { type: "string", example: "ValidationError" },
                },
                {
                    in: "query",
                    name: "page",
                    required: false,
                    schema: { type: "integer", example: 1 },
                },
                {
                    in: "query",
                    name: "limit",
                    required: false,
                    schema: { type: "integer", example: 10 },
                },
            ],
            responses: {
                200: {
                    description: "Paginated audit logs",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ListAuditLogsResponse" },
                        },
                    },
                },
            },
        },

        post: {
            tags: ["AuditLogs"],
            summary: "Create audit log",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/CreateAuditLogDto" },
                        example: {
                            idTransaccion: TRACE_EXAMPLE,
                            idAplicativo: "finanzas-api",
                            idModulo: "API_FINANZAS",
                            paso: "CreateShippingGuide",
                            detalle: "Creación de guía de embarque",
                            tipoEvento: "INFO",
                            idUsuario: "system",
                        },
                    },
                },
            },
            responses: {
                201: {
                    description: "Created",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    activity_logs_uuid: { type: "string", format: "uuid", example: UUID_EXAMPLE },
                                },
                                required: ["activity_logs_uuid"],
                            },
                        },
                    },
                },
                400: { description: "Validation failed" },
            },
        },
    },

    "/audit-logs/export/csv": {
        get: {
            tags: ["AuditLogs"],
            summary: "Export audit logs as CSV (filtered)",
            parameters: [
                { in: "query", name: "fechaInicio", required: true, schema: { type: "string", format: "date-time" } },
                { in: "query", name: "fechaFin", required: true, schema: { type: "string", format: "date-time" } },
                { in: "query", name: "idAplicativo", required: false, schema: { type: "string" } },
                { in: "query", name: "modulo", required: false, schema: { type: "string" } },
                { in: "query", name: "idTransaccion", required: false, schema: { type: "string", pattern: UUID_PATTERN } },
                { in: "query", name: "tipoEvento", required: false, schema: { type: "string", enum: ["ALL", "ERROR", "ALERTA", "INFO"] } },
                { in: "query", name: "codigoError", required: false, schema: { type: "string" } },
                { in: "query", name: "search", required: false, schema: { type: "string" } },
            ],
            responses: {
                200: {
                    description: "CSV file",
                    content: {
                        "text/csv": {
                            schema: { type: "string", example: "IdLog,IdTransaccion,..." },
                        },
                    },
                },
            },
        },
    },

    "/audit-logs/transaction/{idTransaccion}": {
        get: {
            tags: ["AuditLogs"],
            summary: "Get audit logs by transaction id",
            parameters: [
                {
                    in: "path",
                    name: "idTransaccion",
                    required: true,
                    description: "Transaction id (trace_id)",
                    schema: { type: "string", pattern: UUID_PATTERN, example: TRACE_EXAMPLE },
                },
            ],
            responses: {
                200: {
                    description: "Audit logs grouped by transaction id",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AuditLogsByTransactionResponse" },
                        },
                    },
                },
                404: { description: "Not found" },
            },
        },
    },

    "/audit-logs/{id}": {
        get: {
            tags: ["AuditLogs"],
            summary: "Get audit log detail by id",
            parameters: [
                {
                    in: "path",
                    name: "id",
                    required: true,
                    schema: { type: "string", pattern: UUID_PATTERN, example: UUID_EXAMPLE },
                },
            ],
            responses: {
                200: {
                    description: "Audit log detail",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AuditLog" },
                        },
                    },
                },
                404: { description: "Not found" },
            },
        },
    },
};