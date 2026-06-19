import { z } from "zod";

export const UUID = z.string().uuid();

export const AuditTipoEventoSchema = z.enum(["ERROR", "ALERTA", "INFO"]);
export const AuditTipoEventoFilterSchema = z.enum(["ALL", "ERROR", "ALERTA", "INFO"]);

export const CreateAuditLogSchema = z
    .object({
        idTransaccion: UUID,
        idAplicativo: z.string().min(1), // service_name
        idModulo: z.string().min(1),     // modulo
        paso: z.string().min(1),
        detalle: z.string().min(1).optional().nullable(), // message_detail
        fechaHora: z.coerce.date().optional(), // si no viene, se usa NOW
        tipoEvento: AuditTipoEventoSchema,
        idUsuario: z.string().min(1).optional().nullable(), // user_id (string hoy)

        // requeridos si tipoEvento=ERROR (validación en refine)
        idError: z.string().min(1).optional().nullable(),   // codigo_error
        idMensaje: z.string().min(1).optional().nullable(),
        mensaje: z.string().min(1).optional().nullable(),   // message
        log: z.string().min(1).optional().nullable(),       // log
    })
    .superRefine((v, ctx) => {
        if (v.tipoEvento === "ERROR") {
            if (!v.idError) ctx.addIssue({ code: "custom", path: ["idError"], message: "idError is required when tipoEvento=ERROR" });
            if (!v.mensaje) ctx.addIssue({ code: "custom", path: ["mensaje"], message: "mensaje is required when tipoEvento=ERROR" });
            if (!v.log) ctx.addIssue({ code: "custom", path: ["log"], message: "log is required when tipoEvento=ERROR" });
        }
    });

export const ListAuditLogsQuerySchema = z
    .object({
        idAplicativo: z.string().min(1).optional(),
        tipoEvento: AuditTipoEventoFilterSchema.default("ALL"),
        codigoError: z.string().min(1).optional(),
        idTransaccion: UUID.optional(),
        modulo: z.string().min(1).optional(),
        search: z.string().min(1).optional(),

        ids: z
        .union([UUID, z.array(UUID)])
        .optional()
        .transform((v) => {
            if (!v) return undefined;
            return Array.isArray(v) ? v : [v];
        }),



        fechaInicio: z.coerce.date(),
        fechaFin: z.coerce.date(),

        page: z.coerce.number().int().min(1).default(1),
        limit: z.coerce.number().int().min(1).max(200).default(10),
    })
    .strict();

export const AuditLogIdParamSchema = z.object({ id: UUID }).strict();

export const AuditLogTransactionParamSchema = z
    .object({
        idTransaccion: UUID,
    })
    .strict();

export type CreateAuditLogDto = z.infer<typeof CreateAuditLogSchema>;
export type ListAuditLogsQuery = z.infer<typeof ListAuditLogsQuerySchema>;
export type AuditLogIdParam = z.infer<typeof AuditLogIdParamSchema>;
export type AuditLogTransactionParam = z.infer<typeof AuditLogTransactionParamSchema>;