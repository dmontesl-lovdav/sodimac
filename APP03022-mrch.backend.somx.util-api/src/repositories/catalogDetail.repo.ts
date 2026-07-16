import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import { DictionaryLang } from '@/entities/DictionaryLang.entity.js';
import type { SelectQueryBuilder } from 'typeorm';

export const repo = () => datasource.getRepository(CatalogDetail);

export async function findById(id: number): Promise<CatalogDetail | null> {
    return repo().findOne({
        where: { id },
        relations: ['header']
    });
}

export async function findByHeaderIdOrderBySortOrder(headerId: number): Promise<CatalogDetail[]> {
    return repo().find({
        where: { headerId },
        order: { sortOrder: 'ASC' },
        relations: ['header']
    });
}

export async function findByHeaderIdAndStatusOrderBySortOrder(
    headerId: number,
    status: number
): Promise<CatalogDetail[]> {
    return findByHeaderIdAndStatus(headerId, status);
}

export async function findByHeaderIdAndKey(headerId: number, key: string): Promise<CatalogDetail | null> {
    return repo().findOne({
        where: { headerId, key },
        relations: ['header']
    });
}

export async function findByHeaderCode(code: string): Promise<CatalogDetail[]> {
    return repo()
        .createQueryBuilder('d')
        .leftJoinAndSelect('d.header', 'header')
        .where('header.code = :code', { code })
        .orderBy('d.sortOrder', 'ASC')
        .getMany();
}

export async function findByHeaderCodeAndStatus(code: string, status: number): Promise<CatalogDetail[]> {
    return repo()
        .createQueryBuilder('d')
        .leftJoinAndSelect('d.header', 'header')
        .where('header.code = :code', { code })
        .andWhere('d.status = :status', { status })
        .orderBy('d.sortOrder', 'ASC')
        .getMany();
}

export async function findByHeaderCodeAndStatusAndValidDate(
    code: string,
    status: number,
    currentDate: string
): Promise<CatalogDetail[]> {
    return repo()
        .createQueryBuilder('d')
        .leftJoinAndSelect('d.header', 'header')
        .where('header.code = :code', { code })
        .andWhere('d.status = :status', { status })
        .andWhere('(d.validFrom IS NULL OR d.validFrom <= :currentDate)', { currentDate })
        .andWhere('(d.validTo IS NULL OR d.validTo >= :currentDate)', { currentDate })
        .orderBy('d.sortOrder', 'ASC')
        .getMany();
}

export async function findByKeyAndStatus(key: string, status: number): Promise<CatalogDetail | null> {
    return repo().findOne({
        where: { key, status },
        relations: ['header']
    });
}

export async function findByKeyAndStatusAndValidDate(
    key: string,
    status: number,
    currentDate: string
): Promise<CatalogDetail | null> {
    return repo()
        .createQueryBuilder('d')
        .leftJoinAndSelect('d.header', 'header')
        .where('d.key = :key', { key })
        .andWhere('d.status = :status', { status })
        .andWhere('(d.validFrom IS NULL OR d.validFrom <= :currentDate)', { currentDate })
        .andWhere('(d.validTo IS NULL OR d.validTo >= :currentDate)', { currentDate })
        .getOne();
}

export async function findByHeaderIdAndStatusAndValidDate(
    headerId: number,
    status: number,
    currentDate: string
): Promise<CatalogDetail[]> {
    return repo()
        .createQueryBuilder('d')
        .leftJoinAndSelect('d.header', 'header')
        .where('d.headerId = :headerId', { headerId })
        .andWhere('d.status = :status', { status })
        .andWhere('(d.validFrom IS NULL OR d.validFrom <= :currentDate)', { currentDate })
        .andWhere('(d.validTo IS NULL OR d.validTo >= :currentDate)', { currentDate })
        .orderBy('d.sortOrder', 'ASC')
        .getMany();
}

export async function findByHeaderIdAndStatus(headerId: number, status: number): Promise<CatalogDetail[]> {
    return repo().find({
        where: { headerId, status },
        order: { sortOrder: 'ASC' },
        relations: ['header']
    });
}

export async function existsByHeaderIdAndKeyIgnoreCase(headerId: number, key: string): Promise<boolean> {
    const count = await repo()
        .createQueryBuilder('d')
        .where('d.headerId = :headerId', { headerId })
        .andWhere('LOWER(d.key) = LOWER(:key)', { key })
        .getCount();
    return count > 0;
}

