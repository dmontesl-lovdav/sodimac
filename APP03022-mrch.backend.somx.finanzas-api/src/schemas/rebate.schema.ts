import { z } from "zod";

export const UUID = z.string().uuid();

export const CreateRebateSchema = z.object({
    documentNumber: z.string().min(1).max(100),
    referenceNumber: z.string().min(1).max(100),
    sapDocument: z.string().min(1).max(50),
    vendorNumber: z.number().int(),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    source: z.number().int(),
    periodId: z.number().int(),
    dueDate: z.coerce.date(),
    postingDate: z.coerce.date(),
    status: z.number().int().default(1),
    createdBy: z.number().int().optional().nullable(),
});

export const UpdateRebateSchema = z.object({
    documentNumber: z.string().min(1).max(100).optional(),
    referenceNumber: z.string().min(1).max(100).optional(),
    sapDocument: z.string().min(1).max(50).optional(),
    vendorNumber: z.number().int().optional(),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places").optional(),
    source: z.number().int().optional(),
    periodId: z.number().int().optional(),
    dueDate: z.coerce.date().optional(),
    postingDate: z.coerce.date().optional(),
    status: z.number().int().optional(),
    updatedBy: z.number().int().optional(),
}).strict();

export const ListRebateQuerySchema = z.object({
    status: z.coerce.number().int().optional(),
    vendorNumber: z.coerce.number().int().optional(),
    documentNumber: z.string().optional(),
    source: z.coerce.number().int().optional(),
    periodId: z.coerce.number().int().optional(),
    from: z.coerce.date().optional(),
    to: z.coerce.date().optional(),
    limit: z.coerce.number().int().min(1).max(1000).default(20),
    page: z.coerce.number().int().min(0).default(0),
});

// Schema para filtros dinámicos
export const RebateFilterSchema = z.object({
    vendorNumber: z.coerce.number().int().optional(),
    documentNumber: z.string().optional(),
    sapDocument: z.string().optional(),
    status: z.coerce.number().int().optional(),
    periodId: z.coerce.number().int().optional(),
    limit: z.coerce.number().int().min(1).max(1000).default(20),
    page: z.coerce.number().int().min(0).default(0),
});

export const IdParamSchema = z.object({ id: UUID });

export type CreateRebateDto = z.infer<typeof CreateRebateSchema>;
export type UpdateRebateDto = z.infer<typeof UpdateRebateSchema>;
export type ListRebateQuery = z.infer<typeof ListRebateQuerySchema>;
export type RebateFilterDto = z.infer<typeof RebateFilterSchema>;
