import { getDataSource, datasource } from "@/config/typeorm-datasource.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { Reception } from "@/entities/Reception.entity.js";
import { HttpError } from "@/utils/HttpError.js";
import type { NextFunction, Request, Response } from "express";
import { Between, Equal, FindOperator, FindOptionsWhere, LessThanOrEqual, Like, MoreThanOrEqual } from "typeorm";
import {
    CreatePurchaseOrderSchema,
    UpdateStatusReceptionSchema,
    ListPurchaseOrderQuerySchema,
    type CreatePurchaseOrderDto,
    type UpdatePurchaseOrderDto,
    type ListPurchaseOrderQueryDto
} from "@/schemas/purchaseOrder.schema.js";
import {
    ListReceptionQuerySchema,
    type ListReceptionQueryDto
} from "@/schemas/reception.schema.js";

import * as svc from "@/services/purchaseOrder.service.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
//REVISAR PARA PASARLO A SERVICIOS
import { ShippingGuidePurchaseOrder } from "@/entities/ShippingGuidePurchaseOrder.entity.js";
import { ShippingGuide } from "@/entities/ShippingGuide.entity.js";
import { Addendum } from "@/entities/tenant_fiscal.addendum.entity.js";
import { ResponsePageableDTO } from "@/response/ResponseHandler.dto.js";
import { activityLogger, logActivity, getTraceId, logBeforeMethod } from '@/middlewares/logger.js';
import * as svcAxios from "@/services/axios.service.js";
import { Supplier } from '@/response/GenericCatalogDetails.dto.js';
import 'dotenv/config';
import { AuthenticatedRequest } from "@/middlewares/authToken.js";
import * as constants from "@/constants/catalogConstantsCodes.js";


// CONFIG
const DATE_TIME_FORMAT: RegExp = /[0-9]{4}-[0-9]{2}-[0-9]{2}/;
const getPurchaseOrdersRepo = () => getDataSource().getRepository(PurchaseOrder);
const getReceiptsRepo = () => getDataSource().getRepository(Reception);


interface PurchaseOrderCriteria {
    criteria: string,
    dateFrom: string,
    dateTo: string,
    status: number
}

// POST /
export async function save(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const dto: CreatePurchaseOrderDto = CreatePurchaseOrderSchema.parse(request.body);
        await getDataSource().transaction( async (transactionalEntityManager) => {
            const created = await svc.create(dto, transactionalEntityManager, null, null, Number(dto.origen), request.authToken ?? '');
            response.status(201).json({...created, trace_id: getTraceId()});
        });
    } catch (e) {
        logActivity(true, 'ERROR: ', e,  request.body );
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC001, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e);

    }
}

