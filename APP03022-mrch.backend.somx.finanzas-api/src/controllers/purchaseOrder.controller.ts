import { getDataSource, datasource } from "@/config/typeorm-datasource.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { Reception } from "@/entities/Reception.entity.js";
import { HttpError } from "@/utils/HttpError.js";
import type { NextFunction, Response } from "express";
import { Between, Equal, FindOperator, FindOptionsWhere, LessThanOrEqual, MoreThanOrEqual } from "typeorm";
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
    ListReceptionQuerySchemaV2,
    type ListReceptionQueryDto,
    type ListReceptionQueryDtoV2
} from "@/schemas/reception.schema.js";

import * as svc from "@/services/purchaseOrder.service.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { Addendum } from "@/entities/tenant_fiscal.addendum.entity.js";
import { ResponsePageableDTO } from "@/response/ResponseHandler.dto.js";
import { logActivity, getTraceId } from '@/middlewares/logger.js';
import * as svcAxios from "@/services/axios.service.js";
import { Supplier } from '@/response/GenericCatalogDetails.dto.js';
import 'dotenv/config';
import { AuthenticatedRequest } from "@/middlewares/authToken.js";
import * as constants from "@/constants/catalogConstantsCodes.js";



// CONFIG
const DATE_TIME_FORMAT: RegExp = /\d{4}-\d{2}-\d{2}/;
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
        await getDataSource().transaction(async (transactionalEntityManager) => {
            const created = await svc.create(request, dto, transactionalEntityManager, null, Number(dto.origen));
            response.status(201).json({ ...created, trace_id: getTraceId() });
        });
    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR: ', err, request.body);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC001, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
        next(e);

    }
}

