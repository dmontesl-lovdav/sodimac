import {
    IdParamSchema,
    ListShippingGuideQuerySchema,
    UpdateShippingGuideSchema,
    IdParamGuideSchema,
    type ListShippingGuideQuery,
    type UpdateShippingGuideDto,
    type IdParamSchemaDto,
    type IdParamGuideDto
} from "@/schemas/shippingGuide.schema.js";
import * as shippingGuideService from "@/services/shippingGuide.service.js";
import type { NextFunction, Request, Response } from "express";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { logger } from "@/utils/logger.js";
import { logActivity, getTraceId } from '@/middlewares/logger.js';


// GET /shipping-guides/csv
export async function csvExport(req: Request, res: Response, next: NextFunction) {
    try {
        const criteria: ListShippingGuideQuery = ListShippingGuideQuerySchema.parse(req.query);
        return shippingGuideService.writeCsv(criteria, res);
    } catch (e) {
        logger.error("❌ ShippingGuide.csvExport. ERROR  : No fue posible exportar a csv. FAILED → data={} cause={}", req.query, e); 
        await logActivity(true, 'ERROR  : No fue posible exportar a csv, Favor de validar', e , req.query);
        res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e, null, -1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}

// GET /shipping-guides STM 577
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const q: ListShippingGuideQuery =
            ListShippingGuideQuerySchema.parse(req.query);   

        const response = await shippingGuideService.listPaginated(q);
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        await logActivity(true, 'ERROR  :', e , req.query);
        res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e, null, -1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}


// export async function list(req: Request, res: Response, next: NextFunction) {
//     try {
//         const q: ListShippingGuideQuery = ListShippingGuideQuerySchema.parse(req.body);
//         const response = await shippingGuideService.listPaginated(q);
//         res.status(response.httpStatus).json(response);
//     } catch (e) {
//         res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
//         next(e);
//     }
// }

// GET /shipping-guides/:uuid
export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const q: IdParamSchemaDto = IdParamSchema.parse(req.params);
        const response = await shippingGuideService.get(q.uuid);
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        await logActivity(true, 'ERROR  :', e , req.params);
        res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e, null, -1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}

// POST /shipping-guides  //ESTE CREATE SE SUSTITUYE POR EL CONTROLADOR cartaPorte.controller.ts
// export async function create(req: Request, res: Response, next: NextFunction) {
//     try {
//         const dto: CreateShippingGuideParentDto = CreateShippingGuideSchemaParent.parse(req.body);
//         const shippingGuideSchmaString: string = dto.content;
//         const data: CreateShippingGuideDto = CreateShippingGuideSchema.parse(JSON.parse(shippingGuideSchmaString));
//         const files = req.files as Express.Multer.File[];

//         const created = await svc.create(data, files, dto.folder);
//         res.status(201).json(created);
//     } catch (e) { 
//         res.status(400).json(ResponseHandler.responseBuilder("ERROR (BUS2004) : No fue posible registrar la guía carta porte, Favor de validar " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
//         next(e);
//     }
// }

// PUT /shipping-guides/:uuid
export async function updateByUuid(req: Request, res: Response, next: NextFunction) {
    try {
        const q: IdParamSchemaDto = IdParamSchema.parse(req.params);
        const dto: UpdateShippingGuideDto = UpdateShippingGuideSchema.parse(req.body);
        const response = await shippingGuideService.updateOneByUuid(q.uuid, dto);
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        await logActivity(true, 'ERROR  :', e , req.params);
        res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e, null, -1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}

// PATCH /shipping-guides/guide/:idGuide
export async function updateByGuide(req: Request, res: Response, next: NextFunction) {
    try {
        const q: IdParamGuideDto = IdParamGuideSchema.parse(req.params);
        const dto: UpdateShippingGuideDto = UpdateShippingGuideSchema.parse(req.body);
        const response = await shippingGuideService.updateOneByGuide(q.idGuide, dto);
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        await logActivity(true, 'ERROR  :', e , req.params);
        res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e, null, -1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}

// DELETE /shipping-guides/:id
export async function remove(req: Request, res: Response, next: NextFunction) {
    try {
        const q: IdParamSchemaDto = IdParamSchema.parse(req.params);
        await shippingGuideService.remove(q.uuid);
        res.status(204).json({ trace_id: getTraceId()});
    } catch (e) {
        await logActivity(true, 'ERROR  :', e , req.params);
        res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e, null, -1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}