// GET /
export async function list(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {

        const supplierList = await svcAxios.GetSuppliers(request.authToken ?? '');
        const dto: ListPurchaseOrderQueryDto = ListPurchaseOrderQuerySchema.parse(request.query);

        const purchaseOrderQuery = await datasource.manager
        .createQueryBuilder(PurchaseOrder, 'purchaseOrder')
        .leftJoinAndSelect('purchaseOrder.receptions','reception', ' reception.status != 8 ')
        .leftJoinAndSelect('reception.receptionSkus','receptionSku')
        .leftJoinAndSelect('purchaseOrder.shippingGuidePurchaseOrders','shippingGuidePurchaseOrder')
        .leftJoinAndSelect('shippingGuidePurchaseOrder.shippingGuide','shippingGuide')
        .leftJoinAndMapMany("reception.listAddendum", // The property on the User entity where the mapped Photo will be stored
                          Addendum,       // entity to be joined
                          "addendum",      //Alias of entity
                          'addendum.receptionNumber = reception.receptionNumber', // conditions of JOin
          )
        .leftJoinAndSelect('addendum.invoice', 'invoice');
        // .leftJoinAndMapOne("purchaseOrder.listSupplier", // The property on the User entity where the mapped Photo will be stored
        //                   Supplier,       // entity to be joined
        //                   "supplier",      //Alias of entity
        //                   'supplier.supplierNumber = purchaseOrder.supplierNumber', // conditions of JOin
        //   );

        //Las columnas de fechas son obligatorias
        purchaseOrderQuery.andWhere("purchaseOrder.purchaseOrderDate BETWEEN :startDate AND :endDate " 
                                    //" AND reception.status != 8 "  //No incluye las Recepciones borradas
                                    , { startDate: dto.purchaseOrderDateAtInitial,
                                        endDate: dto.purchaseOrderDateAtEnd
                                     });
        
        if(dto.supplierNumber){
            purchaseOrderQuery.andWhere("purchaseOrder.supplierNumber = :supplierNumber", { supplierNumber: dto.supplierNumber });
        }
        if(dto.orderNumber){
            purchaseOrderQuery.andWhere("purchaseOrder.orderNumber = :orderNumber", { orderNumber: dto.orderNumber });
        }
        if(dto.originId){
            purchaseOrderQuery.andWhere("purchaseOrder.originId = :originId", { originId: dto.originId });
        }
        if(dto.status && dto.status != 8){
            purchaseOrderQuery.andWhere("purchaseOrder.status = :status", { status: dto.status });
        } 

        // Get the total count with pagination
        const skip = (parseInt(dto.pageNumber) - 1) * parseInt(dto.pageSize); // Calculate the offset
        const totalCount = await purchaseOrderQuery.getCount();
        const result = await purchaseOrderQuery.skip(skip).take(parseInt(dto.pageSize)).getMany();
        const _numberOfElements = result.length;

        const _totalItems = Number(totalCount?.valueOf() == null ? 0 : Number(totalCount?.valueOf()));
        let _totalPages = _totalItems / parseInt(dto.pageSize);
    
        if (_totalPages - Math.trunc(_totalPages) > 0) {
            _totalPages = Math.trunc(_totalPages) + 1;
        } else {
            _totalPages = Math.trunc(_totalPages)
        }

        result.forEach((item, index) => {
            console.log(`${index}: ${item}`);
            const foundUser: Supplier | undefined = supplierList.find(Supplier => Supplier.supplierNumber === item.supplierNumber);
            (item as any).supplier = foundUser;
        });
        const responsePageableDTO: ResponsePageableDTO = {
            content: result,
            totalElements: _totalItems,
            numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
            totalPages: _totalPages,
            pageNumber: parseInt(dto.pageNumber),
            pageSize: parseInt(dto.pageSize)
    
        };
        return response.json({...ResponseHandler.responseBuilder("",responsePageableDTO,0, StatusCodes.OK, true, ""), trace_id: getTraceId()});

    } catch (e) {
        logActivity(true, 'ERROR: ', e,  request.body );
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC001, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e);
    }
}

// GET /:uuid
export async function getById(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const rows = await getPurchaseOrdersRepo().findBy({ purchaseOrderId: Equal(request.params.uuid || "") as string | FindOperator<string> });
        if (!rows || rows.length === 0) response.json({});

        for(const row of rows){
            row.receptions = await getReceiptsRepo().findBy({ purchaseOrderId: row.purchaseOrderId });
        }

        return response.json({...rows[0], trace_id: getTraceId()});

    } catch (e) { 
        logActivity(true,'ERROR', e, request.params );
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC002, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e);
    }
}

// GET /reception/:uuid
export async function getReceptionById(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {

  
        let uuid = '';
        if(request.params.uuid != undefined){
            uuid = request.params.uuid as string;
        }

        const row = await getReceiptsRepo()
        .createQueryBuilder('receipts')
        .leftJoinAndSelect('receipts.receptionSkus','receptionSku')
        .leftJoinAndMapMany("receipts.listAddendum", // The property on the User entity where the mapped Photo will be stored
                          Addendum,       // entity to be joined
                          "addendum",      //Alias of entity
                          'addendum.receptionNumber = receipts.receptionNumber', // conditions of JOin
          )
        .leftJoinAndSelect('addendum.invoice', 'invoice')
        .where('receipts.reception_id = CAST(:receptionId AS uuid)', { receptionId: uuid })
        .getOne();

        if(row == null){
            return response.json({...ResponseHandler.responseBuilder("",request.params.uuid ,0, StatusCodes.NOT_FOUND, true, ""), trace_id: getTraceId()});
        }
        const order = await getPurchaseOrdersRepo()
        .createQueryBuilder('po')
        .where('po.purchase_order_uuid = CAST(:purchaseOrderId AS uuid)', { purchaseOrderId: row?.purchaseOrderId })
        .getOne();

        const foundSupplier: Supplier | undefined = await svcAxios.GetSupplierBySupplierNumber(order?.supplierNumber ?? 0, request.authToken ?? '');
        (order as any).supplier = foundSupplier;
        
        const data = {
            ...row,
            order: order
        };

         return response.json({...ResponseHandler.responseBuilder("",data,0, StatusCodes.OK, true, ""), trace_id: getTraceId()});

    } catch (e) { 
        logActivity(true,'ERROR', e, request.params );
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC003, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e); 
    }
}