// GET /
export async function list(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        console.log("[purchaseOrder.list] START");
        console.log("[purchaseOrder.list] method:", request.method);
        console.log("[purchaseOrder.list] originalUrl:", request.originalUrl);
        console.log("[purchaseOrder.list] query:", JSON.stringify(request.query, null, 2));
        console.log("[purchaseOrder.list] body:", JSON.stringify(request.body, null, 2));
        console.log("[purchaseOrder.list] authToken exists:", Boolean(request.authToken));
        console.log("[purchaseOrder.list] CATALOGS_API_URL_BFF:", process.env.CATALOGS_API_URL_BFF);

        const supplierList = await svcAxios.GetSuppliers(request.authToken ?? '');

        console.log("[purchaseOrder.list] supplierList isArray:", Array.isArray(supplierList));
        console.log("[purchaseOrder.list] supplierList length:", Array.isArray(supplierList) ? supplierList.length : "not-array");
        console.log("[purchaseOrder.list] supplierList sample:", JSON.stringify(Array.isArray(supplierList) ? supplierList[0] : supplierList, null, 2));

        const dto: ListPurchaseOrderQueryDto = ListPurchaseOrderQuerySchema.parse(request.query);

        console.log("[purchaseOrder.list] dto parsed:", JSON.stringify(dto, null, 2));

        const purchaseOrderQuery = await datasource.manager
            .createQueryBuilder(PurchaseOrder, 'purchaseOrder')
            .leftJoinAndSelect('purchaseOrder.receptions', 'reception', ' reception.status != 8 ')
            .leftJoinAndSelect('reception.receptionSkus', 'receptionSku')
            .leftJoinAndSelect('purchaseOrder.shippingGuidePurchaseOrders', 'shippingGuidePurchaseOrder')
            .leftJoinAndSelect('shippingGuidePurchaseOrder.shippingGuide', 'shippingGuide')
            .leftJoinAndMapMany(
                "reception.listAddendum",
                Addendum,
                "addendum",
                'addendum.receptionNumber = reception.receptionNumber',
            )
            .leftJoinAndSelect('addendum.invoice', 'invoice');

        purchaseOrderQuery.andWhere(
            "reception.receptionDate BETWEEN :startDate AND :endDate ",
            {
                startDate: dto.purchaseOrderDateAtInitial,
                endDate: dto.purchaseOrderDateAtEnd,
            }
        );

        if (dto.supplierNumber) {
            console.log("[purchaseOrder.list] applying supplierNumber:", dto.supplierNumber);
            purchaseOrderQuery.andWhere("purchaseOrder.supplierNumber = :supplierNumber", { supplierNumber: dto.supplierNumber });
        }

        if (dto.orderNumber) {
            console.log("[purchaseOrder.list] applying orderNumber:", dto.orderNumber);
            purchaseOrderQuery.andWhere("purchaseOrder.orderNumber = :orderNumber", { orderNumber: dto.orderNumber });
        }

        if (dto.originId) {
            console.log("[purchaseOrder.list] applying originId:", dto.originId);
            purchaseOrderQuery.andWhere("purchaseOrder.originId = :originId", { originId: dto.originId });
        }

        if (typeof dto.status === "number" && dto.status !== 8) {
            console.log("[purchaseOrder.list] applying reception status:", dto.status);
            purchaseOrderQuery.andWhere("reception.status = :receptionStatus", {
                receptionStatus: dto.status,
            });
        }

        const skip = (parseInt(dto.pageNumber) - 1) * parseInt(dto.pageSize);

        console.log("[purchaseOrder.list] pagination:", {
            pageNumber: dto.pageNumber,
            pageSize: dto.pageSize,
            skip,
        });

        console.log("[purchaseOrder.list] SQL:", purchaseOrderQuery.getSql());
        console.log("[purchaseOrder.list] params:", purchaseOrderQuery.getParameters());

        const totalCount = await purchaseOrderQuery.getCount();

        console.log("[purchaseOrder.list] totalCount:", totalCount);

        const result = await purchaseOrderQuery
            .skip(skip)
            .take(parseInt(dto.pageSize))
            .getMany();

        console.log("[purchaseOrder.list] result length:", result.length);
        console.log("[purchaseOrder.list] first result:", JSON.stringify(result[0] ?? null, null, 2));

        const _numberOfElements = result.length;
        const _totalItems = Number(totalCount?.valueOf() == null ? 0 : Number(totalCount?.valueOf()));

        let _totalPages = _totalItems / parseInt(dto.pageSize);

        if (_totalPages - Math.trunc(_totalPages) > 0) {
            _totalPages = Math.trunc(_totalPages) + 1;
        } else {
            _totalPages = Math.trunc(_totalPages);
        }
        const CatEstatusRecepcion  = await svcAxios.GetCatalogDetailList((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatEstatusRecepcion.CATALOGS_API_STATUS_RECEPTION , request.authToken ?? '');
        result.forEach((item, index) => {
            console.log("[purchaseOrder.list] mapping supplier item:", {
                index,
                purchaseOrderId: item.purchaseOrderId,
                supplierNumber: item.supplierNumber,
            });

            const foundSupplier: Supplier | undefined = supplierList.find(
                (supplier) =>
                    Number(supplier.supplierNumber) === Number(item.supplierNumber)
            );

            console.log("[purchaseOrder.list] found supplier:", JSON.stringify(foundSupplier ?? null, null, 2));

            (item as any).supplier = foundSupplier;
            (item as any).vendorName = foundSupplier?.businessName ?? "";

            item.receptions?.forEach((reception) => {
                (reception as any).supplier = foundSupplier;
                (reception as any).vendorName = foundSupplier?.businessName ?? "";
                const result = CatEstatusRecepcion.find(ite => ite.value === reception.status?.toString());
                if(result){
                    (reception as any).color = result.color;
                }
            });
        });

        await svc.enrichPurchaseOrdersRecepcionesOriginCatalog(result as PurchaseOrder[], request.authToken ?? '');

        const responsePageableDTO: ResponsePageableDTO = {
            content: result,
            totalElements: _totalItems,
            numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
            totalPages: _totalPages,
            pageNumber: parseInt(dto.pageNumber),
            pageSize: parseInt(dto.pageSize),
        };

        console.log("[purchaseOrder.list] responsePageableDTO:", {
            totalElements: responsePageableDTO.totalElements,
            numberOfElements: responsePageableDTO.numberOfElements,
            totalPages: responsePageableDTO.totalPages,
            pageNumber: responsePageableDTO.pageNumber,
            pageSize: responsePageableDTO.pageSize,
        });

        console.log("[purchaseOrder.list] END OK");

        return response.json({
            ...ResponseHandler.responseBuilder("", responsePageableDTO, 0, StatusCodes.OK, true, ""),
            trace_id: getTraceId(),
        });

    } catch (e) {
        console.error("[purchaseOrder.list] ERROR:", e);
        console.error("[purchaseOrder.list] ERROR query:", JSON.stringify(request.query, null, 2));
        console.error("[purchaseOrder.list] ERROR body:", JSON.stringify(request.body, null, 2));
        console.error("[purchaseOrder.list] ERROR CATALOGS_API_URL_BFF:", process.env.CATALOGS_API_URL_BFF);
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR: ', err, request.body);

        const CatMsgExc = await svcAxios.GetCatalogDetail(
            (process.env.CATALOGS_API_URL_BFF ?? "") +
            constants.CatalogException.CATALOGS_API_EXCEPTION +
            constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC001,
            request.authToken ?? ''
        );

        console.error("[purchaseOrder.list] CatMsgExc:", JSON.stringify(CatMsgExc, null, 2));

        response.status(400).json(
            ResponseHandler.responseBuilder(
                "ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description,
                null,
                -1,
                StatusCodes.BAD_REQUEST,
                false,
                err
            )
        );

        next(e);
    }
}

