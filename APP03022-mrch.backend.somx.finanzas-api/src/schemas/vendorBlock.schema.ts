import { z } from "zod";

export const UUID = z.string().uuid();

export const CreateVendorBlockSchema = z.object({
    vendorNumber: z.number().int(),
    blockReason: z.string().min(1).max(10),
    blockDescription: z.string().optional().nullable(),
    startDate: z.coerce.date(),
    endDate: z.coerce.date().optional().nullable(),
    status: z.number().int().default(1),
    autoUnblock: z.boolean().default(false),
    blockType: z.string().min(1).max(5),
    createdBy: z.number().int().optional().nullable(),
});

export const UpdateVendorBlockSchema = z.object({
    vendorNumber: z.number().int().optional(),
    blockReason: z.string().min(1).max(10).optional(),
    blockDescription: z.string().optional().nullable(),
    startDate: z.coerce.date().optional(),
    endDate: z.coerce.date().optional().nullable(),
    status: z.number().int().optional(),
    autoUnblock: z.boolean().optional(),
    blockType: z.string().min(1).max(5).optional(),
    updatedBy: z.number().int().optional(),
}).strict();

export const ListVendorBlockQuerySchema = z.object({
    status: z.coerce.number().int().optional(),
    vendorNumber: z.coerce.number().int().optional(),
    blockType: z.string().optional(),
    autoUnblock: z.coerce.boolean().optional(),
    limit: z.coerce.number().int().min(1).max(200).default(100),
});

export const IdParamSchema = z.object({ id: UUID });

export type CreateVendorBlockDto = z.infer<typeof CreateVendorBlockSchema>;
export type UpdateVendorBlockDto = z.infer<typeof UpdateVendorBlockSchema>;
export type ListVendorBlockQuery = z.infer<typeof ListVendorBlockQuerySchema>;
