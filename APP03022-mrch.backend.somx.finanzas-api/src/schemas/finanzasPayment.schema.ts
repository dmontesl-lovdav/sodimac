import { z } from "zod/v4";

/** Para crear detalle (existente) */
export const CreateFinanzasPaymentSchema = z.object({
    company: z.number().int().min(1, { message: "Invalid field `company` on Payment. value : null or empti" }),
    documentNumber: z.string().nonempty({ message: "Invalid field `documentNumber` on Payment. value cannot be empty, null or blank" }),
    documentReference: z.string().nonempty({ message: "Invalid field `documentReference` on Payment. value cannot be empty, null or blank" }),
    vendorNumber: z.number().int().min(1, { message: "Invalid field `vendorNumber` on Payment. value : NotAllowed" }),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    documentType: z.string().nonempty({ message: "Invalid field `documentType` on Payment. value cannot be empty, null or blank" }),
    sapDocument: z.string().nonempty({ message: "Invalid field `sapDocument` on Payment. value cannot be empty, null or blank" }),
    paymentDate: z.coerce.date().nonoptional({ message: "Invalid field `paymentDate` on Payment. value cannot be empty, null or blank" }),
    status: z.number().int().min(1, { message: "Invalid field `status` on Payment. value : 0" }),
    paymentHeaderUuid: z.uuidv4().optional(),
});

/** Para actualizar (PATCH) */
export const UpdateFinanzasPaymentSchema = z.object({
    vendorNumber: z.number().int().min(1, { message: "Invalid field `supplierNumber` on Payment. value : NotAllowed" }),
    documentNumber: z.string().nonempty({ message: "Invalid field `documentNumber` on Payment. value cannot be empty, null or blank" }),
    sapDocument: z.string().nonempty({ message: "Invalid field `sapDocument` on Payment. value cannot be empty, null or blank" }),
    documentType: z.string().nonempty({ message: "Invalid field `documentType` on Payment. value cannot be empty, null or blank" }),
    status: z.number().int().min(1, { message: "Invalid field `status` on Payment. value : 0" })
}).strict();

export const ListFinanzasPaymentsQuerySchema = z.object({
    createdAtInitial: z.coerce.date().nonoptional({ message: "Invalid field `createdAtInitial` on Payment. value cannot be empty, null or blank" }),
    createdAtEnd: z.coerce.date().nonoptional({ message: "Invalid field `createdAtEnd` on Payment. value cannot be empty, null or blank" }),

    vendorNumber: z.coerce.number().int().optional(),
    finanzasPaymentUuid: z.uuidv4().optional(),
    paymentDate: z.coerce.date().optional(),
    documentNumber: z.string().optional(),
    sapDocument: z.string().optional(),

    pageNumber: z.coerce.number().int(),
    pageSize: z.coerce.number().int(),
}).strict();

/** detalle dentro del create agrupado */
export const CreateFinanzasPaymentDetailItemSchema = z.object({
    documentNumber: z.string().nonempty({ message: "Invalid field `documentNumber` on Payment detail" }),
    documentReference: z.string().nonempty({ message: "Invalid field `documentReference` on Payment detail" }),
    amount: z.string().regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places"),
    documentType: z.string().nonempty({ message: "Invalid field `documentType` on Payment detail" }),
    sapDocument: z.string().nonempty({ message: "Invalid field `sapDocument` on Payment detail" }),
    paymentLineType: z.enum(["INCOME", "CREDIT_NOTE"]),
});

/** crear cabecera + detalles */
export const CreateFinanzasPaymentHeaderWithDetailsSchema = z.object({
    company: z.number().int().min(1),
    anio: z.number().int().min(2000).max(9999),
    vendorNumber: z.number().int().min(1),
    currency: z.string().length(3).optional().default("MXN"),
    totalAmount: z.string()
        .regex(/^\d+(\.\d{1,2})?$/, "decimal with 2 places")
        .refine(v => Number(v) >= 0, { message: "totalAmount must be >= 0" }),
    paymentDate: z.coerce.date(),
    status: z.number().int().min(1).default(1),
    createdBy: z.number().int().optional(),
    details: z.array(CreateFinanzasPaymentDetailItemSchema).min(1, { message: "At least one detail row is required" }),
});

export type CreateFinanzasPaymentDto = z.infer<typeof CreateFinanzasPaymentSchema>;
export type UpdateFinanzasPaymentDto = z.infer<typeof UpdateFinanzasPaymentSchema>;
export type ListFinanzasPaymentQuery = z.infer<typeof ListFinanzasPaymentsQuerySchema>;
export type CreateFinanzasPaymentHeaderWithDetailsDto = z.infer<typeof CreateFinanzasPaymentHeaderWithDetailsSchema>;

export const PaymentHeaderUuidParamSchema = z.object({
    paymentHeaderUuid: z.uuidv4()
});

export const ListFinanzasPaymentDetailsByHeaderQuerySchema = z.object({
    pageNumber: z.coerce.number().int().min(1).default(1),
    pageSize: z.coerce.number().int().min(1).max(200).default(20),
}).strict();

export type PaymentHeaderUuidParamDto = z.infer<typeof PaymentHeaderUuidParamSchema>;
export type ListFinanzasPaymentDetailsByHeaderQueryDto = z.infer<typeof ListFinanzasPaymentDetailsByHeaderQuerySchema>;