// GET /:uuid
export async function getById(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const rows = await getPurchaseOrdersRepo().findBy({ purchaseOrderId: Equal(request.params.uuid || "") as string | FindOperator<string> });
        if (!rows || rows.length === 0) response.json({});
        for (const row of rows) {
            const purchaseOrderId = row.purchaseOrderId;
            if (!purchaseOrderId) {
                return response.json({ ...rows[0], trace_id: getTraceId() });
            }
            const CatEstatusRecepcion  = await svcAxios.GetCatalogDetailList((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatEstatusRecepcion.CATALOGS_API_STATUS_RECEPTION , request.authToken ?? '');
            row.receptions = await getReceiptsRepo().createQueryBuilder("r").where("r.purchase_order_uuid = :id::uuid", {
                id: row.purchaseOrderId
            })  .getMany();

            row.receptions?.forEach((it, idx) =>{
                const result = CatEstatusRecepcion.find(ite => ite.value === it.status?.toString());
                if(result){
                    (it as any).color = result.color;
                }
            });

        }

        return response.json({ ...rows[0], trace_id: getTraceId() });

    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR', err, request.params);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC002, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
        next(e);
    }
}

// GET /reception/:uuid
export async function getReceptionById(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {


        let uuid = '';
        if (request.params.uuid != undefined) {
            uuid = request.params.uuid as string;
        }

        const row = await getReceiptsRepo()
            .createQueryBuilder('receipts')
            .leftJoinAndSelect('receipts.receptionSkus', 'receptionSku')
            .leftJoinAndMapMany("receipts.listAddendum", // The property on the User entity where the mapped Photo will be stored
                Addendum,       // entity to be joined
                "addendum",      //Alias of entity
                'addendum.receptionNumber = receipts.receptionNumber', // conditions of JOin
            )
            .leftJoinAndSelect('addendum.invoice', 'invoice')
            .where('receipts.reception_id = CAST(:receptionId AS uuid)', { receptionId: uuid })
            .getOne();

        if (row == null) {
            return response.json({ ...ResponseHandler.responseBuilder("", request.params.uuid, 0, StatusCodes.NOT_FOUND, true, ""), trace_id: getTraceId() });
        }

        const CatEstatusRecepcion  = await svcAxios.GetCatalogDetailList((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatEstatusRecepcion.CATALOGS_API_STATUS_RECEPTION , request.authToken ?? '');
        const result = CatEstatusRecepcion.find(ite => ite.value === row.status?.toString());
        if(result){
            (row as any).color = result.color;
        }

        const order = await getPurchaseOrdersRepo()
            .createQueryBuilder('po')
            .where('po.purchase_order_uuid = CAST(:purchaseOrderId AS uuid)', { purchaseOrderId: row?.purchaseOrderId })
            .getOne();

        const foundSupplier: Supplier | undefined = await svcAxios.GetSupplierBySupplierNumber(order?.supplierNumber ?? 0, request.authToken ?? '');
        (order as any).supplier = foundSupplier;

        const data = {
            ...row,
            supplier: foundSupplier,
            vendorName: foundSupplier?.businessName ?? "",
            order: order
        };

        return response.json({ ...ResponseHandler.responseBuilder("", data, 0, StatusCodes.OK, true, ""), trace_id: getTraceId() });

    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR', err, request.params);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC003, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
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
            persistence.comment = reference.comment ?? "";
        }

        await getReceiptsRepo().save({ ...persistence, trace_id: getTraceId() });
        response.json(persistence);
    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR', err, request.params);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC004, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
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

        const row = await getPurchaseOrdersRepo().findBy(
            {
                purchaseOrderId: Equal(request.params.uuid || "")
        });

        if (!row || row.length === 0 || !row[0]) {
            throw new HttpError(400, "Invalid uuid");
        }

        // UPDATES status ONLY
        const persistence: PurchaseOrder = row[0];
        if (reference.status) {
            persistence.status = reference.status;
        }

        await getPurchaseOrdersRepo().save(persistence);
        response.json({ ...persistence, trace_id: getTraceId() });
    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR', err, request.params);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC005, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
        next(e);
    }
}

