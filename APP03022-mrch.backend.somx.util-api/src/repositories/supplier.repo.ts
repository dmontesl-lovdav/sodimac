import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import { Supplier } from '@/entities/Supplier.entity.js';

export interface SupplierFilters {
    supplierNumber?: string;
    businessName?: string;
    status?: number;
    page?: number;
    pageSize?: number;
}

export interface SupplierPage {
    items: Supplier[];
    total: number;
    page: number;
    pageSize: number;
}

/**
 * Resuelve ATR002 type keys (ej. 'TPR001','TPR002') a supplier_type_id
 * via catalog_detail.external_key.
 */
async function resolveTypeIds(typeKeys: string[]): Promise<number[]> {
    const rows = await datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('cd')
        .select('cd.externalKey', 'externalKey')
        .where('cd.detailKey IN (:...keys)', { keys: typeKeys })
        .andWhere('cd.externalKey IS NOT NULL')
        .getRawMany<{ externalKey: string }>();

    return rows
        .map(r => parseInt(r.externalKey, 10))
        .filter(n => !isNaN(n));
}

export async function findSuppliers(
    filters: SupplierFilters,
    allowedTypes: string[] | null,
): Promise<SupplierPage> {
    const page = Math.max(1, filters.page ?? 1);
    const pageSize = Math.min(100, Math.max(1, filters.pageSize ?? 20));

    const qb = datasource
        .getRepository(Supplier)
        .createQueryBuilder('s');

    // Filtros opcionales del request
    if (filters.supplierNumber) {
        qb.andWhere('s.supplierNumber ILIKE :sn', { sn: `%${filters.supplierNumber}%` });
    }
    if (filters.businessName) {
        qb.andWhere('s.businessName ILIKE :bn', { bn: `%${filters.businessName}%` });
    }
    if (filters.status !== undefined) {
        qb.andWhere('s.status = :st', { st: filters.status });
    }

    // Filtro de seguridad ATR002 (TipoProveedor)
    if (allowedTypes !== null && allowedTypes.length > 0) {
        const typeIds = await resolveTypeIds(allowedTypes);
        if (typeIds.length > 0) {
            qb.andWhere('s.supplierTypeId IN (:...typeIds)', { typeIds });
        } else {
            // No se resolvió ningún tipo → retorna vacío (keys inválidas)
            qb.andWhere('1 = 0');
        }
    }

    const total = await qb.getCount();
    const items = await qb
        .orderBy('s.supplierNumber', 'ASC')
        .skip((page - 1) * pageSize)
        .take(pageSize)
        .getMany();

    return { items, total, page, pageSize };
}