export async function existsByKeyIgnoreCase(key: string): Promise<boolean> {
    const count = await repo()
        .createQueryBuilder('d')
        .where('LOWER(d.key) = LOWER(:key)', { key })
        .getCount();
    return count > 0;
}

export async function findMaxKeyByHeaderId(headerId: number): Promise<string | null> {
    const result = await repo()
        .createQueryBuilder('d')
        .select('MAX(d.key)', 'maxKey')
        .where('d.headerId = :headerId', { headerId })
        .getRawOne<{ maxKey: string | null }>();
    return result?.maxKey ?? null;
}

export async function findMaxKeyNumberByHeaderIdAndPrefix(
    headerId: number,
    prefix: string
): Promise<number> {
    const result = await repo()
        .createQueryBuilder('d')
        .select(
            `COALESCE(MAX(NULLIF(REGEXP_REPLACE(d.key, '^' || :prefix, ''), '')::INTEGER), 0)`,
            'maxNum'
        )
        .where('d.headerId = :headerId', { headerId })
        .andWhere('d.key LIKE :prefixLike', { prefixLike: `${prefix}%` })
        .andWhere(`REGEXP_REPLACE(d.key, '^' || :prefix, '') ~ '^[0-9]+$'`, { prefix })
        .setParameters({ prefix })
        .getRawOne<{ maxNum: number | string | null }>();

    if (!result?.maxNum) return 0;
    const n = typeof result.maxNum === 'string' ? Number.parseInt(result.maxNum, 10) : result.maxNum;
    return Number.isFinite(n) ? n : 0;
}

export async function countByHeaderId(headerId: number): Promise<number> {
    return repo().count({ where: { headerId } });
}

export async function save(entity: CatalogDetail): Promise<CatalogDetail> {
    return repo().save(entity);
}

export interface ElementFilter {
    catalogId?: number | null | undefined;
    elementId?: number | null | undefined;
    element?: string | null | undefined;
    value?: string | null | undefined;
    parentCatalogId?: number | null | undefined;
    parentElementId?: number | null | undefined;
    status?: number | null | undefined;
    key?: string | null | undefined;
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
    createdAt: 'd.createdAt',
    updatedAt: 'd.updatedAt',
    id: 'd.id',
    key: 'd.key',
    sortOrder: 'd.sortOrder',
    status: 'd.status',
    element: 'COALESCE(dict.description, d.key)'
};

function applyElementFilters(
    qb: SelectQueryBuilder<CatalogDetail>,
    filter: ElementFilter
): SelectQueryBuilder<CatalogDetail> {
    if (filter.catalogId != null) {
        qb.andWhere('d.headerId = :headerId', { headerId: filter.catalogId });
    }
    if (filter.elementId != null) {
        qb.andWhere('d.id = :id', { id: filter.elementId });
    }
    if (filter.element && filter.element.trim() !== '') {
        const term = `%${filter.element.trim().toLowerCase()}%`;
        qb.andWhere(
            '(LOWER(dict.description) LIKE :elem OR LOWER(d.key) LIKE :elem)',
            { elem: term }
        );
    }
    if (filter.value && filter.value.trim() !== '') {
        const term = `%${filter.value.trim().toLowerCase()}%`;
        qb.andWhere('LOWER(d.value) LIKE :val', { val: term });
    }
    if (filter.parentCatalogId != null) {
        qb.andWhere('d.parentCatalogId = :parentCatalogId', { parentCatalogId: filter.parentCatalogId });
    }
    if (filter.parentElementId != null) {
        qb.andWhere('d.parentElementId = :parentElementId', { parentElementId: filter.parentElementId });
    }
    if (filter.status != null) {
        qb.andWhere('d.status = :status', { status: filter.status });
    }
    if (filter.key && filter.key.trim() !== '') {
        const term = `%${filter.key.trim().toLowerCase()}%`;
        qb.andWhere('LOWER(d.key) LIKE :key', { key: term });
    }
    return qb;
}

export async function findElementsPaged(
    filter: ElementFilter,
    options: PageOptions
): Promise<PagedResult<CatalogDetail>> {
    const qb = repo()
        .createQueryBuilder('d')
        .leftJoinAndSelect('d.header', 'header')
        .leftJoin(
            DictionaryLang,
            'dict',
            'dict.dictId = d.dictId AND dict.langId = 1'
        );
    applyElementFilters(qb, filter);

    const sortColumn = SORT_FIELD_MAP[options.sortBy] ?? 'd.createdAt';
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

