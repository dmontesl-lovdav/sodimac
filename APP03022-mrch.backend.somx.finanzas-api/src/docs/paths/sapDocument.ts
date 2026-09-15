import type { OpenAPIV3 } from "openapi-types";

const fiscalUuidQuery: OpenAPIV3.ParameterObject = {
    in: "query",
    name: "fiscalUuid",
    required: false,
    schema: { type: "string", format: "uuid" },
    description: "Filtra documentos SAP asociados a un folio fiscal (UUID SAT)",
};

export const sapDocumentPaths: OpenAPIV3.PathsObject = {
    "/sap-documents": {
        get: {
            tags: ["SapDocument"],
            summary: "List SAP documents",
            parameters: [
                fiscalUuidQuery,
                { in: "query", name: "sapStatus", schema: { type: "integer" } },
                { in: "query", name: "vendorNumber", schema: { type: "integer" } },
                { in: "query", name: "documentType", schema: { type: "string" } },
                { in: "query", name: "source", schema: { type: "integer" } },
                { in: "query", name: "limit", schema: { type: "integer", example: 100 } },
            ],
            responses: {
                200: {
                    description: "List of SAP documents",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/SapDocument" },
                            },
                        },
                    },
                },
                404: { description: "No records found (when fiscalUuid is not provided)" },
            },
        },
        post: {
            tags: ["SapDocument"],
            summary: "Create SAP document with one or more fiscal UUIDs",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/CreateSapDocumentDto" },
                    },
                },
            },
            responses: {
                201: {
                    description: "Created",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/SapDocument" },
                        },
                    },
                },
                400: { description: "Validation error" },
            },
        },
    },
    "/sap-documents/by-fiscal-uuid/{fiscalUuid}": {
        get: {
            tags: ["SapDocument"],
            summary: "List SAP documents related to a fiscal UUID",
            parameters: [
                {
                    in: "path",
                    name: "fiscalUuid",
                    required: true,
                    schema: { type: "string", format: "uuid" },
                },
            ],
            responses: {
                200: {
                    description: "Related SAP documents (empty array if none)",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/SapDocument" },
                            },
                        },
                    },
                },
            },
        },
    },
    "/sap-documents/{uuid}": {
        get: {
            tags: ["SapDocument"],
            summary: "Get SAP document by sap_document_uuid",
            parameters: [
                {
                    in: "path",
                    name: "uuid",
                    required: true,
                    schema: { type: "string", format: "uuid" },
                },
            ],
            responses: {
                200: {
                    description: "Found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/SapDocument" },
                        },
                    },
                },
                404: { description: "Not found" },
            },
        },
        put: {
            tags: ["SapDocument"],
            summary: "Update SAP document (optional extra fiscalUuids are appended)",
            parameters: [
                {
                    in: "path",
                    name: "uuid",
                    required: true,
                    schema: { type: "string", format: "uuid" },
                },
            ],
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/UpdateSapDocumentDto" },
                    },
                },
            },
            responses: {
                200: {
                    description: "Updated",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/SapDocument" },
                        },
                    },
                },
                404: { description: "Not found" },
            },
        },
        delete: {
            tags: ["SapDocument"],
            summary: "Delete SAP document",
            parameters: [
                {
                    in: "path",
                    name: "uuid",
                    required: true,
                    schema: { type: "string", format: "uuid" },
                },
            ],
            responses: {
                204: { description: "Deleted" },
                404: { description: "Not found" },
            },
        },
    },
};
