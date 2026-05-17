// src/controllers/message.controller.ts
import type { Request, Response, NextFunction } from 'express';
import { messageService } from '../services/message.service.js';

export const messageController = {
    async getAll(req: Request, res: Response, next: NextFunction) {
        try {
            const filters: { messageCode?: string; idMessageType?: number } = {};
            if (req.query.messageCode) filters.messageCode = req.query.messageCode as string;
            if (req.query.idMessageType) filters.idMessageType = Number(req.query.idMessageType);

            const data = await messageService.findAll(filters);
            res.json({ success: true, data, count: data.length });
        } catch (error) {
            next(error);
        }
    },

    async getById(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await messageService.findById(id);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Mensaje no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async getByCode(req: Request, res: Response, next: NextFunction) {
        try {
            const code = req.params.code as string;
            const data = await messageService.findByCode(code);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Mensaje no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async create(req: Request, res: Response, next: NextFunction) {
        try {
            const data = await messageService.create(req.body);
            res.status(201).json({ success: true, data });
        } catch (error: any) {
            if (error.code === '23505') { // unique violation
                return res.status(409).json({ success: false, error: 'Ya existe un mensaje con ese codigo' });
            }
            next(error);
        }
    },

    async update(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await messageService.update(id, req.body);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Mensaje no encontrado' });
            }

            res.json({ success: true, data });
        } catch (error: any) {
            if (error.code === '23505') {
                return res.status(409).json({ success: false, error: 'Ya existe un mensaje con ese codigo' });
            }
            next(error);
        }
    },

    async delete(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const deleted = await messageService.delete(id);

            if (!deleted) {
                return res.status(404).json({ success: false, error: 'Mensaje no encontrado' });
            }

            res.json({ success: true, message: 'Mensaje eliminado exitosamente' });
        } catch (error) {
            next(error);
        }
    }
};
