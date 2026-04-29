import { ShippingGuide } from "@/entities/ShippingGuide.entity.js";

import * as guides from "@/repositories/shippingGuide.repo.js";
import type {
    CreateShippingGuideDto,
    ListShippingGuideQuery,
    UpdateShippingGuideDto,
    ShippginGuideSummaryListDto
} from "@/schemas/shippingGuide.schema.js";
import { Response } from "express";
import { ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { Between, EntityManager, In, LessThanOrEqual, MoreThanOrEqual, type FindOptionsWhere } from "typeorm";
import * as svcAxios from "@/services/axios.service.js";
import * as sharedCatalogService from "@/services/sharedCatalog.service.js";
import { ShippingGuideDocument } from "@/entities/ShippingGuideDocument.entity.js";
import { GenericCatalogDetails, Supplier } from '@/response/GenericCatalogDetails.dto.js';
import 'dotenv/config';

import { logger } from "@/utils/logger.js";
import { ShippingGuidePurchaseOrder } from "@/entities/ShippingGuidePurchaseOrder.entity.js";

//export class MyService {
const HEADERS_NAME = [
    "Número de proveedor",
    "Nombre proveedor",
    "Guía de embarque",
    "Placa",
    "Placa remolque",
    "Origen",
    "Tipo entrega",
    "Fecha entrega",
    "Fecha envió",
    "Fecha registro",
    "Estatus",
];
const FIELD_SEPARATOR = '\t';
const LINE_SEPARATOR = '\n';

export async function writeCsv(criteria: ListShippingGuideQuery, response: Response) {
    let page = 0;
    const size = criteria.pageSize || 333;
    let result: ShippingGuide[];

    response.setHeader("Content-Type", "text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=shipping_guides.csv");

    for (const header of HEADERS_NAME) {
        response.write(header);
        response.write(FIELD_SEPARATOR);
    }

    do {
        result = await guides.findAllByPage(criteria, page, size);
        if (result && result.length > 0) {
            for (const guide of result) {
                response.write(LINE_SEPARATOR);
                response.write(guide.vendorNumber || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.vendorNumber || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.shippingGuideId || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.truckPlate || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.trailerPlate || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.origin || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.deliveryType || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.deliveryDate?.toISOString() || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.shippingDate?.toISOString() || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.createdAt?.toISOString() || "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.createdBy || "");
            }
        }
        page++;
    } while (result && result.length === size);
    logger.info("✅ get shippingGuide csv → data={} ", result);
    response.end();
}

export async function list(criteria: ListShippingGuideQuery) {
    return guides.findAll(criteria);
}

export async function listPaginated(q: ListShippingGuideQuery, allowedVendors: number[] | null = null) {
    const {
        statusList,
        tipoProveedorList,
        tipoEntregaGuiaList,
        catOrigenCartaPorteList,
    } = await sharedCatalogService.getShippingGuideCatalogContext();

    const supplierList = await sharedCatalogService.getAllSuppliers(tipoProveedorList);

    const filter: FindOptionsWhere<ShippingGuide> = buildCriteria(q);
    if (allowedVendors && allowedVendors.length > 0) {
        filter.vendorNumber = In(allowedVendors);
    }
    let pageSize: number = 10;  //Por default es 10 registros por pagina
    if (q.pageSize !== undefined) {
        pageSize = q.pageSize;
    }

    let [result, total, _numberOfElements] = await guides.findAllPaginated(filter, pageSize, q.pageNumber);
    result = result as ShippingGuide[];

    result.forEach((item) => {
        const foundSupplier: Supplier | undefined = supplierList.find(
            supplier => supplier.supplierNumber?.toString() === item.vendorNumber?.toString()
        );

        const foundStatus: GenericCatalogDetails | undefined = statusList.find(
            it => it.internalStatus?.toString() === item.status?.toString()
        );

        const foundTipoProveedor = foundSupplier?.supplierType;

        const foundTipoEntrega: GenericCatalogDetails | undefined = tipoEntregaGuiaList.find(
            it => it.internalStatus?.toString() === item.deliveryType?.toString()
        );

        const foundOriginCP: GenericCatalogDetails | undefined = catOrigenCartaPorteList.find(
            it => it.internalStatus?.toString() === item.originId?.toString()
        );

        (item as any).supplier = foundSupplier;
        (item as any).status = foundStatus;
        (item as any).tipoProveedor = foundTipoProveedor;
        (item as any).deliveryType = foundTipoEntrega;
        (item as any).OrigenCartaPorte = foundOriginCP;

        const SGPO: ShippingGuidePurchaseOrder[] = item.shippingGuidePurchaseOrders as ShippingGuidePurchaseOrder[];
        (item as any).orderNumber = SGPO[0]?.purchaseOrder?.orderNumber;
        delete item.shippingGuidePurchaseOrders;
    });

    const _totalItems = Number(total?.valueOf() == null ? 0 : Number(total?.valueOf()));
    let _totalPages = _totalItems / pageSize;

    if (_totalPages - Math.trunc(_totalPages) > 0) {
        _totalPages = Math.trunc(_totalPages) + 1;
    } else {
        _totalPages = Math.trunc(_totalPages);
    }

    const responsePageableDTO: ResponsePageableDTO = {
        content: result,
        totalElements: _totalItems,
        numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
        totalPages: _totalPages,
        pageNumber: q.pageNumber,
        pageSize: pageSize
    };

    logger.info("✅ get shippingGuide List → data={} ", result);
    return ResponseHandler.responseBuilder("", responsePageableDTO, 0, StatusCodes.OK, true, "");
}

export async function get(id: string) {
    const entity = await guides.findById(id);
    if (entity == null || entity == undefined) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF ?? "") + process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS206);
        logger.info("❌ get shippingGuide NOT FOUND → data={} ", entity);
        return ResponseHandler.responseBuilder(CatMsgExc.description, entity, 0, StatusCodes.NOT_FOUND, true, "");
    }
    logger.info("✅ get shippingGuide → data={} ", entity);
    return ResponseHandler.responseBuilder("", entity, 0, StatusCodes.OK, true, "");
}

