import type { OpenAPIV3 } from "openapi-types";

const sapDocumentDto: OpenAPIV3.SchemaObject = {
    type: "object",
    properties: {
        id: { type: "string", format: "uuid", description: "Same as sapDocumentUuid" },
        sapDocumentUuid: { type: "string", format: "uuid" },
        documentNumber: { type: "string" },
        referenceNumber: { type: "string" },
        vendorNumber: { type: "integer" },
        amount: { type: "number" },
        source: { type: "integer" },
        docSap: { type: "string" },
        message: { type: "string", nullable: true },
        sapStatus: { type: "integer", enum: [0, 1, 2, 3] },
        documentType: { type: "string" },
        fiscalUuids: {
            type: "array",
            items: { type: "string", format: "uuid" },
            description: "Folios fiscales (UUID SAT) asociados al documento SAP",
        },
        createdBy: { type: "integer", nullable: true },
        createdAt: { type: "string", format: "date-time" },
        updatedBy: { type: "integer", nullable: true },
        updatedAt: { type: "string", format: "date-time", nullable: true },
    },
};

export const sapDocumentSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    SapDocument: sapDocumentDto,
    CreateSapDocumentDto: {
        type: "object",
        properties: {
            documentNumber: { type: "string" },
            referenceNumber: { type: "string" },
            vendorNumber: { type: "integer" },
            amount: { type: "string", description: "Decimal with up to 2 places" },
            source: { type: "integer" },
            docSap: { type: "string" },
            message: { type: "string", nullable: true },
            sapStatus: { type: "integer", enum: [0, 1, 2, 3] },
            documentType: { type: "string" },
            createdBy: { type: "integer", nullable: true },
            fiscalUuid: { type: "string", format: "uuid" },
            fiscalUuids: {
                type: "array",
                items: { type: "string", format: "uuid" },
            },
        },
        required: [
            "documentNumber",
            "referenceNumber",
            "vendorNumber",
            "amount",
            "source",
            "docSap",
            "documentType",
        ],
    },
    UpdateSapDocumentDto: {
        type: "object",
        properties: {
            documentNumber: { type: "string" },
            referenceNumber: { type: "string" },
            vendorNumber: { type: "integer" },
            amount: { type: "string" },
            source: { type: "integer" },
            docSap: { type: "string" },
            message: { type: "string", nullable: true },
            sapStatus: { type: "integer" },
            documentType: { type: "string" },
            updatedBy: { type: "integer" },
            fiscalUuid: { type: "string", format: "uuid" },
            fiscalUuids: {
                type: "array",
                items: { type: "string", format: "uuid" },
            },
        },
    },
};