// PATCH /reception/:uuid
export async function updateReceptionStatusByUuid(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const reference: Reception = request.body;
        if (!reference) {
            throw new HttpError(400, "Missing update body");
        }

        const row = await getReceiptsRepo().findBy({ receptionId: Equal(request.params.uuid || "") as string | FindOperator<string> });
        if (!row || row.length === 0 || !row[0]) {
            throw new HttpError(400, "Invalid uuid");
        }

        const persistence: Reception = row[0];
        if (reference.status) {
            persistence.status = reference.status;
            persistence.comment = reference.comment || "";
        }

        await getReceiptsRepo().save({...persistence, trace_id: getTraceId()});
        response.json(persistence);
    } catch (e) { 
        logActivity(true,'ERROR', e, request.params );
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC004, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e); 
    }
}

// PATCH /:uuid
export async function updateById(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const reference: PurchaseOrder = request.body;
        if (!reference) {
            throw new HttpError(400, "Missing update body");
        }

        const row = await getPurchaseOrdersRepo().findBy({ purchaseOrderId: Equal(request.params.uuid || "") as string | FindOperator<string> });
        if (!row || row.length === 0 || !row[0]) {
            throw new HttpError(400, "Invalid uuid");
        }

        // UPDATES status ONLY
        const persistence: PurchaseOrder = row[0];
        if (reference.status) {
            persistence.status = reference.status;
        }

        await getPurchaseOrdersRepo().save(persistence);
        response.json({...persistence, trace_id: getTraceId()});
    } catch (e) { 
        logActivity(true,'ERROR', e, request.params );
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC005, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e); 
    }
}

// GET /listReception
export async function listReception(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const q: ListReceptionQueryDto = ListReceptionQuerySchema.parse(request.body);
        const res = await svc.listReception(q, request.authToken ?? ''); 
        return response.status(res.httpStatus).json({...res, trace_id: getTraceId()});

    } catch (e) {
        logActivity(true, 'ERROR: NO FUE POSIBLE LISTAR LAS RECEPCIONES', e , request.body);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC006, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e);
    }
}

// PATCH /updateReception
export async function updateReception(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const dto: UpdatePurchaseOrderDto = UpdateStatusReceptionSchema.parse(request.body);
        const updated = await svc.updateReception(dto, request.authToken ?? '');
        return response.status(updated.httpStatus).json({...updated, trace_id: getTraceId()});
    } catch (e) { 
        logActivity(true,'ERROR', e, request.params );
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC004, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description ,null,-1, StatusCodes.BAD_REQUEST, false, e));
        next(e); 
    }
}


// MISC
function buildCriteria(criteria: PurchaseOrderCriteria): FindOptionsWhere<PurchaseOrder>[] {
    if (!criteria) {
        return [];
    }
    const where: FindOptionsWhere<PurchaseOrder>[] = [];
    if (criteria.criteria) {
        //where.push({ receipts: { receiptNumber: Like(`%${criteria.criteria.trim()}%`) } });

    }

    if (criteria.status) {
        where.push({ status: criteria.status });
    }

    const createdAt: FindOptionsWhere<PurchaseOrder> = {};
    const orderDate: FindOptionsWhere<PurchaseOrder> = {};

    let from: Date | undefined;
    let to: Date | undefined;
    if (criteria.dateFrom && criteria.dateFrom.match(DATE_TIME_FORMAT)) {
        from = new Date(criteria.dateFrom);
        createdAt.createdAt = MoreThanOrEqual(from);
    }
    if (criteria.dateTo && criteria.dateTo.match(DATE_TIME_FORMAT)) {
        to = new Date(criteria.dateTo);
        createdAt.createdAt = from ? Between(from, to) : LessThanOrEqual(to);
    }
    where.push(createdAt);
    where.push(orderDate);

    return where;
}



