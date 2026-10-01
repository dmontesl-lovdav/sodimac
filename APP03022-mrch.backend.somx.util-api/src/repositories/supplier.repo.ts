import { datasource } from '@/config/typeorm-datasource.js';
import { Supplier } from '@/entities/Supplier.entity.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import { Not, In, type FindOptionsWhere } from 'typeorm';

export interface SupplierSecurityFilter {
    vendors?: string[] | null;
    typeIds?: number[] | null;
    groupSuppliers?: string[] | null;
}

function applySecurityWhere(where: FindOptionsWhere<Supplier>, security?: SupplierSecurityFilter): FindOptionsWhere<Supplier> {
    const supplierConstraints: string[][] = [];
    if (security?.vendors && security.vendors.length > 0) {
        supplierConstraints.push(security.vendors.map(String));
    }
    if (security?.groupSuppliers != null) {
        supplierConstraints.push(security.groupSuppliers.map(String));
    }
    if (supplierConstraints.length > 0) {
        let allowed = supplierConstraints[0] ?? [];
        for (let i = 1; i < supplierConstraints.length; i += 1) {
            const set = new Set(supplierConstraints[i] ?? []);
            allowed = allowed.filter((n) => set.has(n));
        }
        where.supplierNumber = In(allowed.length > 0 ? allowed : ['\u0000__none__']);
    }
    if (security?.typeIds && security.typeIds.length > 0) {
        where.supplierTypeId = In(security.typeIds);
    }
    return where;
}

const CATGRUPOPROVEEDORES_KEYS = ['CatGrupoProveedores', 'CATGRUPOPROVEEDORBLOQUEA'] as const;

export async function getSupplierNumbersByGroupKeys(groupKeys: string[]): Promise<string[]> {
    if (groupKeys.length === 0) return [];
    const headerRepo = datasource.getRepository(CatalogHeader);
    const detailRepo = datasource.getRepository(CatalogDetail);

    const parent = await headerRepo.findOne({
        where: CATGRUPOPROVEEDORES_KEYS.flatMap((k) => [{ code: k }, { name: k }]),
    });
    if (!parent) return [];

    const groupElements = await detailRepo.find({
        where: { headerId: parent.id, status: 1, key: In(groupKeys) },
        select: ['value'],
    });
    const childNames = groupElements
        .map((e) => (e.value ?? '').trim())
        .filter((v) => v.length > 0);
    if (childNames.length === 0) return [];

    const childHeaders = await headerRepo.find({
        where: childNames.flatMap((n) => [{ code: n }, { name: n }]),
    });
    const childIds = childHeaders.map((h) => h.id);
    if (childIds.length === 0) return [];

    const supplierElements = await detailRepo.find({
        where: { headerId: In(childIds), status: 1 },
        select: ['value'],
    });
    return supplierElements
        .map((e) => (e.value ?? '').trim())
        .filter((v) => v.length > 0);
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

export async function findSupplierNumbersByRfcs(rfcs: string[]): Promise<string[]> {
    const upper = rfcs.map((r) => String(r).trim().toUpperCase()).filter((r) => r.length > 0);
    if (upper.length === 0) return [];

    const rows = await repo()
        .createQueryBuilder('s')
        .select('s.supplierNumber', 'supplierNumber')
        .where('s.status = :status', { status: Supplier.STATUS_ACTIVE })
        .andWhere('UPPER(s.rfc) IN (:...rfcs)', { rfcs: upper })
        .getRawMany<{ supplierNumber: string | number }>();

    return rows
        .map((r) => String(r.supplierNumber ?? '').trim())
        .filter((n) => n.length > 0);
}

export async function save(entity: Supplier): Promise<Supplier> {
    return repo().save(entity);
}

