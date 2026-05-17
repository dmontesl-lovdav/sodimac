import type { Request, Response, NextFunction } from 'express';
import * as svc from '@/services/accountStatement.service.js';
import {
    ListAccountStatementQuerySchema,
    UuidParamSchema,
} from '@/schemas/accountStatement.schema.js';

const WRN7029 = { success: false, code: 'WRN7029', message: 'El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador' };

function allowedVendors(req: Request): string[] | null | 'wrn7029' {
    const sec = req.security;
    if (!sec) return null;
    if (Array.isArray(sec.vendors) && sec.vendors.length === 0) return 'wrn7029';
    return sec.vendors;
}

export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === 'wrn7029') { res.status(400).json(WRN7029); return; }

        const query = ListAccountStatementQuerySchema.parse(req.query);
        const result = await svc.search(query, vendors);
        res.json(result);
    } catch (e) {
        next(e);
    }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === 'wrn7029') { res.status(400).json(WRN7029); return; }

        const { uuid } = UuidParamSchema.parse(req.params);
        const row = await svc.getById(uuid, vendors);
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
        const pdfBuffer = await svc.getPdf(uuid);
        if (!pdfBuffer) return res.status(404).json({ message: 'Not found' });

        res.setHeader('Content-Type', 'application/pdf');
        res.setHeader('Content-Disposition', `inline; filename="account_statement_${uuid}.pdf"`);
        res.setHeader('Content-Length', pdfBuffer.length.toString());

        return res.send(pdfBuffer);
    } catch (e) {
        next(e);
    }
}
