import { getDataSource } from "@/config/typeorm-datasource.js";
import { FiscalPayment } from "@/entities/FiscalPayment.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => getDataSource().getRepository(FiscalPayment);

export async function findAll(filter: FindOptionsWhere<FiscalPayment>, limit = 100) {
    return repo().find({ where: filter, take: limit, order: { paymentDate: "DESC" } });
}

export async function findById(id: string) {
    return repo().findOneBy({ fiscalPaymentUuid: id });
}

export async function findByPaymentNumber(paymentNumber: string) {
    return repo().findOneBy({ paymentNumber });
}

export async function createOne(data: Partial<FiscalPayment>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(id: string, patch: Partial<FiscalPayment>) {
    await repo().update({ fiscalPaymentUuid: id }, patch);
    return findById(id);
}

export async function deleteOne(id: string) {
    await repo().delete({ fiscalPaymentUuid: id });
}
