import type { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import type { CatalogHeaderDto } from '@/dto/catalog.dto.js';

export function toDto(entity: CatalogHeader | null | undefined): CatalogHeaderDto | null {
    if (!entity) return null;
    return {
        code: entity.code,
        prefix: entity.prefix,
        name: entity.name,
        description: entity.description ?? null,
        module: entity.module ?? null,
        catalogType: entity.catalogType ?? null
    };
}

export function toDtoList(entities: CatalogHeader[] | null | undefined): CatalogHeaderDto[] {
    if (!entities) return [];
    return entities
        .map(e => toDto(e))
        .filter((d): d is CatalogHeaderDto => d !== null);
}

