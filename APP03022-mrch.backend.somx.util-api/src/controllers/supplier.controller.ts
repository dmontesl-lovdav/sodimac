import type { Request, Response, NextFunction } from 'express';
import * as supplierService from '@/services/supplier.service.js';
import { SupplierCreateSchema, SupplierUpdateSchema } from '@/dto/supplier.dto.js';
import { resolveUserKey } from '@/middlewares/permission.middleware.js';
import * as securityService from '@/services/security.service.js';
import type { SupplierSecurityFilter } from '@/repositories/supplier.repo.js';

function userId(req: Request): string {
    const h = req.header('X-User-Id');
    return h && h.trim() !== '' ? h : 'system';
}

async function resolveSupplierSecurity(req: Request): Promise<SupplierSecurityFilter | undefined> {
    const userKey = resolveUserKey(req);
    if (!userKey) return undefined;
    try {
        const data = await securityService.getUserAttributesByKey(userKey);
        const attrs = data?.attributes ?? [];
        const vendors = attrs
            .filter((a) => a.typeKey === 'ATR001')
            .map((a) => a.valueKey)
            .filter((v): v is string => typeof v === 'string' && v.trim() !== '');
        const typeIds = attrs
            .filter((a) => a.typeKey === 'ATR002')
            .map((a) => Number(String(a.valueKey ?? '').replace(/\D/g, '')))
            .filter((n) => !Number.isNaN(n) && n > 0);
        return {
            vendors: vendors.length > 0 ? vendors : null,
            typeIds: typeIds.length > 0 ? typeIds : null,
        };
    } catch {
        return undefined;
    }
}

export async function getAllSuppliers(req: Request, res: Response, next: NextFunction) {
    try {
        const statusRaw = req.query.status;
        const security = await resolveSupplierSecurity(req);
        const suppliers =
            statusRaw !== undefined && statusRaw !== null && statusRaw !== ''
                ? await supplierService.findByStatus(Number(statusRaw), security)
                : await supplierService.findAll(security);
        res.json(suppliers);
    } catch (err) {
        next(err);
    }
}

export async function getSupplierById(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const result = await supplierService.findById(id);
        if (!result) return res.status(404).json({ error: 'Proveedor no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getSupplierByNumber(req: Request, res: Response, next: NextFunction) {
    try {
        const result = await supplierService.findBySupplierNumber(String(req.params.supplierNumber));
        if (!result) return res.status(404).json({ error: 'Proveedor no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getSupplierTypeBlocked(req: Request, res: Response, next: NextFunction) {
    try {
        const supplierNumber = String(req.params.supplierNumber);
        const typeBlocked = await supplierService.isSupplierTypeBlocked(supplierNumber);
        res.json({ supplierNumber, typeBlocked });
    } catch (err) {
        next(err);
    }
}

export async function getSupplierByRfc(req: Request, res: Response, next: NextFunction) {
    try {
        const result = await supplierService.findByRfc(String(req.params.rfc));
        if (!result) return res.status(404).json({ error: 'Proveedor no encontrado' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function createSupplier(req: Request, res: Response, next: NextFunction) {
    try {
        const dto = SupplierCreateSchema.parse(req.body);
        const created = await supplierService.create(dto, userId(req));
        res.status(201).json(created);
    } catch (err) {
        next(err);
    }
}

export async function updateSupplier(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const dto = SupplierUpdateSchema.parse(req.body);
        const updated = await supplierService.update(id, dto, userId(req));
        if (!updated) return res.status(404).json({ error: 'Proveedor no encontrado' });
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

export async function deleteSupplier(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const ok = await supplierService.deleteSupplier(id, userId(req));
        if (!ok) return res.status(404).json({ error: 'Proveedor no encontrado' });
        res.status(204).send();
    } catch (err) {
        next(err);
    }
}

export async function getAllSupplierTypes(_req: Request, res: Response, next: NextFunction) {
    try {
        const result = await supplierService.findAllSupplierTypes();
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getAllPaymentConditions(_req: Request, res: Response, next: NextFunction) {
    try {
        const result = await supplierService.findAllPaymentConditions();
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function filterSuppliers(req: Request, res: Response, next: NextFunction) {
    try {
        const tipoProveedor = Number(req.query.tipoProveedor);
        const estatusBloqueo = Number(req.query.estatusBloqueo);
        if (Number.isNaN(tipoProveedor) || Number.isNaN(estatusBloqueo)) {
            return res.status(400).json({ error: 'Parámetros inválidos' });
        }
        const security = await resolveSupplierSecurity(req);
        const result = await supplierService.findByTypeAndBlockStatus(tipoProveedor, estatusBloqueo, security);
        res.json(result);
    } catch (err) {
        next(err);
    }
}

