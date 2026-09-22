import { datasource } from '@/config/typeorm-datasource.js';
import { Supplier } from '@/entities/Supplier.entity.js';
import { Not, In, type FindOptionsWhere } from 'typeorm';

export interface SupplierSecurityFilter {
    vendors?: string[] | null;
    typeIds?: number[] | null;
}

function applySecurityWhere(where: FindOptionsWhere<Supplier>, security?: SupplierSecurityFilter): FindOptionsWhere<Supplier> {
    if (security?.vendors && security.vendors.length > 0) {
        where.supplierNumber = In(security.vendors);
    }
    if (security?.typeIds && security.typeIds.length > 0) {
        where.supplierTypeId = In(security.typeIds);
    }
    return where;
}

export const repo = () => datasource.getRepository(Supplier);

export async function findById(id: number): Promise<Supplier | null> {
    return repo().findOne({
        where: { id },
        relations: ['supplierType', 'paymentCondition']
    });
}

export async function findBySupplierNumber(supplierNumber: string): Promise<Supplier | null> {
    return repo().findOne({
        where: { supplierNumber },
        relations: ['supplierType', 'paymentCondition']
    });
}

export async function findByRfc(rfc: string): Promise<Supplier | null> {
    return repo().findOne({
        where: { rfc },
        relations: ['supplierType', 'paymentCondition']
    });
}

export async function findByStatus(status: number, security?: SupplierSecurityFilter): Promise<Supplier[]> {
    return repo().find({
        where: applySecurityWhere({ status }, security),
        relations: ['supplierType', 'paymentCondition']
    });
}

export async function findAllVisible(security?: SupplierSecurityFilter): Promise<Supplier[]> {
    return repo().find({
        where: applySecurityWhere({ status: Not(Supplier.STATUS_DELETED) }, security),
        relations: ['supplierType', 'paymentCondition']
    });
}

export async function findExistingStatus(
    supplierNumber: string,
): Promise<'active' | 'inactive' | 'deleted' | null> {
    const existing = await repo().findOne({
        where: { supplierNumber },
        select: ['status'],
    });
    if (!existing) return null;
    if (existing.status === Supplier.STATUS_ACTIVE) return 'active';
    if (existing.status === Supplier.STATUS_INACTIVE) return 'inactive';
    if (existing.status === Supplier.STATUS_DELETED) return 'deleted';
    return null;
}

export async function findBySupplierTypeId(supplierTypeId: number): Promise<Supplier[]> {
    return repo().find({
        where: { supplierTypeId },
        relations: ['supplierType', 'paymentCondition']
    });
}

export async function findBySupplierTypeIdAndStatus(
    supplierTypeId: number,
    status: number
): Promise<Supplier[]> {
    return repo().find({
        where: { supplierTypeId, status },
        relations: ['supplierType', 'paymentCondition']
    });
}

export async function existsBySupplierNumber(supplierNumber: string): Promise<boolean> {
    const count = await repo().count({
        where: { supplierNumber, status: Not(Supplier.STATUS_DELETED) },
    });
    return count > 0;
}

export async function existsByRfc(rfc: string): Promise<boolean> {
    const count = await repo().count({ where: { rfc } });
    return count > 0;
}

export async function findByTypeFilter(tipoProveedor: number): Promise<Supplier[]> {
    const qb = repo()
        .createQueryBuilder('s')
        .leftJoinAndSelect('s.supplierType', 'supplierType')
        .leftJoinAndSelect('s.paymentCondition', 'paymentCondition')
        .where('s.status = :status', { status: Supplier.STATUS_ACTIVE });

    if (tipoProveedor !== 0) {
        qb.andWhere('s.supplierTypeId = :tipo', { tipo: tipoProveedor });
    }

    return qb.getMany();
}

export async function save(entity: Supplier): Promise<Supplier> {
    return repo().save(entity);
}

