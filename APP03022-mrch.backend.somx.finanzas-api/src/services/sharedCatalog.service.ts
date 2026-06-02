import { In } from "typeorm";
import { getDataSource } from "@/config/typeorm-datasource.js";
import { SharedCatalogHeader } from "@/entities/SharedCatalogHeader.entity.js";
import { SharedCatalogDetail } from "@/entities/SharedCatalogDetail.entity.js";
import { SharedSupplier } from "@/entities/SharedSupplier.entity.js";
import { SharedSupplierType } from "@/entities/SharedSupplierType.entity.js";
import { SharedCatalogDictionaryLang } from "@/entities/SharedCatalogDictionaryLang.entity.js";
import type { GenericCatalogDetails, Supplier } from "@/response/GenericCatalogDetails.dto.js";

const CATALOG_CODES = {
    statusCartaPorteFBC: "CatEstatusCartaPorteFBC",
    tipoEntregaGuia: "CatTipoEntregaGuia",
    tipoProveedor: "CatTipoProveedor",
    origenCartaPorte: "CatOrigenCartaPorte",
} as const;

function mapCatalogDetailToDto(entity: SharedCatalogDetail, dictLangEntity: SharedCatalogDictionaryLang[]): GenericCatalogDetails {
    return {
        key: entity.key ?? "",
        value: entity.value ?? "",
        color: entity.color ?? "",
        externalKey: entity.externalKey ?? "",
        internalStatus: entity.internalStatus ?? 0,
        description: dictLangEntity.find(item => item.dictId === entity.dictId)?.description ?? "", //entity.dictId.value ?? "",
        success: true
    };
}

function buildSupplierType(
    supplierTypeId: number | undefined,
    entitySupplierType: SharedSupplierType[],
) {
    const foundTipoProveedor = entitySupplierType.find(
        (it) => it.id?.toString() === supplierTypeId?.toString()
    );

    if (!foundTipoProveedor) {
        return {
            id: supplierTypeId ?? 0,
            code: "",
            description: "",
        };
    }

    return {
        id: foundTipoProveedor.id ?? 0,
        code: foundTipoProveedor.code ?? "",
        description: foundTipoProveedor.description ?? "",
    };
}

function mapSupplierToDto(
    entity: SharedSupplier,
    entitySupplierType: SharedSupplierType[],
): Supplier {
    return {
        supplierNumber: Number(entity.supplierNumber) || 0,
        rfc: entity.rfc ?? "",
        businessName: entity.businessName ?? "",
        supplierType: buildSupplierType(entity.supplierTypeId, entitySupplierType),
        emailFinancial: entity.emailFinancial ?? "",
        emailPrincipal: entity.emailPrincipal ?? "",
        emailCommercial: entity.emailCommercial ?? "",
    };
}

export async function getShippingGuideCatalogContext() {
    const headerRepo = getDataSource().getRepository(SharedCatalogHeader);
    const detailRepo = getDataSource().getRepository(SharedCatalogDetail);
    const dictLangRepo = getDataSource().getRepository(SharedCatalogDictionaryLang);

    const headers = await headerRepo.find({
        where: {
            code: In([
                CATALOG_CODES.statusCartaPorteFBC,
                CATALOG_CODES.tipoEntregaGuia,
                CATALOG_CODES.tipoProveedor,
                CATALOG_CODES.origenCartaPorte,
            ]),
        },
    });

    const headerMap = new Map<string, SharedCatalogHeader>();
    headers.forEach((header) => headerMap.set(header.code, header));

    const statusHeader = headerMap.get(CATALOG_CODES.statusCartaPorteFBC);
    const tipoEntregaHeader = headerMap.get(CATALOG_CODES.tipoEntregaGuia);
    const tipoProveedorHeader = headerMap.get(CATALOG_CODES.tipoProveedor);
    const origenHeader = headerMap.get(CATALOG_CODES.origenCartaPorte);

    if (!statusHeader || !tipoEntregaHeader || !tipoProveedorHeader || !origenHeader) {
        throw new Error("Missing shared catalog headers required for shipping-guide");
    }

    const details = await detailRepo.find({
        where: {
            headerId: In([
                statusHeader.id,
                tipoEntregaHeader.id,
                tipoProveedorHeader.id,
                origenHeader.id,
            ]),
            status: 1,
        },
        order: {
            headerId: "ASC",
            id: "ASC",
        },
    });
    const dictIds = details.map(item => item.dictId);
    const dictLangs = await dictLangRepo.find({
        where: {
            dictId: In(dictIds),
            langId: 1,
        },
        order: {
            dictId: "ASC",
            id: "ASC",
        },
    });

    const detailsByHeaderId = new Map<number, GenericCatalogDetails[]>();

    details.forEach((detail) => {
        const current = detailsByHeaderId.get(detail.headerId) ?? [];
        current.push(mapCatalogDetailToDto(detail, dictLangs));
        detailsByHeaderId.set(detail.headerId, current);
    });

    return {
        statusList: detailsByHeaderId.get(statusHeader.id) ?? [],
        tipoEntregaGuiaList: detailsByHeaderId.get(tipoEntregaHeader.id) ?? [],
        tipoProveedorList: detailsByHeaderId.get(tipoProveedorHeader.id) ?? [],
        catOrigenCartaPorteList: detailsByHeaderId.get(origenHeader.id) ?? [],
    };
}

export async function getAllSuppliers(
    tipoProveedorList: GenericCatalogDetails[],
): Promise<Supplier[]> {
    const supplierRepo = getDataSource().getRepository(SharedSupplier);
    const supplierTypeRepo = getDataSource().getRepository(SharedSupplierType);

    const suppliers = await supplierRepo.find({
        order: { id: "ASC" },
    });

    const suppliersTypes = await supplierTypeRepo.find({
        order: { id: "ASC" },
    });

    return suppliers.map((supplier) => mapSupplierToDto(supplier, suppliersTypes));
}