import { Router, type Router as RouterType } from "express";
import * as controller from "@/controllers/auditLog.controller.js";
import { validateBody, validateQuery, validateParams } from "@/middlewares/validate.js";
import {
    CreateAuditLogSchema,
    ListAuditLogsQuerySchema,
    AuditLogIdParamSchema,
    AuditLogTransactionParamSchema,
} from "@/schemas/auditLog.schema.js";

const r: RouterType = Router();

r.post("/", validateBody(CreateAuditLogSchema), controller.create);

r.get("/", validateQuery(ListAuditLogsQuerySchema), controller.list);

r.get("/export/csv", validateQuery(ListAuditLogsQuerySchema), controller.exportCsv);

r.get(
    "/transaction/:idTransaccion",
    validateParams(AuditLogTransactionParamSchema),
    controller.detailByTransaction
);

r.get("/:id", validateParams(AuditLogIdParamSchema), controller.detail);

export default r;
