import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogConversion } from '@/entities/CatalogConversion.entity.js';
import { In } from 'typeorm';
import type { SelectQueryBuilder } from 'typeorm';

export const repo = () => datasource.getRepository(CatalogConversion);

export async function findById(id: number): Promise<CatalogConversion | null> {
    return repo().findOne({
        where: { id },
        relations: {
            sourceElement: { header: true },
            targetElement: { header: true }
        }
    });
}

export async function findBySourceElementId(sourceElementId: number): Promise<CatalogConversion[]> {
    return repo().find({
        where: { sourceElementId },
        relations: {
            sourceElement: { header: true },
            targetElement: { header: true }
        }
    });
}

export async function findBySourceElementIdAndIsPrincipalTrue(
    sourceElementId: number
): Promise<CatalogConversion | null> {
    return repo().findOne({
        where: { sourceElementId, isPrincipal: true },
        relations: {
            sourceElement: { header: true },
            targetElement: { header: true }
        }
    });
}

export async function clearPrincipalBySourceElement(
    sourceElementId: number,
    userId: string
): Promise<void> {
    await repo()
        .createQueryBuilder()
        .update(CatalogConversion)
        .set({ isPrincipal: false, status: 0, updatedBy: userId })
        .where('source_element_id = :sourceElementId', { sourceElementId })
        .andWhere('is_principal = :isPrincipal', { isPrincipal: true })
        .execute();
}

export async function existsBySourceElementIdAndTargetElementId(
    sourceElementId: number,
    targetElementId: number
): Promise<boolean> {
    const count = await repo().count({ where: { sourceElementId, targetElementId } });
    return count > 0;
}

export async function save(entity: CatalogConversion): Promise<CatalogConversion> {
    return repo().save(entity);
}

export async function existsById(id: number): Promise<boolean> {
    const count = await repo().count({ where: { id } });
    return count > 0;
}

export async function deleteById(id: number): Promise<void> {
    await repo().delete({ id });
}

export async function deleteAllByIdInBatch(ids: number[]): Promise<void> {
    if (!ids || ids.length === 0) return;
    await repo().delete({ id: In(ids) });
}

export interface ConversionFilter {
    sourceElementId?: number | null | undefined;
    targetElementId?: number | null | undefined;
    elemento?: string | null | undefined;
    valor?: string | null | undefined;
    catalogoOrigen?: string | null | undefined;
    estatus?: number | null | undefined;
}

export interface PageOptions {
    page: number;
    pageSize: number;
    sortBy: string;
    sortDir: 'ASC' | 'DESC';
}

export interface PagedResult<T> {
    items: T[];
    total: number;
    page: number;
    pageSize: number;
    totalPages: number;
}

const SORT_FIELD_MAP: Record<string, string> = {
    createdAt: 'c.createdAt',
    updatedAt: 'c.updatedAt',
    id: 'c.id',
    status: 'c.status',
    validFrom: 'c.validFrom',
    validTo: 'c.validTo'
};

function applyFilters(
    qb: SelectQueryBuilder<CatalogConversion>,
    f: ConversionFilter
): SelectQueryBuilder<CatalogConversion> {
    if (f.sourceElementId != null) {
        qb.andWhere('c.sourceElementId = :srcId', { srcId: f.sourceElementId });
    }
    if (f.targetElementId != null) {
        qb.andWhere('c.targetElementId = :tgtId', { tgtId: f.targetElementId });
    }
    if (f.elemento && f.elemento.trim() !== '') {
        qb.andWhere('LOWER(tgt.key) LIKE LOWER(:elem)', { elem: `%${f.elemento}%` });
    }
    if (f.valor && f.valor.trim() !== '') {
        qb.andWhere('LOWER(tgt.value) LIKE LOWER(:val)', { val: `%${f.valor}%` });
    }
    if (f.catalogoOrigen && f.catalogoOrigen.trim() !== '') {
        qb.andWhere('LOWER(th.name) LIKE LOWER(:cat)', { cat: `%${f.catalogoOrigen}%` });
    }
    if (f.estatus != null) {
        qb.andWhere('c.status = :status', { status: f.estatus });
    }
    return qb;
}

export async function searchPaged(
    filter: ConversionFilter,
    options: PageOptions
): Promise<PagedResult<CatalogConversion>> {
    const qb = repo()
        .createQueryBuilder('c')
        .leftJoinAndSelect('c.sourceElement', 'src')
        .leftJoinAndSelect('src.header', 'sh')
        .leftJoinAndSelect('c.targetElement', 'tgt')
        .leftJoinAndSelect('tgt.header', 'th');

    applyFilters(qb, filter);

    const sortColumn = SORT_FIELD_MAP[options.sortBy] ?? 'c.createdAt';
    qb.orderBy(sortColumn, options.sortDir);

    const [items, total] = await qb
        .skip(options.page * options.pageSize)
        .take(options.pageSize)
        .getManyAndCount();

    return {
        items,
        total,
        page: options.page,
        pageSize: options.pageSize,
        totalPages: Math.ceil(total / options.pageSize)
    };
}

