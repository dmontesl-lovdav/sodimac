import { getDataSource } from '@/config/typeorm-datasource.js';
import { AccountStatement } from '@/entities/AccountStatement.entity.js';

export function repo() {
    return getDataSource().getRepository(AccountStatement);
}

export interface FindByFiltersOptions {
    vendorNumber?: number | undefined;
    year: number;
    month: number | 'all';
    allowedVendors?: string[] | null;
    limit: number;
    offset: number;
}

export async function findByFilters(options: FindByFiltersOptions): Promise<{ rows: AccountStatement[]; total: number }> {
    const qb = repo()
        .createQueryBuilder('a')
        .where('a.year = :year', { year: options.year });

    if (options.allowedVendors && options.allowedVendors.length > 0) {
        qb.andWhere('CAST(a.vendor_number AS TEXT) IN (:...allowedVendors)', { allowedVendors: options.allowedVendors });
    }
    if (options.vendorNumber) {
        qb.andWhere('a.vendor_number = :vendorNumber', { vendorNumber: options.vendorNumber });
    }
    if (options.month && options.month !== 'all') {
        qb.andWhere('a.month = :month', { month: options.month });
    }

    const total = await qb.getCount();
    const rows = await qb
        .orderBy('a.year', 'DESC')
        .addOrderBy('a.month', 'DESC')
        .addOrderBy('a.version', 'DESC')
        .skip(options.offset)
        .take(options.limit)
        .getMany();

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
