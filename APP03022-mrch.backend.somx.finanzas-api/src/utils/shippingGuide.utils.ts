import { ShippingGuide } from "@/entities/ShippingGuide.entity.js";
import type { Request } from "express";
import type {
    CreateShippingGuideDto,
} from "@/schemas/shippingGuide.schema.js";
import { ResponseHandlerDTO } from '@/response/ResponseHandler.dto.js';
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { EntityManager, type FindOptionsWhere } from "typeorm";
import * as svcAxios from "@/services/axios.service.js";
import { ShippingGuideDocument } from "@/entities/ShippingGuideDocument.entity.js";
import { GenericCatalogDetails, Supplier } from '@/response/GenericCatalogDetails.dto.js';
import 'dotenv/config';

import { logger } from "@/utils/logger.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { Reception } from "@/entities/Reception.entity.js";
import * as constants from "@/constants/catalogConstantsCodes.js";
import { logActivity, getTraceId } from '@/middlewares/logger.js';
import { ShippingGuideFile } from '@/entities/ShippingGuideFile.entity.js';
import { AuthenticatedRequest } from "@/middlewares/authToken.js";

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

export async function createShippingGuide(
    params: CreateShippingGuideParams
    ) {
    const entityCreatedList: ShippingGuide[] = [];
    const token = params.req.authToken?? '';
    const shippingGuideDocumentList: Partial<ShippingGuideDocument>[] = [];
    if (params.dto.shipingGuideDocumentList != null && params.dto.shipingGuideDocumentList.length > 0) {
        for (const shipdoc of params.dto.shipingGuideDocumentList) {
            let datarec: Partial<ShippingGuideDocument> = {
                fileName: shipdoc.fileName,
                fileType: shipdoc.fileType,
                status: 1, //Nace con el Status 1
            };
            if (params.origin == 2 && params.files != null && (params.saveFileOnDb?.toLowerCase() === "true")) { //Solo se guardan los documentos de CP
                datarec = await saveFilesOnDb(params.files, shipdoc.fileName, params.transactionalEntityManager, datarec);
            }
            shippingGuideDocumentList.push(datarec);
        };
    } 

    const data = {
        guideNumber: params.dto.guideNumber,
        vendorNumber: params.dto.vendorNumber,
        shippingDate: params.dto.deliveryDate,
        truckPlate: params.dto.truckPlate,
        originId: params.dto.originId,
        destinationId: params.dto.destinationId,
        status: params.status,
        deliveryDate: params.dto.deliveryDate,
        deliveryType: params.dto.deliveryType,
        shippingGuideDocuments: shippingGuideDocumentList,
        createdBy: 1,
        createdAt: new Date(),
        isStatusUpdated: false,
    };

    const temp = await params.transactionalEntityManager.create(ShippingGuide, data);
    entityCreatedList.push(temp);


    const entityCreated = await params.transactionalEntityManager.save(entityCreatedList);
    //Elimina campo data de la respuesta, que es el archivo en base  
    for (const it of entityCreated) {
        if (it.shippingGuideDocuments) {
            it.shippingGuideDocuments.forEach(doc => {
                if (doc.shippingGuideFile?.data) {
                    doc.shippingGuideFile.data = undefined;
                }
            });
        }
    }
    if (params.origin == 2 && params.files != null && (params.saveFileOnDb?.toLowerCase() !== "true")) { //Solo se guardan los documentos de CP
        await saveFilesOnBucket(params.files, params.req, token, undefined, params.createShippingGuideList);
    }
    const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS208, token);
    logger.info("✅ Register Carta Porte shippingGuide SUCCESS → data={}", entityCreated);
    params.resp = ResponseHandler.responseBuilder(CatMsgExc.description, { ...entityCreated, status: params.status }, 0, StatusCodes.CREATED, true, "", "BUS208");
    return params.resp;
}

export async function saveFilesOnDb(files: Express.Multer.File[], fileName: string, transactionalEntityManager: EntityManager, datarec: Partial<ShippingGuideDocument>) {
    const file = files.find(ite => ite.originalname == fileName);
    if (file != null) {
        let shippingGuideFileTmp: Partial<ShippingGuideFile> = {
            fileName: file?.originalname,
            mimeType: file?.mimetype,
            fileType: file?.mimetype.includes("xml") ? "xml" : "csv",
            data: file.buffer
        };
        datarec.shippingGuideFile = await transactionalEntityManager.create(ShippingGuideFile, shippingGuideFileTmp);
    }
    return datarec;
}

export async function saveFilesOnBucket(files: Express.Multer.File[], req: Request, token: string, folder: string | undefined
    , createShippingGuideList: CreateShippingGuideDto[]) {
    const nameFiles = files.map(f => f.originalname).join(',');
    const enviados: ResponseHandlerDTO = await svcAxios.sendFilesToBucket(req, files, token, folder);
    if (!enviados.success) {
        const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF ?? "") + constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS207, token);
        logger.info("❌ Register Carta Porte shippingGuide FAILED No se pudieron registrar los documentos en google storage → data={} folder={}", createShippingGuideList, folder);
        logActivity(true, `GCS upload FAILED → bucket. NameFiles:  ${nameFiles}`, enviados.detailError, JSON.stringify({ trace_id: getTraceId() }));
        throw new Error(CatMsgExc.description + JSON.stringify(enviados.detailError));
    } else {
        logActivity(false, `GCS upload SUCCESS → bucket. NameFiles:  ${nameFiles}`, null, JSON.stringify({ trace_id: getTraceId() }));
    }
    return enviados;
}