export async function create(createShippingGuideList: CreateShippingGuideDto[] = []
    , files: Express.Multer.File[] | null, folder: string | null, origin: number
    , status: number, transactionalEntityManager: EntityManager) {

    let enviados = true;
    let resp = ResponseHandler.responseBuilder("", null, 0, StatusCodes.CREATED, true, "", "");

    // files.forEach(async (file, index) => {
    //     console.log(`Archivo ${index + 1}:`);
    //     console.log('Nombre:', file.originalname);
    //     console.log('Tipo:', file.mimetype);
    //     console.log('Tamaño:', file.size);
    //     // Ejemplo: convertir a string si es texto
    //     const contenido = file.buffer.toString('utf-8');
    //     //console.log('Contenido:', contenido);             
    // });

    const entityCreatedList: ShippingGuide[] = [];
    createShippingGuideList.forEach((dto) => {
        const shippingGuideDocumentList: Partial<ShippingGuideDocument>[] = [];
        if (dto.shipingGuideDocumentList != null && dto.shipingGuideDocumentList.length > 0) {
            dto.shipingGuideDocumentList.forEach((shipdoc) => {
                const datarec: Partial<ShippingGuideDocument> = {
                    fileName: shipdoc.fileName,
                    fileType: shipdoc.fileType,
                    status: 1, //Nace con el Status 1
                };

                shippingGuideDocumentList.push(datarec);
            });
        }

        const data = {
            guideNumber: dto.guideNumber,
            vendorNumber: dto.vendorNumber,
            shippingDate: dto.deliveryDate,
            truckPlate: dto.truckPlate,
            originId: origin,
            status: status,
            deliveryDate: dto.deliveryDate,
            deliveryType: dto.deliveryType,
            shippingGuideDocuments: shippingGuideDocumentList,
            createdBy: 1,
            createdAt: new Date(),
            isStatusUpdated: false,
        };

        const temp = transactionalEntityManager.create(ShippingGuide, data);
        entityCreatedList.push(temp);
    });
    const entityCreated = await transactionalEntityManager.save(entityCreatedList);

    if (origin == 2 && files != null && folder != null) { //Solo se guardan los documentos de CP
        enviados = await svcAxios.sendFilesToBucket(files, folder);
        if (!enviados) {
            const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF ?? "") + process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS207);
            logger.info("❌ Register Carta Porte shippingGuide FAILED No se pudieron registrar los documentos en google storage → data={} folder={}", createShippingGuideList, folder);
            throw new Error(CatMsgExc.description);
        }
    }
    const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF ?? "") + process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS208);
    logger.info("✅ Register Carta Porte shippingGuide SUCCESS → data={} folder={}", entityCreated, folder);
    resp = ResponseHandler.responseBuilder(CatMsgExc.description, { ...entityCreated, status: status }, 0, StatusCodes.CREATED, true, "", "BUS208");

    return resp;
}

