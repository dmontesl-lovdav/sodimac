import { z } from "zod/v4";


/** Para crear */
export const CreateReceiptionSkuSchema = z.object({
    sku: z.string({ message: "Invalid field `sku` on ReceiptionSku. value : null or empty" }),
    description: z.string({message: "Invalid field `description` on ReceiptionSku. value cannot be empty, null or blank"}),
    quantity: z.number().min(1),
    unitCost: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    totalCost: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    status: z.number().int(),
    createdBy: z.number().min(1)
});

export type CreateReceiptionSkuDto = z.infer<typeof CreateReceiptionSkuSchema>;