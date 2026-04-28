import type { OpenAPIV3 } from "openapi-types";

export const healthcheckSchemas: Record<string, OpenAPIV3.SchemaObject> = {
    Healthcheck: {
        type: "object",
        properties: {
            healthcheckUuid: { type: "string", format: "uuid" },
            serviceName: { type: "string", example: "finanzas-api" },
            status: { type: "string", example: "OK" },
            message: { type: "string", nullable: true, example: "Healthcheck record alive" },
            createdAt: { type: "string", format: "date-time" },
            updatedAt: { type: "string", format: "date-time", nullable: true },
        },
        required: ["healthcheckUuid", "serviceName", "status", "createdAt"],
    },
};