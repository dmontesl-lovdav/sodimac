// FILE: src/docs/components/schemas/finanzasPayments.schemas.ts
import type { OpenAPIV3 } from "openapi-types";

export const finanzasPaymentsSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    FinanzasPayment: {
        type: "object",
        properties: {
            finanzasPaymentUuid: { type: "string", format: "uuid" },
            company: { type: "integer", example: 10 },
            documentNumber: { type: "string", example: "FAC-1001" },
            documentReference: { type: "string", example: "REF-1001" },
            vendorNumber: { type: "integer", example: 12345 },
            amount: { type: "string", example: "1500.00" },
            currency: { type: "string", example: "MXN" },
            documentType: { type: "string", example: "FAC" },
            sapDocument: { type: "string", example: "490001" },
            paymentDate: { type: "string", format: "date", example: "2026-02-27" },
            status: { type: "integer", example: 1 },
            paymentHeaderUuid: { type: "string", format: "uuid", nullable: true },
            createdBy: { type: "integer", nullable: true, example: 1001 },
            createdAt: { type: "string", format: "date-time" },
            updatedBy: { type: "integer", nullable: true, example: null },
            updatedAt: { type: "string", format: "date-time", nullable: true },
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
            paymentHeaderUuid: { type: "string", format: "uuid" },
            company: { type: "integer", example: 10 },
            anio: { type: "integer", example: 2026 },
            vendorNumber: { type: "integer", example: 12345 },
            currency: { type: "string", example: "MXN" },
            totalAmount: { type: "string", example: "2000.00" },
            paymentDate: { type: "string", format: "date", example: "2026-02-27" },
            status: { type: "integer", example: 1 },
            createdBy: { type: "integer", nullable: true, example: 1001 },
            createdAt: { type: "string", format: "date-time" },
            updatedBy: { type: "integer", nullable: true, example: null },
            updatedAt: { type: "string", format: "date-time", nullable: true },
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
            company: { type: "integer", example: 10 },
            documentNumber: { type: "string", example: "FAC-1001" },
            documentReference: { type: "string", example: "REF-1001" },
            vendorNumber: { type: "integer", example: 12345 },
            amount: { type: "string", example: "1500.00" },
            documentType: { type: "string", example: "FAC" },
            sapDocument: { type: "string", example: "490001" },
            paymentDate: { type: "string", format: "date", example: "2026-02-27" },
            status: { type: "integer", example: 1 },
            paymentHeaderUuid: { type: "string", format: "uuid", nullable: true },
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
            "status",
        ],
    },

    UpdateFinanzasPaymentDto: {
        type: "object",
        properties: {
            vendorNumber: { type: "integer", example: 12345 },
            documentNumber: { type: "string", example: "FAC-1001" },
            sapDocument: { type: "string", example: "490001" },
            documentType: { type: "string", example: "FAC" },
            status: { type: "integer", example: 2 },
        },
        required: ["vendorNumber", "documentNumber", "sapDocument", "documentType", "status"],
    },

    CreateFinanzasPaymentDetailItemDto: {
        type: "object",
        properties: {
            documentNumber: { type: "string", example: "FAC-1001" },
            documentReference: { type: "string", example: "REF-1001" },
            amount: { type: "string", example: "1500.00" },
            documentType: { type: "string", example: "FAC" },
            sapDocument: { type: "string", example: "490001" },
            paymentLineType: { type: "string", enum: ["INCOME", "CREDIT_NOTE"], example: "INCOME" },
        },
        required: ["documentNumber", "documentReference", "amount", "documentType", "sapDocument", "paymentLineType"],
    },

    CreateFinanzasPaymentHeaderWithDetailsDto: {
        type: "object",
        properties: {
            company: { type: "integer", example: 10 },
            anio: { type: "integer", example: 2026 },
            vendorNumber: { type: "integer", example: 12345 },
            currency: { type: "string", example: "MXN" },
            totalAmount: { type: "string", example: "2000.00" },
            paymentDate: { type: "string", format: "date", example: "2026-02-27" },
            status: { type: "integer", example: 1 },
            createdBy: { type: "integer", nullable: true, example: 1001 },
            details: {
                type: "array",
                items: { $ref: "#/components/schemas/CreateFinanzasPaymentDetailItemDto" },
            },
        },
        required: ["company", "anio", "vendorNumber", "currency", "totalAmount", "paymentDate", "status", "details"],
    },

    FinanzasPaymentHeaderWithDetailsResponse: {
        type: "object",
        properties: {
            header: { $ref: "#/components/schemas/FinanzasPaymentHeader" },
            details: {
                type: "array",
                items: { $ref: "#/components/schemas/FinanzasPayment" },
            },
            summary: {
                type: "object",
                properties: {
                    paymentHeaderUuid: { type: "string", format: "uuid" },
                    totalDetails: { type: "integer", example: 2 },
                    headerTotal: { type: "string", example: "2000.00" },
                    expectedTotal: { type: "string", example: "2000.00" },
                },
            },
        },
    },

    FinanzasPaymentHeaderGetWithDetailsResponse: {
        type: "object",
        properties: {
            header: { $ref: "#/components/schemas/FinanzasPaymentHeader" },
            details: {
                type: "array",
                items: { $ref: "#/components/schemas/FinanzasPayment" },
            },
            summary: {
                type: "object",
                properties: {
                    paymentHeaderUuid: { type: "string", format: "uuid" },
                    totalDetails: { type: "integer", example: 2 },
                    totalAmountHeader: { type: "string", example: "2000.00" },
                    totalAmountDetail: { type: "string", example: "2000.00" },
                    amountsMatch: { type: "boolean", example: true },
                },
            },
        },
    },

    FinanzasPaymentHeaderGetWithDetailsPaginatedResponse: {
        type: "object",
        properties: {
            header: { $ref: "#/components/schemas/FinanzasPaymentHeader" },
            detailsPage: {
                type: "object",
                properties: {
                    content: {
                        type: "array",
                        items: { $ref: "#/components/schemas/FinanzasPayment" },
                    },
                    totalElements: { type: "integer", example: 25 },
                    numberOfElements: { type: "integer", example: 10 },
                    totalPages: { type: "integer", example: 3 },
                    pageNumber: { type: "integer", example: 1 },
                    pageSize: { type: "integer", example: 10 },
                },
            },
            summary: {
                type: "object",
                properties: {
                    paymentHeaderUuid: { type: "string", format: "uuid" },
                    totalAmountHeader: { type: "string", example: "2000.00" },
                },
            },
        },
    },
};