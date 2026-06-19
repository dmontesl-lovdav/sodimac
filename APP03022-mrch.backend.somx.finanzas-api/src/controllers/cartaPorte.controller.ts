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
import type { NextFunction, Response } from "express";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { logger } from "@/utils/logger.js";
import { logActivity, getTraceId } from '@/middlewares/logger.js';
import * as svc from "@/services/shippingGuide.service.js";
import * as svcAxios from "@/services/axios.service.js";
import 'dotenv/config';
import { AuthenticatedRequest } from "@/middlewares/authToken.js";
import * as constants from "@/constants/catalogConstantsCodes.js";


// POST /carta-porte/guia-embarque
export async function createGuia(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const dtoParent: BaseArrayFilesDtoParent = BaseArrayFilesSchemaParent.parse(req.body);
        const shippingGuideSchmaString: string = dtoParent.content;
        const dataList: CreateShippingGuideDtoList = CreateShippingGuideSchemaList.parse(JSON.parse(shippingGuideSchmaString));
        const createShippingGuideDto: CreateShippingGuideDto[] = dataList.shippingGuideList; 
        const files = req.files as Express.Multer.File[];

        const created =  await cartaPorteService.createGuia(req, files, Number(dtoParent.origen), req.authToken ?? '', dtoParent.folder, dtoParent.saveFileOnDb, createShippingGuideDto);
        res.status(201).json({...created, trace_id: getTraceId()});
    } catch (e) { 
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logger.error("❌ Register 'Guias Carta Porte' FAILED → data={} cause={}", req.body, err); 
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC007, req.authToken ?? '');
        logActivity(true, 'ERROR : No fue posible registrar la GUIA DE carta porte, Favor de validar', err , req.body);

        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, err),trace_id: getTraceId()});

        next(e);
    }
}

// POST /carta-porte/oc
export async function createOc(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const dtoParent: BaseDtoParent = BaseSchemaParent.parse(req.body);
        const stringContent: string = dtoParent.content;
        const dto: CreatePurchaseOrderDto = CreatePurchaseOrderSchema.parse(JSON.parse(stringContent));

        const created = await cartaPorteService.createOc(req, dto, Number(dto.origen), req.authToken ?? '');
        res.status(201).json({...created, trace_id: getTraceId()});

    } catch (e) {
        const errorMessage = e instanceof Error ? e.message : String(e);
        const errorStack = e instanceof Error ? e.stack : String(e);
        logger.error("❌ Register 'OC Carta Porte' FAILED → data={} cause={}", req.body, e); 
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC008, req.authToken ?? '');
        logActivity(true, 'ERROR : No fue posible registrar la OC DE carta porte, Favor de validar', errorMessage + errorStack  , req.body);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, errorMessage + errorStack),trace_id: getTraceId()});
        next(e);
    }
}

// POST /carta-porte/all
export async function createall(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const dtoParent: BaseArrayFilesDtoParent = BaseArrayFilesSchemaParent.parse(req.body);
        const shippingAndOCSchmaString: string = dtoParent.content;
        const createPurchaseOrderDto: CreatePurchaseOrderDto = CreatePurchaseOrderSchema.parse(JSON.parse(shippingAndOCSchmaString));
        const files = req.files as Express.Multer.File[];

        const created = await cartaPorteService.createAll(req, createPurchaseOrderDto, files, Number(dtoParent.origen), req.authToken ?? '', dtoParent.folder, dtoParent.saveFileOnDb);
        res.status(201).json({...created, trace_id: getTraceId()});
    } catch (e) { 
                let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logger.error("❌ Register 'Guias y OC Carta Porte' FAILED → data={} cause={}", req.body, e); 
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC009, req.authToken ?? '');
        logActivity(true, 'ERROR : No fue posible registrar la OC Y LA DOCUMENTACION DE carta porte, Favor de validar', err , req.body);
        res.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, err),trace_id: getTraceId()});
        next(e);
    }
}


// POST /findAllGuia
export async function findAllGuia(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const dto: ListShippingGuideQuery = ListShippingGuideQuerySchema.parse(request.body);
        const finded = await svc.findAll(dto);
        return response.status(finded.httpStatus).json({...finded, trace_id: getTraceId()});
    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC010, request.authToken ?? '');
        logActivity(true,'ERROR: No fue posible encontrar las guias', err, request.params );
        response.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, err),trace_id: getTraceId()});
        next(e); 
    }
}

// GET /updateAllStatus
export async function updateAllStatusGuia(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const dto: ShippginGuideSummaryListDto = ShippginGuideSummaryListSchema.parse(request.body);
        const finded = await svc.updateAllStatusGuia(dto);
        return response.status(finded.httpStatus).json({...finded, trace_id: getTraceId()});
    } catch (e) { 
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC011, request.authToken ?? '');
        logActivity(true,'ERROR: AL ACTULIAZAR LOS ESTATUS DE LAS GUIAS', err, request.params );
        response.status(400).json({...ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, err),trace_id: getTraceId()});
        next(e); 
    }
}

