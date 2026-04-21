// src/schemas/threeWayMatchRun.schema.ts
import { z } from "zod";

export const RunThreeWayMatchBodySchema = z.object({
    // optional, default: yesterday
    fechaBase: z.coerce.date().optional(),

    // optional, default: 1
    intento: z.coerce.number().min(1).max(10).optional(),
});

export type RunThreeWayMatchBody = z.infer<typeof RunThreeWayMatchBodySchema>;