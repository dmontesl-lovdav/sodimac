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
import * as svcAxios from "@/services/axios.service.js";
import 'dotenv/config';
import { AuthenticatedRequest } from "@/middlewares/authToken.js";
import * as constants from "@/constants/catalogConstantsCodes.js";


// GET /shipping-guides/csv
export async function csvExport(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const criteria: ListShippingGuideQuery = ListShippingGuideQuerySchema.parse(req.query);
        return shippingGuideService.writeCsv(criteria, res);
    } catch (e) {
        logger.error("❌ ShippingGuide.csvExport. ERROR  : No fue posible exportar a csv. FAILED → data={} cause={}", req.query, e); 
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC012, req.authToken ?? '');
        logActivity(true, 'ERROR  : No fue posible exportar a csv, Favor de validar', e , req.query);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e),trace_id: getTraceId()});
        next(e);
    }
}

const WRN7029 = { success: false, code: 'WRN7029', message: 'El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador' };

function allowedVendors(req: Request): number[] | null | 'wrn7029' {
    const sec = req.security;
    if (!sec) return null;
    if (Array.isArray(sec.vendors) && sec.vendors.length === 0) return 'wrn7029';
    return sec.vendors ? sec.vendors.map(Number).filter(n => !isNaN(n)) : null;
}

// GET /shipping-guides STM 577
export async function list(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === 'wrn7029') { res.status(400).json(WRN7029); return; }

        const q: ListShippingGuideQuery =
            ListShippingGuideQuerySchema.parse(req.query);

        const response = await shippingGuideService.listPaginated(q, vendors);
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        logger.error("❌ ShippingGuide.csvExport. ERROR  : No fue posible listar las guias de enbarque. FAILED → data={} cause={}", req.query, e); 
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC013, req.authToken ?? '');
        logActivity(true, 'ERROR  : No fue posible listar las guias de enbarque, Favor de validar', e , req.query);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e),trace_id: getTraceId()});
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
export async function getById(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const q: IdParamSchemaDto = IdParamSchema.parse(req.params);
        const response = await shippingGuideService.get(q.uuid, req.authToken ?? '');
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC013, req.authToken ?? '');
        logActivity(true, CatMsgExc.description , e , req.query);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e),trace_id: getTraceId()});
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
export async function updateByUuid(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const q: IdParamSchemaDto = IdParamSchema.parse(req.params);
        const dto: UpdateShippingGuideDto = UpdateShippingGuideSchema.parse(req.body);
        const response = await shippingGuideService.updateOneByUuid(q.uuid, dto, req.authToken ?? '');
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC014, req.authToken ?? '');
        logActivity(true, CatMsgExc.description , e , req.query);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e),trace_id: getTraceId()});
        next(e);
    }
}

// PATCH /shipping-guides/guide/:idGuide
export async function updateByGuide(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const q: IdParamGuideDto = IdParamGuideSchema.parse(req.params);
        const dto: UpdateShippingGuideDto = UpdateShippingGuideSchema.parse(req.body);
        const response = await shippingGuideService.updateOneByGuide(q.idGuide, dto, req.authToken ?? '');
        res.status(response.httpStatus).json({...response, trace_id: getTraceId()});
    } catch (e) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC014, req.authToken ?? '');
        logActivity(true, CatMsgExc.description , e , req.query);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e),trace_id: getTraceId()});
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
        logActivity(true, 'ERROR  :', e , req.params);
        res.status(400).json(ResponseHandler.responseBuilder("ERROR: " + e, null, -1, StatusCodes.BAD_REQUEST, false, ""));
        next(e);
    }
}
