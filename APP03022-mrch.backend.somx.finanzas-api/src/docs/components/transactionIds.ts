import type { OpenAPIV3 } from "openapi-types";

export const transactionIdSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    CreateTransactionIdDto: {
        type: "object",
        properties: {
            codigoModulo: {
                type: "string",
                minLength: 2,
                maxLength: 4,
                example: "PROV",
                description: "Código del módulo o microservicio origen en mayúsculas.",
            },
            pantallaOrigen: {
                type: "string",
                maxLength: 100,
                example: "ALTA_PROVEEDOR",
                description: "Pantalla o submódulo que origina la acción.",
            },
            caso: {
                type: "string",
                maxLength: 200,
                example: "GUARDAR_PROVEEDOR",
                description: "Caso o flujo ejecutado por el usuario.",
            },
            metadatos: {
                type: "object",
                additionalProperties: true,
                example: {
                    rfc: "ABC010203XYZ",
                    proveedorId: 1001,
                },
                description: "Información adicional libre asociada a la transacción.",
            },
            idUsuario: {
                type: "string",
                example: "12345",
                description: "Usuario autenticado que ejecuta la acción.",
            },
            origen: {
                type: "string",
                example: "portal-proveedores",
                description: "Sistema o microservicio que genera la solicitud.",
            },
        },
        required: ["codigoModulo", "pantallaOrigen", "caso"],
    },

    TransactionIdResponse: {
        type: "object",
        properties: {
            folioVisible: {
                type: "string",
                example: "PROV-0000A3KF",
                description: "Folio visible corto y legible para el usuario.",
            },
            uuidInterno: {
                type: "string",
                format: "uuid",
                example: "550e8400-e29b-41d4-a716-446655440000",
                description: "Identificador técnico interno.",
            },
            fechaHora: {
                type: "string",
                format: "date-time",
                example: "2026-03-18T20:30:45.000Z",
            },
            codigoModulo: {
                type: "string",
                example: "PROV",
            },
            pantallaOrigen: {
                type: "string",
                example: "ALTA_PROVEEDOR",
            },
            caso: {
                type: "string",
                example: "GUARDAR_PROVEEDOR",
            },
            idUsuario: {
                type: "string",
                example: "12345",
            },
            origen: {
                type: "string",
                example: "portal-proveedores",
            },
            estatus: {
                type: "string",
                example: "GENERATED",
            },
            metadatos: {
                type: "object",
                additionalProperties: true,
                example: {
                    rfc: "ABC010203XYZ",
                    proveedorId: 1001,
                },
            },
        },
        required: [
            "folioVisible",
            "uuidInterno",
            "fechaHora",
            "codigoModulo",
            "pantallaOrigen",
            "caso",
            "idUsuario",
            "origen",
            "estatus",
        ],
    },

    TransactionIdApiResponse: {
        type: "object",
        properties: {
            message: {
                type: "string",
                example: "Transaction ID generated successfully",
            },
            data: {
                $ref: "#/components/schemas/TransactionIdResponse",
            },
            count: {
                type: "integer",
                example: 0,
            },
            statusCode: {
                type: "integer",
                example: 201,
            },
            success: {
                type: "boolean",
                example: true,
            },
            details: {
                type: "string",
                example: "",
            },
            trace_id: {
                type: "string",
                format: "uuid",
                example: "209279be-37c7-4154-b3c6-df976fd7b6a",
            },
        },
        required: ["message", "data", "count", "statusCode", "success", "details"],
    },
};