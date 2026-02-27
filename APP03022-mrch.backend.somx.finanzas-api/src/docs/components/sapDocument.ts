// src/docs/components/sapDocument.ts
import type { OpenAPIV3 } from "openapi-types";

export const sapDocumentSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    SapDocument: {
        type: "object",
        properties: {
            id: { type: "string", format: "uuid", description: "UUID primary key" },
            documentNumber: { type: "string", description: "Document number" },
            referenceNumber: { type: "string", description: "Reference number" },
            company: { type: "integer", description: "Company code" },
            fiscalYear: { type: "integer", description: "Fiscal year" },
            documentType: { type: "string", description: "Document type code" },
            amount: { type: "string", description: "Document amount (numeric string)" },
            currency: { type: "string", description: "Currency code" },
            postingDate: { type: "string", format: "date-time", description: "Posting date" },
            documentDate: { type: "string", format: "date-time", description: "Document date" },
            status: { type: "integer", description: "Status code" },
            createdBy: { type: "integer", description: "User ID who created the record" },
            createdAt: { type: "string", format: "date-time", description: "Creation timestamp" },
            updatedBy: { type: "integer", nullable: true, description: "User ID who updated the record" },
            updatedAt: { type: "string", format: "date-time", nullable: true, description: "Update timestamp" },
        },
        required: ["id", "documentNumber", "referenceNumber", "company", "fiscalYear", "documentType", "amount", "currency", "postingDate", "documentDate", "status", "createdBy"],
    },
    CreateSapDocumentDto: {
        type: "object",
        properties: {
            documentNumber: { type: "string" },
            referenceNumber: { type: "string" },
            company: { type: "integer" },
            fiscalYear: { type: "integer" },
            documentType: { type: "string" },
            amount: { type: "string" },
            currency: { type: "string" },
            postingDate: { type: "string", format: "date-time" },
            documentDate: { type: "string", format: "date-time" },
            status: { type: "integer" },
            createdBy: { type: "integer" },
        },
        required: ["documentNumber", "referenceNumber", "company", "fiscalYear", "documentType", "amount", "currency", "postingDate", "documentDate", "status", "createdBy"],
    },
    UpdateSapDocumentDto: {
        type: "object",
        properties: {
            amount: { type: "string" },
            status: { type: "integer" },
            updatedBy: { type: "integer" },
        },
    },
};
