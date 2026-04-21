import type { Request, Response, NextFunction } from 'express';
import * as svc from '@/services/accountStatement.service.js';
import {
    ListAccountStatementQuerySchema,
    UuidParamSchema,
} from '@/schemas/accountStatement.schema.js';

export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const query = ListAccountStatementQuerySchema.parse(req.query);
        const result = await svc.search(query);
        res.json(result);
    } catch (e) {
        next(e);
    }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = UuidParamSchema.parse(req.params);
        const row = await svc.getById(uuid);
        if (!row) return res.status(404).json({ message: 'Not found' });
        res.json(row);
    } catch (e) {
        next(e);
    }
}

export async function confirmReview(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = UuidParamSchema.parse(req.params);
        const updated = await svc.confirmReview(uuid);
        if (!updated) return res.status(404).json({ message: 'Not found' });
        res.json(updated);
    } catch (e) {
        next(e);
    }
}

export async function requestReview(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = UuidParamSchema.parse(req.params);
        const updated = await svc.requestReview(uuid);
        if (!updated) return res.status(404).json({ message: 'Not found' });
        res.json(updated);
    } catch (e) {
        next(e);
    }
}

export async function getPdf(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = UuidParamSchema.parse(req.params);
        const result = await svc.getPdf(uuid);
        if (!result) return res.status(404).json({ message: 'Not found' });
        if (result.redirectUrl) {
            return res.redirect(302, result.redirectUrl);
        }
        res.status(501).json({ message: 'PDF service not configured' });
    } catch (e) {
        next(e);
    }
}
