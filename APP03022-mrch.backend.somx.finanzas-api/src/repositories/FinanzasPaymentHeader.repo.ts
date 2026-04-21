import { datasource } from "@/config/typeorm-datasource.js";
import { FinanzasPaymentHeader } from "@/entities/FinanzasPaymentHeader.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => datasource.getRepository(FinanzasPaymentHeader);

export async function findById(paymentHeaderUuid: string) {
    return repo().findOneBy({ paymentHeaderUuid });
}


export async function findAll(filter: FindOptionsWhere<FinanzasPaymentHeader>) {
    return repo().find({ where: filter, order: { paymentDate: "DESC", createdAt: "DESC" } });
}

export async function createOne(data: Partial<FinanzasPaymentHeader>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(paymentHeaderUuid: string, patch: Partial<FinanzasPaymentHeader>) {
    await repo().update({ paymentHeaderUuid }, patch);
    return findById(paymentHeaderUuid);
}