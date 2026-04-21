import { z } from "zod/v4";
import {
    CreateReceiptionSkuSchema,
    type CreateReceiptionSkuDto,
} from "@/schemas/receptionSku.schema.js";

export const ListReceptionQuerySchema = z.object({
    receptionDateAtInitial: z.coerce.date().nonoptional({message: "Invalid field `receptionDateAtInitial`. value cannot be empty, null or blank"}),
    receptionDateAtEnd: z.coerce.date().nonoptional({message: "Invalid field `receptionDateAtEnd`. value cannot be empty, null or blank"}),
    createdAtInitial: z.coerce.date().optional(),
    createdAtEnd: z.coerce.date().optional(),
    supplierNumber: z.number().int().optional(),
    status: z.number().int().optional(),
    orderNumber: z.string().optional(),
    receptionId: z.string().optional(),

    pageNumber: z.number().int(),
    pageSize: z.number().int(),
});

/** Para crear */
export const CreateReceiptionSchema = z.object({
    receptionNumber: z.string(),
    originId: z.number().optional(),
    origin: z.string().nullable().optional(),   //El origen puede venir en String,hay que convertirlo a Int
    destinationId: z.number().int({message: "Invalid field `destinationId` on Reception. value cannot be empty, null or blank"}),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    comments: z.string(),
    guideNumber: z.string().optional(),
    //sapDocument: z.string().nonempty({message: "Invalid field `sapDocument` on Reception. value cannot be empty, null or blank"}),
    receptionDate: z.coerce.date().nonoptional({message: "Invalid field `receptionDate` on Reception. value cannot be empty, null or blank"}),
    status: z.number().int(),
    createdBy: z.number().int().min(1,{message: "Invalid field `createdBy` on Reception. value : 0"}),
    receiptSkuList: z.array(CreateReceiptionSkuSchema).min(1),
}).superRefine((data, ctx) => {

});

export type ListReceptionQueryDto = z.infer<typeof ListReceptionQuerySchema>;
export type CreateReceiptionDto = z.infer<typeof CreateReceiptionSchema>;