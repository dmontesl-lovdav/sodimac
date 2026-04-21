import { datasource } from "@/config/typeorm-datasource.js";
import { MigoDocument } from "@/entities/MigoDocument.entity.js";
import { MigoDocumentReception } from "@/entities/MigoDocumentReception.entity.js";
import { Between, FindOptionsWhere, ILike, MoreThanOrEqual, LessThanOrEqual } from "typeorm";
import type { ListMigoDocumentsQueryDto, ListMigoReceptionsQueryDto } from "@/schemas/migo.schema.js";

export const docRepo = () => datasource.getRepository(MigoDocument);
export const recRepo = () => datasource.getRepository(MigoDocumentReception);

export async function findDocumentById(id: string) {
    return docRepo().findOne({
        where: { migoDocumentId: id },
        relations: ['receptions'],
    });
}

export async function findDocumentByFolio(folio: string) {
    return docRepo().findOneBy({ folio });
}

export async function findDocumentsPaginated(filter: ListMigoDocumentsQueryDto) {
    const skip = (filter.pageNumber - 1) * filter.pageSize;
    const where: FindOptionsWhere<MigoDocument> = {};

    if (filter.status !== undefined) where.status = filter.status;
    if (filter.fileName) where.fileName = ILike(`%${filter.fileName}%`);

    if (filter.publishedAtStart && filter.publishedAtEnd) {
        where.publishedAt = Between(filter.publishedAtStart, filter.publishedAtEnd);
    } else if (filter.publishedAtStart) {
        where.publishedAt = MoreThanOrEqual(filter.publishedAtStart);
    } else if (filter.publishedAtEnd) {
        where.publishedAt = LessThanOrEqual(filter.publishedAtEnd);
    }

    const [result, total] = await docRepo().findAndCount({
        where,
        order: { publishedAt: 'DESC' },
        skip,
        take: filter.pageSize,
    });

    return { result, total };
}

export async function findReceptionsByDocumentPaginated(filter: ListMigoReceptionsQueryDto) {
    const skip = (filter.pageNumber - 1) * filter.pageSize;
    const where: Record<string, any> = {};
    if (filter.migoDocumentId) where.migoDocumentId = filter.migoDocumentId;

    const [result, total] = await recRepo().findAndCount({
        where,
        order: { rowNumber: 'ASC' },
        skip,
        take: filter.pageSize,
    });

    return { result, total };
}

export async function findAllReceptionsByDocument(migoDocumentId: string) {
    return recRepo().find({
        where: { migoDocumentId },
        order: { rowNumber: 'ASC' },
    });
}

export async function saveDocument(doc: Partial<MigoDocument>) {
    return docRepo().save(doc);
}

export async function updateDocument(id: string, data: Partial<MigoDocument>) {
    await docRepo().update({ migoDocumentId: id }, data);
    return findDocumentById(id);
}
