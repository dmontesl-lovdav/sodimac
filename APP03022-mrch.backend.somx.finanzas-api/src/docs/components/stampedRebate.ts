// src/docs/components/stampedRebate.ts
import type { OpenAPIV3 } from "openapi-types";

export const stampedRebateSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    StampedRebate: {
        type: "object",
        properties: {
            id: { type: "string", format: "uuid", description: "UUID primary key" },
            documentNumber: { type: "string", description: "Document number" },
            vendorNumber: { type: "integer", description: "Vendor identifier" },
            status: { type: "integer", description: "Status code" },
            amount: { type: "string", description: "Total amount (numeric string)" },
            periodId: { type: "integer", description: "Period identifier (e.g., 202501)" },
            createdBy: { type: "integer", description: "User ID who created the record" },
            createdAt: { type: "string", format: "date-time", description: "Creation timestamp" },
            updatedBy: { type: "integer", nullable: true, description: "User ID who updated the record" },
            updatedAt: { type: "string", format: "date-time", nullable: true, description: "Update timestamp" },
            rebates: {
                type: "array",
                items: { $ref: "#/components/schemas/Rebate" },
                description: "Related rebates"
            },
        },
        required: ["id", "documentNumber", "vendorNumber", "status", "amount", "periodId", "createdBy"],
    },
    CreateStampedRebateDto: {
        type: "object",
        properties: {
            documentNumber: { type: "string" },
            vendorNumber: { type: "integer" },
            status: { type: "integer" },
            amount: { type: "string" },
            periodId: { type: "integer" },
            createdBy: { type: "integer" },
        },
        required: ["documentNumber", "vendorNumber", "status", "amount", "periodId", "createdBy"],
    },
    UpdateStampedRebateDto: {
        type: "object",
        properties: {
            status: { type: "integer" },
            amount: { type: "string" },
            updatedBy: { type: "integer" },
        },
    },
};
