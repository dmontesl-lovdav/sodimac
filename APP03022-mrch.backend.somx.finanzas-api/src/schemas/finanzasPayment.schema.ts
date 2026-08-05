import { z } from "zod/v4";

const DecimalAmountSchema = z
    .string()
    .regex(/^\d+(\.\d{1,2})?$/, "decimal with up to 2 places");

const StatusSchema = z
    .number()
    .int()
    .min(0, {
        message: "Invalid field `status` on Payment. value must be greater than or equal to 0",
    });

/**
 * Crea un detalle de pago y lo puede relacionar con una cabecera existente.
 */
export const CreateFinanzasPaymentSchema = z
    .object({
        company: z.number().int().min(1, {
            message: "Invalid field `company` on Payment. value cannot be empty or less than 1",
        }),

        documentNumber: z.string().nonempty({
            message:
                "Invalid field `documentNumber` on Payment. value cannot be empty, null or blank",
        }),

        documentReference: z.string().nonempty({
            message:
                "Invalid field `documentReference` on Payment. value cannot be empty, null or blank",
        }),

        vendorNumber: z.number().int().min(1, {
            message: "Invalid field `vendorNumber` on Payment. value is not allowed",
        }),

        amount: DecimalAmountSchema,

        currency: z.string().length(3).optional().default("MXN"),

        documentType: z.string().nonempty({
            message:
                "Invalid field `documentType` on Payment. value cannot be empty, null or blank",
        }),

        sapDocument: z.string().nonempty({
            message:
                "Invalid field `sapDocument` on Payment. value cannot be empty, null or blank",
        }),

        paymentDate: z.coerce.date().nonoptional({
            message:
                "Invalid field `paymentDate` on Payment. value cannot be empty, null or blank",
        }),

        status: StatusSchema.optional().default(0),

        paymentHeaderUuid: z.uuidv4().nullable().optional(),

        createdBy: z.number().int().nullable().optional(),
    })
    .strict();

/**
 * Parámetro para identificar un detalle de manera única.
 *
 * PATCH /finanzas-payment/:finanzasPaymentUuid
 */
export const FinanzasPaymentUuidParamSchema = z
    .object({
        finanzasPaymentUuid: z.uuidv4(),
    })
    .strict();

/**
 * PATCH parcial de un detalle.
 *
 * Todos los campos son opcionales, pero debe enviarse al menos uno.
 * No permite modificar el UUID primario ni los campos createdAt/createdBy.
 */
export const UpdateFinanzasPaymentSchema = z
    .object({
        finanzasPaymentUuid: z.uuidv4(),

        company: z.number().int().min(1).optional(),

        documentNumber: z
            .string()
            .nonempty({
                message:
                    "Invalid field `documentNumber` on Payment. value cannot be empty, null or blank",
            })
            .optional(),

        documentReference: z
            .string()
            .nonempty({
                message:
                    "Invalid field `documentReference` on Payment. value cannot be empty, null or blank",
            })
            .optional(),

        vendorNumber: z
            .number()
            .int()
            .min(1, {
                message:
                    "Invalid field `vendorNumber` on Payment. value is not allowed",
            })
            .optional(),

        amount: DecimalAmountSchema.optional(),
        currency: z.string().length(3).optional(),

        documentType: z
            .string()
            .nonempty()
            .optional(),

        sapDocument: z
            .string()
            .nonempty()
            .optional(),

        paymentDate: z.coerce.date().optional(),
        status: StatusSchema.optional(),
        paymentHeaderUuid: z.uuidv4().nullable().optional(),
        updatedBy: z.number().int().nullable().optional(),
    })
    .strict()
    .refine(
        ({ finanzasPaymentUuid: _uuid, ...patch }) =>
            Object.keys(patch).length > 0,
        {
            message:
                "At least one field must be provided to update the payment",
        }
    );

