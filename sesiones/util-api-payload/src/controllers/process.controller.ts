// src/controllers/process.controller.ts
import type { Request, Response, NextFunction } from 'express';
import { processService } from '../services/process.service.js';

export const processController = {
    async getAll(req: Request, res: Response, next: NextFunction) {
        try {
            const filters: { name?: string } = {};
            if (req.query.name) filters.name = req.query.name as string;

            const data = await processService.findAll(filters);
            res.json({ success: true, data, count: data.length });
        } catch (error) {
            next(error);
        }
    },

    async getById(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await processService.findById(id);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Proceso no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async create(req: Request, res: Response, next: NextFunction) {
        try {
            const data = await processService.create(req.body);
            res.status(201).json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async update(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await processService.update(id, req.body);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Proceso no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async delete(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const deleted = await processService.delete(id);

            if (!deleted) {
                return res.status(404).json({ success: false, error: 'Proceso no encontrado' });
            }

            res.json({ success: true, message: 'Proceso eliminado exitosamente' });
        } catch (error) {
            next(error);
        }
    }
};
