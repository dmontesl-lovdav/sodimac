import * as purchaseOrderRepo from "@/repositories/purchaseOrder.repo.js";
import * as tenantFianaceAddenduemRepo from "@/repositories/tenant_fiscal.addendum.repo.js";
import * as rececetionRepo from "@/repositories/reception.repo.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { z } from "zod/v4";
import { HttpError } from "@/utils/HttpError.js";
import type {
CreatePurchaseOrderDto,
UpdatePurchaseOrderDto,
ListPurchaseOrderQueryDto,
} from "@/schemas/purchaseOrder.schema.js";
import type {
ListReceptionQueryDto
} from "@/schemas/reception.schema.js";
import { ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { Reception } from '@/entities/Reception.entity.js';
import { ReceptionSku } from '@/entities/ReceptionSku.entity.js';
import { AddendumManual } from '@/entities/AddendumManual.entity.js';
import {
    FindOptionsWhere,
    EntityManager,
    Between,
    MoreThanOrEqual,
    LessThanOrEqual
} from "typeorm";
import { logger } from "@/utils/logger.js";
import * as svcAxios from "@/services/axios.service.js";
import { GenericCatalogDetails, Supplier } from '@/response/GenericCatalogDetails.dto.js';
import 'dotenv/config';
import { DeepPartial } from 'typeorm';
import * as constants from "@/constants/catalogConstantsCodes.js";
import * as POUtils from "@/utils/purchaseOrder.utils.js";
import { AuthenticatedRequest } from "@/middlewares/authToken.js";
import { QueryFailedError } from "typeorm"


type Item = {
    color?: string;
    supplier?: Supplier | undefined;
};
type ItemWithReception = Item & Reception;
export async function listReception(q: ListReceptionQueryDto, token: string) {

    const supplierList = await svcAxios.GetSuppliers(token);
    const receptionDateAtEnd = q.receptionDateAtEnd;
    const parsedDateEnd = z.coerce.date().parse(receptionDateAtEnd);
    parsedDateEnd.setDate(parsedDateEnd.getDate() + 1);
    q.receptionDateAtEnd = parsedDateEnd;
    const CatEstatusRecepcion  = await svcAxios.GetCatalogDetailList((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatEstatusRecepcion.CATALOGS_API_STATUS_RECEPTION , token);

    let [result, total, _numberOfElements] = await rececetionRepo.findAllPaginated(q, q.pageSize, q.pageNumber);
        result = result as Reception[];
        result.forEach((item, index) => {
            const foundSupplier: Supplier | undefined = supplierList.find(Supplier => Supplier.supplierNumber == item.purchaseOrder?.supplierNumber);
            (item as ItemWithReception).supplier = foundSupplier;

            const result = CatEstatusRecepcion.find(ite => ite.value === item.status?.toString());
            if(result){
                (item as ItemWithReception).color = result.color;
            }

        });

        await POUtils.enrichReceptionsListOriginCatalog(result as Reception[], token);

    const _totalItems = Number(total?.valueOf() == null ? 0 : Number(total?.valueOf()));
    let _totalPages = _totalItems/q.pageSize;

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
    const catalogBase =
        (process.env.CATALOGS_API_URL_BFF ?? "") +
        constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA;

    if (!purchaseOrder) {
        const CatMsgWrn = await svcAxios.GetCatalogDetail(
            catalogBase +
                constants.CatalogAdvertencia
                    .CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN301,
            token
        );
        return ResponseHandler.responseBuilder(
            POUtils.buildCatalogWarningMessage(
                CatMsgWrn,
                dto.orderNumber,
                dto.supplierNumber
            ),
            purchaseOrder,
            0,
            StatusCodes.NOT_FOUND,
            false,
            ""
        );
    }

    const persistenceList = await rececetionRepo.findAll(
        purchaseOrder.purchaseOrderId
    );

    if (persistenceList.length === 0) {
        const CatMsgWrn = await svcAxios.GetCatalogDetail(
            catalogBase +
                constants.CatalogAdvertencia
                    .CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN302,
            token
        );
        return ResponseHandler.responseBuilder(
            POUtils.buildCatalogWarningMessage(
                CatMsgWrn,
                dto.orderNumber,
                dto.supplierNumber
            ),
            purchaseOrder,
            -1,
            StatusCodes.NOT_FOUND,
            false,
            ""
        );
    }

    const receptionMatch = persistenceList.find(
        (recep) =>
            String(recep.receptionNumber).trim() ===
            String(dto.receptionNumber).trim()
    );

    if (!receptionMatch) {
        const CatMsgWrn = await svcAxios.GetCatalogDetail(
            catalogBase +
                constants.CatalogAdvertencia
                    .CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN302,
            token
        );
        return ResponseHandler.responseBuilder(
            POUtils.buildCatalogWarningMessage(
                CatMsgWrn,
                dto.orderNumber,
                dto.supplierNumber
            ),
            purchaseOrder,
            -1,
            StatusCodes.NOT_FOUND,
            false,
            ""
        );
    }

    const persistence: Reception = receptionMatch;

    if (dto.uuid != null && dto.uuid != undefined && dto.status == 2) {
        const supplier: Supplier | undefined = await svcAxios.GetSupplierBySupplierNumber(dto.supplierNumber, token);
        //Valida que no exista en tenant_fiscal.Invoice
        const add = await tenantFianaceAddenduemRepo.findByInvoideUuid(dto.uuid);
        if (add){
             throw new HttpError(404, "La factura uuid se encuentra previamente registrada en addenda, Por favor, validar con el área de finanzas");
        }
        const addendaManual = new AddendumManual();
        addendaManual.supplierNumber = dto.supplierNumber;
        addendaManual.orderNumber = dto.orderNumber;
        addendaManual.invoiceId = dto.uuid;
        addendaManual.createdAt = new Date();
        addendaManual.receptionId = persistence.receptionId;
        if (supplier != undefined) {
            addendaManual.supplierTypeId = supplier.supplierType.id;
        }
        persistence.addendumManual = addendaManual;
    }
    const valStatus = await POUtils.validarStatus(persistence, dto.status, 5, token);
    persistence.status = dto.status;
    persistence.comment = dto.comments;
    persistence.updatedAt = new Date();

    if (persistence?.receptionId && valStatus) {
        try { 
                const receptionUpdated = await rececetionRepo.updateOne(persistence.receptionId,persistence );
                const CatMsg = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogExitoso.CATALOGS_API_EXITOSO + constants.CatalogExitoso.CATALOGS_API_EXITOSO_DETAILS_KEY_RES205, token);
                return ResponseHandler.responseBuilder(CatMsg.description,receptionUpdated,0, StatusCodes.OK, true, "");

            } 
            catch (error) {
                // Validación para errores de base de datos
                if (error instanceof QueryFailedError) {                  
                        // PostgreSQL
                        if ((error as any).code === '23505') {
                            const detail = (error as any).detail;
                            if (detail.includes('invoice_uuid')) {
                               throw new HttpError(404, "La factura uuid se encuentra previamente registrada en addenda Manual, Por favor, validar con el área de finanzas");
                            }
                        }
                    }

                    // Otros errores
                let err = "";
                if (error instanceof Error) {
                    err= error.message + error.cause + error.stack;
                    throw new Error('Error al actualizar la recepción: ' + err);
                }

            }
    }

    const CatMsg = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA + constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN103, token);
    return ResponseHandler.responseBuilder(CatMsg.description ,dto,-1, StatusCodes.BAD_REQUEST, false, CatMsg.key + "=" + CatMsg.description);
}



