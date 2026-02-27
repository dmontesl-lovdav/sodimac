// src/controllers/itemType.controller.ts
import type { Request, Response, NextFunction } from 'express';
import { itemTypeService } from '../services/itemType.service.js';

export const itemTypeController = {
    async getAll(req: Request, res: Response, next: NextFunction) {
        try {
            const filters: { name?: string } = {};
            if (req.query.name) filters.name = req.query.name as string;

            const data = await itemTypeService.findAll(filters);
            res.json({ success: true, data, count: data.length });
        } catch (error) {
            next(error);
        }
    },

    async getById(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await itemTypeService.findById(id);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Tipo de elemento no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async create(req: Request, res: Response, next: NextFunction) {
        try {
            const data = await itemTypeService.create(req.body);
            res.status(201).json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async update(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await itemTypeService.update(id, req.body);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Tipo de elemento no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async delete(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const deleted = await itemTypeService.delete(id);

            if (!deleted) {
                return res.status(404).json({ success: false, error: 'Tipo de elemento no encontrado' });
            }

            res.json({ success: true, message: 'Tipo de elemento eliminado exitosamente' });
        } catch (error) {
            next(error);
        }
    }
};
