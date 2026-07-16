import type { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import type { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import type { CatalogElementDto, CatalogSimpleDto } from '@/dto/catalog.dto.js';
import * as headerRepo from '@/repositories/catalogHeader.repo.js';
import * as detailRepo from '@/repositories/catalogDetail.repo.js';
import * as dictRepo from '@/repositories/dictionaryLang.repo.js';

const DEFAULT_LANG_ID = 1;

async function resolveElementName(entity: CatalogDetail): Promise<string> {
    if (entity.dictId == null || entity.dictId <= 0) return entity.key;
    const dict = await dictRepo.findByDictIdAndLangId(entity.dictId, DEFAULT_LANG_ID);
    return dict?.description ?? entity.key;
}

function buildBaseDto(entity: CatalogDetail, elementName: string): CatalogElementDto {
    return {
        id: entity.id,
        catalogId: entity.header?.id ?? entity.headerId ?? null,
        catalogCode: entity.header?.code ?? null,
        element: elementName,
        value: entity.value ?? null,
        key: entity.key,
        validFrom: entity.validFrom ?? null,
        validTo: entity.validTo ?? null,
        status: entity.status ?? null,
        statusDescription: entity.status === 1 ? 'Activo' : 'Inactivo',
        parentCatalogId: entity.parentCatalogId ?? null,
        parentElementId: entity.parentElementId ?? null,
        createdBy: entity.createdBy ?? null,
        createdAt: entity.createdAt ?? null,
        updatedBy: entity.updatedBy ?? null,
        updatedAt: entity.updatedAt ?? null,
        externalKey: entity.externalKey ?? null,
        sortOrder: entity.sortOrder ?? null,
        attributes: entity.attributes ?? null,
    };
}

async function resolveParentCatalogName(parentCatalogId: number | null | undefined): Promise<string | undefined> {
    if (parentCatalogId == null) return undefined;
    const parent = await headerRepo.findById(parentCatalogId);
    return parent?.name;
}

async function resolveParentElementName(parentElementId: number | null | undefined): Promise<string | undefined> {
    if (parentElementId == null) return undefined;
    const parent = await detailRepo.findById(parentElementId);
    if (!parent) return undefined;
    if (parent.dictId == null || parent.dictId <= 0) return parent.key;
    const pdict = await dictRepo.findByDictIdAndLangId(parent.dictId, DEFAULT_LANG_ID);
    return pdict?.description ?? parent.key;
}

export async function toDto(entity: CatalogDetail | null | undefined): Promise<CatalogElementDto | null> {
    if (!entity) return null;

    const elementName = await resolveElementName(entity);
    const dto = buildBaseDto(entity, elementName);

    const parentCatalogName = await resolveParentCatalogName(entity.parentCatalogId);
    if (parentCatalogName !== undefined) {
        dto.parentCatalogName = parentCatalogName;
    }

    const parentElementName = await resolveParentElementName(entity.parentElementId);
    if (parentElementName !== undefined) {
        dto.parentElementName = parentElementName;
    }

    return dto;
}

export async function toDtoList(entities: CatalogDetail[]): Promise<CatalogElementDto[]> {
    const results = await Promise.all(entities.map(e => toDto(e)));
    return results.filter((d): d is CatalogElementDto => d !== null);
}

export function toSimpleDto(entity: CatalogHeader | null | undefined): CatalogSimpleDto | null {
    if (!entity) return null;
    return {
        id: entity.id,
        code: entity.code,
        name: entity.name,
        description: entity.description ?? null,
        catalogType: entity.catalogType ?? null,
        status: entity.status
    };
}

export function toSimpleDtoList(entities: CatalogHeader[]): CatalogSimpleDto[] {
    return entities
        .map(e => toSimpleDto(e))
        .filter((d): d is CatalogSimpleDto => d !== null);
}

