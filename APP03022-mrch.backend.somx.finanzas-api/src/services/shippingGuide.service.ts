import { ShippingGuide } from "@/entities/ShippingGuide.entity.js";
import * as guides from "@/repositories/shippingGuide.repo.js";
import * as receptions from "@/repositories/reception.repo.js";
import type {
    CreateShippingGuideDto,
    ListShippingGuideQuery,
    UpdateShippingGuideDto,
    ShippginGuideSummaryListDto
} from "@/schemas/shippingGuide.schema.js";
import { Response } from "express";
import { ResponseHandlerDTO, ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { Between, EntityManager, In, LessThanOrEqual, MoreThanOrEqual, type FindOptionsWhere } from "typeorm";
import * as svcAxios from "@/services/axios.service.js";
import * as sharedCatalogService from "@/services/sharedCatalog.service.js";
import { GenericCatalogDetails, Supplier } from '@/response/GenericCatalogDetails.dto.js';
import 'dotenv/config';

import { logger } from "@/utils/logger.js";
import { ShippingGuidePurchaseOrder } from "@/entities/ShippingGuidePurchaseOrder.entity.js";
import * as constants from "@/constants/catalogConstantsCodes.js";
import { AuthenticatedRequest } from "@/middlewares/authToken.js";
import * as SGUtils from "@/utils/shippingGuide.utils.js";


const HEADERS_NAME = [
    "Número de proveedor",
    "Nombre Proveedor",
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
                response.write(guide.vendorNumber ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.vendorNumber ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.shippingGuideId ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.truckPlate ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.trailerPlate ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.origin ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.deliveryType ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.deliveryDate?.toISOString() ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.shippingDate?.toISOString() ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.createdAt?.toISOString() ?? "");
                response.write(FIELD_SEPARATOR);
                response.write(guide.createdBy ?? "");
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


type Item = {
  supplier?: Supplier | undefined;
  status?: undefined | GenericCatalogDetails  ;
  tipoProveedor: {
    id: number;
    code: string;
    description: string;
} | undefined;
// deliveryType?: GenericCatalogDetails | undefined;
OrigenCartaPorte?: GenericCatalogDetails | undefined;
orderNumber: string;
purchaseOrderStatus: number;
}

type ItemWithSG =
  Omit<ShippingGuide, 'status' | 'deliveryType'> &
 {
    supplier?: Supplier | undefined;
    status?: undefined | GenericCatalogDetails  ;
    tipoProveedor: {
        id: number;
        code: string;
        description: string;
    } | undefined;
    deliveryType?: GenericCatalogDetails | undefined;
    OrigenCartaPorte?: GenericCatalogDetails | undefined;
    orderNumber: string;
    purchaseOrderStatus: number;
  };



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
    let mappedResult: ItemWithSG[] = [];
    
    result.forEach((item) => {
        mappedResult = result.map((item) => {
            const foundSupplier = supplierList.find(
                supplier => supplier.supplierNumber?.toString() === item.vendorNumber?.toString()
            );

            const foundStatus = statusList.find(
                it => it.internalStatus?.toString() === item.status?.toString()
            );

            const foundTipoProveedor = foundSupplier?.supplierType;

            const foundTipoEntrega = tipoEntregaGuiaList.find(
                it => it.internalStatus?.toString() === item.deliveryType?.toString()
            );

            const foundOriginCP = catOrigenCartaPorteList.find(
                it => it.internalStatus?.toString() === item.originId?.toString()
            );

            const SGPO = item.shippingGuidePurchaseOrders as ShippingGuidePurchaseOrder[];

            return {
                ...item,
                status: foundStatus, // ✅ ya tipa bien
                supplier: foundSupplier,
                tipoProveedor: foundTipoProveedor,
                deliveryType: foundTipoEntrega,
                OrigenCartaPorte: foundOriginCP,
                orderNumber: String(SGPO[0]?.purchaseOrder?.orderNumber),
                purchaseOrderStatus: SGPO[0]?.purchaseOrder?.status ?? null,
            };
        });
    });

    const _totalItems = Number(total?.valueOf() == null ? 0 : Number(total?.valueOf()));
    let _totalPages = _totalItems / pageSize;

    if (_totalPages - Math.trunc(_totalPages) > 0) {
        _totalPages = Math.trunc(_totalPages) + 1;
    } else {
        _totalPages = Math.trunc(_totalPages);
    }

    const responsePageableDTO: ResponsePageableDTO = {
        content: mappedResult,
        totalElements: _totalItems,
        numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
        totalPages: _totalPages,
        pageNumber: q.pageNumber,
        pageSize: pageSize
    };

    logger.info("✅ get shippingGuide List → data={} ", result);
    return ResponseHandler.responseBuilder("", responsePageableDTO, 0, StatusCodes.OK, true, "");
}


export async function get(id: string, token: string) {
    const entity = await guides.findById(id);
    if (entity == null || entity == undefined) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS206, token);
        logger.info("❌ get shippingGuide NOT FOUND → data={} ", entity);
        return ResponseHandler.responseBuilder(CatMsgExc.description, entity, 0, StatusCodes.NOT_FOUND, true, "");
    }

    const {
        statusList,
        tipoProveedorList,
        tipoEntregaGuiaList,
        catOrigenCartaPorteList,
    } = await sharedCatalogService.getShippingGuideCatalogContext();
    const supplierList = await sharedCatalogService.getAllSuppliers(tipoProveedorList);

    const payload = SGUtils.mapShippingGuideToDetailPayload(entity, {
        statusList,
        tipoEntregaGuiaList,
        catOrigenCartaPorteList,
        supplierList,
    });

    logger.info("✅ get shippingGuide → data={} ", payload);
    return ResponseHandler.responseBuilder("", payload, 0, StatusCodes.OK, true, "");
}

