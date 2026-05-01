import type { Request, Response, NextFunction } from 'express';
import * as supplierBlockService from '@/services/supplierBlock.service.js';
import { SupplierBlockCreateSchema, SupplierBlockUpdateSchema } from '@/dto/supplier.dto.js';

function userId(req: Request): string {
    const h = req.header('X-User-Id');
    return h && h.trim() !== '' ? h : 'system';
}

export async function getAllSupplierBlocks(req: Request, res: Response, next: NextFunction) {
    try {
        const statusRaw = req.query.status;
        const result =
            statusRaw !== undefined && statusRaw !== null && statusRaw !== ''
                ? await supplierBlockService.findByStatus(Number(statusRaw))
                : await supplierBlockService.findAll();
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getSupplierBlockById(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const result = await supplierBlockService.findById(id);
        if (!result) return res.status(404).json({ error: 'Bloqueo no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getBlocksBySupplierNumber(req: Request, res: Response, next: NextFunction) {
    try {
        const result = await supplierBlockService.findBySupplierNumber(String(req.params.supplierNumber));
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getCurrentActiveBlocks(req: Request, res: Response, next: NextFunction) {
    try {
        const result = await supplierBlockService.findCurrentActiveBlocks(String(req.params.supplierNumber));
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function isSupplierBlocked(req: Request, res: Response, next: NextFunction) {
    try {
        const supplierNumber = String(req.params.supplierNumber);
        const blocked = await supplierBlockService.isSupplierCurrentlyBlocked(supplierNumber);
        res.json({ supplierNumber, blocked });
    } catch (err) {
        next(err);
    }
}

export async function getBlocksAtDate(req: Request, res: Response, next: NextFunction) {
    try {
        const supplierNumber = String(req.params.supplierNumber);
        const date = String(req.query.date ?? '');
        if (!date) return res.status(400).json({ error: 'El parámetro date es requerido' });
        const result = await supplierBlockService.findActiveBlocksAtDate(supplierNumber, date);
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function createSupplierBlock(req: Request, res: Response, next: NextFunction) {
    try {
        const dto = SupplierBlockCreateSchema.parse(req.body);
        const created = await supplierBlockService.create(dto, userId(req));
        res.status(201).json(created);
    } catch (err) {
        next(err);
    }
}

export async function updateSupplierBlock(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const dto = SupplierBlockUpdateSchema.parse(req.body);
        const updated = await supplierBlockService.update(id, dto, userId(req));
        if (!updated) return res.status(404).json({ error: 'Bloqueo no encontrado' });
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

export async function deleteSupplierBlock(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const ok = await supplierBlockService.deleteBlock(id, userId(req));
        if (!ok) return res.status(404).json({ error: 'Bloqueo no encontrado' });
        res.status(204).send();
    } catch (err) {
        next(err);
    }
}

