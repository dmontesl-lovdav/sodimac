import type { OpenAPIV3 } from "openapi-types";

export const accountStatementSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    AccountStatementListItem: {
        type: "object",
        properties: {
            accountStatementUuid: { type: "string", format: "uuid" },
            vendorNumber: { type: "integer" },
            vendorName: { type: "string" },
            year: { type: "integer" },
            month: { type: "integer" },
            status: { type: "integer" },
            statusLabel: { type: "string" },
            processedAt: { type: "string", format: "date-time", nullable: true },
            reviewedAt: { type: "string", format: "date-time", nullable: true },
        },
        required: ["accountStatementUuid", "vendorNumber", "vendorName", "year", "month", "status", "statusLabel"],
    },
    AccountStatementDetail: {
        type: "object",
        properties: {
            accountStatementUuid: { type: "string", format: "uuid" },
            vendorNumber: { type: "integer" },
            vendorName: { type: "string" },
            year: { type: "integer" },
            month: { type: "integer" },
            version: { type: "integer" },
            status: { type: "integer" },
            statusLabel: { type: "string" },
            initialBalance: { type: "string", nullable: true },
            finalBalance: { type: "string", nullable: true },
            processedAt: { type: "string", format: "date-time", nullable: true },
            reviewedAt: { type: "string", format: "date-time", nullable: true },
            issuedAt: { type: "string", format: "date-time", nullable: true },
            periodStart: { type: "string", format: "date", nullable: true },
        },
        required: [
            "accountStatementUuid",
            "vendorNumber",
            "vendorName",
            "year",
            "month",
            "version",
            "status",
            "statusLabel",
        ],
    },
    AccountStatementListResponse: {
        type: "object",
        properties: {
            items: {
                type: "array",
                items: { $ref: "#/components/schemas/AccountStatementListItem" },
            },
            totalItems: { type: "integer" },
            totalPages: { type: "integer" },
            currentPage: { type: "integer" },
        },
        required: ["items", "totalItems", "totalPages", "currentPage"],
    },
};

export const tags: OpenAPIV3.TagObject[] = [{ name: "AccountStatement" }];