export async function create(req: AuthenticatedRequest
    , files: Express.Multer.File[] | null,  origin: number
    , status: number, transactionalEntityManager: EntityManager, saveFileOnDb?: string, createShippingGuideList: CreateShippingGuideDto[] = []) {

    let resp = ResponseHandler.responseBuilder("", null, 0, StatusCodes.CREATED, true, "", "");


    for (const dto of createShippingGuideList) {
        //Valida si existe el proveedor
        await SGUtils.validateSupplier(req, dto.vendorNumber);
        //Verifica que no exista la guia de embarque en la  base
        const shippingGuide = await SGUtils.validateShippingGuide(dto, transactionalEntityManager);
        if (!shippingGuide) {
            
        type CreateShippingGuideParams = {
            dto: CreateShippingGuideDto;
            origin: number;
            files: Express.Multer.File[] | null;
            saveFileOnDb?: string | undefined;
            transactionalEntityManager: EntityManager;
            status: number;
            req: AuthenticatedRequest;
            createShippingGuideList: CreateShippingGuideDto[];
            resp: ResponseHandlerDTO;
        };
        
        const params: CreateShippingGuideParams = {
        dto,
        origin,
        files,
        saveFileOnDb,
        transactionalEntityManager,
        status,
        req,
        createShippingGuideList,
        resp
        };

        resp = await SGUtils.createShippingGuide(params);
        } else {
            resp = ResponseHandler.responseBuilder("La Guia de embarque ya se encuentra creada en FBC", null, 0, StatusCodes.CREATED, true, "", "BUS208");
        }
    }
    return resp;
}

export async function updateOneByUuid(id: string, dto: UpdateShippingGuideDto, token: string) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
        isStatusUpdated: true
    };

    const entityUpdated = await guides.updateOneByUuid(id, patch as Partial<ShippingGuide>);
    let response = ResponseHandler.responseBuilder("", entityUpdated, 0, StatusCodes.OK, true, "");
    if (!entityUpdated) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS209, token);
        logger.info("✅ shippingGuide  NOT updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.NOT_FOUND, false, "Guia de Embarque no encontrada");
    } else {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS210, token);
        logger.info("✅ shippingGuide updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.OK, true, "");
    }
    return response;
}


export async function updateOneByGuide(guideNumber: string, dto: UpdateShippingGuideDto, token: string) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
        isStatusUpdated: true
    };

    const entityUpdated = await guides.updateOneByGuide(guideNumber, patch as Partial<ShippingGuide>);
    let response = ResponseHandler.responseBuilder("", entityUpdated, 0, StatusCodes.OK, true, "");
    if (!entityUpdated) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS209, token);
        logger.info("✅ shippingGuide  NOT updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.NOT_FOUND, false, "Guia de Embarque no encontrada");
    } else {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS210, token);
        logger.info("✅ shippingGuide updated → data={}", entityUpdated);
        response = ResponseHandler.responseBuilder(CatMsgExc.description, entityUpdated, 0, StatusCodes.OK, true, "");
    }
    return response;
}

