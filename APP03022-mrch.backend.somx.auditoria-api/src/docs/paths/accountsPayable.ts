// src/docs/paths/accountsPayable.ts
import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
// Regex UUID (v4-friendly) en mayúsculas/minúsculas
const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const accountsPayablePaths: OpenAPIV3.PathsObject = {
    "/accounts": {
        get: {
            tags: ["AccountsPayable"],
            summary: "List accounts payable",
            parameters: [
                {
                    in: "query",
                    name: "status",
                    required: false,
                    description: "Status code (e.g. 1=open, 2=closed)",
                    schema: { type: "integer", example: 1 }
                },
                {
                    in: "query",
                    name: "vendorNumber",
                    required: false,
                    description: "Vendor number identifier",
                    schema: { type: "integer", example: 12345 }
                }
            ],
            responses: {
                200: {
                    description: "List of accounts payable",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/AccountsPayable" }
                            },
                            examples: {
                                sample: {
                                    value: [
                                        {
                                            id: UUID_EXAMPLE,
                                            orderNumber: "PO-1001",
                                            vendorNumber: 12345,
                                            sourceId: 7,
                                            totalAmount: "1520.75",
                                            currency: "USD",
                                            status: 1,
                                            orderDate: "2025-09-30T10:00:00.000Z",
                                            deliveryDate: "2025-10-15T10:00:00.000Z",
                                            terms: "Net 30 days",
                                            createdBy: 101,
                                            createdAt: "2025-09-30T11:00:00.000Z",
                                            updatedBy: null,
                                            updatedAt: null
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            }
        },

        post: {
            tags: ["AccountsPayable"],
            summary: "Create account payable",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/CreateAccountsPayableDto" },
                        example: {
                            orderNumber: "PO-1001",
                            vendorNumber: 12345,
                            sourceId: 7,
                            totalAmount: "1520.75",
                            currency: "USD",
                            status: 1,
                            orderDate: "2025-09-30T10:00:00.000Z",
                            deliveryDate: "2025-10-15T10:00:00.000Z",
                            terms: "Net 30 days",
                            createdBy: 101
                        }
                    }
                }
            },
            responses: {
                201: {
                    description: "Created",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AccountsPayable" },
                            example: {
                                id: UUID_EXAMPLE,
                                orderNumber: "PO-1001",
                                vendorNumber: 12345,
                                sourceId: 7,
                                totalAmount: "1520.75",
                                currency: "USD",
                                status: 1,
                                orderDate: "2025-09-30T10:00:00.000Z",
                                deliveryDate: "2025-10-15T10:00:00.000Z",
                                terms: "Net 30 days",
                                createdBy: 101,
                                createdAt: "2025-09-30T11:00:00.000Z",
                                updatedBy: null,
                                updatedAt: null
                            }
                        }
                    }
                }
            }
        }
    },

    "/accounts/{id}": {
        get: {
            tags: ["AccountsPayable"],
            summary: "Get account payable by ID",
            parameters: [
                {
                    in: "path",
                    name: "id",
                    required: true,
                    description: "Purchase order UUID",
                    schema: {
                        type: "string",
                        pattern: UUID_PATTERN,
                        example: UUID_EXAMPLE
                    }
                }
            ],
            responses: {
                200: {
                    description: "Found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AccountsPayable" },
                            example: {
                                id: UUID_EXAMPLE,
                                orderNumber: "PO-1001",
                                vendorNumber: 12345,
                                sourceId: 7,
                                totalAmount: "1520.75",
                                currency: "USD",
                                status: 1,
                                orderDate: "2025-09-30T10:00:00.000Z",
                                deliveryDate: "2025-10-15T10:00:00.000Z",
                                terms: "Net 30 days",
                                createdBy: 101,
                                createdAt: "2025-09-30T11:00:00.000Z",
                                updatedBy: null,
                                updatedAt: null
                            }
                        }
                    }
                },
                404: { description: "Not found" }
            }
        },

        patch: {
            tags: ["AccountsPayable"],
            summary: "Update account payable",
            parameters: [
                {
                    in: "path",
                    name: "id",
                    required: true,
                    description: "Purchase order UUID",
                    schema: {
                        type: "string",
                        pattern: UUID_PATTERN,
                        example: UUID_EXAMPLE
                    }
                }
            ],
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/UpdateAccountsPayableDto" },
                        example: {
                            status: 2,
                            totalAmount: "1800.00",
                            deliveryDate: "2025-11-01T10:00:00.000Z",
                            terms: "Updated payment terms",
                            updatedBy: 202
                        }
                    }
                }
            },
            responses: {
                200: {
                    description: "Updated",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/AccountsPayable" },
                            example: {
                                id: UUID_EXAMPLE,
                                orderNumber: "PO-1001",
                                vendorNumber: 12345,
                                sourceId: 7,
                                totalAmount: "1800.00",
                                currency: "USD",
                                status: 2,
                                orderDate: "2025-09-30T10:00:00.000Z",
                                deliveryDate: "2025-11-01T10:00:00.000Z",
                                terms: "Updated payment terms",
                                createdBy: 101,
                                createdAt: "2025-09-30T11:00:00.000Z",
                                updatedBy: 202,
                                updatedAt: "2025-10-10T09:30:00.000Z"
                            }
                        }
                    }
                },
                404: { description: "Not found" }
            }
        },

        delete: {
            tags: ["AccountsPayable"],
            summary: "Remove account payable",
            parameters: [
                {
                    in: "path",
                    name: "id",
                    required: true,
                    description: "Purchase order UUID",
                    schema: {
                        type: "string",
                        pattern: UUID_PATTERN,
                        example: UUID_EXAMPLE
                    }
                }
            ],
            responses: {
                204: { description: "Deleted" },
                404: { description: "Not found" }
            }
        }
    }
};
