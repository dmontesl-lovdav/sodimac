import type { OpenAPIV3 } from "openapi-types";

export const healthcheckPaths: OpenAPIV3.PathsObject = {
    "/healthcheck": {
        get: {
            tags: ["Healthcheck"],
            summary: "Validate Finanzas API and database health",
            description:
                "Returns one internal healthcheck record from database. If data is returned, the API and DB connection are alive.",
            responses: {
                200: {
                    description: "Healthcheck data found",
                    content: {
                        "application/json": {
                            schema: {
                                type: "object",
                                properties: {
                                    alive: { type: "boolean", example: true },
                                    count: { type: "integer", example: 1 },
                                    data: {
                                        type: "array",
                                        items: { $ref: "#/components/schemas/Healthcheck" },
                                    },
                                },
                            },
                        },
                    },
                },
                404: {
                    description: "No healthcheck records found",
                },
            },
        },
    },
};