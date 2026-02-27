// src/docs/paths/rebate.ts
import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
const UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const rebatePaths: OpenAPIV3.PathsObject = {
    "/rebates": {
        get: {
            tags: ["Rebates"],
            summary: "List all rebates",
            description: "Retrieve all rebates with optional relations to stamped rebates",
            responses: {
                200: {
                    description: "List of rebates",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/Rebate" }
                            },
                        }
                    }
                }
            }
        },

        post: {
            tags: ["Rebates"],
            summary: "Create new rebate",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/CreateRebateDto" },
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
                        }
                    }
                }
            },
            responses: {
                201: {
                    description: "Created",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Rebate" },
                        }
                    }
                },
                400: { description: "Invalid input data" }
            }
        }
    },

    "/rebates/{id}": {
        get: {
            tags: ["Rebates"],
            summary: "Get rebate by ID",
            parameters: [
                {
                    in: "path",
                    name: "id",
                    required: true,
                    description: "Rebate UUID",
                    schema: {
                        type: "string",
                        pattern: UUID_PATTERN,
                        example: UUID_EXAMPLE
                    }
                }
            ],
            responses: {
                200: {
                    description: "Rebate found",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Rebate" },
                        }
                    }
                },
                404: { description: "Rebate not found" }
            }
        },

        put: {
            tags: ["Rebates"],
            summary: "Update rebate",
            parameters: [
                {
                    in: "path",
                    name: "id",
                    required: true,
                    description: "Rebate UUID",
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
                        schema: { $ref: "#/components/schemas/UpdateRebateDto" },
                        example: {
                            amount: "20000.00",
                            status: 2,
                            updatedBy: 1,
                        }
                    }
                }
            },
            responses: {
                200: {
                    description: "Updated",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/Rebate" },
                        }
                    }
                },
                404: { description: "Rebate not found" }
            }
        },

        delete: {
            tags: ["Rebates"],
            summary: "Delete rebate",
            parameters: [
                {
                    in: "path",
                    name: "id",
                    required: true,
                    description: "Rebate UUID",
                    schema: {
                        type: "string",
                        pattern: UUID_PATTERN,
                        example: UUID_EXAMPLE
                    }
                }
            ],
            responses: {
                204: { description: "Deleted" },
                404: { description: "Rebate not found" }
            }
        }
    },

    "/rebates/published": {
        get: {
            tags: ["Rebates"],
            summary: "Get published rebates",
            description: "Retrieve all rebates with status = 2 (published)",
            responses: {
                200: {
                    description: "List of published rebates",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/Rebate" }
                            },
                        }
                    }
                }
            }
        }
    },

    "/rebates/published/export/csv": {
        get: {
            tags: ["Rebates"],
            summary: "Export published rebates to CSV",
            description: "Generate CSV file with all published rebates (status = 2)",
            responses: {
                200: {
                    description: "CSV file generated",
                    content: {
                        "text/csv": {
                            schema: {
                                type: "string",
                                format: "binary"
                            }
                        }
                    },
                    headers: {
                        "Content-Disposition": {
                            description: "Attachment filename",
                            schema: {
                                type: "string",
                                example: "attachment; filename=\"published-rebates-2025-10-07.csv\""
                            }
                        }
                    }
                }
            }
        }
    },

    "/rebates/vendor/{vendorNumber}": {
        get: {
            tags: ["Rebates"],
            summary: "Get rebates by vendor number",
            description: "Retrieve all rebates for a specific vendor",
            parameters: [
                {
                    in: "path",
                    name: "vendorNumber",
                    required: true,
                    description: "Vendor identifier",
                    schema: {
                        type: "integer",
                        example: 1001
                    }
                }
            ],
            responses: {
                200: {
                    description: "List of rebates for vendor",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/Rebate" }
                            },
                        }
                    }
                },
                400: { description: "Invalid vendor number" }
            }
        }
    },

    "/rebates/search": {
        get: {
            tags: ["Rebates"],
            summary: "Search rebates with dynamic filters (Criteria API equivalent)",
            description: "Advanced search endpoint supporting multiple filters.",
            parameters: [
                {
                    in: "query",
                    name: "vendorNumber",
                    required: false,
                    description: "Filter by exact vendor number",
                    schema: { type: "integer", example: 1001 }
                },
                {
                    in: "query",
                    name: "documentNumber",
                    required: false,
                    description: "Filter by document number (LIKE search - partial match)",
                    schema: { type: "string", example: "SR-2025" }
                },
                {
                    in: "query",
                    name: "sapDocument",
                    required: false,
                    description: "Filter by SAP document (LIKE search - partial match)",
                    schema: { type: "string", example: "SAP-001" }
                },
                {
                    in: "query",
                    name: "status",
                    required: false,
                    description: "Filter by status code (1=pending, 2=published)",
                    schema: { type: "integer", example: 1 }
                },
                {
                    in: "query",
                    name: "periodId",
                    required: false,
                    description: "Filter by period identifier",
                    schema: { type: "integer", example: 202501 }
                },
                {
                    in: "query",
                    name: "limit",
                    required: false,
                    description: "Results per page (max 1000)",
                    schema: { type: "integer", default: 20, minimum: 1, maximum: 1000 }
                },
                {
                    in: "query",
                    name: "page",
                    required: false,
                    description: "Page number (0-indexed)",
                    schema: { type: "integer", default: 0, minimum: 0 }
                }
            ],
            responses: {
                200: {
                    description: "List of matching rebates",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/Rebate" }
                            },
                        }
                    }
                },
                400: { description: "Invalid filter parameters" }
            }
        }
    },

    "/rebates/export/csv": {
        get: {
            tags: ["Rebates"],
            summary: "Export filtered rebates to CSV",
            description: "Generate CSV file with rebates matching the provided filters. Supports all search filters from /rebates/search endpoint.",
            parameters: [
                {
                    in: "query",
                    name: "vendorNumber",
                    required: false,
                    description: "Filter by exact vendor number",
                    schema: { type: "integer" }
                },
                {
                    in: "query",
                    name: "documentNumber",
                    required: false,
                    description: "Filter by document number (LIKE search)",
                    schema: { type: "string" }
                },
                {
                    in: "query",
                    name: "status",
                    required: false,
                    description: "Filter by status code",
                    schema: { type: "integer" }
                },
                {
                    in: "query",
                    name: "periodId",
                    required: false,
                    description: "Filter by period identifier",
                    schema: { type: "integer" }
                },
                {
                    in: "query",
                    name: "dueDateFrom",
                    required: false,
                    schema: { type: "string", format: "date" }
                },
                {
                    in: "query",
                    name: "dueDateTo",
                    required: false,
                    schema: { type: "string", format: "date" }
                }
            ],
            responses: {
                200: {
                    description: "CSV file generated",
                    content: {
                        "text/csv": {
                            schema: {
                                type: "string",
                                format: "binary"
                            }
                        }
                    },
                    headers: {
                        "Content-Disposition": {
                            description: "Attachment filename",
                            schema: {
                                type: "string",
                                example: "attachment; filename=\"filtered-rebates-2025-10-07.csv\""
                            }
                        }
                    }
                }
            }
        }
    }
};
