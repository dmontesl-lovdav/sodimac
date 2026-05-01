import type { Request, Response, NextFunction } from 'express';
import * as statusTrainService from '@/services/statusTrain.service.js';
import { StatusTrainCreateSchema, StatusTrainUpdateSchema } from '@/dto/statusTrain.dto.js';

export async function validateTransition(req: Request, res: Response, next: NextFunction) {
    try {
        const optionId = Number(req.query.optionId);
        const sourceStatus = Number(req.query.sourceStatus);
        const targetStatus = Number(req.query.targetStatus);

        if (Number.isNaN(optionId) || Number.isNaN(sourceStatus) || Number.isNaN(targetStatus)) {
            return res.status(400).json({ error: 'Parámetros inválidos' });
        }

        const response = await statusTrainService.validateTransition(optionId, sourceStatus, targetStatus);
        if (!response.success) {
            return res.status(400).json(response);
        }
        res.json(response);
    } catch (err) {
        next(err);
    }
}

export async function getAllowedDestinations(req: Request, res: Response, next: NextFunction) {
    try {
        const optionId = Number(req.query.optionId);
        const sourceStatus = Number(req.query.sourceStatus);

        if (Number.isNaN(optionId) || Number.isNaN(sourceStatus)) {
            return res.status(400).json({ error: 'Parámetros inválidos' });
        }

        const response = await statusTrainService.getAllowedDestinations(optionId, sourceStatus);
        if (!response.success) {
            return res.status(400).json(response);
        }
        res.json(response);
    } catch (err) {
        next(err);
    }
}

export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto = StatusTrainCreateSchema.parse(req.body);
        const created = await statusTrainService.create(dto);
        res.status(201).json(created);
    } catch (err) {
        next(err);
    }
}

export async function update(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const optionId = Number(req.query.optionId);
        if (Number.isNaN(id) || Number.isNaN(optionId)) {
            return res.status(400).json({ error: 'Parámetros inválidos' });
        }
        const dto = StatusTrainUpdateSchema.parse(req.body);
        const updated = await statusTrainService.update(id, optionId, dto);
        if (!updated) return res.status(404).json({ error: 'Regla no encontrada' });
        res.json(updated);
    } catch (err) {
        next(err);
    }
}

export async function deleteOne(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const ok = await statusTrainService.deleteById(id);
        if (!ok) return res.status(404).json({ error: 'Regla no encontrada' });
        res.status(204).send();
    } catch (err) {
        next(err);
    }
}

export async function findById(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const result = await statusTrainService.findById(id);
        if (!result) return res.status(404).json({ error: 'Regla no encontrada' });
        res.json(result);
    } catch (err) {
        next(err);
    }
}

