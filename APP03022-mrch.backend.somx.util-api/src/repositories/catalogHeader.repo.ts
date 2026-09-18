import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';

export const repo = () => datasource.getRepository(CatalogHeader);

export async function findParentCatalogsForCatalog(catalogId: number): Promise<CatalogHeader[]> {
    return repo()
        .createQueryBuilder('ph')
        .innerJoin(CatalogDetail, 'e', 'e.parent_catalog_id = ph.id')
        .where('e.header_id = :catalogId', { catalogId })
        .andWhere('e.parent_catalog_id IS NOT NULL')
        .andWhere('ph.status = 1')
        .distinct(true)
        .orderBy('ph.name', 'ASC')
        .getMany();
}

export async function findById(id: number): Promise<CatalogHeader | null> {
    return repo().findOne({ where: { id } });
}

export async function findByCode(code: string): Promise<CatalogHeader | null> {
    return repo().findOne({ where: { code } });
}

export async function findByName(name: string): Promise<CatalogHeader[]> {
    return repo()
        .createQueryBuilder('h')
        .where('LOWER(h.name) = LOWER(:name)', { name })
        .getMany();
}

export async function findByStatus(status: number): Promise<CatalogHeader[]> {
    return repo().find({ where: { status } });
}

export async function findByModule(module: string): Promise<CatalogHeader[]> {
    return repo().find({ where: { module } });
}

export async function findByModuleAndStatus(module: string, status: number): Promise<CatalogHeader[]> {
    return repo().find({ where: { module, status } });
}

export async function findByPrefix(prefix: string): Promise<CatalogHeader | null> {
    return repo().findOne({ where: { prefix } });
}

export async function findByPrefixAndStatus(prefix: string, status: number): Promise<CatalogHeader | null> {
    return repo().findOne({ where: { prefix, status } });
}

export async function findByCatalogType(catalogType: string): Promise<CatalogHeader[]> {
    return repo().find({ where: { catalogType } });
}

export async function findByCatalogTypeAndStatus(catalogType: string, status: number): Promise<CatalogHeader[]> {
    return repo().find({ where: { catalogType, status } });
}

export async function save(entity: CatalogHeader): Promise<CatalogHeader> {
    return repo().save(entity);
}

export async function existsById(id: number): Promise<boolean> {
    const count = await repo().count({ where: { id } });
    return count > 0;
}

