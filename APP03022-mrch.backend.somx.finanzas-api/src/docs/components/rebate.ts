import type { OpenAPIV3 } from "openapi-types";

const statusDescription =
    "Estatus mostrado en Finanzas: 1=Pendiente, 2=Aprobado, 3=Rechazado";

export const rebateSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    Rebate: {
        type: "object",
        properties: {
            rebateId: {
                type: "string",
                format: "uuid",
                description: "Identificador interno del descuento comercial",
            },
            documentNumber: {
                type: "string",
                description: "Número de documento",
            },
            referenceNumber: {
                type: "string",
                nullable: true,
                description: "Referencia del documento",
            },
            sapDocument: {
                type: "string",
                nullable: true,
                description: "Documento SAP",
            },
            vendorNumber: {
                type: "integer",
                nullable: true,
                description: "Número de proveedor",
            },
            amount: {
                type: "string",
                nullable: true,
                description: "Importe expresado como cadena decimal",
                example: "114.81",
            },
            source: {
                type: "integer",
                nullable: true,
                description: "Identificador del tipo de rebate",
            },
            periodId: {
                type: "integer",
                nullable: true,
                description: "Identificador del período",
            },
            dueDate: {
                type: "string",
                format: "date-time",
                nullable: true,
                description: "Fecha de vencimiento",
            },
            postingDate: {
                type: "string",
                format: "date-time",
                nullable: true,
                description: "Fecha de aplicación",
            },
            status: {
                type: "integer",
                nullable: true,
                description: statusDescription,
            },
            createdBy: {
                nullable: true,
                oneOf: [
                    { type: "integer" },
                    { type: "string" },
                ],
                description: "Usuario de creación; bigint puede serializarse como texto",
            },
            createdAt: {
                type: "string",
                format: "date-time",
                description: "Fecha de creación",
            },
            updatedBy: {
                nullable: true,
                oneOf: [
                    { type: "integer" },
                    { type: "string" },
                ],
                description: "Usuario de actualización",
            },
            updatedAt: {
                type: "string",
                format: "date-time",
                nullable: true,
                description: "Fecha de actualización",
            },
            stampedRebate: {
                type: "object",
                nullable: true,
                allOf: [
                    { $ref: "#/components/schemas/StampedRebate" },
                ],
            },
        },
        required: [
            "rebateId",
            "documentNumber",
            "createdAt",
        ],
    },

    CreateRebateDto: {
        type: "object",
        properties: {
            documentNumber: {
                type: "string",
                minLength: 1,
                maxLength: 100,
            },
            referenceNumber: {
                type: "string",
                minLength: 1,
                maxLength: 100,
            },
            sapDocument: {
                type: "string",
                minLength: 1,
                maxLength: 50,
            },
            vendorNumber: {
                type: "integer",
                description: "Número de proveedor",
            },
            amount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                description: "Importe decimal con hasta dos decimales",
                example: "114.81",
            },
            source: {
                type: "integer",
                description: "Identificador del tipo de rebate",
            },
            periodId: {
                type: "integer",
                description: "Identificador del período",
            },
            dueDate: {
                type: "string",
                format: "date-time",
                description: "Fecha de vencimiento",
            },
            postingDate: {
                type: "string",
                format: "date-time",
                description: "Fecha de aplicación",
            },
            status: {
                type: "integer",
                default: 1,
                description: statusDescription,
            },
            createdBy: {
                type: "integer",
                nullable: true,
                description: "Usuario de creación",
            },
        },
        required: [
            "documentNumber",
            "referenceNumber",
            "sapDocument",
            "vendorNumber",
            "amount",
            "source",
            "periodId",
            "dueDate",
            "postingDate",
        ],
    },

    UpdateRebateDto: {
        type: "object",
        additionalProperties: false,
        properties: {
            documentNumber: {
                type: "string",
                minLength: 1,
                maxLength: 100,
            },
            referenceNumber: {
                type: "string",
                minLength: 1,
                maxLength: 100,
            },
            sapDocument: {
                type: "string",
                minLength: 1,
                maxLength: 50,
            },
            vendorNumber: {
                type: "integer",
            },
            amount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                example: "114.81",
            },
            source: {
                type: "integer",
            },
            periodId: {
                type: "integer",
            },
            dueDate: {
                type: "string",
                format: "date-time",
            },
            postingDate: {
                type: "string",
                format: "date-time",
            },
            status: {
                type: "integer",
                description: statusDescription,
            },
            updatedBy: {
                type: "integer",
            },
        },
    },

    RebateFilterDto: {
        type: "object",
        properties: {
            vendorNumber: {
                type: "integer",
                description: "Número de proveedor",
            },
            supplierType: {
                type: "integer",
                description: "Identificador del tipo de proveedor",
            },
            documentNumber: {
                type: "string",
                description: "Coincidencia parcial del documento",
            },
            sapDocument: {
                type: "string",
                description: "Coincidencia parcial del documento SAP",
            },
            status: {
                type: "integer",
                description: statusDescription,
            },
            source: {
                type: "integer",
                description: "Identificador del tipo de rebate",
            },
            periodId: {
                type: "integer",
            },
            from: {
                type: "string",
                format: "date-time",
                description: "Inicio del rango de fecha de aplicación",
            },
            to: {
                type: "string",
                format: "date-time",
                description: "Fin del rango de fecha de aplicación",
            },
            limit: {
                type: "integer",
                default: 20,
                minimum: 1,
                maximum: 1000,
            },
            page: {
                type: "integer",
                default: 0,
                minimum: 0,
                description: "Página comenzando en cero",
            },
        },
    },

    StampedRebate: {
        type: "object",
        properties: {
            stampedRebateUuid: {
                type: "string",
                format: "uuid",
                description:
                    "Identificador interno de la relación; no es el UUID fiscal de la NC",
            },
            documentNumber: {
                type: "string",
            },
            referenceNumber: {
                type: "string",
            },
            status: {
                type: "integer",
                description: "Estatus de la relación fiscal",
            },
            invoiceFiscalUuid: {
                type: "string",
                format: "uuid",
                nullable: true,
                description: "UUID fiscal de la factura original",
            },
            ncFiscalUuid: {
                type: "string",
                format: "uuid",
                nullable: true,
                description: "UUID fiscal de la NC relacionada",
            },
            createdBy: {
                nullable: true,
                oneOf: [
                    { type: "integer" },
                    { type: "string" },
                ],
            },
            createdAt: {
                type: "string",
                format: "date-time",
            },
            updatedBy: {
                nullable: true,
                oneOf: [
                    { type: "integer" },
                    { type: "string" },
                ],
            },
            updatedAt: {
                type: "string",
                format: "date-time",
                nullable: true,
            },
        },
        required: [
            "stampedRebateUuid",
            "documentNumber",
            "referenceNumber",
            "status",
            "createdAt",
        ],
    },

    RebateCreditNote: {
        type: "object",
        description: "Datos fiscales de la nota de crédito mostrada en el grid",
        properties: {
            id: {
                type: "string",
                format: "uuid",
                description: "Identificador interno tenant_fiscal.invoice.invoice_uuid",
            },
            uuid: {
                type: "string",
                format: "uuid",
                description: "UUID fiscal de la NC; se utiliza para visualizarla",
            },
            registeredAt: {
                type: "string",
                format: "date-time",
                nullable: true,
                description: "Fecha de registro de la NC en el repositorio fiscal",
            },
            amount: {
                type: "string",
                nullable: true,
                description: "Importe total de la NC, tomado de invoice.total",
                example: "114.81",
            },
            series: {
                type: "string",
                nullable: true,
                description: "Serie de la NC",
            },
            folio: {
                type: "string",
                nullable: true,
                description: "Folio de la NC",
                example: "9200913182",
            },
        },
        required: [
            "id",
            "uuid",
            "registeredAt",
            "amount",
            "series",
            "folio",
        ],
    },

    RebateFiscalDetail: {
        type: "object",
        description:
            "Detalle fiscal del descuento. La respuesta es un objeto directo, sin envoltorio data.",
        properties: {
            rebateId: {
                type: "string",
                format: "uuid",
                description: "UUID interno del descuento consultado",
            },
            vendorNumber: {
                type: "integer",
                nullable: true,
                description: "Número de proveedor del descuento",
            },
            invoiceFiscalUuid: {
                type: "string",
                format: "uuid",
                nullable: true,
                description: "UUID fiscal de la factura original",
            },
            ncFiscalUuid: {
                type: "string",
                format: "uuid",
                nullable: true,
                description: "UUID fiscal de la NC seleccionada en la relación",
            },
            creditNotes: {
                type: "array",
                description:
                    "Notas encontradas por el UUID fiscal guardado. Vacío cuando no existe una NC disponible.",
                items: {
                    $ref: "#/components/schemas/RebateCreditNote",
                },
            },
            message: {
                type: "string",
                nullable: true,
                description:
                    "Explica la ausencia de relación o de datos fiscales. Null cuando se encontró la NC.",
            },
        },
        required: [
            "rebateId",
            "vendorNumber",
            "invoiceFiscalUuid",
            "ncFiscalUuid",
            "creditNotes",
            "message",
        ],
    },
};