export async function remove(id: string) {
    await guides.deleteOne(id);
}

const ALLOWED_SOURCE_STATUSES = [1, 4] as const;

function isAllowedGuideStatusTransition(
    sourceStatus: number,
    targetStatus: number
): boolean {
    if (sourceStatus === 1) return targetStatus === 3 || targetStatus === 4;
    if (sourceStatus === 4) return targetStatus === 1;
    return false;
}

function buildStatusChangeComment(dto: {
    targetStatus: number;
    reasonId: number;
    series?: string | undefined;
    folio?: string | undefined;
    uuid?: string | undefined;
    comment: string;
}): string {
    const userComment = dto.comment.trim();
    if (dto.targetStatus === 3) {
        const meta = `S:${dto.series?.trim()}|F:${dto.folio?.trim()}|U:${dto.uuid?.trim()}|R:${dto.reasonId}`;
        const combined = `${meta} ${userComment}`;
        return combined.length <= 100 ? combined : combined.slice(0, 100);
    }
    const withReason = `[R:${dto.reasonId}] ${userComment}`;
    return withReason.length <= 100 ? withReason : withReason.slice(0, 100);
}

/** Cambio de estatus (1→3, 1→4, 4→1) — POST /shipping-guide/status. */
export async function updateGuideStatus(
    dto: {
        shippingGuideId: string;
        targetStatus: number;
        reasonId: number;
        series?: string | undefined;
        folio?: string | undefined;
        uuid?: string | undefined;
        comment: string;
    },
    token: string
) {
    const guide = await guides.findById(dto.shippingGuideId);
    if (!guide) {
        const CatMsgExc = await svcAxios.GetCatalogDetail(
            (process.env.CATALOGS_API_URL_BFF ?? "") +
                constants.CatalogNegocio.CATALOGS_API_NEGOCIO +
                constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS209,
            token
        );
        return ResponseHandler.responseBuilder(
            CatMsgExc.description,
            null,
            0,
            StatusCodes.NOT_FOUND,
            false,
            "Guía de Embarque no encontrada"
        );
    }

    const sourceStatus = Number(guide.status ?? 0);
    if (
        !ALLOWED_SOURCE_STATUSES.includes(
            sourceStatus as (typeof ALLOWED_SOURCE_STATUSES)[number]
        )
    ) {
        const CatMsgExc = await svcAxios.GetCatalogDetail(
            (process.env.CATALOGS_API_URL_BFF ?? "") +
                constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA +
                constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN301,
            token
        );
        return ResponseHandler.responseBuilder(
            CatMsgExc.description ??
                "Solo puedes actualizar guías en estatus 1 o 4.",
            null,
            -1,
            StatusCodes.BAD_REQUEST,
            false,
            "Estatus origen no permitido"
        );
    }

    let transitionOk = isAllowedGuideStatusTransition(
        sourceStatus,
        dto.targetStatus
    );
    const optionId = Number(process.env.SHIPPING_GUIDE_STATUS_OPTION_ID ?? 0);
    if (optionId > 0) {
        try {
            const catalogOk = await svcAxios.ValidStatus(
                (process.env.CATALOGS_API_URL_BFF ?? "") +
                    constants.CatalogStatusTrain.CATALOGS_API_STATUS_TRAIN +
                    constants.CatalogStatusTrain.CATALOGS_API_VALID_TRAIN,
                optionId,
                sourceStatus,
                dto.targetStatus,
                token
            );
            if (catalogOk) transitionOk = true;
        } catch {
            /* fallback a reglas locales */
        }
    }

    if (!transitionOk) {
        const CatMsgExc = await svcAxios.GetCatalogDetail(
            (process.env.CATALOGS_API_URL_BFF ?? "") +
                constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA +
                constants.CatalogAdvertencia.CATALOGS_API_ADVERTENCIA_DETAILS_KEY_WRN302,
            token
        );
        return ResponseHandler.responseBuilder(
            CatMsgExc.description ??
                "Transición no permitida. Solo se permite: 1→3, 1→4 o 4→1.",
            null,
            -1,
            StatusCodes.BAD_REQUEST,
            false,
            "Transición de estatus no válida"
        );
    }

    const comments = buildStatusChangeComment(dto);
    const entityUpdated = await guides.updateOneByUuid(dto.shippingGuideId, {
        status: dto.targetStatus,
        comments,
        updatedAt: new Date(),
        isStatusUpdated: true,
    } as Partial<ShippingGuide>);

    if (!entityUpdated) {
        const CatMsgExc = await svcAxios.GetCatalogDetail(
            (process.env.CATALOGS_API_URL_BFF ?? "") +
                constants.CatalogNegocio.CATALOGS_API_NEGOCIO +
                constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS209,
            token
        );
        return ResponseHandler.responseBuilder(
            CatMsgExc.description,
            null,
            0,
            StatusCodes.NOT_FOUND,
            false,
            "Guía de Embarque no encontrada"
        );
    }

    const CatMsg = await svcAxios.GetCatalogDetail(
        (process.env.CATALOGS_API_URL_BFF ?? "") +
            constants.CatalogNegocio.CATALOGS_API_NEGOCIO +
            constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS210,
        token
    );
    return ResponseHandler.responseBuilder(
        CatMsg.description,
        entityUpdated,
        0,
        StatusCodes.OK,
        true,
        ""
    );
}

