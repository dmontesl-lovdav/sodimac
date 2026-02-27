import { getDataSource } from "@/config/typeorm-datasource.js";
import { SapDocument } from "@/entities/SapDocument.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => getDataSource().getRepository(SapDocument);

export async function findAll(filter: FindOptionsWhere<SapDocument>, limit = 100) {
    return repo().find({ where: filter, take: limit, order: { createdAt: "DESC" } });
}

export async function findById(id: string) {
    return repo().findOneBy({ sapDocumentId: id });
}

export async function findByDocumentAndReference(documentNumber: string, documentReference: string) {
    return repo().findOneBy({ documentNumber, documentReference });
}

export async function createOne(data: Partial<SapDocument>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(id: string, patch: Partial<SapDocument>) {
    await repo().update({ sapDocumentId: id }, patch);
    return findById(id);
}

export async function deleteOne(id: string) {
    await repo().delete({ sapDocumentId: id });
}
