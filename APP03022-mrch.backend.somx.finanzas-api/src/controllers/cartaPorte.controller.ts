import {
    CreateShippingGuideSchemaList,
    ListShippingGuideQuerySchema,
    ShippginGuideSummaryListSchema,
    type CreateShippingGuideDto,
    type CreateShippingGuideDtoList,
    type ListShippingGuideQuery,
    type ShippginGuideSummaryListDto
} from "@/schemas/shippingGuide.schema.js";
import {
    CreatePurchaseOrderSchema,
    type CreatePurchaseOrderDto,

} from "@/schemas/purchaseOrder.schema.js";
import { 
    BaseArrayFilesSchemaParent,
    BaseSchemaParent,
    type BaseArrayFilesDtoParent,
    type BaseDtoParent
} from "@/schemas/base.shema.js";
import * as cartaPorteService from "@/services/cartaPorte.service.js";
import type { NextFunction, Request, Response } from "express";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes, ReasonPhrases } from 'http-status-codes';
import { logger } from "@/utils/logger.js";
import { logActivity, getTraceId } from '@/middlewares/logger.js';
import * as svc from "@/services/shippingGuide.service.js";


// POST /carta-porte/guia-embarque
export async function createGuia(req: Request, res: Response, next: NextFunction) {
    try {
        const dtoParent: BaseArrayFilesDtoParent = BaseArrayFilesSchemaParent.parse(req.body);
        const shippingGuideSchmaString: string = dtoParent.content;
        const dataList: CreateShippingGuideDtoList = CreateShippingGuideSchemaList.parse(JSON.parse(shippingGuideSchmaString));
        const createShippingGuideDto: CreateShippingGuideDto[] = dataList.shippingGuideList; 
        const files = req.files as Express.Multer.File[];

        const created =  await cartaPorteService.createGuia(createShippingGuideDto, files, dtoParent.folder, Number(dtoParent.origen));
        res.status(201).json({...created, trace_id: getTraceId()});
    } catch (e) { 
        logger.error("❌ Register 'Guias Carta Porte' FAILED → data={} cause={}", req.body, e); 
        await logActivity(true, 'ERROR (BUS2004) : No fue posible registrar la guía carta porte, Favor de validar', e , req.body);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR (BUS2004) : No fue posible registrar la guía carta porte, Favor de validar " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""), trace_id: getTraceId()});
        next(e);
    }
}

// POST /carta-porte/oc
export async function createOc(req: Request, res: Response, next: NextFunction) {
    try {
        const dtoParent: BaseDtoParent = BaseSchemaParent.parse(req.body);
        const stringContent: string = dtoParent.content;
        const dto: CreatePurchaseOrderDto = CreatePurchaseOrderSchema.parse(JSON.parse(stringContent));

        const created = await cartaPorteService.createOc(dto, Number(dto.origen));
        res.status(201).json({...created, trace_id: getTraceId()});

    } catch (e) {
        logger.error("❌ Register 'OC Carta Porte' FAILED → data={} cause={}", req.body, e); 
        await logActivity(true, 'ERROR : No fue posible registrar la OC DE carta porte, Favor de validar', e , req.body);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR (BUS2004) : No fue posible registrar la OC, Favor de validar " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""), trace_id: getTraceId()});
        next(e);
    }
}

// POST /carta-porte/all
export async function createall(req: Request, res: Response, next: NextFunction) {
    try {
        const dtoParent: BaseArrayFilesDtoParent = BaseArrayFilesSchemaParent.parse(req.body);
        const shippingAndOCSchmaString: string = dtoParent.content;
        const createPurchaseOrderDto: CreatePurchaseOrderDto = CreatePurchaseOrderSchema.parse(JSON.parse(shippingAndOCSchmaString));
        const files = req.files as Express.Multer.File[];

        const created = await cartaPorteService.createAll(createPurchaseOrderDto, files, dtoParent.folder, Number(dtoParent.origen));
        res.status(201).json({created, trace_id: getTraceId()});
    } catch (e) { 
        logger.error("❌ Register 'Guias y OC Carta Porte' FAILED → data={} cause={}", req.body, e); 
        await logActivity(true, 'ERROR : No fue posible registrar la OC Y LA DOCUMENTACION DE carta porte, Favor de validar', e , req.body);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR (BUS2004) : No fue posible registrar la OC y guía carta porte, Favor de validar " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""), trace_id: getTraceId()});
        next(e);
    }
}

// GET /findAllGuia
export async function findAllGuia(request: Request, response: Response, next: NextFunction) {
    try {
        const dto: ListShippingGuideQuery = ListShippingGuideQuerySchema.parse(request.body);
        const finded = await svc.findAll(dto);
        return response.status(finded.httpStatus).json({...finded, trace_id: getTraceId()});
    } catch (e) { 
        await logActivity(true,'ERROR', e, request.params );
        response.json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e); 
    }
}

// GET /updateAllStatus
export async function updateAllStatusGuia(request: Request, response: Response, next: NextFunction) {
    try {
        const dto: ShippginGuideSummaryListDto = ShippginGuideSummaryListSchema.parse(request.body);
        const finded = await svc.updateAllStatusGuia(dto);
        return response.status(finded.httpStatus).json({...finded, trace_id: getTraceId()});
    } catch (e) { 
        await logActivity(true,'ERROR', e, request.params );
        response.json(ResponseHandler.responseBuilder("ERROR: " + e ,null,-1, StatusCodes.BAD_REQUEST, false, ""));
        next(e); 
    }
}

