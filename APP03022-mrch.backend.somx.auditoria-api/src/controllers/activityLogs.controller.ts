import type { NextFunction, Request, Response } from "express";
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
import { z } from "zod/v4";
// POST /activity-logs/
export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: CreateActivityLogDto = CreateActivityLogSchema.parse(req.body);
        res.status(201).json({ message: 'Data received, processing in background.' });
        svc.CreateLogActivity(dto);

    } catch (e) { 
        logger.error("❌ Register 'Save Log Failes' FAILED → data={} cause={}", req.body, e); 
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR " + e ,null,-1, StatusCodes.BAD_REQUEST, false, "")});
        next(e);
    }
}

//get /activity-logs/
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: ListActivityLogQueryDto = ListActivityLogQuerySchema.parse(req.body);
        const list =  await svc.ReadLogActivity(dto);
        res.status(list.httpStatus).json(list);
    } catch (e) { 
        logger.error("❌ Register 'Listed Log Failes' FAILED → data={} cause={}", req.body, e); 
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR " + e ,null,-1, StatusCodes.BAD_REQUEST, false, "")});
        next(e);
    }
}

//get /activity-logs/uuid
export async function uuid(req: Request, res: Response, next: NextFunction) {
    try {
        const uuid =  crypto.randomUUID();
        res.status(201).json(uuid);
    } catch (e) { 
        logger.error("❌ Register 'Create UUID' FAILED → data={} cause={}", req.body, e); 
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR " + e ,null,-1, StatusCodes.BAD_REQUEST, false, "")});
        next(e);
    }
}