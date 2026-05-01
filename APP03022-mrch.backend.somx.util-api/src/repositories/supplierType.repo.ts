import { datasource } from '@/config/typeorm-datasource.js';
import { SupplierType } from '@/entities/SupplierType.entity.js';

export const repo = () => datasource.getRepository(SupplierType);

export async function findById(id: number): Promise<SupplierType | null> {
    return repo().findOne({ where: { id } });
}

export async function findByCode(code: string): Promise<SupplierType | null> {
    return repo().findOne({ where: { code } });
}

export async function findByStatus(status: number): Promise<SupplierType[]> {
    return repo().find({ where: { status } });
}

export async function save(entity: SupplierType): Promise<SupplierType> {
    return repo().save(entity);
}

