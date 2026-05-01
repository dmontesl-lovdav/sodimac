import type { Request, Response, NextFunction } from 'express';
import * as elementService from '@/services/catalogElement.service.js';
import {
    CatalogElementCreateSchema,
    CatalogElementStatusSchema,
    CatalogElementUpdateSchema
} from '@/dto/catalog.dto.js';

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

export async function getElements(req: Request, res: Response, next: NextFunction) {
    try {
        const catalogId = Number(req.params.catalogId);
        if (Number.isNaN(catalogId)) return res.status(400).json({ error: 'ID inválido' });

        const q = req.query;
        const result = await elementService.findElements({
            catalogId,
            elementId: toIntOrNull(q.idElemento),
            element: toStringOrNull(q.elemento),
            value: toStringOrNull(q.valor),
            parentCatalogId: toIntOrNull(q.idCatalogoPadre),
            parentElementId: toIntOrNull(q.idElementoPadre),
            status: toIntOrNull(q.estatus),
            key: toStringOrNull(q.clave),
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

export async function getElementById(req: Request, res: Response, next: NextFunction) {
    try {
        const catalogId = Number(req.params.catalogId);
        const elementId = Number(req.params.elementId);
        const result = await elementService.findElementById(catalogId, elementId);
        if (!result) return res.status(404).json({ error: 'Elemento no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function createElement(req: Request, res: Response, next: NextFunction) {
    try {
        const catalogId = Number(req.params.catalogId);
        const dto = CatalogElementCreateSchema.parse(req.body);
        const created = await elementService.createElement(catalogId, dto, userId(req));
        res.status(201).json(created);
    } catch (err) {
        next(err);
    }
}

export async function updateElement(req: Request, res: Response, next: NextFunction) {
    try {
        const catalogId = Number(req.params.catalogId);
        const elementId = Number(req.params.elementId);
        const dto = CatalogElementUpdateSchema.parse(req.body);
        const updated = await elementService.updateElement(catalogId, elementId, dto, userId(req));
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

export async function changeStatus(req: Request, res: Response, next: NextFunction) {
    try {
        const elementId = Number(req.params.elementId);
        const dto = CatalogElementStatusSchema.parse(req.body);
        const updated = await elementService.changeStatus(elementId, dto.status, userId(req));
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

export async function getPrimaryCatalogs(_req: Request, res: Response, next: NextFunction) {
    try {
        const result = await elementService.findPrimaryCatalogs();
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getActiveElements(req: Request, res: Response, next: NextFunction) {
    try {
        const catalogId = Number(req.params.catalogId);
        const result = await elementService.findActiveElements(catalogId);
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCatalogDetail(req: Request, res: Response, next: NextFunction) {
    try {
        const catalogId = Number(req.params.catalogId);
        const result = await elementService.findCatalogById(catalogId);
        if (!result) return res.status(404).json({ error: 'Catálogo no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

