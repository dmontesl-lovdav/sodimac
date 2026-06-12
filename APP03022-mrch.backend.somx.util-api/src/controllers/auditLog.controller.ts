import type { Request, Response, NextFunction } from "express";
import * as svc from "@/services/auditLog.service.js";
import {
    CreateAuditLogSchema,
    ListAuditLogsQuerySchema,
    AuditLogIdParamSchema,
    AuditLogTransactionParamSchema,
} from "@/schemas/auditLog.schema.js";

export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto = CreateAuditLogSchema.parse(req.body);
        const result = await svc.create(dto);
        res.status(201).json(result);
    } catch (e) {
        next(e);
    }
}

export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const q = ListAuditLogsQuerySchema.parse(req.query);
        const result = await svc.list(q);
        res.json(result);
    } catch (e) {
        next(e);
    }
}

export async function detail(req: Request, res: Response, next: NextFunction) {
    try {
        const p = AuditLogIdParamSchema.parse(req.params);
        const result = await svc.detail(p.id);
        res.json(result);
    } catch (e) {
        next(e);
    }
}

export async function detailByTransaction(req: Request, res: Response, next: NextFunction) {
    try {
        const p = AuditLogTransactionParamSchema.parse(req.params);
        const result = await svc.detailByTransaction(p.idTransaccion);
        res.json(result);
    } catch (e) {
        next(e);
    }
}

export async function exportCsv(req: Request, res: Response, next: NextFunction) {
    try {
        const q = ListAuditLogsQuerySchema.parse(req.query);
        const csv = await svc.exportCsv(q);

        const timestamp = new Date().toISOString().replace(/[:.]/g, "-");

        res.setHeader("Content-Type", "text/csv; charset=utf-8");
        res.setHeader("Content-Disposition", `attachment; filename="audit_logs_${timestamp}.csv"`);
        res.send(csv);
    } catch (e) {
        next(e);
    }
}
