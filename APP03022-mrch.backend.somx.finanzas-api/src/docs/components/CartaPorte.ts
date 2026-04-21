import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";


export const CartaPorteSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    BaseArrayFilesSchemaParent: {
        type: "object",
        properties: {
            folder: { type: "string", example: "nameFolder" },
            origen: {type: "string", example: "2" },
            files: {
                    type: "array", 
                    items: {type: "string",  format: "binary" },
                    minItems: 1, 
                   },
            content: {
                type: "string",
                description: "guias de embarque en Json String",
                example: JSON.stringify(
                {
                    "shippingGuideList": [
                        {
                        "guideNumber": "LG602871012202510441",
                        "vendorNumber": 1,
                        "truckPlate": "abc",
                        "originId": 2,
                        "deliveryType": 1,
                        "deliveryDate": "2025-10-23 15:58:35",
                        "shipingGuideDocumentList": [
                            {
                            "fileName": "File Name xml",
                            "fileType": 1,
                            "status": 1
                            },
                            {
                            "fileName": "File Name csv",
                            "fileType": 2,
                            "status": 1
                            }
                        ]
                        }
                    ]
                }
                )
            },
        },
        required: ["folder", "origen", "files", "content"],
    },

    BaseSchemaParent: {
        type: "object",
        properties: {
        content: {
                type: "string",
                description: "Orden de Compra en Json String",
                example: JSON.stringify(
                    {
                        "orderNumber": "recep54CP.xml",
                        "supplierNumber": 1,
                        "origen": "1",
                        "amount": "10.0",
                        "status": 1,
                        "purchaseOrderDate": "2025-05-24 02:00:00",
                        "createdBy": 1,
                        "guideNumber": [
                            {
                            "guide": "LG602871012202510441"
                            }
                        ],
                        "receptionList": [
                            {
                            "receptionNumber": "recep54CP.xml",
                            "originId": 1,
                            "destinationId": 1,
                            "amount": "10.0",
                            "status": 0,
                            "sapDocument": "fas",
                            "comments": "NO COMMENTS",
                            "receptionDate": "2025-05-23 02:00:00",
                            "createdBy": 1,
                            "receiptSkuList": [
                                {
                                "sku": "V01",
                                "description": "V01",
                                "quantity": 1,
                                "unitCost": "10.0",
                                "totalCost": "10.0",
                                "status": 0,
                                "createdBy": 1
                                }
                            ]
                            }
                        ]
                    }
                )
            },
        },
        required: ["content"],
        
    },

    ListShippingGuideQuerySchema: {
        type: "object",
        properties: {
            id: { type: "string", example: "1" },
            guideNumber: { type: "string", example: "PAK34313" },
            status: { type: "number", example: 2 },
            vendorNumber: { type: "number", example: 3458897 },
            originId: { type: "number", example: 2 },
            destinationId: { type: "number", example: 5 },
            deliveryType: { type: "number", example: 5 },
            isStatusUpdated: { type: "boolean", example: true },
            from: {type: "string",format: "date", example: "2025-12-23" },
            to: {type: "string",format: "date", example: "2025-12-26" },
            pageNumber: { type: "number", example: 1 },
            pageSize: { type: "number", example: 10 },
        },
        required: ["from", "to", "isStatusUpdated", "pageNumber", "pageSize"],
    },

    ShippginGuideSummaryListSchema: {
        type: "object",
        properties: {
            data: { type: "array", items: { type: "string" }, },
        },
        required: ["data"],
    },


};