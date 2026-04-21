import type { OpenAPIV3 } from "openapi-types";

export const threeWayMatchSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    ThreeWayMatch: {
        type: "object",
        properties: {
            id: { type: "string" },
            numeroProveedor: { type: "string" },
            ordenCompra: { type: "string" },
            fechaOrdenCompra: { type: "string", format: "date" },
            montoOrdenCompra: { type: "number" },
            recepcion: { type: "string" },
            fechaRecepcion: { type: "string", format: "date" },
            montoRecepcion: { type: "number" },
            serie: { type: "string" },
            folio: { type: "string" },
            uuid: { type: "string" },
            fechaTimbrado: { type: "string", format: "date" },
            montoFactura: { type: "number" },
            numeroNotaCredito: { type: "string" },
            montoNotaCredito: { type: "number" },
            documentoSap: { type: "string" },
            fechaContable: { type: "string", format: "date" },
            montoContable: { type: "number" },
            referenciaPago: { type: "string" },
            fechaPago: { type: "string", format: "date" },
            montoPago: { type: "number" },
            estatus: { type: "integer" },
        },
    },

    RunThreeWayMatchBody: {
        type: "object",
        properties: {
            fechaBase: { type: "string", format: "date" },
            intento: { type: "integer", minimum: 1, maximum: 10, example: 1 },
        },
        additionalProperties: false,
    },

    RunThreeWayMatchResponse: {
        type: "object",
        properties: {
            ok: { type: "boolean", example: true },
            message: { type: "string", example: "ThreeWayMatch executed" },
            fechaBase: { type: "string", format: "date-time" },
            intento: { type: "integer", example: 1 },
        },
        additionalProperties: false,
    },
};