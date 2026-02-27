// src/docs/components/accountsPayable.ts
import type { OpenAPIV3 } from "openapi-types";

export const accountsPayableSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    AccountsPayable: {
        type: "object",
        properties: {
            id: { type: "string" },
            vendorNumber: { type: "string" },
            status: { type: "string" },
            amount: { type: "number" },
            dueDate: { type: "string", format: "date" },
        },
        required: ["id", "vendorNumber", "status", "amount"],
    },
    CreateAccountsPayableDto: {
        type: "object",
        properties: {
            vendorNumber: { type: "string" },
            status: { type: "string" },
            amount: { type: "number" },
            dueDate: { type: "string", format: "date" },
        },
        required: ["vendorNumber", "status", "amount"],
    },
    UpdateAccountsPayableDto: {
        type: "object",
        properties: {
            status: { type: "string" },
            amount: { type: "number" },
            dueDate: { type: "string", format: "date" },
        },
    },
};
