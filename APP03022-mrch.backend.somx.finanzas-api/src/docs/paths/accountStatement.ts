import type { OpenAPIV3 } from "openapi-types";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const accountStatementPaths: OpenAPIV3.PathsObject = {
    "/account-statement": {
        get: {
            tags: ["AccountStatement"],
            summary: "List account statements",
            parameters: [
                {
                    in: "query",
                    name: "vendorNumber",
                    required: false,
                    schema: { type: "integer" },
                },
                {
                    in: "query",
                    name: "year",
                    required: true,
                    schema: { type: "integer", minimum: 2026 },
                },
                {
                    in: "query",
                    name: "month",
                    required: false,
                    schema: {
                        oneOf: [
                            { type: "integer", minimum: 1, maximum: 12 },
                            { type: "string", enum: ["all"] },
                        ],
                    },
                },
                {
                    in: "query",
                    name: "page",
                    required: false,
                    schema: { type: "integer", minimum: 1, default: 1 },
                },
                {
                    in: "query",
                    name: "pageSize",
                    required: false,
                    schema: { type: "integer", minimum: 1, maximum: 100, default: 10 },
                },
            ],
            responses: {
                200: {
                    description: "Paged list",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AccountStatementListResponse" },
                        },
                    },
                },
                400: {
                    description: "Validation error",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Error" },
                        },
                    },
                },
            },
        },
    },
    "/account-statement/{uuid}/pdf": {
        get: {
            tags: ["AccountStatement"],
            summary: "Download account statement PDF",
            parameters: [
                {
                    in: "path",
                    name: "uuid",
                    required: true,
                    schema: { type: "string", pattern: UUID_PATTERN },
                },
            ],
            responses: {
                200: {
                    description: "PDF binary",
                    content: {
                        "application/pdf": {
                            schema: { type: "string", format: "binary" },
                        },
                    },
                },
                404: {
                    description: "Not found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Error" },
                        },
                    },
                },
            },
        },
    },
    "/account-statement/{uuid}/confirm-review": {
        patch: {
            tags: ["AccountStatement"],
            summary: "Confirm review",
            parameters: [
                {
                    in: "path",
                    name: "uuid",
                    required: true,
                    schema: { type: "string", pattern: UUID_PATTERN },
                },
            ],
            responses: {
                200: {
                    description: "Updated",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AccountStatementDetail" },
                        },
                    },
                },
                404: {
                    description: "Not found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Error" },
                        },
                    },
                },
            },
        },
    },
    "/account-statement/{uuid}/request-review": {
        patch: {
            tags: ["AccountStatement"],
            summary: "Request review (reject)",
            parameters: [
                {
                    in: "path",
                    name: "uuid",
                    required: true,
                    schema: { type: "string", pattern: UUID_PATTERN },
                },
            ],
            responses: {
                200: {
                    description: "Updated",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AccountStatementDetail" },
                        },
                    },
                },
                404: {
                    description: "Not found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Error" },
                        },
                    },
                },
            },
        },
    },
    "/account-statement/{uuid}": {
        get: {
            tags: ["AccountStatement"],
            summary: "Get account statement by UUID",
            parameters: [
                {
                    in: "path",
                    name: "uuid",
                    required: true,
                    schema: { type: "string", pattern: UUID_PATTERN },
                },
            ],
            responses: {
                200: {
                    description: "Found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AccountStatementDetail" },
                        },
                    },
                },
                404: {
                    description: "Not found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Error" },
                        },
                    },
                },
            },
        },
    },
};
