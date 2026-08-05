// FILE: src/docs/paths/finanzasPayments.paths.ts

import type { OpenAPIV3 } from "openapi-types";

const PAYMENT_HEADER_UUID_EXAMPLE =
    "ddd4420c-83e0-48d7-8c44-88e9b5987811";

const FINANZAS_PAYMENT_UUID_EXAMPLE =
    "6e734bca-0896-437e-95c1-99b3147abd5f";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const finanzasPaymentsPaths: OpenAPIV3.PathsObject = {
    "/finanzas-payment": {
        get: {
            tags: ["FinanzasPayments"],
            summary: "List provider payment details",
            description:
                "Returns paginated payment detail records from payment_detail. Filters are sent as query parameters.",
            parameters: [
                {
                    in: "query",
                    name: "createdAtInitial",
                    required: true,
                    description: "Initial creation date",
                    schema: {
                        type: "string",
                        format: "date",
                        example: "2026-03-04",
                    },
                },
                {
                    in: "query",
                    name: "createdAtEnd",
                    required: true,
                    description: "Final creation date",
                    schema: {
                        type: "string",
                        format: "date",
                        example: "2026-07-30",
                    },
                },
                {
                    in: "query",
                    name: "vendorNumber",
                    required: false,
                    schema: {
                        type: "integer",
                        minimum: 1,
                        example: 12345,
                    },
                },
                {
                    in: "query",
                    name: "finanzasPaymentUuid",
                    required: false,
                    schema: {
                        type: "string",
                        format: "uuid",
                        pattern: UUID_PATTERN,
                        example: FINANZAS_PAYMENT_UUID_EXAMPLE,
                    },
                },
                {
                    in: "query",
                    name: "paymentDate",
                    required: false,
                    schema: {
                        type: "string",
                        format: "date",
                        example: "2026-02-27",
                    },
                },
                {
                    in: "query",
                    name: "documentNumber",
                    required: false,
                    schema: {
                        type: "string",
                        example: "FAC-1001",
                    },
                },
                {
                    in: "query",
                    name: "sapDocument",
                    required: false,
                    schema: {
                        type: "string",
                        example: "490001",
                    },
                },
                {
                    in: "query",
                    name: "pageNumber",
                    required: true,
                    schema: {
                        type: "integer",
                        minimum: 1,
                        example: 1,
                    },
                },
                {
                    in: "query",
                    name: "pageSize",
                    required: true,
                    schema: {
                        type: "integer",
                        minimum: 1,
                        maximum: 200,
                        example: 10,
                    },
                },
            ],
            responses: {
                200: {
                    description: "Paginated payment details",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    data: {
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
                                                example: 7,
                                            },
                                            numberOfElements: {
                                                type: "integer",
                                                example: 7,
                                            },
                                            totalPages: {
                                                type: "integer",
                                                example: 1,
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
                                    },
                                    message: {
                                        type: "string",
                                        example: "",
                                    },
                                    errorCode: {
                                        type: "integer",
                                        example: 0,
                                    },
                                    code: {
                                        type: "string",
                                        example: "",
                                    },
                                    httpStatus: {
                                        type: "integer",
                                        example: 200,
                                    },
                                    success: {
                                        type: "boolean",
                                        example: true,
                                    },
                                    detailError: {
                                        type: "string",
                                        example: "",
                                    },
                                    timeStamp: {
                                        type: "integer",
                                        format: "int64",
                                        example: 1785865650756,
                                    },
                                },
                            },
                        },
                    },
                },
                400: {
                    description: "Invalid query parameters",
                },
            },
        },

        post: {
            tags: ["FinanzasPayments"],
            summary: "Create single payment detail record",
            description:
                "Creates one payment detail. paymentHeaderUuid may be supplied to relate the detail to an existing header or an external UUID.",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: {
                            $ref: "#/components/schemas/CreateFinanzasPaymentDto",
                        },
                        example: {
                            company: 10,
                            documentNumber: "FAC-1001",
                            documentReference: "REF-1001",
                            vendorNumber: 12345,
                            amount: "1500.00",
                            currency: "MXN",
                            documentType: "FAC",
                            sapDocument: "490001",
                            paymentDate: "2026-02-27",
                            status: 0,
                            paymentHeaderUuid:
                                PAYMENT_HEADER_UUID_EXAMPLE,
                            createdBy: 1001,
                        },
                    },
                },
            },
            responses: {
                201: {
                    description: "Payment detail created",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    data: {
                                        $ref: "#/components/schemas/FinanzasPayment",
                                    },
                                    message: {
                                        type: "string",
                                        example: "",
                                    },
                                    errorCode: {
                                        type: "integer",
                                        example: 0,
                                    },
                                    code: {
                                        type: "string",
                                        example: "",
                                    },
                                    httpStatus: {
                                        type: "integer",
                                        example: 201,
                                    },
                                    success: {
                                        type: "boolean",
                                        example: true,
                                    },
                                    detailError: {
                                        type: "string",
                                        example: "",
                                    },
                                    timeStamp: {
                                        type: "integer",
                                        format: "int64",
                                    },
                                },
                            },
                        },
                    },
                },
                400: {
                    description: "Invalid payment detail payload",
                },
            },
        },

        patch: {
            tags: ["FinanzasPayments"],
            summary: "Partially update a payment detail",
            description:
                "Updates only the supplied fields. finanzasPaymentUuid is sent in the request body and identifies the payment detail. Supports status 0 and allows assigning, changing or removing paymentHeaderUuid.",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: {
                            $ref: "#/components/schemas/UpdateFinanzasPaymentDto",
                        },
                        examples: {
                            updateStatus: {
                                summary: "Update payment status",
                                value: {
                                    finanzasPaymentUuid:
                                        FINANZAS_PAYMENT_UUID_EXAMPLE,
                                    status: 2,
                                    updatedBy: 1001,
                                },
                            },
                            updateStatusAndRelation: {
                                summary:
                                    "Update status and payment header relation",
                                value: {
                                    finanzasPaymentUuid:
                                        FINANZAS_PAYMENT_UUID_EXAMPLE,
                                    status: 2,
                                    paymentHeaderUuid:
                                        PAYMENT_HEADER_UUID_EXAMPLE,
                                    updatedBy: 1001,
                                },
                            },
                            updateAllEditableFields: {
                                summary: "Update all editable fields",
                                value: {
                                    finanzasPaymentUuid:
                                        FINANZAS_PAYMENT_UUID_EXAMPLE,
                                    company: 10,
                                    documentNumber: "FAC-3001-ACT",
                                    documentReference: "REF-3001-ACT",
                                    vendorNumber: 54321,
                                    amount: "1750.50",
                                    currency: "MXN",
                                    documentType: "FAC",
                                    sapDocument: "4903999",
                                    paymentDate: "2026-03-01",
                                    status: 2,
                                    paymentHeaderUuid:
                                        "ddd4420c-83e0-48d7-8c44-88e9b5987812",
                                    updatedBy: 1002,
                                },
                            },
                            removeHeaderRelation: {
                                summary:
                                    "Remove logical payment header relation",
                                value: {
                                    finanzasPaymentUuid:
                                        FINANZAS_PAYMENT_UUID_EXAMPLE,
                                    paymentHeaderUuid: null,
                                    updatedBy: 1001,
                                },
                            },
                        },
                    },
                },
            },
            responses: {
                200: {
                    description: "Payment detail updated",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    data: {
                                        $ref: "#/components/schemas/FinanzasPayment",
                                    },
                                    message: {
                                        type: "string",
                                        example: "",
                                    },
                                    errorCode: {
                                        type: "integer",
                                        example: 0,
                                    },
                                    code: {
                                        type: "string",
                                        example: "",
                                    },
                                    httpStatus: {
                                        type: "integer",
                                        example: 200,
                                    },
                                    success: {
                                        type: "boolean",
                                        example: true,
                                    },
                                    detailError: {
                                        type: "string",
                                        example: "",
                                    },
                                    timeStamp: {
                                        type: "integer",
                                        format: "int64",
                                    },
                                },
                            },
                        },
                    },
                },
                400: {
                    description:
                        "Invalid UUID, missing editable fields or invalid field value",
                },
                404: {
                    description: "Payment detail not found",
                },
            },
        },
    },

    "/finanzas-payment/header-with-details": {
        post: {
            tags: ["FinanzasPayments"],
            summary:
                "Create payment header with optional detail lines",
            description:
                "Creates a payment header and optionally its detail lines in one transaction. The consumer may supply paymentHeaderUuid; otherwise the backend generates one. INCOME and total-breakdown validations run only when details are supplied.",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: {
                            $ref: "#/components/schemas/CreateFinanzasPaymentHeaderWithDetailsDto",
                        },
                        examples: {
                            headerOnly: {
                                summary:
                                    "Create payment header without details",
                                value: {
                                    paymentHeaderUuid:
                                        PAYMENT_HEADER_UUID_EXAMPLE,
                                    company: 10,
                                    anio: 2026,
                                    vendorNumber: 12345,
                                    currency: "MXN",
                                    totalAmount: "1000.00",
                                    paymentDate: "2026-02-27",
                                    status: 0,
                                    createdBy: 1001,
                                    details: [],
                                },
                            },
                            headerOnlyGeneratedUuid: {
                                summary:
                                    "Create payment header and generate UUID in backend",
                                value: {
                                    company: 10,
                                    anio: 2026,
                                    vendorNumber: 12345,
                                    currency: "MXN",
                                    totalAmount: "1000.00",
                                    paymentDate: "2026-02-27",
                                    status: 0,
                                    createdBy: 1001,
                                    details: [],
                                },
                            },
                            headerWithDetails: {
                                summary:
                                    "Create header and related details",
                                value: {
                                    paymentHeaderUuid:
                                        PAYMENT_HEADER_UUID_EXAMPLE,
                                    company: 10,
                                    anio: 2026,
                                    vendorNumber: 12345,
                                    currency: "MXN",
                                    totalAmount: "1000.00",
                                    paymentDate: "2026-02-27",
                                    status: 0,
                                    createdBy: 1001,
                                    details: [
                                        {
                                            documentNumber:
                                                "FAC-2001",
                                            documentReference:
                                                "REF-2001",
                                            amount: "1500.00",
                                            documentType: "FAC",
                                            sapDocument: "4902001",
                                            paymentLineType:
                                                "INCOME",
                                            status: 0,
                                        },
                                        {
                                            documentNumber:
                                                "NC-2001",
                                            documentReference:
                                                "REF-NC-2001",
                                            amount: "500.00",
                                            documentType: "NC",
                                            sapDocument: "4902002",
                                            paymentLineType:
                                                "CREDIT_NOTE",
                                            status: 0,
                                        },
                                    ],
                                },
                            },
                        },
                    },
                },
            },
            responses: {
                201: {
                    description:
                        "Payment header and optional details created",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    data: {
                                        $ref: "#/components/schemas/FinanzasPaymentHeaderWithDetailsResponse",
                                    },
                                    message: {
                                        type: "string",
                                        example: "",
                                    },
                                    errorCode: {
                                        type: "integer",
                                        example: 0,
                                    },
                                    code: {
                                        type: "string",
                                        example: "",
                                    },
                                    httpStatus: {
                                        type: "integer",
                                        example: 201,
                                    },
                                    success: {
                                        type: "boolean",
                                        example: true,
                                    },
                                    detailError: {
                                        type: "string",
                                        example: "",
                                    },
                                    timeStamp: {
                                        type: "integer",
                                        format: "int64",
                                    },
                                },
                            },
                        },
                    },
                },
                400: {
                    description:
                        "Validation error, including WRN7024 or WRN7025",
                },
                409: {
                    description:
                        "The supplied paymentHeaderUuid already exists",
                },
            },
        },
    },

    "/finanzas-payment/header-with-details/{paymentHeaderUuid}": {
        get: {
            tags: ["FinanzasPayments"],
            summary: "Get payment header with detail lines",
            description:
                "Returns a payment header and its details by paymentHeaderUuid. Detail pagination is optional.",
            parameters: [
                {
                    in: "path",
                    name: "paymentHeaderUuid",
                    required: true,
                    description: "Payment header identifier",
                    schema: {
                        type: "string",
                        format: "uuid",
                        pattern: UUID_PATTERN,
                        example: PAYMENT_HEADER_UUID_EXAMPLE,
                    },
                },
                {
                    in: "query",
                    name: "pageNumber",
                    required: false,
                    schema: {
                        type: "integer",
                        minimum: 1,
                        default: 1,
                        example: 1,
                    },
                    description:
                        "Optional page number for detail pagination",
                },
                {
                    in: "query",
                    name: "pageSize",
                    required: false,
                    schema: {
                        type: "integer",
                        minimum: 1,
                        maximum: 200,
                        default: 20,
                        example: 10,
                    },
                    description:
                        "Optional page size for detail pagination",
                },
            ],
            responses: {
                200: {
                    description:
                        "Payment header with full or paginated details",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    data: {
                                        oneOf: [
                                            {
                                                $ref: "#/components/schemas/FinanzasPaymentHeaderGetWithDetailsResponse",
                                            },
                                            {
                                                $ref: "#/components/schemas/FinanzasPaymentHeaderGetWithDetailsPaginatedResponse",
                                            },
                                        ],
                                    },
                                    message: {
                                        type: "string",
                                        example: "",
                                    },
                                    errorCode: {
                                        type: "integer",
                                        example: 0,
                                    },
                                    code: {
                                        type: "string",
                                        example: "",
                                    },
                                    httpStatus: {
                                        type: "integer",
                                        example: 200,
                                    },
                                    success: {
                                        type: "boolean",
                                        example: true,
                                    },
                                    detailError: {
                                        type: "string",
                                        example: "",
                                    },
                                    timeStamp: {
                                        type: "integer",
                                        format: "int64",
                                    },
                                },
                            },
                        },
                    },
                },
                400: {
                    description:
                        "Invalid paymentHeaderUuid or pagination values",
                },
                404: {
                    description: "Payment header not found",
                },
            },
        },
    },
};