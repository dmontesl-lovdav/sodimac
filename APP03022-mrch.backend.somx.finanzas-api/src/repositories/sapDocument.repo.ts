import { getDataSource } from "@/config/typeorm-datasource.js";
import { SapDocument } from "@/entities/SapDocument.entity.js";
import { SapDocumentFiscalUuid } from "@/entities/SapDocumentFiscalUuid.entity.js";
import { In, type FindOptionsWhere } from "typeorm";
 
const RELATIONS = { fiscalUuids: true } as const;
 
export const repo = () => getDataSource().getRepository(SapDocument);
export const fiscalUuidRepo = () => getDataSource().getRepository(SapDocumentFiscalUuid);
 
export async function findAll(filter: FindOptionsWhere<SapDocument>, limit = 100) {
    return repo().find({
        where: filter,
        relations: RELATIONS,
        take: limit,
        order: { createdAt: "DESC" },
    });
}
 
export async function findById(id: string) {
    return repo().findOne({
        where: { sapDocumentUuid: id },
        relations: RELATIONS,
    });
}
 
export async function findByDocumentAndReference(
    documentNumber: string,
    referenceNumber: string
) {
    return repo().findOne({
        where: { documentNumber, referenceNumber },
        relations: RELATIONS,
    });
}
 
export async function findByFiscalUuid(fiscalUuid: string, limit = 100) {
    const matches = await repo()
        .createQueryBuilder("d")
        .innerJoin("d.fiscalUuids", "f")
        .where("f.fiscalUuid = :fiscalUuid", { fiscalUuid })
        .orderBy("d.createdAt", "DESC")
        .take(limit)
        .getMany();
 
    const ids = matches.map((row) => row.sapDocumentUuid);
    if (!ids.length) return [];
 
    return repo().find({
        where: { sapDocumentUuid: In(ids) },
        relations: RELATIONS,
        order: { createdAt: "DESC" },
    });
}
 
export async function createOne(
    data: Partial<SapDocument>,
    fiscalUuids: string[]
) {
    return getDataSource().transaction(async (manager) => {
        const docRepo = manager.getRepository(SapDocument);
        const linkRepo = manager.getRepository(SapDocumentFiscalUuid);
        const saved = await docRepo.save(docRepo.create(data));
 
        for (const fiscalUuid of fiscalUuids) {
            await linkRepo.save(
                linkRepo.create({
                    sapDocumentUuid: saved.sapDocumentUuid,
                    fiscalUuid,
                    createdBy: data.createdBy ?? null,
                })
            );
        }
 
        return docRepo.findOne({
            where: { sapDocumentUuid: saved.sapDocumentUuid },
            relations: RELATIONS,
        });
    });
}
 
export async function addFiscalUuids(
    sapDocumentUuid: string,
    fiscalUuids: string[],
    createdBy?: number | null
) {
    if (!fiscalUuids.length) return;
    const linkRepo = fiscalUuidRepo();
    for (const fiscalUuid of fiscalUuids) {
        const exists = await linkRepo.findOneBy({ sapDocumentUuid, fiscalUuid });
        if (exists) continue;
        await linkRepo.save(
            linkRepo.create({
                sapDocumentUuid,
                fiscalUuid,
                createdBy: createdBy ?? null,
            })
        );
    }
}
 
export async function updateOne(id: string, patch: Partial<SapDocument>) {
    await repo().update({ sapDocumentUuid: id }, patch);
    return findById(id);
}
 
export async function deleteOne(id: string) {
    await repo().delete({ sapDocumentUuid: id });
}
 