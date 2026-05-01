import { datasource } from '@/config/typeorm-datasource.js';
import { PaymentCondition } from '@/entities/PaymentCondition.entity.js';

export const repo = () => datasource.getRepository(PaymentCondition);

export async function findById(id: number): Promise<PaymentCondition | null> {
    return repo().findOne({ where: { id } });
}

export async function findByStatus(status: number): Promise<PaymentCondition[]> {
    return repo().find({ where: { status } });
}

export async function findBySupplierNumber(supplierNumber: string): Promise<PaymentCondition[]> {
    return repo().find({ where: { supplierNumber } });
}

export async function findBySupplierNumberAndStatus(
    supplierNumber: string,
    status: number
): Promise<PaymentCondition[]> {
    return repo().find({ where: { supplierNumber, status } });
}

export async function save(entity: PaymentCondition): Promise<PaymentCondition> {
    return repo().save(entity);
}

