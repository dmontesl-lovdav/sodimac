import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";
const TRACE_EXAMPLE = "a4b6c2b0-1f2b-4a0f-9c3c-9c2f9e7b1a11";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

export const cartaPortePaths: OpenAPIV3.PathsObject = {
    "/carta-porte/guia-embarque": {
        post: {
            tags: ["CartaPorte"],
            summary: "Create shipping guides for cartaPorte Batch",
            requestBody: {
                required: true,
                content: {
                    "multipart/form-data": {
                        schema: { $ref: "#/components/schemas/BaseArrayFilesSchemaParent" },
                        example: {
                                    encoding: { files: { contentType: "text/csv,application/xml,text/xml"}},
                                    folder: "folder",
                                    origen: "2",
                                    content: JSON.stringify(
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
                                }
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

    "/carta-porte/oc": {
        post: {
            tags: ["CartaPorte"],
            summary: "Create purchase order for CartaPorte batch",
            requestBody: {
                required: true,
                content: {
                    "multipart/form-data": {
                        schema: { $ref: "#/components/schemas/BaseSchemaParent" },
                        example: {
                                    content:    JSON.stringify  (
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

    "/carta-porte/all": {
        post: {
            tags: ["CartaPorte"],
            summary: "Create purchase order and shipping Guides for CartaPorte batch",
            requestBody: {
                required: true,
                content: {
                    "multipart/form-data": {
                        schema: { $ref: "#/components/schemas/BaseArrayFilesSchemaParent" },
                        example: {
                                    encoding: { files: { contentType: "text/csv,application/xml,text/xml"}},
                                    folder: "folder",
                                    origen: "2",
                                    content: JSON.stringify (
                                                                {
                                                                "orderNumber": "recep55CP.xml",
                                                                "supplierNumber": 1,
                                                                "origen": "1",
                                                                "amount": "10.0",
                                                                "status": 1,
                                                                "purchaseOrderDate": "2025-05-24 02:00:00",
                                                                "createdBy": 1,
                                                                "guideNumber": [
                                                                    {
                                                                    "guide": "EU0453C1112202515444"
                                                                    }
                                                                ],
                                                                "receptionList": [
                                                                    {
                                                                    "receptionNumber": "recep55CP.xml",
                                                                    "originId": 1,
                                                                    "destinationId": 1,
                                                                    "amount": "10.0",
                                                                    "status": 0,
                                                                    "sapDocument": "sapdoc",
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
                                                                ],
                                                                "shippingGuideList": [
                                                                    {
                                                                    "guideNumber": "EU0453C1112202515444",
                                                                    "vendorNumber": 1,
                                                                    "truckPlate": "abc",
                                                                    "originId": 2,
                                                                    "deliveryType": 1,
                                                                    "deliveryDate": "2025-10-23 15:58:35",
                                                                    "shipingGuideDocumentList": [
                                                                        {
                                                                        "fileName": "Carta_Porte_DAD_EU0453C1112202515444.xml",
                                                                        "fileType": 1,
                                                                        "status": 1
                                                                        },
                                                                        {
                                                                        "fileName": "Carta_Porte_DAD_EU0453C1112202515444.csv",
                                                                        "fileType": 2,
                                                                        "status": 1
                                                                        }
                                                                    ]
                                                                    }
                                                                ]
                                                                }
                                                            )
                                }
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

    "/carta-porte/findAllGuia": {
        post: {
            tags: ["CartaPorte"],
            summary: "Obtiene todas las guias que has sido actualizados los estatus",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/ListShippingGuideQuerySchema" },
                        example:{
                                    from: "2025-12-23",
                                    to: "2025-12-26",
                                    isStatusUpdated: true
                                }
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

    "/carta-porte/updateAllStatusGuia": {
        post: {
            tags: ["CartaPorte"],
            summary: "Actualiza el campo Status de las guias que fueron actualizadas en carta porte",
            requestBody: {
                required: true,
                content: {
                    "application/json": {
                        schema: { $ref: "#/components/schemas/ShippginGuideSummaryListSchema" },
                        example:{
                                    data: [
                                        "e787a64b-8980-46ab-b99d-bc60a66f9286",
                                        "60995784-bb34-4aa6-b612-a4c9d269357a"
                                    ]
                                }
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
};