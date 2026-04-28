import { z } from "zod";

export const HealthcheckQuerySchema = z.object({});

export type HealthcheckQuery = z.infer<typeof HealthcheckQuerySchema>;