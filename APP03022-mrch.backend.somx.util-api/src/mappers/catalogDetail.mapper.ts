import type { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import type { CatalogDetailDto } from '@/dto/catalog.dto.js';

export function toDto(
    entity: CatalogDetail | null | undefined,
    dictionary: Map<number, string>
): CatalogDetailDto | null {
    if (!entity) return null;
    const description = dictionary.get(entity.dictId) ?? '';
    return {
        key: entity.key,
        internalStatus: entity.internalStatus ?? null,
        externalKey: entity.externalKey ?? null,
        value: entity.value ?? null,
        description,
        color: entity.color ?? null,
        sortOrder: entity.sortOrder ?? null,
        validFrom: entity.validFrom ?? null,
        validTo: entity.validTo ?? null,
        attributes: entity.attributes ?? null
    };
}

export function toDtoList(
    entities: CatalogDetail[] | null | undefined,
    dictionary: Map<number, string>
): CatalogDetailDto[] {
    if (!entities) return [];
    return entities
        .map(e => toDto(e, dictionary))
        .filter((d): d is CatalogDetailDto => d !== null);
}