function buildCancelComment(
    reasonId: number,
    comment?: string | undefined
): string | undefined {
    const userComment = (comment ?? "").trim();
    const withReason = `[R:${reasonId}]${userComment ? " " + userComment : ""}`;
    const stored = withReason.length <= 100 ? withReason : withReason.slice(0, 100);
    return stored || undefined;
}

/** Cancelación lógica: estatus 9 (ECF009). */
export async function cancelGuides(
    dto: {
        shippingGuideIds: string[];
        reasonId: number;
        comment?: string | undefined;
    },
    token: string
) {
    const comments = buildCancelComment(dto.reasonId, dto.comment);
    const updated: ShippingGuide[] = [];

    for (const id of dto.shippingGuideIds) {
        const entityUpdated = await guides.updateOneByUuid(id, {
            status: 9,
            comments,
            updatedAt: new Date(),
            isStatusUpdated: true,
        } as any);

        // Actualizar estatus recepciones ligadas a la guía de embarque (7 = Cancelada)
        if (entityUpdated) {
            const receptionPatch = {
                status: 7,
                updatedAt: new Date(),
            } as any;

            const guideNumber = entityUpdated.guideNumber?.trim() ?? "";
            if (guideNumber) {
                await receptions.updateManyByGuideNumber(guideNumber, receptionPatch);
            } else {
                const purchaseOrderIds = (entityUpdated.shippingGuidePurchaseOrders ?? [])
                    .map((link) => link.purchaseOrderId)
                    .filter((id): id is string => Boolean(id));
                if (purchaseOrderIds.length > 0) {
                    await receptions.updateManyByPurchaseOrderIds(
                        purchaseOrderIds,
                        receptionPatch
                    );
                }
            }
        }

        if (entityUpdated) {
            updated.push(entityUpdated);
        }
    }

    if (updated.length === 0) {
        const CatMsgExc = await svcAxios.GetCatalogDetail(
            (process.env.CATALOGS_API_URL_BFF ?? "") +
                constants.CatalogNegocio.CATALOGS_API_NEGOCIO +
                constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS209,
            token
        );
        return ResponseHandler.responseBuilder(
            CatMsgExc.description,
            null,
            0,
            StatusCodes.NOT_FOUND,
            false,
            "Guía de Embarque no encontrada"
        );
    }

    const CatMsg = await svcAxios.GetCatalogDetail(
        (process.env.CATALOGS_API_URL_BFF ?? "") +
            constants.CatalogNegocio.CATALOGS_API_NEGOCIO +
            constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS210,
        token
    );
    return ResponseHandler.responseBuilder(
        CatMsg.description,
        { updatedCount: updated.length },
        0,
        StatusCodes.OK,
        true,
        ""
    );
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
            filter.createdAt = Between(from, toEnd);
        } else {
            filter.createdAt = LessThanOrEqual(toEnd);
        }
    } else if (from) {
        filter.createdAt = MoreThanOrEqual(from);
    }

    return filter;
}
//}
