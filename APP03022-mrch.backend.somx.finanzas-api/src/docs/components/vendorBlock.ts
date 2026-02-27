// src/docs/components/vendorBlock.ts
import type { OpenAPIV3 } from "openapi-types";

export const vendorBlockSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    VendorBlock: {
        type: "object",
        properties: {
            id: { type: "string", format: "uuid", description: "UUID primary key" },
            vendorNumber: { type: "integer", description: "Vendor identifier" },
            blockReason: { type: "string", description: "Block reason code (e.g., FRAUD, DEBT)" },
            blockDescription: { type: "string", description: "Detailed block description" },
            startDate: { type: "string", format: "date-time", description: "Block start date" },
            endDate: { type: "string", format: "date-time", nullable: true, description: "Block end date (null for permanent blocks)" },
            status: { type: "integer", description: "Status code (1=active, 0=inactive)" },
            autoUnblock: { type: "boolean", description: "Auto-unblock when endDate is reached" },
            blockType: { type: "string", description: "Block type (TEMP=temporary, PERM=permanent)" },
            createdBy: { type: "integer", description: "User ID who created the record" },
            createdAt: { type: "string", format: "date-time", description: "Creation timestamp" },
            updatedBy: { type: "integer", nullable: true, description: "User ID who updated the record" },
            updatedAt: { type: "string", format: "date-time", nullable: true, description: "Update timestamp" },
        },
        required: ["id", "vendorNumber", "blockReason", "blockDescription", "startDate", "status", "autoUnblock", "blockType", "createdBy"],
    },
    CreateVendorBlockDto: {
        type: "object",
        properties: {
            vendorNumber: { type: "integer" },
            blockReason: { type: "string" },
            blockDescription: { type: "string" },
            startDate: { type: "string", format: "date-time" },
            endDate: { type: "string", format: "date-time", nullable: true },
            status: { type: "integer" },
            autoUnblock: { type: "boolean" },
            blockType: { type: "string" },
            createdBy: { type: "integer" },
        },
        required: ["vendorNumber", "blockReason", "blockDescription", "startDate", "status", "autoUnblock", "blockType", "createdBy"],
    },
    UpdateVendorBlockDto: {
        type: "object",
        properties: {
            status: { type: "integer" },
            endDate: { type: "string", format: "date-time" },
            blockDescription: { type: "string" },
            updatedBy: { type: "integer" },
        },
    },
};
