import { z } from "zod";

export const UUID = z.string().uuid();

export const CreateSapDocumentSchema = z.object({
    documentNumber: z.string().min(1).max(100),
    referenceNumber: z.string().min(1).max(100),
    vendorNumber: z.number().int(),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    source: z.number().int(),
    docSap: z.string().min(1).max(15),
    message: z.string().max(254).optional().nullable(),
    sapStatus: z.number().int().default(1),
    documentType: z.string().min(1).max(5),
    createdBy: z.number().int().optional().nullable(),
});

export const UpdateSapDocumentSchema = z.object({
    documentNumber: z.string().min(1).max(100).optional(),
    referenceNumber: z.string().min(1).max(100).optional(),
    vendorNumber: z.number().int().optional(),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places").optional(),
    source: z.number().int().optional(),
    docSap: z.string().min(1).max(15).optional(),
    message: z.string().max(254).optional().nullable(),
    sapStatus: z.number().int().optional(),
    documentType: z.string().min(1).max(5).optional(),
    updatedBy: z.number().int().optional(),
}).strict();

export const ListSapDocumentQuerySchema = z.object({
    sapStatus: z.coerce.number().int().optional(),
    vendorNumber: z.coerce.number().int().optional(),
    documentType: z.string().optional(),
    source: z.coerce.number().int().optional(),
    limit: z.coerce.number().int().min(1).max(200).default(100),
});

export const IdParamSchema = z.object({ id: UUID });

export type CreateSapDocumentDto = z.infer<typeof CreateSapDocumentSchema>;
export type UpdateSapDocumentDto = z.infer<typeof UpdateSapDocumentSchema>;
export type ListSapDocumentQuery = z.infer<typeof ListSapDocumentQuerySchema>;
