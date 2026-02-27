import * as purchaseOrderRepo from "@/repositories/purchaseOrder.repo.js";
import * as rececetionRepo from "@/repositories/reception.repo.js";
import * as shippingRepo from "@/repositories/shippingGuide.repo.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes, ReasonPhrases } from 'http-status-codes';
import { z } from "zod/v4";
import type {
CreatePurchaseOrderDto,
UpdatePurchaseOrderDto,
ListPurchaseOrderQueryDto
} from "@/schemas/purchaseOrder.schema.js";
import type {
ListReceptionQueryDto
} from "@/schemas/reception.schema.js";
import { ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { Reception } from '@/entities/Reception.entity.js';
import { ReceptionSku } from '@/entities/ReceptionSku.entity.js';
import { ShippingGuidePurchaseOrder } from '@/entities/ShippingGuidePurchaseOrder.entity.js';
import {
    FindOptionsWhere,
    EntityManager,
    Between,
    MoreThanOrEqual,
    LessThanOrEqual
} from "typeorm";
import { logger } from "@/utils/logger.js";
import * as svcShipping from "@/services/shippingGuide.service.js";
import { ResponseHandlerDTO } from "@/response/ResponseHandler.dto.js";
import { ShippingGuide } from "@/entities/ShippingGuide.entity.js";
import * as svcAxios from "@/services/axios.service.js";
import 'dotenv/config';


export async function listOrders(q: ListReceptionQueryDto) {


    // const responsePageableDTO: ResponsePageableDTO = {
    //     content: result,
    //     totalElements: _totalItems,
    //     numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
    //     totalPages: _totalPages,
    //     pageNumber: q.pageNumber,
    //     pageSize: q.pageSize

    // };

    //return ResponseHandler.responseBuilder("",responsePageableDTO,0, StatusCodes.OK, true, "");
}

export async function listReception(q: ListReceptionQueryDto) {


   interface Supplier {
        supplierNumber: number;
        rfc: string;
        businessName: string;
        supplierType: {
                        id: number;
                        code: string;
                        description: string;
                        };
    };
    const allSuppliers = await svcAxios.axiosGet((process.env.CATALOGS_API_URL_BBF?? "") +  process.env.CATALOGS_API_GET_ALL_SUPPLIERS);
    const supplierList: Supplier[] = allSuppliers.data as Supplier[];

    const receptionDateAtEnd = q.receptionDateAtEnd;
    const parsedDateEnd = z.coerce.date().parse(receptionDateAtEnd);
    parsedDateEnd.setDate(parsedDateEnd.getDate() + 1);
    q.receptionDateAtEnd = parsedDateEnd;

    var [result, total, _numberOfElements] = await rececetionRepo.findAllPaginated(q, q.pageSize, q.pageNumber);
        result = result as Reception[];
        result.forEach((item, index) => {
            const foundUser: Supplier | undefined = supplierList.find(Supplier => Supplier.supplierNumber === item.purchaseOrder?.supplierNumber);
            (item as any).supplier = foundUser;
        });

    const _totalItems = Number(total?.valueOf() == null ? 0 : Number(total?.valueOf()));
    var _totalPages = _totalItems/q.pageSize;

    if(_totalPages - Math.trunc(_totalPages) > 0){
        _totalPages = Math.trunc(_totalPages) + 1;
    } else {
        _totalPages = Math.trunc(_totalPages)
    }

    const responsePageableDTO: ResponsePageableDTO = {
        content: result,
        totalElements: _totalItems,
        numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
        totalPages: _totalPages,
        pageNumber: q.pageNumber,
        pageSize: q.pageSize

    };

      return ResponseHandler.responseBuilder("",responsePageableDTO,0, StatusCodes.OK, true, "");
  
}


export async function updateReception(dto: UpdatePurchaseOrderDto) {
    const filter: FindOptionsWhere<PurchaseOrder> = {};

    //if (dto.supplierNumber !== undefined) filter.supplierNumber = dto.supplierNumber;
    if (dto.orderNumber !== undefined) filter.orderNumber = dto.orderNumber;

    const purchaseOrder = await purchaseOrderRepo.findByOrderNumber(filter);
    let response = ResponseHandler.responseBuilder("",purchaseOrder,0, StatusCodes.OK, true, "");
    if (!purchaseOrder) response = ResponseHandler.responseBuilder("ORDER NOT FOUND",purchaseOrder,0, StatusCodes.NOT_FOUND, false, "");
    
        // UPDATES status ONLY
    const persistenceList: Reception[] = purchaseOrder?.receptions ?? [];
    if (persistenceList.length == 0) {
        response = ResponseHandler.responseBuilder("ORDER NOT FOUND",purchaseOrder,-1, StatusCodes.NOT_FOUND, false, "");
    } else {
          
        let receiptionToUpdate = persistenceList.filter(function(recep) {
            if(recep.receptionId == dto.receptionId){

                return recep;
            }
   
        });
        const reception = receiptionToUpdate.at(0)?? new Reception();
        const persistence: Reception = reception;
        const valStatus = validarStatus(persistence, dto.status);
        persistence.status = dto.status;
        persistence.updatedAt = new Date();

        if (reception?.receptionId && valStatus) {
            const receptionUpdated = await rececetionRepo.updateOne(persistence.receptionId, persistence);
            response = ResponseHandler.responseBuilder("UPDATED RECEPTION",receptionUpdated,0, StatusCodes.OK, true, "");
        } else {
            response = ResponseHandler.responseBuilder("Error de Status",dto,-1, StatusCodes.BAD_REQUEST, false, "El Cambio de estatus no es valido");
        }

    }


    return response
}

function validarStatus(reception: Reception, newStatus: number){
    const statusActual = reception.status;
    let response = false;
    //statusActual == 0, Recepción disponilbe
    if(statusActual == 0 && (newStatus == 1 || newStatus == 2 || newStatus == 7 || newStatus == 8)){
        response = true;
    }
    
    if(statusActual == 1 && (newStatus == 3 || newStatus == 4)){ 
        response = true; 
    }
    if(statusActual == 2 && newStatus == 3 || newStatus == 4){ 
        response = true; 
    }
    if(statusActual == 3 && newStatus == 5){ 
        response = true; 
    }
    if(statusActual == 4 && newStatus == 5){ 
        response = true; 
    }
    if(statusActual == 5 && newStatus == 6){ 
        response = true; 
    }

    if(statusActual == 6 || statusActual == 7 || statusActual == 8 ){ 
        response = false; 
    }

    return response;
}


export async function create(dto: CreatePurchaseOrderDto, transactionalEntityManager: EntityManager
    ,files: Express.Multer.File[] | null, folder: string | null , origin: number) {

    var resp = ResponseHandler.responseBuilder("",null,0, StatusCodes.CREATED, true, "");

        let receptionsList = new Array(dto.receptionList.length);
        dto.receptionList.forEach(function (reception){
            let receptionsSkuList = new Array(reception.receiptSkuList.length);
            reception.receiptSkuList.forEach(function (receptionSku){
                const datarecSku: Partial<ReceptionSku> = {
                    sku: receptionSku.sku,
                    description: receptionSku.description,
                    quantity: receptionSku.quantity,
                    unitCost: z.coerce.number().parse(receptionSku.unitCost),
                    totalCost: z.coerce.number().parse(receptionSku.totalCost),
                    status: 0, //Nace con el status 0
                    createdBy: receptionSku.createdBy,

            
                };

                receptionsSkuList.push(datarecSku);

            });

            const datarec: Partial<Reception> = {
                originId: reception.originId,
                destinationId: reception.destinationId,
                amount: z.coerce.number().parse(reception.amount),
                status: 0, //Nace con el Status 0
                comment: reception.comments,
                createdBy: reception.createdBy,
                receptionDate: reception.receptionDate,
                receptionSkus: receptionsSkuList
                
            };

            receptionsList.push(datarec);
        });
        
        const data: Partial<PurchaseOrder> = {
            orderNumber: dto.orderNumber,
            supplierNumber: dto.supplierNumber,
            originId: origin,
            amount: z.coerce.number().parse(dto.amount),
            purchaseOrderDate: dto.purchaseOrderDate,
            status: 1, //Nace con el valor 1 (1 - Disponible)
            createdBy: dto.createdBy,
            receptions: receptionsList
        };
        
        const entityCreatedOrder = await transactionalEntityManager.save(PurchaseOrder, data); 

        if(dto.shippingGuideList && dto.shippingGuideList.length > 0) {  //APLICA PARA CARTA PORTE
            const status = 2; //Nace Guia de embarque Con OC
            let created : ResponseHandlerDTO;
            
            created = await svcShipping.create(dto.shippingGuideList, files, folder, origin, status, transactionalEntityManager);
            if(!created.success){    
                throw new Error("NO SE PUDIERON GUARDAR LAS GUIAS DE EMBARQUE O LOS ARCHIVOS EN EL BUCKET DE GCP  " + created.message + created.detailError);
            }
            logger.info("✅ ShippingGuide Created → data={}", created);
        }

        if(dto.guideNumber && dto.guideNumber.length > 0){ 
            let shippingGuidePurchaseOrderList: ShippingGuidePurchaseOrder[] = [];
            for (let i: number = 0; i < dto.guideNumber.length; i++) {
                const filter: FindOptionsWhere<ShippingGuide> = {};
                if (dto.guideNumber[i]?.guide !== undefined) {
                    let guia = dto.guideNumber[i]?.guide ?? '';
                    filter.guideNumber = guia;
                }

                const shippingGuide = await transactionalEntityManager.findOneBy(ShippingGuide, filter);
                 if (!shippingGuide) throw new Error('No se encuentra la Guia para relacionar ' + dto.guideNumber[i]?.guide + ' Con la OC: ' + dto.orderNumber);

                const shippingPurchase: Partial<ShippingGuidePurchaseOrder> = {
                    purchaseOrderId: entityCreatedOrder.purchaseOrderId,
                    shippingGuideId: shippingGuide.shippingGuideId,
                    createdBy: dto.createdBy,
                    createdAt: new Date()
                };
                const entity = transactionalEntityManager.create(ShippingGuidePurchaseOrder, shippingPurchase);
                shippingGuidePurchaseOrderList.push(entity);
            }
            const entityCreatedShippingOrder = await transactionalEntityManager.save(shippingGuidePurchaseOrderList);
            logger.info("✅ entityCreatedShippingOrder Created → data={}", entityCreatedShippingOrder);
        }


        logger.info("✅ purchaseOrder Created → data={}", entityCreatedOrder);
        resp =  ResponseHandler.responseBuilder("",entityCreatedOrder,0, StatusCodes.CREATED, true, "");
    return resp;
}


function buildCriteria(criteria: ListPurchaseOrderQueryDto): FindOptionsWhere<PurchaseOrder> {
    const filter: FindOptionsWhere<PurchaseOrder> = {};

    if (criteria.status !== undefined) filter.status = criteria.status;
    if (criteria.supplierNumber !== undefined) filter.supplierNumber = criteria.supplierNumber;
    if (criteria.orderNumber !== undefined) filter.orderNumber = criteria.orderNumber;
    if (criteria.originId !== undefined) filter.originId = criteria.originId;
    if (criteria.purchaseOrderId !== undefined) filter.purchaseOrderId = criteria.purchaseOrderId;

    if (criteria.purchaseOrderDateAtInitial && criteria.purchaseOrderDateAtEnd) filter.purchaseOrderDate = Between(criteria.purchaseOrderDateAtInitial, criteria.purchaseOrderDateAtEnd);
    else if (criteria.purchaseOrderDateAtInitial) filter.purchaseOrderDate = MoreThanOrEqual(criteria.purchaseOrderDateAtInitial);
    else if (criteria.purchaseOrderDateAtEnd) filter.purchaseOrderDate = LessThanOrEqual(criteria.purchaseOrderDateAtEnd);

    return filter;
}

