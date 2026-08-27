import type { Request, Response, NextFunction } from "express";
import * as svc from "@/services/rebate.service.js";
import { HttpError } from "@/utils/HttpError.js";
import {
    CreateRebateSchema,
    UpdateRebateSchema,
    ListRebateQuerySchema,
    RebateFilterSchema,
    IdParamSchema,
    type CreateRebateDto,
    type UpdateRebateDto,
    type ListRebateQuery,
    type RebateFilterDto,
} from "@/schemas/rebate.schema.js";
import { RebateCsvStream } from "@/utils/csvStream.js";

// GET /rebates/published - Obtener rebates publicados (status = 1)
export async function getPublishedRebates(req: Request, res: Response, next: NextFunction) {
    try {
        const q: ListRebateQuery = ListRebateQuerySchema.parse(req.query);
        const rows = await svc.listPublished(q);

        if (!rows.length) throw new HttpError(404, "No published rebates found");

        res.json(rows);
    } catch (e) {
        next(e);
    }
}

// GET /rebates/vendor/:vendorNumber - Obtener rebates por número de proveedor
export async function getRebatesByVendor(req: Request, res: Response, next: NextFunction) {
    try {
        const vendorNumberParam = req.params.vendorNumber as string;
        if (!vendorNumberParam) {
            throw new HttpError(400, "Vendor number is required");
        }

        const vendorNumber = parseInt(vendorNumberParam);
        const q: ListRebateQuery = ListRebateQuerySchema.parse(req.query);

        if (isNaN(vendorNumber)) {
            throw new HttpError(400, "Invalid vendor number");
        }

        const rows = await svc.listByVendor(vendorNumber, q);

        if (!rows.length) throw new HttpError(404, `No rebates found for vendor ${vendorNumber}`);

        res.json(rows);
    } catch (e) {
        next(e);
    }
}

// GET /rebates/search - Búsqueda con filtros dinámicos (Criteria API equivalent)
export async function searchRebates(req: Request, res: Response, next: NextFunction) {
    try {
        const filters: RebateFilterDto = RebateFilterSchema.parse(req.query);
        const rows = await svc.searchWithFilters(filters);

        if (!rows.length) throw new HttpError(404, "No rebates found matching filters");

        res.json(rows);
    } catch (e) {
        next(e);
    }
}

// GET /rebates/published/export/csv - Exportar rebates publicados a CSV (Streaming)
export async function exportPublishedRebatesToCsv(req: Request, res: Response, next: NextFunction) {
    try {
        const q: ListRebateQuery = ListRebateQuerySchema.parse(req.query);
        const rebates = await svc.listPublished(q);

        const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
        const filename = `published_rebates_${timestamp}.csv`;

        res.setHeader('Content-Type', 'text/csv; charset=utf-8');
        res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
        res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');

        // Use streaming instead of loading all data into memory
        const csvStream = new RebateCsvStream(rebates);
        csvStream.pipe(res);
    } catch (e) {
        next(e);
    }
}

// GET /rebates/export/csv - Exportar rebates filtrados a CSV (Streaming)
export async function exportFilteredRebatesToCsv(req: Request, res: Response, next: NextFunction) {
    try {
        const filters: RebateFilterDto = RebateFilterSchema.parse(req.query);
        const rebates = await svc.searchWithFilters(filters);

        const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
        const filename = `filtered_rebates_${timestamp}.csv`;

        res.setHeader('Content-Type', 'text/csv; charset=utf-8');
        res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
        res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');

        // Use streaming instead of loading all data into memory
        const csvStream = new RebateCsvStream(rebates);
        csvStream.pipe(res);
    } catch (e) {
        next(e);
    }
}

// GET /rebates - Lista general con paginación
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const q: ListRebateQuery = ListRebateQuerySchema.parse(req.query);
        const rows = await svc.list(q);

        if (!rows.length) throw new HttpError(404, "No records found for that filter");

        res.json(rows);
    } catch (e) {
        next(e);
    }
}

// GET /rebates/:id
export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = IdParamSchema.parse(req.params);
        const row = await svc.get(uuid);
        if (!row) return res.status(404).json({ message: "Not found" });
        res.json(row);
    } catch (e) { next(e); }
}

// POST /rebates
export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: CreateRebateDto = CreateRebateSchema.parse(req.body);
        const created = await svc.create(dto);
        res.status(201).json(created);
    } catch (e) { next(e); }
}

// PUT /rebates/:id
export async function update(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = IdParamSchema.parse(req.params);
        const dto: UpdateRebateDto = UpdateRebateSchema.parse(req.body);
        const updated = await svc.update(uuid, dto);
        if (!updated) return res.status(404).json({ message: "Not found" });
        res.json(updated);
    } catch (e) { next(e); }
}

// DELETE /rebates/:id
export async function remove(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = IdParamSchema.parse(req.params);
        await svc.remove(uuid);
        res.status(204).end();
    } catch (e) { next(e); }
}
