import { z } from "zod";

export const UUID = z.string().uuid();

export const CreateFiscalPaymentSchema = z.object({
    paymentNumber: z.string().min(1).max(50),
    company: z.number().int(),
    documentNumber: z.string().min(1).max(100),
    referenceNumber: z.string().min(1).max(100),
    vendorNumber: z.number().int(),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    currency: z.string().length(3).default('MXN'),
    documentType: z.string().min(1).max(5),
    sapDocument: z.string().max(50).optional().nullable(),
    paymentDate: z.coerce.date(),
    status: z.number().int().default(1),
    paymentMethod: z.string().max(10).optional().nullable(),
    bankAccount: z.string().max(50).optional().nullable(),
    referencePayment: z.string().max(100).optional().nullable(),
    createdBy: z.number().int().optional().nullable(),
});

export const UpdateFiscalPaymentSchema = z.object({
    paymentNumber: z.string().min(1).max(50).optional(),
    company: z.number().int().optional(),
    documentNumber: z.string().min(1).max(100).optional(),
    referenceNumber: z.string().min(1).max(100).optional(),
    vendorNumber: z.number().int().optional(),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places").optional(),
    currency: z.string().length(3).optional(),
    documentType: z.string().min(1).max(5).optional(),
    sapDocument: z.string().max(50).optional().nullable(),
    paymentDate: z.coerce.date().optional(),
    status: z.number().int().optional(),
    paymentMethod: z.string().max(10).optional().nullable(),
    bankAccount: z.string().max(50).optional().nullable(),
    referencePayment: z.string().max(100).optional().nullable(),
    updatedBy: z.number().int().optional(),
}).strict();

export const ListFiscalPaymentQuerySchema = z.object({
    status: z.coerce.number().int().optional(),
    vendorNumber: z.coerce.number().int().optional(),
    company: z.coerce.number().int().optional(),
    documentType: z.string().optional(),
    from: z.coerce.date().optional(),
    to: z.coerce.date().optional(),
    limit: z.coerce.number().int().min(1).max(200).default(100),
});

export const IdParamSchema = z.object({ id: UUID });

export type CreateFiscalPaymentDto = z.infer<typeof CreateFiscalPaymentSchema>;
export type UpdateFiscalPaymentDto = z.infer<typeof UpdateFiscalPaymentSchema>;
export type ListFiscalPaymentQuery = z.infer<typeof ListFiscalPaymentQuerySchema>;
