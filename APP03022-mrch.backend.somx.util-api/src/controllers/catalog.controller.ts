import type { Request, Response, NextFunction } from 'express';
import * as catalogHeaderService from '@/services/catalogHeader.service.js';

function parseLang(req: Request): number {
    const raw = req.query.lang;
    if (raw === undefined || raw === null || raw === '') return 1;
    const n = Number(raw);
    return Number.isFinite(n) ? n : 1;
}

export async function getAllCatalogs(req: Request, res: Response, next: NextFunction) {
    try {
        const lang = parseLang(req);
        const result = await catalogHeaderService.findAllCatalogs(lang);
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCatalogsByModule(req: Request, res: Response, next: NextFunction) {
    try {
        const module = String(req.params.module);
        const lang = parseLang(req);
        const result = await catalogHeaderService.findCatalogsByModule(module, lang);
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCatalogByCode(req: Request, res: Response, next: NextFunction) {
    try {
        const code = String(req.params.code);
        const lang = parseLang(req);
        const result = await catalogHeaderService.findCatalogByCode(code, lang);
        if (!result) return res.status(404).json({ error: 'Catálogo no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCatalogDetails(req: Request, res: Response, next: NextFunction) {
    try {
        const code = String(req.params.code);
        const lang = parseLang(req);
        const dependsOn = req.query.dependsOn as string | undefined;

        if (!dependsOn || dependsOn.trim() === '') {
            const result = await catalogHeaderService.findDetailsByCode(code, lang);
            return res.json(result);
        }

        const parts = dependsOn.split(':');
        if (parts.length !== 2) {
            return res.status(400).json({ error: 'Formato de dependsOn inválido' });
        }

        const targetCatalogCode = parts[0]!.trim();
        const targetExternalKey = parts[1]!.trim();

        const result = await catalogHeaderService.findDetailsByCodeWithDependency(
            code,
            targetCatalogCode,
            targetExternalKey,
            lang
        );
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCatalogDetailByKey(req: Request, res: Response, next: NextFunction) {
    try {
        const code = String(req.params.code);
        const key = String(req.params.key);
        const lang = parseLang(req);
        const result = await catalogHeaderService.findDetailByCodeAndKey(code, key, lang);
        if (!result) return res.status(404).json({ error: 'Detalle no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getMessageByKey(req: Request, res: Response, next: NextFunction) {
    try {
        const key = String(req.params.key);
        const lang = parseLang(req);
        const result = await catalogHeaderService.findDetailByKey(key, lang);
        if (!result) return res.status(404).json({ error: 'Mensaje no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getMessageByKeyFormatted(req: Request, res: Response, next: NextFunction) {
    try {
        const key = String(req.params.key);
        const lang = parseLang(req);
        let params: string[] = [];
        if (req.query.params !== undefined) {
            if (Array.isArray(req.query.params)) {
                params = (req.query.params as unknown[]).map(String);
            } else {
                params = String(req.query.params).split(',').map(s => s.trim());
            }
        }
        const result = await catalogHeaderService.findDetailByKeyFormatted(key, lang, params);
        if (!result) return res.status(404).json({ error: 'Mensaje no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCatalogById(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        if (Number.isNaN(id)) return res.status(400).json({ error: 'ID inválido' });
        const lang = parseLang(req);
        const result = await catalogHeaderService.findCatalogById(id, lang);
        if (!result) return res.status(404).json({ error: 'Catálogo no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCatalogByPrefix(req: Request, res: Response, next: NextFunction) {
    try {
        const prefix = String(req.params.prefix);
        const lang = parseLang(req);
        const result = await catalogHeaderService.findCatalogByPrefix(prefix, lang);
        if (!result) return res.status(404).json({ error: 'Catálogo no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

