import { z } from "zod";

/** UUID v4 */
export const UUID = z.string().uuid();

/** Para crear */
export const CreateAccountsPayableSchema = z.object({
    orderNumber: z.string().min(1),
    vendorNumber: z.number().int(),
    sourceId: z.number().int(),
    totalAmount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    currency: z.string().length(3),
    status: z.number().int(),
    orderDate: z.coerce.date(),
    deliveryDate: z.coerce.date().optional().nullable(),
    terms: z.string().optional().nullable(),
    createdBy: z.number().int().optional().nullable(),
});

/** PATCH */
export const UpdateAccountsPayableSchema = z
    .object({
        orderNumber: z.string().min(1).optional(),
        vendorNumber: z.number().int().optional(),
        sourceId: z.number().int().optional(),
        totalAmount: z
            .string()
            .regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places")
            .optional(),
        currency: z.string().length(3).optional(),
        status: z.number().int().optional(),
        orderDate: z.coerce.date().optional(),
        deliveryDate: z.coerce.date().optional().nullable(),
        terms: z.string().optional().nullable(),
        createdBy: z.number().int().optional().nullable(),
        updatedBy: z.number().int().optional(),
    })
    .strict();

export const ListAccountsQuerySchema = z.object({
    status: z.coerce.number().int().optional(),
    vendorNumber: z.coerce.number().int().optional(),
    from: z.coerce.date().optional(),
    to: z.coerce.date().optional(),
    limit: z.coerce.number().int().min(1).max(200).default(100),
});

export const IdParamSchema = z.object({ id: UUID });

export type CreateAccountsPayableDto = z.infer<typeof CreateAccountsPayableSchema>;
export type UpdateAccountsPayableDto = z.infer<typeof UpdateAccountsPayableSchema>;
export type ListAccountsQuery = z.infer<typeof ListAccountsQuerySchema>;
