import { z } from "zod/v4";

export const ListMigoDocumentsQuerySchema = z.object({
    publishedAtStart: z.coerce.date({ error: "La fecha inicio de publicación es obligatoria" }),
    publishedAtEnd: z.coerce.date({ error: "La fecha final de publicación es obligatoria" }),
    status: z.coerce.number().int().optional(),
    fileName: z.string().optional(),
    pageNumber: z.coerce.number().int().default(1),
    pageSize: z.coerce.number().int().default(10),
});

export const AuthorizeMigoSchema = z.object({
    migoDocumentId: z.string().uuid(),
});

export const RejectMigoSchema = z.object({
    migoDocumentId: z.string().uuid(),
    rejectionReason: z.string().min(1, "El motivo de rechazo es obligatorio").max(500),
});

export const ListMigoReceptionsQuerySchema = z.object({
    migoDocumentId: z.string().uuid().optional(),
    pageNumber: z.coerce.number().int().default(1),
    pageSize: z.coerce.number().int().default(10),
});

export const MigoDocumentIdParamSchema = z.object({
    id: z.string().uuid(),
});

export type ListMigoDocumentsQueryDto = z.infer<typeof ListMigoDocumentsQuerySchema>;
export type AuthorizeMigoDto = z.infer<typeof AuthorizeMigoSchema>;
export type RejectMigoDto = z.infer<typeof RejectMigoSchema>;
export type ListMigoReceptionsQueryDto = z.infer<typeof ListMigoReceptionsQuerySchema>;
