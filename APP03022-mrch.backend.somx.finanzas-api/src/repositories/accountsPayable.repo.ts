import { getDataSource } from "@/config/typeorm-datasource.js";
import { AccountsPayable } from "@/entities/AccountsPayable.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => getDataSource().getRepository(AccountsPayable);

export async function findAll(filter: FindOptionsWhere<AccountsPayable>, limit = 100) {
    return repo().find({ where: filter, take: limit, order: { createdAt: "DESC" } });
}

export async function findById(id: string) {
    return repo().findOneBy({ accountsPayableUuid: id });
}

export async function createOne(data: Partial<AccountsPayable>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(id: string, patch: Partial<AccountsPayable>) {
    await repo().update({ accountsPayableUuid: id }, patch);
    return findById(id);
}

export async function deleteOne(id: string) {
    await repo().delete({ accountsPayableUuid: id });
}
