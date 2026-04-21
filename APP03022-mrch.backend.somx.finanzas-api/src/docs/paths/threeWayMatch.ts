import type { OpenAPIV3 } from "openapi-types";

export const threeWayMatchPaths: OpenAPIV3.PathsObject = {
    "/three-way-match": {
        get: {
            tags: ["ThreeWayMatch"],
            summary: "Search Three Way Match records",
            parameters: [
                {
                    in: "query",
                    name: "tipoFecha",
                    required: true,
                    schema: {
                        type: "string",
                        enum: [
                            "fechaRecepcion",
                            "fechaTimbrado",
                            "fechaOrdenCompra",
                            "fechaPago",
                        ],
                    },
                },
                {
                    in: "query",
                    name: "fechaInicio",
                    required: true,
                    schema: { type: "string", format: "date" },
                },
                {
                    in: "query",
                    name: "fechaFin",
                    required: true,
                    schema: { type: "string", format: "date" },
                },
                { in: "query", name: "numeroProveedor", schema: { type: "string" } },
                { in: "query", name: "ordenCompra", schema: { type: "string" } },
                { in: "query", name: "recepcion", schema: { type: "string" } },
                { in: "query", name: "page", schema: { type: "integer", example: 1 } },
                { in: "query", name: "limit", schema: { type: "integer", example: 20 } },
            ],
            responses: {
                200: {
                    description: "Three Way Match results",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    data: {
                                        type: "array",
                                        items: {
                                            $ref: "#/components/schemas/ThreeWayMatch",
                                        },
                                    },
                                    total: { type: "integer" },
                                    page: { type: "integer" },
                                    limit: { type: "integer" },
                                    totalPages: { type: "integer" },
                                },
                            },
                        },
                    },
                },
            },
        },
    },

    "/three-way-match/export/csv": {
        get: {
            tags: ["ThreeWayMatch"],
            summary: "Export Three Way Match results to CSV",
            parameters: [
                {
                    in: "query",
                    name: "tipoFecha",
                    required: true,
                    schema: {
                        type: "string",
                        enum: [
                            "fechaRecepcion",
                            "fechaTimbrado",
                            "fechaOrdenCompra",
                            "fechaPago",
                        ],
                    },
                },
                {
                    in: "query",
                    name: "fechaInicio",
                    required: true,
                    schema: { type: "string", format: "date" },
                },
                {
                    in: "query",
                    name: "fechaFin",
                    required: true,
                    schema: { type: "string", format: "date" },
                },
                { in: "query", name: "numeroProveedor", schema: { type: "string" } },
                { in: "query", name: "ordenCompra", schema: { type: "string" } },
                { in: "query", name: "recepcion", schema: { type: "string" } },
            ],
            responses: {
                200: {
                    description: "CSV file generated",
                    content: {
                        "text/csv": {
                            schema: {
                                type: "string",
                                format: "binary",
                            },
                        },
                    },
                },
            },
        },
    },

    "/three-way-match/export/xlsx": {
        get: {
            tags: ["ThreeWayMatch"],
            summary: "Export Three Way Match results to XLSX",
            parameters: [
                {
                    in: "query",
                    name: "tipoFecha",
                    required: true,
                    schema: {
                        type: "string",
                        enum: [
                            "fechaRecepcion",
                            "fechaTimbrado",
                            "fechaOrdenCompra",
                            "fechaPago",
                        ],
                    },
                },
                {
                    in: "query",
                    name: "fechaInicio",
                    required: true,
                    schema: { type: "string", format: "date" },
                },
                {
                    in: "query",
                    name: "fechaFin",
                    required: true,
                    schema: { type: "string", format: "date" },
                },
                { in: "query", name: "numeroProveedor", schema: { type: "string" } },
                { in: "query", name: "ordenCompra", schema: { type: "string" } },
                { in: "query", name: "recepcion", schema: { type: "string" } },
            ],
            responses: {
                200: {
                    description: "XLSX file generated",
                    content: {
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                        {
                            schema: {
                                type: "string",
                                format: "binary",
                            },
                        },
                    },
                },
            },
        },
    },

    // ✅ NEW
    "/three-way-match/run": {
        post: {
            tags: ["ThreeWayMatch"],
            summary: "Run Three Way Match batch (trigger from frontend)",
            requestBody: {
                required: false,
                content: {
                    "application/json": {
                        schema: {
                            $ref: "#/components/schemas/RunThreeWayMatchBody",
                        },
                    },
                },
            },
            responses: {
                200: {
                    description: "Batch executed",
                    content: {
                        "application/json": {
                            schema: {
                                $ref: "#/components/schemas/RunThreeWayMatchResponse",
                            },
                        },
                    },
                },
            },
        },
    },
};