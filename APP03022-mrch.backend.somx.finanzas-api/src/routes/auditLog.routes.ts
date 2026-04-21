import { Router } from "express";
import * as controller from "@/controllers/auditLog.controller.js";
import { validateBody, validateQuery, validateParams } from "@/middlewares/validate.js";
import {
    CreateAuditLogSchema,
    ListAuditLogsQuerySchema,
    AuditLogIdParamSchema,
    AuditLogTransactionParamSchema,
} from "@/schemas/auditLog.schema.js";

const r = Router();

// POST /audit-logs (registro)
r.post("/", validateBody(CreateAuditLogSchema), controller.create);

// GET /audit-logs
r.get("/", validateQuery(ListAuditLogsQuerySchema), controller.list);

// GET /audit-logs/export/csv (antes de /:id)
r.get("/export/csv", validateQuery(ListAuditLogsQuerySchema), controller.exportCsv);

// GET /audit-logs/transaction/:idTransaccion (antes de /:id)
r.get(
    "/transaction/:idTransaccion",
    validateParams(AuditLogTransactionParamSchema),
    controller.detailByTransaction
);

// GET /audit-logs/:id
r.get("/:id", validateParams(AuditLogIdParamSchema), controller.detail);

export default r;