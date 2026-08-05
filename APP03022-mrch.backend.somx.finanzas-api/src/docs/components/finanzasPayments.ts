// FILE: src/docs/components/schemas/finanzasPayments.schemas.ts

import type { OpenAPIV3 } from "openapi-types";

export const finanzasPaymentsSchemas: Record<
    string,
    OpenAPIV3.SchemaObject
> = {
    FinanzasPayment: {
        type: "object",
        properties: {
            finanzasPaymentUuid: {
                type: "string",
                format: "uuid",
                example: "fe4d83bc-52ef-4e3c-a25e-e1f194613837",
            },
            company: {
                type: "integer",
                minimum: 1,
                example: 10,
            },
            documentNumber: {
                type: "string",
                example: "FAC-1001",
            },
            documentReference: {
                type: "string",
                example: "REF-1001",
            },
            vendorNumber: {
                type: "integer",
                minimum: 1,
                example: 12345,
            },
            amount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                example: "1500.00",
            },
            currency: {
                type: "string",
                minLength: 3,
                maxLength: 3,
                default: "MXN",
                example: "MXN",
            },
            documentType: {
                type: "string",
                example: "FAC",
            },
            sapDocument: {
                type: "string",
                example: "490001",
            },
            paymentDate: {
                type: "string",
                format: "date",
                example: "2026-02-27",
            },
            status: {
                type: "integer",
                minimum: 0,
                default: 0,
                example: 0,
            },
            paymentHeaderUuid: {
                type: "string",
                format: "uuid",
                nullable: true,
                example: "ddd4420c-83e0-48d7-8c44-88e9b5987811",
            },
            createdBy: {
                type: "integer",
                nullable: true,
                example: 1001,
            },
            createdAt: {
                type: "string",
                format: "date-time",
                example: "2026-08-04T17:47:30.740Z",
            },
            updatedBy: {
                type: "integer",
                nullable: true,
                example: null,
            },
            updatedAt: {
                type: "string",
                format: "date-time",
                nullable: true,
                example: null,
            },
        },
        required: [
            "finanzasPaymentUuid",
            "company",
            "documentNumber",
            "documentReference",
            "vendorNumber",
            "amount",
            "currency",
            "documentType",
            "sapDocument",
            "paymentDate",
            "status",
            "createdAt",
        ],
    },

    FinanzasPaymentHeader: {
        type: "object",
        properties: {
            paymentHeaderUuid: {
                type: "string",
                format: "uuid",
                example: "ddd4420c-83e0-48d7-8c44-88e9b5987811",
            },
            company: {
                type: "integer",
                minimum: 1,
                example: 10,
            },
            anio: {
                type: "integer",
                minimum: 2000,
                maximum: 9999,
                example: 2026,
            },
            vendorNumber: {
                type: "integer",
                minimum: 1,
                example: 12345,
            },
            currency: {
                type: "string",
                minLength: 3,
                maxLength: 3,
                default: "MXN",
                example: "MXN",
            },
            totalAmount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                example: "1000.00",
            },
            paymentDate: {
                type: "string",
                format: "date",
                example: "2026-02-27",
            },
            status: {
                type: "integer",
                minimum: 0,
                default: 0,
                example: 0,
            },
            createdBy: {
                type: "integer",
                nullable: true,
                example: 1001,
            },
            createdAt: {
                type: "string",
                format: "date-time",
                example: "2026-08-04T17:47:30.740Z",
            },
            updatedBy: {
                type: "integer",
                nullable: true,
                example: null,
            },
            updatedAt: {
                type: "string",
                format: "date-time",
                nullable: true,
                example: null,
            },
        },
        required: [
            "paymentHeaderUuid",
            "company",
            "anio",
            "vendorNumber",
            "currency",
            "totalAmount",
            "paymentDate",
            "status",
            "createdAt",
        ],
    },

    CreateFinanzasPaymentDto: {
        type: "object",
        properties: {
            company: {
                type: "integer",
                minimum: 1,
                example: 10,
            },
            documentNumber: {
                type: "string",
                example: "FAC-1001",
            },
            documentReference: {
                type: "string",
                example: "REF-1001",
            },
            vendorNumber: {
                type: "integer",
                minimum: 1,
                example: 12345,
            },
            amount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                example: "1500.00",
            },
            currency: {
                type: "string",
                minLength: 3,
                maxLength: 3,
                default: "MXN",
                example: "MXN",
            },
            documentType: {
                type: "string",
                example: "FAC",
            },
            sapDocument: {
                type: "string",
                example: "490001",
            },
            paymentDate: {
                type: "string",
                format: "date",
                example: "2026-02-27",
            },
            status: {
                type: "integer",
                minimum: 0,
                default: 0,
                example: 0,
            },
            paymentHeaderUuid: {
                type: "string",
                format: "uuid",
                nullable: true,
                description:
                    "Optional logical identifier used to relate this detail to an existing payment header or external record.",
                example: "ddd4420c-83e0-48d7-8c44-88e9b5987811",
            },
            createdBy: {
                type: "integer",
                nullable: true,
                example: 1001,
            },
        },
        required: [
            "company",
            "documentNumber",
            "documentReference",
            "vendorNumber",
            "amount",
            "documentType",
            "sapDocument",
            "paymentDate",
        ],
    },

    UpdateFinanzasPaymentDto: {
        type: "object",
        minProperties: 2,
        additionalProperties: false,
        description:
            "Partial update payload. finanzasPaymentUuid identifies the payment detail and at least one additional editable field must be supplied. Only supplied fields are updated.",
        properties: {
            finanzasPaymentUuid: {
                type: "string",
                format: "uuid",
                description:
                    "Unique identifier of the payment detail to update.",
                example: "6e734bca-0896-437e-95c1-99b3147abd5f",
            },
            company: {
                type: "integer",
                minimum: 1,
                example: 10,
            },
            documentNumber: {
                type: "string",
                example: "FAC-3001-ACT",
            },
            documentReference: {
                type: "string",
                example: "REF-3001-ACT",
            },
            vendorNumber: {
                type: "integer",
                minimum: 1,
                example: 54321,
            },
            amount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                example: "1750.50",
            },
            currency: {
                type: "string",
                minLength: 3,
                maxLength: 3,
                example: "MXN",
            },
            documentType: {
                type: "string",
                example: "FAC",
            },
            sapDocument: {
                type: "string",
                example: "4903999",
            },
            paymentDate: {
                type: "string",
                format: "date",
                example: "2026-03-01",
            },
            status: {
                type: "integer",
                minimum: 0,
                example: 2,
            },
            paymentHeaderUuid: {
                type: "string",
                format: "uuid",
                nullable: true,
                description:
                    "Allows assigning, changing or removing the logical payment header relation.",
                example: "ddd4420c-83e0-48d7-8c44-88e9b5987812",
            },
            updatedBy: {
                type: "integer",
                nullable: true,
                example: 1002,
            },
        },
        required: ["finanzasPaymentUuid"],
    },

    CreateFinanzasPaymentDetailItemDto: {
        type: "object",
        properties: {
            documentNumber: {
                type: "string",
                example: "FAC-1001",
            },
            documentReference: {
                type: "string",
                example: "REF-1001",
            },
            amount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                example: "1500.00",
            },
            documentType: {
                type: "string",
                example: "FAC",
            },
            sapDocument: {
                type: "string",
                example: "490001",
            },
            paymentLineType: {
                type: "string",
                enum: ["INCOME", "CREDIT_NOTE"],
                example: "INCOME",
            },
            status: {
                type: "integer",
                minimum: 0,
                description:
                    "Optional detail status. If omitted, the detail inherits the header status.",
                example: 0,
            },
        },
        required: [
            "documentNumber",
            "documentReference",
            "amount",
            "documentType",
            "sapDocument",
            "paymentLineType",
        ],
    },

    CreateFinanzasPaymentHeaderWithDetailsDto: {
        type: "object",
        properties: {
            paymentHeaderUuid: {
                type: "string",
                format: "uuid",
                description:
                    "Optional UUID supplied by the consumer. If omitted, the backend generates one.",
                example: "ddd4420c-83e0-48d7-8c44-88e9b5987811",
            },
            company: {
                type: "integer",
                minimum: 1,
                example: 10,
            },
            anio: {
                type: "integer",
                minimum: 2000,
                maximum: 9999,
                example: 2026,
            },
            vendorNumber: {
                type: "integer",
                minimum: 1,
                example: 12345,
            },
            currency: {
                type: "string",
                minLength: 3,
                maxLength: 3,
                default: "MXN",
                example: "MXN",
            },
            totalAmount: {
                type: "string",
                pattern: "^\\d+(\\.\\d{1,2})?$",
                example: "1000.00",
            },
            paymentDate: {
                type: "string",
                format: "date",
                example: "2026-02-27",
            },
            status: {
                type: "integer",
                minimum: 0,
                default: 0,
                example: 0,
            },
            createdBy: {
                type: "integer",
                nullable: true,
                example: 1001,
            },
            details: {
                type: "array",
                description:
                    "Optional payment detail lines. Validation of INCOME and total breakdown is applied only when details are supplied.",
                default: [],
                items: {
                    $ref: "#/components/schemas/CreateFinanzasPaymentDetailItemDto",
                },
            },
        },
        required: [
            "company",
            "anio",
            "vendorNumber",
            "totalAmount",
            "paymentDate",
        ],
    },

    FinanzasPaymentHeaderWithDetailsResponse: {
        type: "object",
        properties: {
            header: {
                $ref: "#/components/schemas/FinanzasPaymentHeader",
            },
            details: {
                type: "array",
                items: {
                    $ref: "#/components/schemas/FinanzasPayment",
                },
            },
            summary: {
                type: "object",
                properties: {
                    paymentHeaderUuid: {
                        type: "string",
                        format: "uuid",
                        example: "ddd4420c-83e0-48d7-8c44-88e9b5987811",
                    },
                    totalDetails: {
                        type: "integer",
                        example: 2,
                    },
                    headerTotal: {
                        type: "string",
                        example: "1000.00",
                    },
                    expectedTotal: {
                        type: "string",
                        nullable: true,
                        description:
                            "Null when the header is created without detail lines.",
                        example: "1000.00",
                    },
                },
            },
        },
        required: ["header", "details", "summary"],
    },

    FinanzasPaymentHeaderGetWithDetailsResponse: {
        type: "object",
        properties: {
            header: {
                $ref: "#/components/schemas/FinanzasPaymentHeader",
            },
            details: {
                type: "array",
                items: {
                    $ref: "#/components/schemas/FinanzasPayment",
                },
            },
            summary: {
                type: "object",
                properties: {
                    paymentHeaderUuid: {
                        type: "string",
                        format: "uuid",
                    },
                    totalDetails: {
                        type: "integer",
                        example: 2,
                    },
                    totalAmountHeader: {
                        type: "string",
                        example: "1000.00",
                    },
                    totalAmountDetail: {
                        type: "string",
                        example: "2000.00",
                    },
                    amountsMatch: {
                        type: "boolean",
                        example: false,
                    },
                },
            },
        },
        required: ["header", "details", "summary"],
    },

    FinanzasPaymentHeaderGetWithDetailsPaginatedResponse: {
        type: "object",
        properties: {
            header: {
                $ref: "#/components/schemas/FinanzasPaymentHeader",
            },
            detailsPage: {
                type: "object",
                properties: {
                    content: {
                        type: "array",
                        items: {
                            $ref: "#/components/schemas/FinanzasPayment",
                        },
                    },
                    totalElements: {
                        type: "integer",
                        example: 25,
                    },
                    numberOfElements: {
                        type: "integer",
                        example: 10,
                    },
                    totalPages: {
                        type: "integer",
                        example: 3,
                    },
                    pageNumber: {
                        type: "integer",
                        example: 1,
                    },
                    pageSize: {
                        type: "integer",
                        example: 10,
                    },
                },
                required: [
                    "content",
                    "totalElements",
                    "numberOfElements",
                    "totalPages",
                    "pageNumber",
                    "pageSize",
                ],
            },
            summary: {
                type: "object",
                properties: {
                    paymentHeaderUuid: {
                        type: "string",
                        format: "uuid",
                    },
                    totalAmountHeader: {
                        type: "string",
                        example: "1000.00",
                    },
                },
            },
        },
        required: ["header", "detailsPage", "summary"],
    },
};