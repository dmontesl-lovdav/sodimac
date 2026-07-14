import { getDataSource } from '@/config/typeorm-datasource.js';
import { AccountStatement } from '@/entities/AccountStatement.entity.js';
import { Supplier } from '@/entities/tenant_catalogs.cat_supplier.entity.js';
import { SharedSupplier } from '@/entities/SharedSupplier.entity.js';
import { SharedSupplierType } from '@/entities/SharedSupplierType.entity.js';

export function repo() {
    return getDataSource().getRepository(AccountStatement);
}

export interface FindByFiltersOptions {
    vendorNumber?: number | undefined;
    supplierType?: number | undefined;
    year: number;
    month: number | 'all';
    allowedVendors?: string[] | null;
    limit: number;
    offset: number;
}

export interface AccountStatementListRow {
    statement: AccountStatement;
    vendorName: string;
    supplierTypeId: number | null;
    supplierTypeCode: string | null;
    supplierTypeDescription: string | null;
}

export async function findByFilters(options: FindByFiltersOptions): Promise<{ rows: AccountStatementListRow[]; total: number }> {
    const qb = repo()
        .createQueryBuilder('a')
        .leftJoin(
            SharedSupplier,
            's',
            's.supplier_number::text = CAST(a.vendor_number AS TEXT)'
        )
        .leftJoin(SharedSupplierType, 'st', 'st.id = s.supplier_type_id')
        .addSelect(['s.businessName', 's.supplierTypeId', 'st.code', 'st.description'])
        .where('a.year = :year', { year: options.year })
        .andWhere('a.status > 0');

    if (options.allowedVendors && options.allowedVendors.length > 0) {
        qb.andWhere('CAST(a.vendor_number AS TEXT) IN (:...allowedVendors)', { allowedVendors: options.allowedVendors });
    }
    if (options.vendorNumber) {
        qb.andWhere('a.vendor_number = :vendorNumber', { vendorNumber: options.vendorNumber });
    }
    if (options.supplierType !== undefined) {
        qb.andWhere('s.supplier_type_id = :supplierType', { supplierType: options.supplierType });
    }
    if (options.month && options.month !== 'all') {
        qb.andWhere('a.month = :month', { month: options.month });
    }

    const total = await qb.getCount();
    const { entities, raw } = await qb
        .orderBy('a.year', 'DESC')
        .addOrderBy('a.month', 'DESC')
        .addOrderBy('a.version', 'DESC')
        .skip(options.offset)
        .take(options.limit)
        .getRawAndEntities();

    const rows: AccountStatementListRow[] = entities.map((statement, index) => {
        const row = raw[index] as Record<string, unknown>;
        return {
            statement,
            vendorName: String(row['s_business_name'] ?? ''),
            supplierTypeId: row['s_supplier_type_id'] != null ? Number(row['s_supplier_type_id']) : null,
            supplierTypeCode: row['st_code'] != null ? String(row['st_code']) : null,
            supplierTypeDescription: row['st_description'] != null ? String(row['st_description']) : null,
        };
    });

    return { rows, total };
}

export async function findById(uuid: string, allowedVendors: string[] | null = null): Promise<AccountStatement | null> {
    if (!allowedVendors || allowedVendors.length === 0) {
        return repo().findOneBy({ accountStatementUuid: uuid });
    }
    return repo()
        .createQueryBuilder('a')
        .where('a.account_statement_uuid = :uuid', { uuid })
        .andWhere('CAST(a.vendor_number AS TEXT) IN (:...allowedVendors)', { allowedVendors })
        .getOne();
}

export async function updateReviewStatus(
    uuid: string,
    status: number,
    reviewedAt: Date
): Promise<AccountStatement | null> {
    await repo().update(
        { accountStatementUuid: uuid },
        { status, reviewedAt, updatedAt: new Date() }
    );
    return findById(uuid);
}

export async function findExistingActive(
    vendorNumber: number,
    year: number,
    month: number
): Promise<AccountStatement | null> {
    return repo()
        .createQueryBuilder('a')
        .where('a.vendor_number = :vendorNumber', { vendorNumber })
        .andWhere('a.year = :year', { year })
        .andWhere('a.month = :month', { month })
        .andWhere('a.status > 0')
        .getOne();
}

export async function createStatement(
    data: Partial<AccountStatement>
): Promise<AccountStatement> {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function softDelete(uuid: string): Promise<void> {
    await repo().update(
        { accountStatementUuid: uuid },
        { status: 0, updatedAt: new Date() }
    );
}

export async function findAllVendorNumbers(): Promise<number[]> {
    const suppliers = await getDataSource()
        .getRepository(Supplier)
        .find({ select: { supplierNumber: true } });
    return suppliers
        .map(s => Number(s.supplierNumber))
        .filter(n => Number.isFinite(n) && n > 0);
}
