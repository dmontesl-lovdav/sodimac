import { z } from "zod";

export const UUID = z.string().uuid();

export const CreateStampedRebateSchema = z.object({
    documentNumber: z.string().min(1).max(100),
    referenceNumber: z.string().min(1).max(100),
    status: z.number().int().default(1),
    createdBy: z.number().int().optional().nullable(),
});

export const UpdateStampedRebateSchema = z.object({
    documentNumber: z.string().min(1).max(100).optional(),
    referenceNumber: z.string().min(1).max(100).optional(),
    status: z.number().int().optional(),
    updatedBy: z.number().int().optional(),
}).strict();

export const ListStampedRebateQuerySchema = z.object({
    status: z.coerce.number().int().optional(),
    documentNumber: z.string().optional(),
    limit: z.coerce.number().int().min(1).max(200).default(100),
});

export const IdParamSchema = z.object({ id: UUID });

export type CreateStampedRebateDto = z.infer<typeof CreateStampedRebateSchema>;
export type UpdateStampedRebateDto = z.infer<typeof UpdateStampedRebateSchema>;
export type ListStampedRebateQuery = z.infer<typeof ListStampedRebateQuerySchema>;
