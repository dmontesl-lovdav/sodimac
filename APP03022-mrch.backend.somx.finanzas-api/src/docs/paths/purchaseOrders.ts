import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
const TRACE_EXAMPLE = "a4b6c2b0-1f2b-4a0f-9c3c-9c2f9e7b1a11";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const purchaseOrdersPaths: OpenAPIV3.PathsObject = {
    "/purchase-orders": {
        get: {
            tags: ["PurchaseOrders"],
            summary: "List Purchase Orders (filtered + paginated)",
            parameters: [
                {
                    in: "query",
                    name: "purchaseOrderDateAtInitial",
                    required: true,
                    description: "Start datetime (required)",
                    schema: { type: "string", format: "date-time", example: "2026-02-01 00:00:00" },
                },
                {
                    in: "query",
                    name: "purchaseOrderDateAtEnd",
                    required: true,
                    description: "End datetime (required)",
                    schema: { type: "string", format: "date-time", example: "2026-02-21 23:59:59" },
                },
                {
                    in: "query",
                    name: "supplierNumber",
                    required: false,
                    description: "Supplier Number",
                    schema: { type: "number", example: 23876 },
                },
                {
                    in: "query",
                    name: "orderNumber",
                    required: false,
                    description: "Order Number",
                    schema: { type: "string",  example: "ON3459" },
                },
                {
                    in: "query",
                    name: "originId",
                    required: false,
                    description: "Origin ID",
                    schema: { type: "number",  example: 1 },
                },
                {
                    in: "query",
                    name: "status",
                    required: false,
                    description: "Order Status",
                    schema: { type: "number", example: 1 },
                },
                {
                    in: "query",
                    name: "pageNumber",
                    required: true,
                    description: "Page Number",
                    schema: { type: "string", example: "1" },
                },
                {
                    in: "query",
                    name: "pageSize",
                    required: true,
                    description: "Page Size",
                    schema: { type: "string", example: "10" },
                },

            ],
            responses: {
                201: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
                404: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
            },
        },

        post: {
            tags: ["PurchaseOrders"],
            summary: "Create Order with Receptions and ReceptionsSkus",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/CreatePurchaseOrderDto" },
                        example: {
                            supplierNumber: 34786,
                            orderNumber: "987",
                            amount: "10.45",
                            purchaseOrderDate: "2025-05-24 02:00:00",
                            status: 1,
                            createdBy: 1,
                            origen: "1",
                            receptionList: [
                                {
                                    receptionNumber: "REc01",
                                    originId: 1,
                                    origin: "SLI",
                                    destinationId: "1",
                                    amount: "10.45",
                                    comments: "Comentarios extras",
                                    guideNumber: "GN8976",
                                    receptionDate: "2025-05-24 02:00:00",
                                    status: 0,
                                    createdBy: 1,
                                    receiptSkuList: [
                                        {
                                            sku: "V01",
                                            description: "Descripcion SKU",
                                            quantity:  1,
                                            unitCost: "10.45",
                                            totalCost: "10.45",
                                            status: 0,
                                            createdBy: "1"
                                        }
                                    ]

                                }
                            ],
                        },
                    },
                },
            },
            responses: {
                201: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
                404: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
            },
        },
    },

    "/purchase-orders/:uuid": {
        get: {
            tags: ["PurchaseOrders"],
            summary: "Get Purchase Orders by uuid",
            parameters: [
                {
                    in: "params",
                    name: "uuid",
                    required: true,
                    description: "Purchase Order UUID",
                    schema: { type: "string", format: "UUID", example: UUID_EXAMPLE },
                }
            ],
            responses: {
                200: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
                400: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
            },
        },


    },

    "/purchase-orders/reception/:uuid": {
        get: {
            tags: ["PurchaseOrders"],
            summary: "Get reception by uuid",
            parameters: [
                {
                    in: "params",
                    name: "uuid",
                    required: true,
                    description: "Reception UUID",
                    schema: { type: "string", format: "UUID", example: UUID_EXAMPLE },
                }
            ],
            responses: {
                200: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
                400: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
            },
        },

        patch: {
            tags: ["PurchaseOrders"],
            summary: "Update reception status by uuid",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/ReceptionDto" },
                        example: {
                            supplierNumber: 34786,
                            orderNumber: "on987",
                            receptionId: "92fe3361-60f7-4890-b377-669eb651e9d",
                            status: 3,
                        },
                    },
                },
            },
            responses: {
                200: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
                400: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
            },
        },

    },

    "/purchase-orders/listReception": {
        get: {
            tags: ["PurchaseOrders"],
            summary: "List Receptions(filtered + paginated)",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/ListReceptionQuerySchema" },
                        example: {
                            receptionDateAtInitial: "2025-05-23",
                            receptionDateAtEnd: "2025-11-06",
                            orderNumber: "odn45",
                            supplierNumber: 45632,
                            status: 0,
                            receptionId: "2e9c22e4-6b43-4936-92d4-3d437f812eba",
                            pageNumber: 1,
                            pageSize: 10

                        },
                    },
                },
            },
            responses: {
                200: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
                404: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
            },
        },


    },

    "/purchase-orders/updateReception": {
        patch: {
            tags: ["PurchaseOrders"],
            summary: "Update Status Reception",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/UpdateStatusReceptionSchema" },
                        example: {
                            orderNumber: "odn45",
                            supplierNumber: 45632,
                            status: 0,
                            receptionId: "2e9c22e4-6b43-4936-92d4-3d437f812eba",
                        },
                    },
                },
            },
            responses: {
                200: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
                404: {
                    description: "General Response",
                    content: {
                        "application/json": {
                            schema: { $ref: "#/components/schemas/ResponseHandlerDTO" },
                        },
                    },
                },
            },
        },


    },
};