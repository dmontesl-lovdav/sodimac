import type { Request, Response, NextFunction } from 'express';
import * as supplierService from '@/services/supplier.service.js';

export async function listSuppliers(req: Request, res: Response, next: NextFunction) {
    try {
        const xUserTypes = req.headers['x-user-types'] as string | undefined;

        const filters: import('@/repositories/supplier.repo.js').SupplierFilters = {
            page:     req.query.page     ? Number(req.query.page)     : 1,
            pageSize: req.query.pageSize ? Number(req.query.pageSize) : 20,
        };
        if (req.query.supplierNumber) filters.supplierNumber = String(req.query.supplierNumber);
        if (req.query.businessName)   filters.businessName   = String(req.query.businessName);
        if (req.query.status !== undefined) filters.status   = Number(req.query.status);

        const result = await supplierService.listSuppliers(filters, xUserTypes);

        if (!result.success) {
            res.status(400).json(result);
            return;
        }

        res.json(result);
    } catch (error) {
        next(error);
    }
}