export const ListFinanzasPaymentsQuerySchema = z
    .object({
        createdAtInitial: z.coerce.date().nonoptional({
            message:
                "Invalid field `createdAtInitial` on Payment. value cannot be empty, null or blank",
        }),

        createdAtEnd: z.coerce.date().nonoptional({
            message:
                "Invalid field `createdAtEnd` on Payment. value cannot be empty, null or blank",
        }),

        vendorNumber: z.coerce.number().int().optional(),
        finanzasPaymentUuid: z.uuidv4().optional(),
        paymentDate: z.coerce.date().optional(),
        documentNumber: z.string().optional(),
        sapDocument: z.string().optional(),

        pageNumber: z.coerce.number().int().min(1),
        pageSize: z.coerce.number().int().min(1).max(200),
    })
    .strict();

/**
 * Detalle incluido dentro de la creación agrupada.
 */
export const CreateFinanzasPaymentDetailItemSchema = z
    .object({
        documentNumber: z.string().nonempty({
            message: "Invalid field `documentNumber` on Payment detail",
        }),

        documentReference: z.string().nonempty({
            message: "Invalid field `documentReference` on Payment detail",
        }),

        amount: DecimalAmountSchema,

        documentType: z.string().nonempty({
            message: "Invalid field `documentType` on Payment detail",
        }),

        sapDocument: z.string().nonempty({
            message: "Invalid field `sapDocument` on Payment detail",
        }),

        paymentLineType: z.enum(["INCOME", "CREDIT_NOTE"]),

        /**
         * Permite controlar el estatus individual del detalle.
         * Si no llega, el servicio puede heredar el status de la cabecera.
         */
        status: StatusSchema.optional(),
    })
    .strict();

/**
 * Crea cabecera y, opcionalmente, sus detalles.
 *
 * paymentHeaderUuid:
 * - Si llega, se conserva el UUID proporcionado por el consumidor.
 * - Si no llega, el backend puede generar uno.
 *
 * details:
 * - Puede omitirse.
 * - Puede enviarse como arreglo vacío.
 */
export const CreateFinanzasPaymentHeaderWithDetailsSchema = z
    .object({
        paymentHeaderUuid: z.uuidv4().optional(),

        company: z.number().int().min(1),

        anio: z.number().int().min(2000).max(9999),

        vendorNumber: z.number().int().min(1),

        currency: z.string().length(3).optional().default("MXN"),

        totalAmount: DecimalAmountSchema.refine(
            (value) => Number(value) >= 0,
            {
                message: "totalAmount must be greater than or equal to 0",
            }
        ),

        paymentDate: z.coerce.date(),

        status: StatusSchema.optional().default(0),

        createdBy: z.number().int().nullable().optional(),

        details: z
            .array(CreateFinanzasPaymentDetailItemSchema)
            .optional()
            .default([]),
    })
    .strict();

export const PaymentHeaderUuidParamSchema = z
    .object({
        paymentHeaderUuid: z.uuidv4(),
    })
    .strict();

export const ListFinanzasPaymentDetailsByHeaderQuerySchema = z
    .object({
        pageNumber: z.coerce.number().int().min(1).default(1),
        pageSize: z.coerce.number().int().min(1).max(200).default(20),
    })
    .strict();

export type CreateFinanzasPaymentDto = z.infer<
    typeof CreateFinanzasPaymentSchema
>;

export type UpdateFinanzasPaymentDto = z.infer<
    typeof UpdateFinanzasPaymentSchema
>;

export type UpdateFinanzasPaymentPatchDto = Omit<
    UpdateFinanzasPaymentDto,
    "finanzasPaymentUuid"
>;

export type FinanzasPaymentUuidParamDto = z.infer<
    typeof FinanzasPaymentUuidParamSchema
>;

export type ListFinanzasPaymentQuery = z.infer<
    typeof ListFinanzasPaymentsQuerySchema
>;

export type CreateFinanzasPaymentHeaderWithDetailsDto = z.infer<
    typeof CreateFinanzasPaymentHeaderWithDetailsSchema
>;

export type PaymentHeaderUuidParamDto = z.infer<
    typeof PaymentHeaderUuidParamSchema
>;

export type ListFinanzasPaymentDetailsByHeaderQueryDto = z.infer<
    typeof ListFinanzasPaymentDetailsByHeaderQuerySchema
>;