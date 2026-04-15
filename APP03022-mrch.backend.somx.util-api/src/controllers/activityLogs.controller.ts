import type { NextFunction, Request, Response } from "express";
import { randomUUID } from 'node:crypto';
import {
    CreateActivityLogSchema,
    ListActivityLogQuerySchema,
    type CreateActivityLogDto,
    type ListActivityLogQueryDto
} from "@/schemas/activityLog.schema.js";
import * as svc from "@/services/activityLogs.service.js";
import { logger } from "@/utils/logger.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';

// POST /activity-logs/
export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: CreateActivityLogDto = CreateActivityLogSchema.parse(req.body);
        res.status(201).json({ message: 'Data received, processing in background.' });
        svc.CreateLogActivity(dto);
    } catch (e) {
        logger.error({ body: req.body, err: e }, "Register 'Save Log' FAILED");
        res.status(400).json({ ...ResponseHandler.responseBuilder("ERROR " + e, null, -1, StatusCodes.BAD_REQUEST, false, "") });
        next(e);
    }
}

// GET /activity-logs/
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: ListActivityLogQueryDto = ListActivityLogQuerySchema.parse(req.body);
        const result = await svc.ReadLogActivity(dto);
        res.status(result.httpStatus).json(result);
    } catch (e) {
        logger.error({ body: req.body, err: e }, "Register 'List Logs' FAILED");
        res.status(400).json({ ...ResponseHandler.responseBuilder("ERROR " + e, null, -1, StatusCodes.BAD_REQUEST, false, "") });
        next(e);
    }
}

// GET /activity-logs/uuid
export async function uuid(_req: Request, res: Response, next: NextFunction) {
    try {
        res.status(201).json(randomUUID());
    } catch (e) {
        logger.error({ err: e }, "Register 'Create UUID' FAILED");
        res.status(400).json({ ...ResponseHandler.responseBuilder("ERROR " + e, null, -1, StatusCodes.BAD_REQUEST, false, "") });
        next(e);
    }
}
