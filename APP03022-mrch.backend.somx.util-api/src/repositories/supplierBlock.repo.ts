import { datasource } from '@/config/typeorm-datasource.js';
import { SupplierBlock } from '@/entities/SupplierBlock.entity.js';

export const repo = () => datasource.getRepository(SupplierBlock);

export async function findById(id: number): Promise<SupplierBlock | null> {
    return repo().findOne({ where: { id } });
}

export async function findBySupplierNumber(supplierNumber: string): Promise<SupplierBlock[]> {
    return repo().find({ where: { supplierNumber } });
}

export async function findBySupplierNumberAndStatus(
    supplierNumber: string,
    status: number
): Promise<SupplierBlock[]> {
    return repo().find({ where: { supplierNumber, status } });
}

export async function findByStatus(status: number): Promise<SupplierBlock[]> {
    return repo().find({ where: { status } });
}

export async function findActiveBlocksAtDate(
    supplierNumber: string,
    date: string
): Promise<SupplierBlock[]> {
    return repo()
        .createQueryBuilder('sb')
        .where('sb.supplierNumber = :supplierNumber', { supplierNumber })
        .andWhere('sb.status = 1')
        .andWhere(':date >= sb.validFrom', { date })
        .andWhere(':date <= sb.validTo', { date })
        .getMany();
}

export async function findCurrentActiveBlocks(supplierNumber: string): Promise<SupplierBlock[]> {
    return repo()
        .createQueryBuilder('sb')
        .where('sb.supplierNumber = :supplierNumber', { supplierNumber })
        .andWhere('sb.status = 1')
        .andWhere('CURRENT_DATE >= sb.validFrom')
        .andWhere('CURRENT_DATE <= sb.validTo')
        .getMany();
}

export async function isSupplierCurrentlyBlocked(supplierNumber: string): Promise<boolean> {
    const count = await repo()
        .createQueryBuilder('sb')
        .where('sb.supplierNumber = :supplierNumber', { supplierNumber })
        .andWhere('sb.status = 1')
        .andWhere('CURRENT_DATE >= sb.validFrom')
        .andWhere('CURRENT_DATE <= sb.validTo')
        .getCount();
    return count > 0;
}

export async function findOverlappingBlocks(
    supplierNumber: string,
    startDate: string,
    endDate: string
): Promise<SupplierBlock[]> {
    return repo()
        .createQueryBuilder('sb')
        .where('sb.supplierNumber = :supplierNumber', { supplierNumber })
        .andWhere('sb.status = 1')
        .andWhere('sb.validFrom <= :endDate', { endDate })
        .andWhere('sb.validTo >= :startDate', { startDate })
        .getMany();
}

export async function save(entity: SupplierBlock): Promise<SupplierBlock> {
    return repo().save(entity);
}

