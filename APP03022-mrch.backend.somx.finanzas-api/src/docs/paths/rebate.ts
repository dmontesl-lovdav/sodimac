import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
const NC_UUID_EXAMPLE = "4d16b318-6e96-4d09-a759-6ce55f4f8bc9";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

const rebateIdParameter: OpenAPIV3.ParameterObject = {
    in: "path",
    name: "id",
    required: true,
    description:
        "UUID interno del descuento (rebateId). No utilizar el UUID fiscal de la NC.",
    schema: {
        type: "string",
        format: "uuid",
        pattern: UUID_PATTERN,
        example: UUID_EXAMPLE,
    },
};

const searchParameters: OpenAPIV3.ParameterObject[] = [
    {
        in: "query",
        name: "vendorNumber",
        schema: { type: "integer", example: 34786 },
        description: "Número exacto de proveedor",
    },
    {
        in: "query",
        name: "supplierType",
        schema: { type: "integer" },
        description: "Identificador del tipo de proveedor",
    },
    {
        in: "query",
        name: "documentNumber",
        schema: { type: "string" },
        description: "Coincidencia parcial del número de documento",
    },
    {
        in: "query",
        name: "sapDocument",
        schema: { type: "string" },
        description: "Coincidencia parcial del documento SAP",
    },
    {
        in: "query",
        name: "status",
        schema: { type: "integer", example: 1 },
        description: "1=Pendiente, 2=Aprobado, 3=Rechazado",
    },
    {
        in: "query",
        name: "source",
        schema: { type: "integer" },
        description: "Identificador del tipo de rebate",
    },
    {
        in: "query",
        name: "periodId",
        schema: { type: "integer" },
        description: "Identificador del período",
    },
    {
        in: "query",
        name: "from",
        schema: { type: "string", format: "date-time" },
        description: "Inicio del rango de fecha de aplicación (postingDate)",
    },
    {
        in: "query",
        name: "to",
        schema: { type: "string", format: "date-time" },
        description: "Fin del rango de fecha de aplicación (postingDate)",
    },
    {
        in: "query",
        name: "limit",
        schema: {
            type: "integer",
            default: 20,
            minimum: 1,
            maximum: 1000,
        },
        description: "Máximo de resultados por página",
    },
    {
        in: "query",
        name: "page",
        schema: {
            type: "integer",
            default: 0,
            minimum: 0,
        },
        description: "Número de página comenzando en cero",
    },
];

const rebateResponse: OpenAPIV3.ResponseObject = {
    description: "Descuento comercial",
    content: {
        "application/json": {
            schema: { $ref: "#/components/schemas/Rebate" },
        },
    },
};

const rebateListResponse: OpenAPIV3.ResponseObject = {
    description: "Lista de descuentos comerciales",
    content: {
        "application/json": {
            schema: {
                type: "array",
                items: { $ref: "#/components/schemas/Rebate" },
            },
        },
    },
};

const csvResponse: OpenAPIV3.ResponseObject = {
    description: "Archivo CSV",
    content: {
        "text/csv": {
            schema: {
                type: "string",
                format: "binary",
            },
        },
    },
    headers: {
        "Content-Disposition": {
            description: "Nombre del archivo descargable",
            schema: {
                type: "string",
                example: 'attachment; filename="rebates.csv"',
            },
        },
    },
};

