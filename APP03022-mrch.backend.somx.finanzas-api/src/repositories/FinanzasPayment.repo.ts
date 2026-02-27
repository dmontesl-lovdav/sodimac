import { datasource } from "@/config/typeorm-datasource.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => datasource.getRepository(FinanzasPayment);

export async function findAllPaginated(filter: FindOptionsWhere<FinanzasPayment>, pageSize: number, pageNumber: number) {
    const skip = (pageNumber - 1) * pageSize; // Calculate the offset
    var [result, total] = await repo().findAndCount({ where: filter, take: pageSize, skip: skip, order: { paymentDate: "DESC" } });
    return [result, total, result.length];
}

export async function countFinanzasPayment(filter: FindOptionsWhere<FinanzasPayment>) {
    const totalItems = await repo().count({ where: filter });
    return totalItems;
}

export async function findById(finanzasPaymentUuid: string) {
    return repo().findOneBy({ finanzasPaymentUuid });
}

export async function findAll(filter: FindOptionsWhere<FinanzasPayment>,) {
    return repo().find({ where: filter });
}

export async function createOne(data: Partial<FinanzasPayment>) {
    const entity = repo().create(data);
    //const createPayment = repo().save(entity);
    //return Object.assign(Payment, createPayment);
    return repo().save(entity);
}

export async function updateOne(finanzasPaymentUuid: string, patch: Partial<FinanzasPayment>) {
    await repo().update({ finanzasPaymentUuid }, patch);
    return findById(finanzasPaymentUuid);
}

export async function updateStatus(filter: FindOptionsWhere<FinanzasPayment>, _status: number) {
    return repo().update(filter, { status: _status });
}

export async function update(finanzasPaymentUuid: string, patch: Partial<FinanzasPayment>) {
    await repo().update({ finanzasPaymentUuid }, patch);
    return findById(finanzasPaymentUuid);
}

export async function updateByFilter(filter: FindOptionsWhere<FinanzasPayment>, status: number) {

    // const whereCondition: FindOptionsWhere<FinanzasPayment> = {
    //     idPayment: 30,
    // };
    return repo().update(filter, { status: status });
}