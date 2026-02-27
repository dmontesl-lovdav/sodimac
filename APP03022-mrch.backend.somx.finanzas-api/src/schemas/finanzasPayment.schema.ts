import { z } from "zod/v4";


/** Para crear */
export const CreateFinanzasPaymentSchema = z.object({
    company: z.number().int().min(1,{ message: "Invalid field `company` on Payment. value : null or empti" }),
    documentNumber: z.string().nonempty({message: "Invalid field `documentNumber` on Payment. value cannot be empty, null or blank"}),
    documentReference: z.string().nonempty({message: "Invalid field `documentReference` on Payment. value cannot be empty, null or blank"}),
    vendorNumber: z.number().int().min(1,{ message: "Invalid field `vendorNumber` on Payment. value : NotAllowed"}),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    documentType: z.string().nonempty({message: "Invalid field `documentType` on Payment. value cannot be empty, null or blank"}),
    sapDocument: z.string().nonempty({message: "Invalid field `sapDocument` on Payment. value cannot be empty, null or blank"}),
    paymentDate: z.coerce.date().nonoptional({message: "Invalid field `paymentDate` on Payment. value cannot be empty, null or blank"}),
    status: z.number().int().min(1,{message: "Invalid field `status` on Payment. value : 0"}),
});

/** Para actualizar (PATCH) — incluimos updatedBy */
export const UpdateFinanzasPaymentSchema = z.object({
    vendorNumber: z.number().int().min(1,{ message: "Invalid field `supplierNumber` on Payment. value : NotAllowed"}),
    documentNumber: z.string().nonempty({message: "Invalid field `documentNumber` on Payment. value cannot be empty, null or blank"}),
    sapDocument: z.string().nonempty({message: "Invalid field `sapDocument` on Payment. value cannot be empty, null or blank"}),
    documentType: z.string().nonempty({message: "Invalid field `documentType` on Payment. value cannot be empty, null or blank"}),
    status: z.number().int().min(1,{message: "Invalid field `status` on Payment. value : 0"})
}).strict();

export const ListFinanzasPaymentsQuerySchema = z.object({
    createdAtInitial: z.coerce.date().nonoptional({message: "Invalid field `createdAtInitial` on Payment. value cannot be empty, null or blank"}),
    createdAtEnd: z.coerce.date().nonoptional({message: "Invalid field `createdAtEnd` on Payment. value cannot be empty, null or blank"}),
    vendorNumber: z.number().int().optional(),
    finanzasPaymentUuid: z.uuidv4().optional(),
    paymentDate: z.coerce.date().optional(),
    documentNumber: z.string().optional(),
    sapDocument: z.string().optional(),

    pageNumber: z.number().int(),
    pageSize: z.number().int(),
});

export type CreateFinanzasPaymentDto = z.infer<typeof CreateFinanzasPaymentSchema>;
export type UpdateFinanzasPaymentDto = z.infer<typeof UpdateFinanzasPaymentSchema>;
export type ListFinanzasPaymentQuery = z.infer<typeof ListFinanzasPaymentsQuerySchema>;