export async function validateSupplier(req: AuthenticatedRequest, vendorNumber: Number) {
    const supplierList = await svcAxios.GetSuppliers(req.authToken ?? '');
    const foundSupplier: Supplier | undefined = supplierList.find(
        supplier => supplier.supplierNumber?.toString() === vendorNumber?.toString()
    );
    if (foundSupplier == undefined) {
        throw new Error("No existe el proveedor en el catalogo de proveedores. Proveedor: " + vendorNumber);
    }
}

export async function validateShippingGuide(dto: CreateShippingGuideDto
    , transactionalEntityManager: EntityManager) {
    const filter: FindOptionsWhere<ShippingGuide> = {};
    if (dto.guideNumber !== undefined) {
        let guia = dto.guideNumber ?? '';
        filter.guideNumber = guia;
    }

    const shippingGuide = await transactionalEntityManager.findOneBy(ShippingGuide, filter);
    return shippingGuide;
}

/** Recepción activa ligada a la guía (misma lógica que purchase-orders: status de recepción, no de PO). */
export function resolveReceptionForGuide(
    receptions: Reception[] | undefined,
    guideNumber?: string
): Reception | undefined {
    const active = (receptions ?? []).filter((r) => Number(r.status) !== 8);
    const normalizedGuide = guideNumber?.trim();
    if (normalizedGuide) {
        const byGuide = active.find(
            (r) => r.guideNumber?.trim() === normalizedGuide
        );
        if (byGuide) return byGuide;
    }
    return active[0];
}

/** Evita referencias circulares (guía ↔ vínculos OC) al serializar el detalle. */
export function mapShippingGuideToDetailPayload(
    item: ShippingGuide,
    ctx: {
        statusList: GenericCatalogDetails[];
        tipoEntregaGuiaList: GenericCatalogDetails[];
        catOrigenCartaPorteList: GenericCatalogDetails[];
        supplierList: Supplier[];
    }
) {
    const foundSupplier = ctx.supplierList.find(
        (supplier) =>
            supplier.supplierNumber?.toString() === item.vendorNumber?.toString()
    );
    const foundStatus = ctx.statusList.find(
        (it) => it.internalStatus?.toString() === item.status?.toString()
    );
    const foundTipoEntrega = ctx.tipoEntregaGuiaList.find(
        (it) => it.internalStatus?.toString() === item.deliveryType?.toString()
    );
    const foundOriginCP = ctx.catOrigenCartaPorteList.find(
        (it) => it.internalStatus?.toString() === item.originId?.toString()
    );

    const shippingGuidePurchaseOrders = (
        item.shippingGuidePurchaseOrders ?? []
    ).map((link) => {
        const po = link.purchaseOrder as PurchaseOrder | undefined;
        const poSupplier = po
            ? ctx.supplierList.find(
                  (supplier) =>
                      supplier.supplierNumber?.toString() ===
                      po.supplierNumber?.toString()
              )
            : undefined;

        const reception = resolveReceptionForGuide(
            po?.receptions,
            item.guideNumber
        );
        const receptionDate = reception?.receptionDate ?? null;

        return {
            shippingGuidePurchaseOrderId: link.shippingGuidePurchaseOrderId,
            shippingGuideId: link.shippingGuideId,
            purchaseOrderId: link.purchaseOrderId,
            createdBy: link.createdBy,
            createdAt: link.createdAt,
            updatedBy: link.updatedBy,
            updatedAt: link.updatedAt,
            purchaseOrder: po
                ? {
                      purchaseOrderId: po.purchaseOrderId,
                      orderNumber: po.orderNumber,
                      supplierNumber: po.supplierNumber,
                      supplierBusinessName:
                          poSupplier?.businessName ?? null,
                      originId: po.originId,
                      amount: po.amount,
                      status: reception?.status ?? po.status,
                      purchaseOrderDate: po.purchaseOrderDate,
                      receptionDate,
                      createdBy: po.createdBy,
                      createdAt: po.createdAt,
                      updatedBy: po.updatedBy,
                      updatedAt: po.updatedAt,
                  }
                : null,
        };
    });

    return {
        shippingGuideId: item.shippingGuideId,
        guideNumber: item.guideNumber,
        vendorNumber: item.vendorNumber,
        truckPlate: item.truckPlate,
        trailerPlate: item.trailerPlate,
        originId: item.originId,
        deliveryType: foundTipoEntrega ?? item.deliveryType,
        status: foundStatus ?? item.status,
        comments: item.comments,
        deliveryDate: item.deliveryDate,
        shippingDate: item.shippingDate,
        createdBy: item.createdBy,
        createdAt: item.createdAt,
        updatedBy: item.updatedBy,
        updatedAt: item.updatedAt,
        isStatusUpdated: item.isStatusUpdated,
        supplier: foundSupplier,
        tipoProveedor: foundSupplier?.supplierType,
        OrigenCartaPorte: foundOriginCP,
        shippingGuidePurchaseOrders,
    };
}