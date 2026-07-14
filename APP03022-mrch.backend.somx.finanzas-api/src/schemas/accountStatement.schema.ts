import { z } from 'zod';

export const UuidParamSchema = z.object({ uuid: z.string().uuid() });

export const ListAccountStatementQuerySchema = z.object({
    vendorNumber: z.coerce.number().int().optional(),
    supplierType: z.coerce.number().int().optional(),
    year: z.coerce.number().int().min(2026),
    month: z.union([z.literal('all'), z.coerce.number().int().min(1).max(12)]).optional(),
    page: z.coerce.number().int().min(1).default(1),
    pageSize: z.coerce.number().int().min(1).max(100).default(10),
});

export type ListAccountStatementQuery = z.infer<typeof ListAccountStatementQuerySchema>;

export const BatchAccountStatementBodySchema = z.object({
    supplierNumber: z.coerce.number().int().positive().optional(),
    year:           z.coerce.number().int().min(2020).optional(),
    month:          z.coerce.number().int().min(1).max(12).optional(),
});

export type BatchAccountStatementBody = z.infer<typeof BatchAccountStatementBodySchema>;
