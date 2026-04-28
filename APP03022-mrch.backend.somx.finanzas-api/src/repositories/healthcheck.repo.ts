import { getDataSource } from "@/config/typeorm-datasource.js";
import { Healthcheck } from "@/entities/Healthcheck.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => getDataSource().getRepository(Healthcheck);

export async function findAll(
    filter: FindOptionsWhere<Healthcheck>,
    limit = 100
) {
    return repo().find({
        where: filter,
        take: limit,
        order: { createdAt: "DESC" },
    });
}

export async function findById(id: string) {
    return repo().findOneBy({ healthcheckUuid: id });
}

export async function createOne(data: Partial<Healthcheck>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(id: string, patch: Partial<Healthcheck>) {
    await repo().update({ healthcheckUuid: id }, patch);
    return findById(id);
}

export async function deleteOne(id: string) {
    await repo().delete({ healthcheckUuid: id });
}