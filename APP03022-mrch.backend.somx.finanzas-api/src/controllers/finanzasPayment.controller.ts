import type { Request, Response, NextFunction } from "express";
import { StatusCodes, ReasonPhrases } from 'http-status-codes';
import * as svc from "@/services/finanzasPayment.service.js";
import { HttpError } from "@/utils/HttpError.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import {
    CreateFinanzasPaymentSchema,
    UpdateFinanzasPaymentSchema,
    ListFinanzasPaymentsQuerySchema,
    type CreateFinanzasPaymentDto,
    type UpdateFinanzasPaymentDto,
    type ListFinanzasPaymentQuery,
} from "@/schemas/finanzasPayment.schema.js";

// GET /finanzas-payment
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const q: ListFinanzasPaymentQuery = ListFinanzasPaymentsQuerySchema.parse(req.body);
        const response = await svc.list(q); 
        

         res.json(response);
    } catch (e) {
        res.json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}

// POST /finanzas-payment
export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        //console.log('[DEBUG] Query params:', req.body);
        const dto: CreateFinanzasPaymentDto = CreateFinanzasPaymentSchema.parse(req.body);
        const created = await svc.create(dto);
        //console.log('[DEBUG] Paso el Create', '');
        res.status(201).json(created);
    } catch (e) { 
        res.status(404).json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e); 
    }
}

// PATCH /accounts-payable
export async function update(req: Request, res: Response, next: NextFunction) {
    try {
        const dto: UpdateFinanzasPaymentDto = UpdateFinanzasPaymentSchema.parse(req.body);
        const updated = await svc.update(dto);
        //if (!updated) return res.status(404).json({ message: "Not found" });
        res.json(updated);
    } catch (e) { 
        res.json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e); 
    }
}