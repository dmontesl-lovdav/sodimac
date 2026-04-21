import { z } from "zod";

const folioRegex = /^[A-Z]{2,4}-[0-9A-HJ-NP-Z]{8}$/;

export const CreateTransactionIdSchema = z.object({
    codigoModulo: z
        .string()
        .trim()
        .min(2)
        .max(4)
        .regex(/^[A-Z]+$/, "codigoModulo must contain only uppercase letters"),
    pantallaOrigen: z
        .string()
        .trim()
        .min(1)
        .max(100),
    caso: z
        .string()
        .trim()
        .min(1)
        .max(200),
    metadatos: z.record(z.string(), z.any()).optional(),
    idUsuario: z.string().trim().min(1).max(100).optional(),
    origen: z.string().trim().min(1).max(150).optional(),
});

export const TransactionIdFolioParamSchema = z.object({
    folioVisible: z
        .string()
        .trim()
        .regex(folioRegex, "folioVisible format is invalid"),
});

export type CreateTransactionIdDto = z.infer<typeof CreateTransactionIdSchema>;
export type TransactionIdFolioParamDto = z.infer<typeof TransactionIdFolioParamSchema>;