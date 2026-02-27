import { getDataSource } from "@/config/typeorm-datasource.js";
import { StampedRebate } from "@/entities/StampedRebate.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => getDataSource().getRepository(StampedRebate);

export async function findAll(filter: FindOptionsWhere<StampedRebate>, limit = 100) {
    return repo().find({
        where: filter,
        take: limit,
        order: { createdAt: "DESC" },
        relations: ['rebates']
    });
}

export async function findById(id: string) {
    return repo().findOne({
        where: { stampedRebateUuid: id },
        relations: ['rebates']
    });
}

export async function findByDocumentNumber(documentNumber: string) {
    return repo().findOne({
        where: { documentNumber },
        relations: ['rebates']
    });
}

export async function createOne(data: Partial<StampedRebate>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(id: string, patch: Partial<StampedRebate>) {
    await repo().update({ stampedRebateUuid: id }, patch);
    return findById(id);
}

export async function deleteOne(id: string) {
    await repo().delete({ stampedRebateUuid: id });
}
