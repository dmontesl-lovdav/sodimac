import * as purchaseOrderRepo from "@/repositories/purchaseOrder.repo.js";
import * as rececetionRepo from "@/repositories/reception.repo.js";
import * as shippingRepo from "@/repositories/shippingGuide.repo.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes, ReasonPhrases } from 'http-status-codes';
import { string, z } from "zod/v4";
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
import { AddendumManual } from '@/entities/AddendumManual.entity.js';
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
import { GenericCatalogDetails, Supplier } from '@/response/GenericCatalogDetails.dto.js';
import 'dotenv/config';
import { DeepPartial } from 'typeorm';
import * as constants from "@/constants/catalogConstantsCodes.js";


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

export async function listReception(q: ListReceptionQueryDto, token: string) {

    const supplierList = await svcAxios.GetSuppliers(token);
    const receptionDateAtEnd = q.receptionDateAtEnd;
    const parsedDateEnd = z.coerce.date().parse(receptionDateAtEnd);
    parsedDateEnd.setDate(parsedDateEnd.getDate() + 1);
    q.receptionDateAtEnd = parsedDateEnd;

    var [result, total, _numberOfElements] = await rececetionRepo.findAllPaginated(q, q.pageSize, q.pageNumber);
        result = result as Reception[];
        result.forEach((item, index) => {
            const foundSupplier: Supplier | undefined = supplierList.find(Supplier => Supplier.supplierNumber == item.purchaseOrder?.supplierNumber);
            (item as any).supplier = foundSupplier;
        });

        await enrichReceptionsListOriginCatalog(result as Reception[], token);

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


export async function updateReception(dto: UpdatePurchaseOrderDto, token: string) {
    const filter: FindOptionsWhere<PurchaseOrder> = {};

    if (dto.supplierNumber !== undefined) filter.supplierNumber = dto.supplierNumber;
    if (dto.orderNumber !== undefined) filter.orderNumber = dto.orderNumber;

    const purchaseOrder = await purchaseOrderRepo.findByOrderNumber(filter);
    let response = ResponseHandler.responseBuilder("",purchaseOrder,0, StatusCodes.OK, true, "");
    if (!purchaseOrder) {
        const CatMsgWrn = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA + constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN301, token);
        response = ResponseHandler.responseBuilder("WARNING: " + CatMsgWrn.key + ". " + CatMsgWrn.description,purchaseOrder,0, StatusCodes.NOT_FOUND, false, "");
    }
    
        // UPDATES status ONLY
    const persistenceList: Reception[] = purchaseOrder?.receptions ?? [];
    if (persistenceList.length == 0) {
        const CatMsgWrn = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA + constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN302, token);
        response = ResponseHandler.responseBuilder("WARNING: " + CatMsgWrn.key + ". " + CatMsgWrn.description,purchaseOrder,-1, StatusCodes.NOT_FOUND, false, "");
    } else {
          
        let receiptionToUpdate = persistenceList.filter(async function(recep) {
            if(recep.receptionNumber == dto.receptionNumber){
                
                if(dto.uuid != null && dto.uuid != undefined && dto.status == 2){ // ES ADDENDA MANUAL
                    const supplier: Supplier | undefined = await svcAxios.GetSupplierBySupplierNumber(dto.supplierNumber, token);
                    let addendaManual = new AddendumManual();
                    addendaManual.supplierNumber = dto.supplierNumber;
                    addendaManual.orderNumber = dto.orderNumber;
                    addendaManual.invoiceId = dto.uuid;
                    addendaManual.createdAt = new Date();
                    addendaManual.receptionId = recep.receptionId;
                    if(supplier != undefined){
                        addendaManual.supplierTypeId = supplier.supplierType.id;
                    }
                    
                    recep.addendumManual = addendaManual;
                }
                

                return recep;
            }
   
        });
        const reception = receiptionToUpdate.at(0)?? new Reception();
        const persistence: Reception = reception;
        const valStatus = await validarStatus(persistence, dto.status, 5, token);
        persistence.status = dto.status;
        persistence.comment = dto.comments;
        persistence.updatedAt = new Date();

        if (reception?.receptionId && valStatus) {
            const receptionUpdated = await rececetionRepo.updateOne(persistence.receptionId, persistence);
            const CatMsg = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogExitoso.CATALOGS_API_EXITOSO + constants.CatalogExitoso.CATALOGS_API_EXITOSO_DETAILS_KEY_RES205, token);
            response = ResponseHandler.responseBuilder(CatMsg.description,receptionUpdated,0, StatusCodes.OK, true, "");
        } else {

            const CatMsg = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA + constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN103, token);
            response = ResponseHandler.responseBuilder(CatMsg.description ,dto,-1, StatusCodes.BAD_REQUEST, false, CatMsg.key + "=" + CatMsg.description);
        }

    }


    return response
}

