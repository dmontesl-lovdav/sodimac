// FILE: src/docs/paths/finanzasPayments.paths.ts
import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6aa";
const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const finanzasPaymentsPaths: OpenAPIV3.PathsObject = {
    "/finanzas-payments": {
        get: {
            tags: ["FinanzasPayments"],
            summary: "List provider payment details (legacy detail table)",
            description: "Returns paginated payment detail records from payment_detail.",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: {
                            type: "object",
                            properties: {
                                createdAtInitial: { type: "string", format: "date-time" },
                                createdAtEnd: { type: "string", format: "date-time" },
                                vendorNumber: { type: "integer" },
                                finanzasPaymentUuid: { type: "string", format: "uuid" },
                                paymentDate: { type: "string", format: "date" },
                                documentNumber: { type: "string" },
                                sapDocument: { type: "string" },
                                pageNumber: { type: "integer", example: 1 },
                                pageSize: { type: "integer", example: 10 },
                            },
                            required: ["createdAtInitial", "createdAtEnd", "pageNumber", "pageSize"],
                        },
                    },
                },
            },
            responses: {
                200: {
                    description: "Paginated detail payments",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    success: { type: "boolean", example: true },
                                    data: {
                                        type: "object",
                                        properties: {
                                            content: {
                                                type: "array",
                                                items: { $ref: "#/components/schemas/FinanzasPayment" },
                                            },
                                            totalElements: { type: "integer" },
                                            numberOfElements: { type: "integer" },
                                            totalPages: { type: "integer" },
                                            pageNumber: { type: "integer" },
                                            pageSize: { type: "integer" },
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },

        post: {
            tags: ["FinanzasPayments"],
            summary: "Create single payment detail record",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/CreateFinanzasPaymentDto" },
                    },
                },
            },
            responses: {
                201: {
                    description: "Payment detail created",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/FinanzasPayment" },
                        },
                    },
                },
            },
        },

        patch: {
            tags: ["FinanzasPayments"],
            summary: "Update payment detail status by filters",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/UpdateFinanzasPaymentDto" },
                    },
                },
            },
            responses: {
                200: {
                    description: "Payment detail status updated",
                },
            },
        },
    },

    "/finanzas-payments/header-with-details": {
        post: {
            tags: ["FinanzasPayments"],
            summary: "Create grouped payment header with detail lines",
            description:
                "Creates a payment header and related payment detail lines in a single transaction. Validates INCOME presence and header total vs detail breakdown.",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/CreateFinanzasPaymentHeaderWithDetailsDto" },
                    },
                },
            },
            responses: {
                201: {
                    description: "Header and details created successfully",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/FinanzasPaymentHeaderWithDetailsResponse" },
                        },
                    },
                },
                400: {
                    description: "Validation error (WRN7024 / WRN7025)",
                },
            },
        },
    },

    "/finanzas-payments/header-with-details/{paymentHeaderUuid}": {
        get: {
            tags: ["FinanzasPayments"],
            summary: "Get payment header with detail lines",
            description:
                "Returns header and details by paymentHeaderUuid. Supports optional pagination for details.",
            parameters: [
                {
                    in: "path",
                    name: "paymentHeaderUuid",
                    required: true,
                    description: "Grouped payment header identifier",
                    schema: {
                        type: "string",
                        pattern: UUID_PATTERN,
                        example: UUID_EXAMPLE,
                    },
                },
                {
                    in: "query",
                    name: "pageNumber",
                    required: false,
                    schema: { type: "integer", minimum: 1, example: 1 },
                    description: "Optional page number for detail pagination",
                },
                {
                    in: "query",
                    name: "pageSize",
                    required: false,
                    schema: { type: "integer", minimum: 1, maximum: 200, example: 10 },
                    description: "Optional page size for detail pagination",
                },
            ],
            responses: {
                200: {
                    description: "Header with details (full or paginated)",
                    content: {
                        "application/json": {
                            schema: {
                                oneOf: [
                                    { $ref: "#/components/schemas/FinanzasPaymentHeaderGetWithDetailsResponse" },
                                    { $ref: "#/components/schemas/FinanzasPaymentHeaderGetWithDetailsPaginatedResponse" },
                                ],
                            },
                        },
                    },
                },
                404: {
                    description: "Header not found",
                },
            },
        },
    },
};