export const rebatePaths: OpenAPIV3.PathsObject = {
    "/rebates": {
        get: {
            tags: ["Rebates"],
            summary: "Listar descuentos comerciales",
            description:
                "Obtiene descuentos con su relación stampedRebate cuando existe.",
            responses: {
                200: rebateListResponse,
                400: { description: "Parámetros inválidos" },
                404: { description: "Sin registros para el filtro indicado" },
            },
        },

        post: {
            tags: ["Rebates"],
            summary: "Crear descuento comercial",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: {
                            $ref: "#/components/schemas/CreateRebateDto",
                        },
                        example: {
                            documentNumber: "SR-2025-001",
                            referenceNumber: "REF-001-A",
                            sapDocument: "SAP-001",
                            vendorNumber: 1001,
                            amount: "15000.50",
                            source: 1,
                            periodId: 202501,
                            dueDate: "2025-02-15T00:00:00.000Z",
                            postingDate: "2025-01-15T00:00:00.000Z",
                            status: 1,
                            createdBy: 1,
                        },
                    },
                },
            },
            responses: {
                201: {
                    ...rebateResponse,
                    description: "Descuento creado",
                },
                400: { description: "Datos de entrada inválidos" },
            },
        },
    },

    "/rebates/{id}/fiscal-detail": {
        get: {
            tags: ["Rebates"],
            operationId: "getRebateFiscalDetail",
            summary: "Consultar la nota de crédito relacionada con un descuento",
            description:
                "Obtiene el UUID de la NC, el UUID de la factura original y los " +
                "datos para el grid: UUID, fecha de registro, importe total, serie y folio. " +
                "Consulta exclusivamente la NC identificada por ncFiscalUuid. " +
                "Si el descuento existe pero no tiene una NC disponible, devuelve " +
                "HTTP 200 con creditNotes vacío y un mensaje explicativo. " +
                "No crea ni modifica relaciones.",
            parameters: [rebateIdParameter],
            responses: {
                200: {
                    description: "Detalle fiscal del descuento",
                    content: {
                        "application/json": {
                            schema: {
                                $ref: "#/components/schemas/RebateFiscalDetail",
                            },
                            examples: {
                                withCreditNote: {
                                    summary: "Descuento con NC disponible",
                                    value: {
                                        rebateId: UUID_EXAMPLE,
                                        vendorNumber: 34786,
                                        invoiceFiscalUuid: null,
                                        ncFiscalUuid: NC_UUID_EXAMPLE,
                                        creditNotes: [
                                            {
                                                id: "39a021ce-63df-4e46-a79d-5f8123186201",
                                                uuid: NC_UUID_EXAMPLE,
                                                registeredAt:
                                                    "2026-09-04T20:13:52.998Z",
                                                amount: "114.81",
                                                series: null,
                                                folio: "9200913182",
                                            },
                                        ],
                                        message: null,
                                    },
                                },

                                withoutRelation: {
                                    summary: "Descuento sin relación fiscal",
                                    value: {
                                        rebateId: UUID_EXAMPLE,
                                        vendorNumber: 34786,
                                        invoiceFiscalUuid: null,
                                        ncFiscalUuid: null,
                                        creditNotes: [],
                                        message:
                                            "Este descuento todavía no tiene una nota de crédito relacionada.",
                                    },
                                },

                                legacyRelation: {
                                    summary: "Relación sin UUID fiscal de NC registrado",
                                    value: {
                                        rebateId: UUID_EXAMPLE,
                                        vendorNumber: 34786,
                                        invoiceFiscalUuid:
                                            "68f7b8a0-c2e5-4d18-9f62-482be702e103",
                                        ncFiscalUuid: null,
                                        creditNotes: [],
                                        message:
                                            "La relación existente no tiene registrado el UUID fiscal de la nota de crédito.",
                                    },
                                },

                                unavailableCreditNote: {
                                    summary: "UUID relacionado sin documento fiscal disponible",
                                    value: {
                                        rebateId: UUID_EXAMPLE,
                                        vendorNumber: 34786,
                                        invoiceFiscalUuid: null,
                                        ncFiscalUuid: NC_UUID_EXAMPLE,
                                        creditNotes: [],
                                        message:
                                            "La nota de crédito relacionada no está disponible en el repositorio fiscal.",
                                    },
                                },
                            },
                        },
                    },
                },
                400: {
                    description: "El identificador del descuento no es un UUID válido",
                },
                404: {
                    description: "No se encontró el descuento comercial",
                },
                500: {
                    description: "Error al consultar el detalle fiscal",
                },
            },
        },
    },

    "/rebates/{id}": {
        get: {
            tags: ["Rebates"],
            summary: "Consultar descuento por UUID",
            parameters: [rebateIdParameter],
            responses: {
                200: rebateResponse,
                400: { description: "UUID inválido" },
                404: { description: "Descuento no encontrado" },
            },
        },

        put: {
            tags: ["Rebates"],
            summary: "Actualizar descuento",
            parameters: [rebateIdParameter],
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: {
                            $ref: "#/components/schemas/UpdateRebateDto",
                        },
                        example: {
                            amount: "20000.00",
                            status: 2,
                            updatedBy: 1,
                        },
                    },
                },
            },
            responses: {
                200: {
                    ...rebateResponse,
                    description: "Descuento actualizado",
                },
                400: { description: "Datos de entrada inválidos" },
                404: { description: "Descuento no encontrado" },
            },
        },

        delete: {
            tags: ["Rebates"],
            summary: "Eliminar descuento",
            parameters: [rebateIdParameter],
            responses: {
                204: { description: "Operación de eliminación completada" },
                400: { description: "UUID inválido" },
                404: { description: "Descuento no encontrado" },
            },
        },
    },

    "/rebates/published": {
        get: {
            tags: ["Rebates"],
            summary: "Consultar descuentos publicados",
            description:
                "El servicio actual selecciona registros con status = 1. " +
                "El nombre de la ruta se conserva por compatibilidad.",
            responses: {
                200: rebateListResponse,
                400: { description: "Parámetros inválidos" },
                404: { description: "No se encontraron descuentos" },
            },
        },
    },

    "/rebates/published/export/csv": {
        get: {
            tags: ["Rebates"],
            summary: "Exportar descuentos publicados a CSV",
            description:
                "Exporta los descuentos seleccionados por el servicio listPublished, " +
                "que actualmente utiliza status = 1.",
            responses: {
                200: csvResponse,
                400: { description: "Parámetros inválidos" },
            },
        },
    },

    "/rebates/vendor/{vendorNumber}": {
        get: {
            tags: ["Rebates"],
            summary: "Consultar descuentos de un proveedor",
            description:
                "El servicio actual limita esta consulta a descuentos con status = 1.",
            parameters: [
                {
                    in: "path",
                    name: "vendorNumber",
                    required: true,
                    description: "Número de proveedor",
                    schema: {
                        type: "integer",
                        example: 34786,
                    },
                },
            ],
            responses: {
                200: rebateListResponse,
                400: { description: "Número de proveedor o parámetros inválidos" },
                404: { description: "Sin descuentos para el proveedor" },
            },
        },
    },

    "/rebates/search": {
        get: {
            tags: ["Rebates"],
            summary: "Buscar descuentos con filtros",
            description:
                "Permite filtrar por proveedor, tipo de proveedor, documento, SAP, " +
                "estatus, origen, período y fecha de aplicación.",
            parameters: searchParameters,
            responses: {
                200: rebateListResponse,
                400: { description: "Filtros inválidos" },
                404: { description: "Sin registros para los filtros indicados" },
            },
        },
    },

    "/rebates/export/csv": {
        get: {
            tags: ["Rebates"],
            summary: "Exportar descuentos filtrados a CSV",
            description:
                "Utiliza los mismos filtros que /rebates/search. " +
                "El rango from/to se aplica a postingDate.",
            parameters: searchParameters,
            responses: {
                200: csvResponse,
                400: { description: "Filtros inválidos" },
            },
        },
    },
};