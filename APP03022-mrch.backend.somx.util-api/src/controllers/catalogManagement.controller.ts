import type { Request, Response, NextFunction } from 'express';
import * as catalogService from '@/services/catalogManagement.service.js';
import { CatalogCreateSchema, CatalogUpdateSchema } from '@/dto/catalog.dto.js';

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

export async function getCatalogs(req: Request, res: Response, next: NextFunction) {
    try {
        const q = req.query;
        const result = await catalogService.findCatalogs({
            id: toIntOrNull(q.id),
            name: toStringOrNull(q.nombre),
            description: toStringOrNull(q.descripcion),
            catalogType: toStringOrNull(q.tipo),
            status: toIntOrNull(q.estatus),
            code: toStringOrNull(q.code),
            prefix: toStringOrNull(q.prefix),
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

export async function getCatalogById(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        if (Number.isNaN(id)) return res.status(400).json({ error: 'ID inválido' });
        const result = await catalogService.findById(id);
        if (!result) return res.status(404).json({ error: 'Catálogo no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function createCatalog(req: Request, res: Response, next: NextFunction) {
    try {
        const dto = CatalogCreateSchema.parse(req.body);
        const created = await catalogService.createCatalog(dto, userId(req));
        res.status(201).json(created);
    } catch (err) {
        next(err);
    }
}

export async function updateCatalog(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const dto = CatalogUpdateSchema.parse(req.body);
        const updated = await catalogService.updateCatalog(id, dto, userId(req));
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

