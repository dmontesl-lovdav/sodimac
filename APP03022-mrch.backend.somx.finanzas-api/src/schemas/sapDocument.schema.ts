import { z } from "zod";

export const UUID = z.string().uuid();

export function collectFiscalUuids(input: {
    fiscalUuid?: string | null | undefined;
    fiscalUuids?: string[] | null | undefined;
}): string[] {
    const set = new Set<string>();
    if (input.fiscalUuid) set.add(input.fiscalUuid);
    for (const uuid of input.fiscalUuids ?? []) {
        if (uuid) set.add(uuid);
    }
    return [...set];
}

const fiscalUuidFields = {
    fiscalUuid: UUID.optional().nullable(),
    fiscalUuids: z.array(UUID).optional(),
};

export const CreateSapDocumentSchema = z
    .object({
        documentNumber: z.string().min(1).max(100),
        referenceNumber: z.string().min(1).max(100),
        vendorNumber: z.number().int(),
        amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
        source: z.number().int(),
        docSap: z.string().min(1).max(15),
        message: z.string().max(254).optional().nullable(),
        sapStatus: z.number().int().min(0).max(3).default(1),
        documentType: z.string().min(1).max(5),
        createdBy: z.number().int().optional().nullable(),
        ...fiscalUuidFields,
    })
    .superRefine((data, ctx) => {
        if (collectFiscalUuids(data).length === 0) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                message: "At least one fiscalUuid is required",
                path: ["fiscalUuids"],
            });
        }
    });

export const UpdateSapDocumentSchema = z
    .object({
        documentNumber: z.string().min(1).max(100).optional(),
        referenceNumber: z.string().min(1).max(100).optional(),
        vendorNumber: z.number().int().optional(),
        amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places").optional(),
        source: z.number().int().optional(),
        docSap: z.string().min(1).max(15).optional(),
        message: z.string().max(254).optional().nullable(),
        sapStatus: z.number().int().min(0).max(3).optional(),
        documentType: z.string().min(1).max(5).optional(),
        updatedBy: z.number().int().optional(),
        createdBy: z.number().int().optional().nullable(),
        ...fiscalUuidFields,
    })
    .strict();

export const ListSapDocumentQuerySchema = z.object({
    sapStatus: z.coerce.number().int().optional(),
    vendorNumber: z.coerce.number().int().optional(),
    documentType: z.string().optional(),
    source: z.coerce.number().int().optional(),
    fiscalUuid: UUID.optional(),
    limit: z.coerce.number().int().min(1).max(200).default(100),
});

export const IdParamSchema = z.object({ uuid: UUID });

export const FiscalUuidParamSchema = z.object({ fiscalUuid: UUID });

export type CreateSapDocumentDto = z.infer<typeof CreateSapDocumentSchema>;
export type UpdateSapDocumentDto = z.infer<typeof UpdateSapDocumentSchema>;
export type ListSapDocumentQuery = z.infer<typeof ListSapDocumentQuerySchema>;
