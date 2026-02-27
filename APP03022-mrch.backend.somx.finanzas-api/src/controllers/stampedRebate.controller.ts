import type { Request, Response, NextFunction } from "express";
import * as svc from "@/services/stampedRebate.service.js";
import { HttpError } from "@/utils/HttpError.js";
import {
    CreateStampedRebateSchema,
    UpdateStampedRebateSchema,
    ListStampedRebateQuerySchema,
    IdParamSchema,
    type CreateStampedRebateDto,
    type UpdateStampedRebateDto,
    type ListStampedRebateQuery,
} from "@/schemas/stampedRebate.schema.js";

// GET /stamped-rebates
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const q: ListStampedRebateQuery = ListStampedRebateQuerySchema.parse(req.query);
        const rows = await svc.list(q);

        if (!rows.length) throw new HttpError(404, "No records found for that filter");

        res.json(rows);
    } catch (e) {
        next(e);
    }
}

// GET /stamped-rebates/:id
export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = IdParamSchema.parse(req.params);
        const row = await svc.get(id);
        if (!row) return res.status(404).json({ message: "Not found" });
        res.json(row);
    } catch (e) { next(e); }
}

// POST /stamped-rebates
export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: CreateStampedRebateDto = CreateStampedRebateSchema.parse(req.body);
        const created = await svc.create(dto);
        res.status(201).json(created);
    } catch (e) { next(e); }
}

// PUT /stamped-rebates/:id
export async function update(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = IdParamSchema.parse(req.params);
        const dto: UpdateStampedRebateDto = UpdateStampedRebateSchema.parse(req.body);
        const updated = await svc.update(id, dto);
        if (!updated) return res.status(404).json({ message: "Not found" });
        res.json(updated);
    } catch (e) { next(e); }
}

// DELETE /stamped-rebates/:id
export async function remove(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = IdParamSchema.parse(req.params);
        await svc.remove(id);
        res.status(204).end();
    } catch (e) { next(e); }
}

// GET /stamped-rebates/export/csv - Export to CSV
export async function exportCsv(req: Request, res: Response, next: NextFunction) {
    try {
        const csvData = await svc.generateCsvReport();

        const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
        const filename = `stamped_rebate_report_${timestamp}.csv`;

        res.setHeader('Content-Type', 'text/csv; charset=utf-8');
        res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
        res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');

        res.send(csvData);
    } catch (e) {
        next(e);
    }
}