async function validarStatus(reception: Reception, newStatus: number, optionId: number, token?: string){
    const statusActual = reception.status;
    const isValid = await svcAxios.ValidStatus((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogStatusTrain.CATALOGS_API_STATUS_TRAIN + constants.CatalogStatusTrain.CATALOGS_API_VALID_TRAIN, optionId, (statusActual?? 0) ,newStatus, token ?? ''  );
    return isValid;

    // let response = false;
    // //statusActual == 0, Recepción disponilbe, 1=Consumida, 2=Consumida manual, 7=cancelada, 8=Borrado logico
    // if(statusActual == 0 && (newStatus == 1 || newStatus == 2 || newStatus == 7 || newStatus == 8)){
    //     response = true;
    // }
    // //1=Consumida, 3=En proceso Contable, 4=Rechazo Contable
    // if(statusActual == 1 && (newStatus == 3 || newStatus == 4)){ 
    //     response = true; 
    // }
    // //2=Consumida manual
    // if(statusActual == 2 && newStatus == 3 || newStatus == 4){ 
    //     response = true; 
    // }
    // //5=En proceso de Pago
    // if(statusActual == 3 && newStatus == 5){ 
    //     response = true; 
    // }
    // if(statusActual == 4 && newStatus == 5){ 
    //     response = true; 
    // }
    // //6=Pagada
    // if(statusActual == 5 && newStatus == 6){ 
    //     response = true; 
    // }
    // //6=Pagada, 7=Cancelada, 8=Borrado logico
    // if(statusActual == 6 || statusActual == 7 || statusActual == 8 ){ 
    //     response = false; 
    // }

    // return response;
}


export async function create(dto: CreatePurchaseOrderDto, transactionalEntityManager: EntityManager
    ,files: Express.Multer.File[] | null, folder: string | null , origin: number, token: string) {

    var resp = ResponseHandler.responseBuilder("",null,0, StatusCodes.CREATED, true, "");

        //let receptionsList: Array<Partial<Reception>> = new Array();
        let receptionsList: DeepPartial<Reception>[] = [];
        let receptionsSkuList: Partial<ReceptionSku>[] = [];
        const supplier: Supplier | undefined = await svcAxios.GetSupplierBySupplierNumber(dto.supplierNumber, token);
        const tipoReceptionSodimacList: GenericCatalogDetails[] = await svcAxios.GetCatalogDetailList((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogSupplierUrls.CATALOGS_API_TIPO_RECEPCION_SODIMAC + "/details",  token);

        dto.receptionList.forEach(async function (reception){
            if(supplier != undefined && supplier != null  && supplier.supplierType.id == 2 && reception.guideNumber != undefined){
                throw new Error('El tipo de proveedor que desea ingresar en la recepción no acepta este tipo de guías, favor de validar.');
            }
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

            // Calculate the total sum of the 'value' field
            const totalAmountSku: number = receptionsSkuList.reduce((accumulator, currentItem) => {
            return accumulator + z.coerce.number().parse(currentItem.totalCost);
            }, 0); // The '0' is the initial value of the accumulator
            if(totalAmountSku != z.coerce.number().parse(reception.amount) ){
                throw new Error('No coinicide la suma total del costo total de los articulos con su recepcion. ReceptionNumber: ' + reception.receptionNumber);
            }

            //Se obtiene el origenId cuando viene el campo Origin en String
            reception.originId = (reception.originId == undefined || reception.originId == null) ? 1 : reception.originId;
            if(reception.origin != undefined && reception.origin != null && reception.origin != "" && reception.originId == 0){
                //Busca el originid en catalogos

                const foundCatalog: GenericCatalogDetails | undefined = tipoReceptionSodimacList.find(Cat => Cat.externalKey == reception.origin);
                reception.originId = (foundCatalog == undefined) ? 1 : foundCatalog.internalStatus;
            };
            const datarec: Partial<Reception> = {
                originId: reception.originId,
                destinationId: reception.destinationId,
                amount: z.coerce.number().parse(reception.amount),
                status: (reception.status == undefined || reception.status == null) ? 0 : reception.status, //Nace con el Status 0
                comment: reception.comments,
                createdBy: reception.createdBy,
                receptionDate: reception.receptionDate,
                receptionSkus: receptionsSkuList,
                receptionNumber: reception.receptionNumber,
                guideNumber: (reception.guideNumber == undefined) ? "" : reception.guideNumber,
                
            };

            receptionsList.push(datarec);
        });
        
        //Valida si la OC ya existe en la base
         const filter: FindOptionsWhere<PurchaseOrder> = {};
        if (dto.supplierNumber !== undefined) filter.supplierNumber = dto.supplierNumber;
        if (dto.orderNumber !== undefined) filter.orderNumber = dto.orderNumber;
        let purchaseOrder = await purchaseOrderRepo.findByOrderNumber(filter);

        
        let dataP: DeepPartial<PurchaseOrder> | PurchaseOrder;
        let entityCreatedOrder: PurchaseOrder | (Partial<PurchaseOrder> & PurchaseOrder);
        if (purchaseOrder == null){


            // Suma segura (ver nota de Zod más abajo)
            const totalOCValue: number = receptionsList.reduce((acc, currentItem) => {
            // Usa coerce con safeParse o maneja undefined → 0
            const parsed = z.coerce.number().safeParse(currentItem.amount);
            return acc + (parsed.success && Number.isFinite(parsed.data) ? parsed.data : 0);
            }, 0);


            dataP  = {
                orderNumber: dto.orderNumber,
                supplierNumber: dto.supplierNumber,
                originId: origin,
                amount: z.coerce.number().parse(totalOCValue),
                purchaseOrderDate: dto.purchaseOrderDate,
                status: 1, //Nace con el valor 1 (1 - Disponible)
                createdBy: dto.createdBy,
                receptions: receptionsList
            };
        } else {
            //La PO existe y se busca sus recepciones actuales en la DB
            let recsactuales = await rececetionRepo.findAll(purchaseOrder.purchaseOrderId);

            //Verifica que no se de alta alguna recepcion que ya esta en la base
            receptionsList.forEach(async function (reception){
                const foundReception: Reception | undefined = recsactuales.find(rec => rec.receptionNumber == reception.receptionNumber);
                if(foundReception != undefined || foundReception != null ){
                        throw new Error('Ya existe una recepción dada de alta que se encuentra en la Peticion. ReceptionNumber: ' + reception.receptionNumber);
                    }
                
            });

            
            const allReceptions: DeepPartial<Reception>[] = [
            ...(recsactuales?.map(r => ({ ...r })) ?? []), // copiar a objeto simple
            ...receptionsList, // ya es DeepPartial<Reception>[]
            ];
            
            
            const receptionEntities: Reception[] = await Promise.all(
            allReceptions.map(p =>
                rececetionRepo.createOne(p, purchaseOrder) // Promise<Reception>
            )
            );

            // let allReceptions: Reception[] = [
            // ...(recsactuales || []),
            // ...receptionsList,
            // ] ;

            purchaseOrder.receptions = receptionEntities;

            
            // // Ahora sí, son Reception[]
            // purchaseOrder.receptions = [
            // ...((purchaseOrder.receptions ?? []) as Reception[]),
            // ...receptionEntities,
            // ];


            // Calculate the total sum of the 'value' field
            const totalOCValue: number = allReceptions.reduce((accumulator, currentItem) => {
            return accumulator + z.coerce.number().parse(currentItem.amount == undefined ?  0 : z.coerce.number().parse(currentItem.amount) );
            }, 0); // The '0' is the initial value of the accumulator
            
            purchaseOrder.amount = totalOCValue;
            dataP = purchaseOrder;
        }
        const po = transactionalEntityManager.create(PurchaseOrder, dataP); 
        entityCreatedOrder = await transactionalEntityManager.save(PurchaseOrder, po); 

        if(dto.shippingGuideList && dto.shippingGuideList.length > 0) {  //APLICA PARA CARTA PORTE
            const status = 2; //Nace Guia de embarque Con OC
            let created : ResponseHandlerDTO;
            
            created = await svcShipping.create(dto.shippingGuideList, files, folder, origin, status, transactionalEntityManager, token);
            if(!created.success){    
                const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC015, token);
                throw new Error(CatMsgExc.description + created.message + created.detailError);
            }
            logger.info("✅ ShippingGuide Created → data={}", created);
        }

        if(dto.guideNumber && dto.guideNumber.length > 0){  //APLICA PARA CARTA PORTE
            let shippingGuidePurchaseOrderList: ShippingGuidePurchaseOrder[] = [];
            for (let i: number = 0; i < dto.guideNumber.length; i++) {
                const filter: FindOptionsWhere<ShippingGuide> = {};
                if (dto.guideNumber[i]?.guide !== undefined) {
                    let guia = dto.guideNumber[i]?.guide ?? '';
                    filter.guideNumber = guia;
                }

                const shippingGuide = await transactionalEntityManager.findOneBy(ShippingGuide, filter);
                if (!shippingGuide) {
                    const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogException.CATALOGS_API_EXCEPTION + constants.CatalogException.CATALOGS_API_EXCEPTION_DETAILS_KEY_EXC016, token);
                    throw new Error(CatMsgExc.description + dto.guideNumber[i]?.guide + ' Con la OC: ' + dto.orderNumber);
                }
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

        const CatMsg = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS212, token);
        logger.info("✅ purchaseOrder Created → data={}", entityCreatedOrder);
        resp =  ResponseHandler.responseBuilder(CatMsg.description,entityCreatedOrder,0, StatusCodes.CREATED, true, "");

        //} else {


            //const entityCreatedOrder = await transactionalEntityManager.save(PurchaseOrder, purchaseOrder); 

        //}
        
    
    return resp;
}

async function fetchReceptionOriginIdToLabel(token: string): Promise<Map<number, string>> {
    const map = new Map<number, string>();
    try {
        const base =
            (process.env.CATALOGS_API_URL_BFF ?? "") +
            constants.CatalogSupplierUrls.CATALOGS_API_TIPO_RECEPCION_SODIMAC +
            "/details";
        const rows = await svcAxios.GetCatalogDetailList(base, token);
        for (const c of rows ?? []) {
            const id = Number(c.internalStatus);
            if (!Number.isFinite(id)) {
                continue;
            }
            const label =
                [c.description, c.value, c.externalKey, c.key].find(
                    (s): s is string => typeof s === "string" && String(s).trim().length > 0,
                )?.trim() ?? "";
            if (label) {
                map.set(id, label);
            }
        }
    } catch (e) {
        logger.warn("fetchReceptionOriginIdToLabel: catálogo de orígenes no disponible: {}", e);
    }
    return map;
}

function applyOriginCatalogLabels(receptions: Reception[], lookup: Map<number, string>): void {
    for (const rec of receptions) {
        const raw =
            rec.originId === undefined || rec.originId === null
                ? NaN
                : Number(rec.originId);
        const nm = Number.isFinite(raw) ? lookup.get(raw) ?? "" : "";
        (rec as Reception & { originName?: string }).originName = nm;
    }
}

/** Listado GET `/purchase-orders`: enriquece cada recepción anidada con `originName` (catálogo BFF). */
export async function enrichPurchaseOrdersRecepcionesOriginCatalog(
    purchaseOrders: PurchaseOrder[],
    token: string,
): Promise<void> {
    if (!purchaseOrders?.length) {
        return;
    }
    const lookup = await fetchReceptionOriginIdToLabel(token);
    for (const po of purchaseOrders) {
        applyOriginCatalogLabels(po.receptions ?? [], lookup);
    }
}

/** Listado GET `/purchase-orders/listReception`. */
export async function enrichReceptionsListOriginCatalog(
    receptions: Reception[],
    token: string,
): Promise<void> {
    if (!receptions?.length) {
        return;
    }
    const lookup = await fetchReceptionOriginIdToLabel(token);
    applyOriginCatalogLabels(receptions, lookup);
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

function updateReceptionAndAddendaManual(dto: { supplierNumber: number; orderNumber: string; receptionNumber: string; status: number; uuid?: string | undefined; }) {
    let response = ResponseHandler.responseBuilder("",null,0, StatusCodes.OK, true, "");
    return response;
}

