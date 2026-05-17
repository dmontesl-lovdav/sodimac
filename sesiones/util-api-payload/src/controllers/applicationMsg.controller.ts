// src/controllers/applicationMsg.controller.ts
import type { Request, Response, NextFunction } from 'express';
import { applicationMsgService } from '../services/applicationMsg.service.js';

export const applicationMsgController = {
    async getAll(req: Request, res: Response, next: NextFunction) {
        try {
            const filters: { idApplication?: number; idMessage?: number } = {};
            if (req.query.idApplication) filters.idApplication = Number(req.query.idApplication);
            if (req.query.idMessage) filters.idMessage = Number(req.query.idMessage);

            const data = await applicationMsgService.findAll(filters);
            res.json({ success: true, data, count: data.length });
        } catch (error) {
            next(error);
        }
    },

    async getById(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const data = await applicationMsgService.findById(id);

            if (!data) {
                return res.status(404).json({ success: false, error: 'Relacion mensaje-aplicativo no encontrada' });
            }

            res.json({ success: true, data });
        } catch (error) {
            next(error);
        }
    },

    async create(req: Request, res: Response, next: NextFunction) {
        try {
            const data = await applicationMsgService.create(req.body);
            res.status(201).json({ success: true, data });
        } catch (error: any) {
            if (error.code === '23505') { // unique violation
                return res.status(409).json({ success: false, error: 'Ya existe esta relacion mensaje-aplicativo' });
            }
            next(error);
        }
    },

    async delete(req: Request, res: Response, next: NextFunction) {
        try {
            const id = Number(req.params.id);
            const deleted = await applicationMsgService.delete(id);

            if (!deleted) {
                return res.status(404).json({ success: false, error: 'Relacion mensaje-aplicativo no encontrada' });
            }

            res.json({ success: true, message: 'Relacion eliminada exitosamente' });
        } catch (error) {
            next(error);
        }
    }
};