export async function updateOneByUuid(id: string, dto: UpdateShippingGuideDto) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
        isStatusUpdated: true
    };

    const entityUpdated = await guides.updateOneByUuid(id, patch as any);
    let response = ResponseHandler.responseBuilder("", entityUpdated, 0, StatusCodes.OK, true, "");
    if (!entityUpdated) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF ?? "") + process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS209);
        logger.info("✅ shippingGuide  NOT updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.NOT_FOUND, false, "Guia de Embarque no encontrada");
    } else {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF ?? "") + process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS210);
        logger.info("✅ shippingGuide updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.OK, true, "");
    }
    return response;
}

export async function updateOneByGuide(guideNumber: string, dto: UpdateShippingGuideDto) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
        isStatusUpdated: true
    };

    const entityUpdated = await guides.updateOneByGuide(guideNumber, patch as any);
    let response = ResponseHandler.responseBuilder("", entityUpdated, 0, StatusCodes.OK, true, "");
    if (!entityUpdated) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF ?? "") + process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS209);
        logger.info("✅ shippingGuide  NOT updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.NOT_FOUND, false, "Guia de Embarque no encontrada");
    } else {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF ?? "") + process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS210);
        logger.info("✅ shippingGuide updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.OK, true, "");
    }
    return response;
}

export async function remove(id: string) {
    await guides.deleteOne(id);
}

export async function findAll(dto: ListShippingGuideQuery) {
    const filter: FindOptionsWhere<ShippingGuide> = {};
    interface ShippingGuideSummary {
        shippingGuideId: string | undefined;
        guideNumber: string | undefined;
        status?: number | undefined;
        vendorNumber?: number | undefined;
        originId?: number | undefined;
    }

    const shippingGuideList = await guides.findByAll(buildCriteria(dto));

    const shippingGuideSummarys: ShippingGuideSummary[] = shippingGuideList.map(item => ({
        shippingGuideId: item.shippingGuideId,
        guideNumber: item.guideNumber,
        vendorNumber: item.vendorNumber,
        status: item.status,
        originId: item.originId
    }));

    const response = ResponseHandler.responseBuilder("", shippingGuideSummarys, 0, StatusCodes.OK, true, "");
    return response;
}

export async function updateAllStatusGuia(dto: ShippginGuideSummaryListDto) {
    const shippingGuideIds: string[] = dto.data.map(item => item);
    const updateEntities = await guides.updateAllStatus(shippingGuideIds);

    const response = ResponseHandler.responseBuilder("", updateEntities, 0, StatusCodes.OK, true, "");
    return response;
}

export function buildCriteria(criteria: ListShippingGuideQuery): FindOptionsWhere<ShippingGuide> {
    const filter: FindOptionsWhere<ShippingGuide> = {};

    if (criteria.id) filter.shippingGuideId = criteria.id;
    if (criteria.guideNumber) filter.guideNumber = criteria.guideNumber;
    if (criteria.status !== undefined) filter.status = criteria.status;
    if (criteria.vendorNumber !== undefined) filter.vendorNumber = criteria.vendorNumber;
    if (criteria.deliveryType !== undefined) filter.deliveryType = criteria.deliveryType;
    if (criteria.originId !== undefined) filter.originId = criteria.originId;
    if (criteria.isStatusUpdated !== undefined) filter.isStatusUpdated = criteria.isStatusUpdated;

    let from = criteria.from ?? undefined;
    let to = criteria.to ?? undefined;

    if (from && !to) {
        to = new Date(from);
    }

    if (to) {
        const toEnd = new Date(to.getTime());
        toEnd.setDate(toEnd.getDate() + 1);

        if (from) {
            filter.deliveryDate = Between(from, toEnd);
        } else {
            filter.deliveryDate = LessThanOrEqual(toEnd);
        }
    } else if (from) {
        filter.deliveryDate = MoreThanOrEqual(from);
    }

    return filter;
}
//}