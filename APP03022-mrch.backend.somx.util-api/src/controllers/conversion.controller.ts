import type { Request, Response, NextFunction } from 'express';
import * as conversionService from '@/services/conversion.service.js';
import { ConversionCreateSchema, ConversionUpdateSchema } from '@/dto/conversion.dto.js';

function userId(req: Request): string {
    const h = req.header('X-User-Id');
    return h && h.trim() !== '' ? h : 'system';
}

function parseSortDir(raw: unknown): 'ASC' | 'DESC' {
    const s = String(raw ?? 'desc').toLowerCase();
    return s === 'asc' ? 'ASC' : 'DESC';
}

function toIntOrNull(v: unknown): number | null {
    if (v === undefined || v === null || v === '') return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
}

function toStringOrNull(v: unknown): string | null {
    if (v === undefined || v === null) return null;
    const s = String(v);
    return s.trim() === '' ? null : s;
}

export async function search(req: Request, res: Response, next: NextFunction) {
    try {
        const q = req.query;
        const result = await conversionService.search({
            sourceElementId: toIntOrNull(q.idElementoOrigen),
            targetElementId: toIntOrNull(q.idElemento),
            elemento: toStringOrNull(q.elemento),
            valor: toStringOrNull(q.valorElemento),
            catalogoOrigen: toStringOrNull(q.catalogoOrigen),
            estatus: toIntOrNull(q.estatus),
            page: Math.max(1, Number(q.page ?? 1) || 1),
            pageSize: Math.max(1, Number(q.pageSize ?? 10) || 10),
            sortBy: String(q.sortBy ?? 'createdAt'),
            sortDir: parseSortDir(q.sortDir)
        });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const result = await conversionService.getById(id);
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto = ConversionCreateSchema.parse(req.body);
        const created = await conversionService.create(dto, userId(req));
        res.status(201).json(created);
    } catch (err) {
        next(err);
    }
}

export async function update(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const dto = ConversionUpdateSchema.parse(req.body);
        const updated = await conversionService.update(id, dto, userId(req));
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

export async function setPrincipal(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const body = (req.body ?? {}) as Record<string, unknown>;
        const isPrincipalRaw = body.esPrincipal ?? body.conversionPrincipal ?? true;
        const isPrincipal = isPrincipalRaw === true || isPrincipalRaw === 'true';
        const updated = await conversionService.setPrincipal(id, isPrincipal, userId(req));
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

export async function deleteOne(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await conversionService.deleteById(id);
        res.status(204).send();
    } catch (err) {
        next(err);
    }
}

export async function deleteMultiple(req: Request, res: Response, next: NextFunction) {
    try {
        const body = (req.body ?? {}) as Record<string, unknown>;
        const raw = body.ids;
        if (Array.isArray(raw) && raw.length > 0) {
            const ids = raw.map(v => Number(v)).filter(n => Number.isFinite(n));
            await conversionService.deleteMultiple(ids);
        }
        res.status(204).send();
    } catch (err) {
        next(err);
    }
}

