// src/controllers/module.controller.ts
import type { Request, Response, NextFunction } from 'express';
import { moduleService } from '../services/module.service.js';

export const moduleController = {
    async getAll(req: Request, res: Response, next: NextFunction) {
        try {
            const filters: { name?: string } = {};
            if (req.query.name) filters.name = req.query.name as string;

            const data = await moduleService.findAll(filters);
            res.json({ success: true, data, count: data.length });
        } catch (error) {
            next(error);
        }
    },

    async getById(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await moduleService.findById(id);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Modulo no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async create(req: Request, res: Response, next: NextFunction) {
        try {
            const data = await moduleService.create(req.body);
            res.status(201).json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async update(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await moduleService.update(id, req.body);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Modulo no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async delete(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const deleted = await moduleService.delete(id);

            if (!deleted) {
                return res.status(404).json({ success: false, error: 'Modulo no encontrado' });
            }

            res.json({ success: true, message: 'Modulo eliminado exitosamente' });
        } catch (error) {
            next(error);
        }
    }
};
