import { getDataSource } from '@/config/typeorm-datasource.js';
import { AccountStatement } from '@/entities/AccountStatement.entity.js';

export function repo() {
    return getDataSource().getRepository(AccountStatement);
}

export interface FindByFiltersOptions {
    vendorNumber?: number | undefined;
    year: number;
    month: number | 'all' ;
    limit: number;
    offset: number;
}

export async function findByFilters(options: FindByFiltersOptions): Promise<{ rows: AccountStatement[]; total: number }> {
    const qb = repo()
        .createQueryBuilder('a')
        .where('a.year = :year', { year: options.year });

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

export async function findById(uuid: string): Promise<AccountStatement | null> {
    return repo().findOneBy({ accountStatementUuid: uuid });
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
