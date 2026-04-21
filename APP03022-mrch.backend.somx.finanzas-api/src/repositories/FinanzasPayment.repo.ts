import { datasource } from "@/config/typeorm-datasource.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => datasource.getRepository(FinanzasPayment);

export async function findAllPaginated(
    filter: FindOptionsWhere<FinanzasPayment>,
    pageSize: number,
    pageNumber: number
): Promise<[FinanzasPayment[], number, number]> {
    const start = Date.now();
    const skip = (pageNumber - 1) * pageSize;

    console.log("[finanzas-payment][repo.findAllPaginated] START", {
        filter,
        pageSize,
        pageNumber,
        skip,
    });

    const [result, total] = await repo().findAndCount({
        where: filter,
        take: pageSize,
        skip,
        order: { paymentDate: "DESC" }
    });

    console.log("[finanzas-payment][repo.findAllPaginated] END", {
        elapsedMs: Date.now() - start,
        rows: result.length,
        total,
    });

    return [result, total, result.length];
}

export async function countFinanzasPayment(filter: FindOptionsWhere<FinanzasPayment>) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.countFinanzasPayment] START", { filter });

    const totalItems = await repo().count({ where: filter });

    console.log("[finanzas-payment][repo.countFinanzasPayment] END", {
        elapsedMs: Date.now() - start,
        totalItems,
    });

    return totalItems;
}

export async function findById(finanzasPaymentUuid: string) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.findById] START", { finanzasPaymentUuid });

    const result = await repo().findOneBy({ finanzasPaymentUuid });

    console.log("[finanzas-payment][repo.findById] END", {
        elapsedMs: Date.now() - start,
        found: !!result,
    });

    return result;
}

export async function findAll(filter: FindOptionsWhere<FinanzasPayment>) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.findAll] START", { filter });

    const result = await repo().find({ where: filter });

    console.log("[finanzas-payment][repo.findAll] END", {
        elapsedMs: Date.now() - start,
        rows: result.length,
    });

    return result;
}

export async function createOne(data: Partial<FinanzasPayment>) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.createOne] START");

    const entity = repo().create(data);
    const saved = await repo().save(entity);

    console.log("[finanzas-payment][repo.createOne] END", {
        elapsedMs: Date.now() - start,
    });

    return saved;
}

export async function updateOne(finanzasPaymentUuid: string, patch: Partial<FinanzasPayment>) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.updateOne] START", { finanzasPaymentUuid });

    await repo().update({ finanzasPaymentUuid }, patch);
    const updated = await findById(finanzasPaymentUuid);

    console.log("[finanzas-payment][repo.updateOne] END", {
        elapsedMs: Date.now() - start,
        found: !!updated,
    });

    return updated;
}

export async function updateStatus(filter: FindOptionsWhere<FinanzasPayment>, _status: number) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.updateStatus] START", { filter, status: _status });

    const result = await repo().update(filter, { status: _status });

    console.log("[finanzas-payment][repo.updateStatus] END", {
        elapsedMs: Date.now() - start,
        affected: result.affected,
    });

    return result;
}

export async function update(finanzasPaymentUuid: string, patch: Partial<FinanzasPayment>) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.update] START", { finanzasPaymentUuid });

    await repo().update({ finanzasPaymentUuid }, patch);
    const updated = await findById(finanzasPaymentUuid);

    console.log("[finanzas-payment][repo.update] END", {
        elapsedMs: Date.now() - start,
        found: !!updated,
    });

    return updated;
}

export async function updateByFilter(filter: FindOptionsWhere<FinanzasPayment>, status: number) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.updateByFilter] START", { filter, status });

    const result = await repo().update(filter, { status });

    console.log("[finanzas-payment][repo.updateByFilter] END", {
        elapsedMs: Date.now() - start,
        affected: result.affected,
    });

    return result;
}

export async function findByPaymentHeaderUuid(paymentHeaderUuid: string) {
    const start = Date.now();
    console.log("[finanzas-payment][repo.findByPaymentHeaderUuid] START", { paymentHeaderUuid });

    const result = await repo().find({
        where: { paymentHeaderUuid },
        order: { createdAt: "ASC" }
    });

    console.log("[finanzas-payment][repo.findByPaymentHeaderUuid] END", {
        elapsedMs: Date.now() - start,
        rows: result.length,
    });

    return result;
}

export async function findAllPaginatedByPaymentHeaderUuid(
    paymentHeaderUuid: string,
    pageSize: number,
    pageNumber: number
): Promise<[FinanzasPayment[], number, number]> {
    const start = Date.now();
    const skip = (pageNumber - 1) * pageSize;

    console.log("[finanzas-payment][repo.findAllPaginatedByPaymentHeaderUuid] START", {
        paymentHeaderUuid,
        pageSize,
        pageNumber,
        skip,
    });

    const [result, total] = await repo().findAndCount({
        where: { paymentHeaderUuid },
        take: pageSize,
        skip,
        order: { createdAt: "ASC" }
    });

    console.log("[finanzas-payment][repo.findAllPaginatedByPaymentHeaderUuid] END", {
        elapsedMs: Date.now() - start,
        rows: result.length,
        total,
    });

    return [result, total, result.length];
}