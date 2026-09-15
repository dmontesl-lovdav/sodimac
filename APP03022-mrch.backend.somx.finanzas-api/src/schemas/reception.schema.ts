import { z } from "zod/v4";
import {
    CreateReceiptionSkuSchema,
} from "@/schemas/receptionSku.schema.js";

const optionalQueryInt = (): z.ZodType<number | undefined> =>
    z.preprocess(
        (val) =>
            val === "" || val === null || typeof val === "undefined" ? undefined : val,
        z.coerce.number().int().optional()
    );

export const ListReceptionQuerySchema = z.object({
    receptionDateAtInitial: z.coerce.date().nonoptional({message: "Invalid field `receptionDateAtInitial`. value cannot be empty, null or blank"}),
    receptionDateAtEnd: z.coerce.date().nonoptional({message: "Invalid field `receptionDateAtEnd`. value cannot be empty, null or blank"}),
    createdAtInitial: z.coerce.date().optional(),
    createdAtEnd: z.coerce.date().optional(),
    supplierNumber: z.number().int().optional(),
    status: z.number().int().optional(),
    orderNumber: z.string().optional(),
    receptionId: z.string().optional(),
    receptionTypeId: z.number().int().optional(),

    pageNumber: z.number().int(),
    pageSize: z.number().int(),
});

export const ListReceptionQuerySchemaV2 = z.object({
    receptionDateAtInitial: z.coerce.date().nonoptional({message: "Invalid field `receptionDateAtInitial`. value cannot be empty, null or blank"}),
    receptionDateAtEnd: z.coerce.date().nonoptional({message: "Invalid field `receptionDateAtEnd`. value cannot be empty, null or blank"}),
    createdAtInitial: z.coerce.date().optional(),
    createdAtEnd: z.coerce.date().optional(),
    supplierNumber: z.number().int().optional(),
    status: z.number().int().optional(),
    orderNumber: z.string().optional(),
    receptionId: z.string().optional(),
    receptionTypeId: optionalQueryInt(),

    pageNumber: z.string(),
    pageSize: z.string(),
});

function withReceptionTypeAlias(input: unknown): unknown {
    if (!input || typeof input !== "object" || Array.isArray(input)) {
        return input;
    }
    const row = input as Record<string, unknown>;
    if (row.receptionTypeId != null && String(row.receptionTypeId).trim() !== "") {
        return input;
    }
    const alias = row.tipoRecepcion ?? row.idTipoRecepcion ?? row.receptionType;
    if (alias == null || String(alias).trim() === "") {
        return input;
    }
    return { ...row, receptionTypeId: alias };
}

/** Para crear */
export const CreateReceiptionSchema = z.preprocess(
    withReceptionTypeAlias,
    z.object({
    receptionNumber: z.string(),
    originId: z.number().optional(),
    origin: z.string().nullable().optional(),   //El origen puede venir en String,hay que convertirlo a Int
    receptionTypeId: z.preprocess(
        (val) =>
            val === "" || val === null || typeof val === "undefined" ? undefined : val,
        z.coerce.number().int().min(1).optional()
    ),
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

})
);

export type ListReceptionQueryDto = z.infer<typeof ListReceptionQuerySchema>;
export type ListReceptionQueryDtoV2 = z.infer<typeof ListReceptionQuerySchemaV2>;
export type CreateReceiptionDto = z.infer<typeof CreateReceiptionSchema>;