// GET /listReception
export async function listReception(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const q: ListReceptionQueryDto = ListReceptionQuerySchema.parse(request.body);
        const res = await svc.listReception(q, request.authToken ?? '');
        return response.status(res.httpStatus).json({ ...res, trace_id: getTraceId() });

    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR: NO FUE POSIBLE LISTAR LAS RECEPCIONES', err, request.body);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC006, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
        next(e);
    }
}

// GET /listReceptionV2
export async function listReceptionV2(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const q: ListReceptionQueryDtoV2 = ListReceptionQuerySchemaV2.parse(request.query);
        const q2: ListReceptionQueryDto = {
            receptionDateAtInitial : q.receptionDateAtInitial,
            receptionDateAtEnd : q.receptionDateAtEnd,
            createdAtInitial : q.createdAtInitial,
            createdAtEnd : q.createdAtEnd,
            supplierNumber: q.supplierNumber,
            orderNumber: q.orderNumber,
            status: q.status,
            receptionId: q.receptionId,
            pageNumber : parseInt(q.pageNumber,10),
            pageSize : parseInt(q.pageSize,10)
        };
        const res = await svc.listReception(q2, request.authToken ?? '');
        return response.status(res.httpStatus).json({ ...res, trace_id: getTraceId() });

    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR: NO FUE POSIBLE LISTAR LAS RECEPCIONES', err, request.body);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC006, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
        next(e);
    }
}

// PATCH /updateReception
export async function updateReception(request: AuthenticatedRequest, response: Response, next: NextFunction) {
    try {
        const dto: UpdatePurchaseOrderDto = UpdateStatusReceptionSchema.parse(request.body);
        const updated = await svc.updateReception(dto, request.authToken ?? '');
        return response.status(updated.httpStatus).json({ ...updated, trace_id: getTraceId() });
    } catch (e) {
        let err = "";
        if (e instanceof Error) {
            err= e.message + e.cause + e.stack;
        }
        logActivity(true, 'ERROR', err, request.params);
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC004, request.authToken ?? '');
        response.status(400).json(ResponseHandler.responseBuilder("ERROR: " + CatMsgExc.key + ". " + CatMsgExc.description, null, -1, StatusCodes.BAD_REQUEST, false, err));
        next(e);
    }
}


// MISC
function buildCriteria(criteria: PurchaseOrderCriteria): FindOptionsWhere<PurchaseOrder>[] {
    if (!criteria) {
        return [];
    }
    const where: FindOptionsWhere<PurchaseOrder>[] = [];
    if (criteria.status) {
        where.push({ status: criteria.status });
    }

    const createdAt: FindOptionsWhere<PurchaseOrder> = {};
    const orderDate: FindOptionsWhere<PurchaseOrder> = {};

    let from: Date | undefined;
    let to: Date | undefined;
    if (criteria.dateFrom?.match(DATE_TIME_FORMAT)){
        from = new Date(criteria.dateFrom);
        createdAt.createdAt = MoreThanOrEqual(from);
    }
    if (criteria.dateTo?.match(DATE_TIME_FORMAT)) {
        to = new Date(criteria.dateTo);
        createdAt.createdAt = from ? Between(from, to) : LessThanOrEqual(to);
    }
    where.push(createdAt);
    where.push(orderDate);

    return where;
}



