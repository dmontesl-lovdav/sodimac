import type { OpenAPIV3 } from "openapi-types";

export const healthcheckPaths: OpenAPIV3.PathsObject = {
    "/healthcheck": {
        get: {
            tags: ["Healthcheck"],
            summary: "Validate Finanzas API and database health",
            description:
                "Returns the Finanzas API healthcheck status from database. If this record is returned, the API, database connection, and healthcheck endpoint are working correctly.",
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