export async function create(req: AuthenticatedRequest, dto: CreatePurchaseOrderDto, transactionalEntityManager: EntityManager
    ,files: Express.Multer.File[] | null, origin: number, folder?: string, saveFileOnDb?: string) {
    const token = req.authToken ?? '';
    let resp = ResponseHandler.responseBuilder("",null,0, StatusCodes.CREATED, true, "");   

    let receptionsList: DeepPartial<Reception>[] = [];
    let receptionsSkuList: Partial<ReceptionSku>[] = [];
    const { supplier, tipoReceptionSodimacList }
    : { supplier: Supplier | undefined; tipoReceptionSodimacList: GenericCatalogDetails[]; } 
    = await POUtils.getCatalogs(dto, token);
    if(supplier == undefined){
          throw new Error("No existe el proveedor en el catalogo de proveedores. Proveedor: " + dto.supplierNumber);
    }
    POUtils.fillReceptionList(dto, supplier, receptionsSkuList, tipoReceptionSodimacList, receptionsList);
    
    //Valida si la OC ya existe en la base
    let purchaseOrder = await POUtils.getPO(dto);

    let dataP: DeepPartial<PurchaseOrder> | PurchaseOrder;
    let entityCreatedOrder: PurchaseOrder | (Partial<PurchaseOrder> & PurchaseOrder);
    if (purchaseOrder == null){
        dataP = POUtils.fillPO(receptionsList, dto, origin);
    } else {
        //La PO existe y se busca sus recepciones actuales en la DB
        dataP = await POUtils.fillReceptionOnPO(purchaseOrder, receptionsList);
    }
    entityCreatedOrder = await POUtils.createOrUpdatePO(transactionalEntityManager, dataP); 

    if(dto.shippingGuideList && dto.shippingGuideList.length > 0) {  //APLICA PARA CARTA PORTE
        await POUtils.createShippingGuide(req, files, origin, transactionalEntityManager, saveFileOnDb, dto, token);
    }

    if(dto.guideNumber && dto.guideNumber.length > 0){  //APLICA PARA CARTA PORTE
        await POUtils.createRelations(dto, transactionalEntityManager, token, entityCreatedOrder);
    }

    const CatMsg = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS212, token);
    logger.info("✅ purchaseOrder Created → data={}", entityCreatedOrder);
    resp =  ResponseHandler.responseBuilder(CatMsg.description,entityCreatedOrder,0, StatusCodes.CREATED, true, "");

    //} else {


        //const entityCreatedOrder = await transactionalEntityManager.save(PurchaseOrder, purchaseOrder); 

    //}
        
    
    return resp;
}



/** Listado GET `/purchase-orders`: enriquece cada recepción anidada con `originName` (catálogo BFF). */
export async function enrichPurchaseOrdersRecepcionesOriginCatalog(
    purchaseOrders: PurchaseOrder[],
    token: string,
): Promise<void> {
    if (!purchaseOrders?.length) {
        return;
    }
    const lookup = await POUtils.fetchReceptionOriginIdToLabel(token);
    for (const po of purchaseOrders) {
        POUtils.applyOriginCatalogLabels(po.receptions ?? [], lookup);
    }
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

