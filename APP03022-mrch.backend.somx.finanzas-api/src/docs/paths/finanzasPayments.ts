// src/docs/paths/accountsPayable.ts
import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
// Regex UUID (v4-friendly) en mayúsculas/minúsculas
const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const finanazasPaymentsPaths: OpenAPIV3.PathsObject = {
    "/finanzas-payments": {
        get: {
            tags: ["FinanzasPayments"],
            summary: "Payments to providers",
            // parameters: [
            //     {
            //         in: "query",
            //         name: "status",
            //         required: false,
            //         description: "Status code (e.g. 1=open, 2=closed)",
            //         schema: { type: "integer", example: 1 }
            //     },
            //     {
            //         in: "query",
            //         name: "vendorNumber",
            //         required: false,
            //         description: "Vendor number identifier",
            //         schema: { type: "integer", example: 12345 }
            //     }
            // ],
            responses: {
                200: {
                    description: "Test",
                    content: {
                        "application/json": {
                            schema: {
                                type: "array",
                                items: { $ref: "#/components/schemas/AccountsPayable" }
                            },
                            examples: {
                                sample: {
                                    value: [
                                        {
                                            id: UUID_EXAMPLE,
                                            orderNumber: "PO-1001",
                                            vendorNumber: 12345,
                                            sourceId: 7,
                                            totalAmount: "1520.75",
                                            currency: "USD",
                                            status: 1,
                                            orderDate: "2025-09-30T10:00:00.000Z",
                                            deliveryDate: "2025-10-15T10:00:00.000Z",
                                            terms: "Net 30 days",
                                            createdBy: 101,
                                            createdAt: "2025-09-30T11:00:00.000Z",
                                            updatedBy: null,
                                            updatedAt: null
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            }
        },


    },

};
