import type { OpenAPIV3 } from "openapi-types";

const UUID_EXAMPLE = "209279be-37c7-4154-b3c6-df976fd7b6a";

const UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";


export const purchaseOrdersSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    CreatePurchaseOrderDto: {
        type: "object",
        properties: {
            supplierNumber: { type: "number", example: 123.45 },
            orderNumber: {type: "string", example: "order123" },

            amount: { type: "string", example: "10.34" },
            purchaseOrderDate: {type: "string",format: "date-time", nullable: true, example: "2026-02-21 22:10:00.000" },
            status: { type: "number", example: 1 },
            createdBy:  {type: "number", example: 1 },

            receptionList: {
                type: "array",
                items: { $ref: "#/components/schemas/CreateReceiptionSchema" },
            },
            guideNumber: {
                type: "array",
                items: {type: "string" },
                example: ["REC001", "REC002", "REC003"],
                nullable: true, description: "Aplica Obligatorio para CartaPorte"
            },
            shippingGuideList: {
                type: "array",
                items: { $ref: "#/components/schemas/CreateShippingGuideSchema" },
                nullable: true, description: "Aplica Obligatorio para CartaPorte"
            },
            origen: { type: "string", example: "1" },
        },
        required: ["supplierNumber", "orderNumber", "amount", "purchaseOrderDate", "status", "createdBy", "receptionList"],
    },

    CreateReceiptionSchema: {
        type: "object",
        properties: {
            receptionNumber: { type: "string", example: "rec001" },
            originId: { type: "number", example: 1 },
            origin: { type: "string", example: "SLI" },
            destinationId: { type: "number", example: "1" },
            amount: { type: "string", example: "10.34" },
            comments: { type: "string", example: "comentarios extras" },
            guideNumber: { type: "string", example: "245243V424R245" },
            receptionDate: { type: "string", format: "date-time", example: "2026-02-21 22:10:00.000"  },

            // error fields (cuando tipoEvento=ERROR)
            status: { type: "number", example: "1" },
            createdBy: { type: "number", example: "1" },
            receiptSkuList: {
                type: "array",
                items: { $ref: "#/components/schemas/CreateReceiptionSkuSchema" },
                description: "Lista de Skus de las recepciones"
            },
    
        },
        required: ["receptionNumber", "origin", "destinationId", "amount", "comments","guideNumber", "receptionDate", "status", "createdBy", "receiptSkuList"],
    },

    CreateReceiptionSkuSchema: {
        type: "object",
        properties: {
            sku: { type: "string", example: "V01" },
            description: { type: "string", example: "Descripcion del SKU" },
            quantity: { type: "number", example: 2 },
            unitCost: { type: "string", example: "10.1" },
            totalCost: { type: "string", example: "20.2" },
            status: { type: "number", example: 2 },
            createdBy: { type: "number", example: 3 },
        },
        required: ["sku", "description", "quantity", "unitCost", "totalCost", "status", "createdBy"],
    },

    CreateShippingGuideSchema: {
        type: "object",
        properties: {
            guideNumber: { type: "string", example: "adfeREUPI" },
            vendorNumber: { type: "number", example: 22876 },
            truckPlate: { type: "string", example: "pack345" },
            trailerPlate: { type: "string", example: "TrailerPlate", nullable: true },
            driverName: { type: "string", example: "Nombre Apellido", nullable: true },
            driverLicense: { type: "string", example: "LIC21", nullable: true },
            originId: { type: "number", example: 1 },
            destinationId: { type: "number", example: 2,  nullable: true },
            deliveryType: { type: "number", example: 2},
            status: { type: "number", example: 2},
            comments: { type: "string", example: "comentarios extras", nullable: true },
            deliveryDate: {type: "string",format: "date-time", example: "2026-02-21 22:10:00.000" },
            shippingDate: {type: "string",format: "date-time", nullable: true, example: "2026-02-21 22:10:00.000" },
            estimatedArrival: {type: "string",format: "date-time", nullable: true, example: "2026-02-21 22:10:00.000" },
            actualArrival: {type: "string",format: "date-time", nullable: true, example: "2026-02-21 22:10:00.000" },
            sentAt: {type: "string",format: "date-time", nullable: true, example: "2026-02-21 22:10:00.000" },
            createdBy: { type: "number", example: 2},
            shipingGuideDocumentList: {
                type: "array",
                items: { $ref: "#/components/schemas/CreateShipingGuideDocumentSchema" },
                description: "Lista de Skus de las recepciones"
            },

        },
        required: ["guideNumber", "vendorNumber", "truckPlate"
            , "originId", "deliveryType","status", "deliveryDate", "shipingGuideDocumentList"],
    },

    CreateShipingGuideDocumentSchema: {
        type: "object",
        properties: {
            fileName: { type: "string", example: "filename.ext" },
            fileType: { type: "number", example: 1 },
            status: { type: "number", example: 2},

        },
        required: ["fileName", "fileType", "status"],
    },
    
    ListPurchaseOrderQuerySchema: {
        type: "object",
        properties: {
            purchaseOrderDateAtInitial: {type: "string",format: "date-time", example: "2026-02-21 22:10:00.000" },
            purchaseOrderDateAtEnd: {type: "string",format: "date-time", example: "2026-02-21 22:10:00.000" },
            purchaseOrderId: { type: "string", example: "PO1", nullable: true },
            orderNumber: { type: "string", example: "4576", nullable: true },
            originId: { type: "number", example: 1, nullable: true },
            supplierNumber: { type: "number", example: 23498, nullable: true },
            pageNumber: { type: "string", example: "1" },
            pageSize: { type: "string", example: "10" },
            status: { type: "number", example: 1, nullable: true },

        },
        required: ["purchaseOrderDateAtInitial", "purchaseOrderDateAtEnd", "pageNumber", "pageSize"],
    },


    ListReceptionQuerySchema: {
        type: "object",
        properties: {
            receptionDateAtInitial: {type: "string",format: "date-time", example: "2026-02-21 22:10:00.000" },
            receptionDateAtEnd: {type: "string",format: "date-time", example: "2026-02-21 22:10:00.000" },
            createdAtInitial: {type: "string",format: "date-time", example: "2026-02-21 22:10:00.000", nullable: true },
            createdAtEnd: {type: "string",format: "date-time", example: "2026-02-21 22:10:00.000", nullable: true },
            supplierNumber: { type: "number", example: 23498, nullable: true },
            orderNumber: { type: "string", example: "1", nullable:true },
            receptionId: { type: "string", example: "1", nullable: true },
            pageNumber: { type: "string", example: "1" },
            pageSize: { type: "string", example: "10" },
            status: { type: "number", example: 1, nullable: true },

        },
        required: ["receptionDateAtInitial", "receptionDateAtEnd", "pageNumber", "pageSize"],
    },

    UpdateStatusReceptionSchema: {
        type: "object",
        properties: {
            supplierNumber: { type: "number", example: 23498 },
            orderNumber: { type: "string", example: "1"},
            receptionNumber: { type: "string", example: "1" },
            uuid: { type: "string",  pattern: UUID_PATTERN, example: UUID_EXAMPLE, nullable: true},
            comments: { type: "string", example: "Comentarios extras" },
            status: { type: "number", example: 1,},

        },
        required: ["supplierNumber", "orderNumber", "receptionNumber", "status", "comments"],
    },

    ResponseHandlerDTO: {
        type: "object",
        properties: {
            data: { type: "object", additionalProperties: true, example: { test: "rewee" } },
            message: { type: "string", example: "Mensaje de respuesta"},
            errorCode: { type: "number", example: -1 },
            code: { type: "string", example: "ERR01"},
            httpStatus: { type: "number", example: 200 },
            success: { type: "boolean", example: true},
            detailError: { type: "object", additionalProperties: true, example: { detailError: "rewee" } },
            timeStamp: { type: "number", example: 4524535243523243},

        },
    },

    ReceptionDTO: {
        type: "object",
        properties: {
            supplierNumber: { type: "number", example: 76345 },
            orderNumber: { type: "string", example: "on345" },
            receptionId: { type: "string", pattern: UUID_PATTERN, example: UUID_EXAMPLE },
            status: { type: "number", example: 3},
        },
         required: ["supplierNumber", "orderNumber", "receptionId", "status"],
    },

};