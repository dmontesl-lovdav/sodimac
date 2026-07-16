import { datasource } from "@/config/typeorm-datasource.js";
import { MigoDocument, MigoStatus } from "@/entities/MigoDocument.entity.js";
import { MigoDocumentReception } from "@/entities/MigoDocumentReception.entity.js";
import { Between, FindOptionsWhere, ILike, MoreThanOrEqual, LessThanOrEqual } from "typeorm";
import type { ListMigoDocumentsQueryDto, ListMigoReceptionsQueryDto } from "@/schemas/migo.schema.js";

export const docRepo = () => datasource.getRepository(MigoDocument);
export const recRepo = () => datasource.getRepository(MigoDocumentReception);

function resolveSchema(): string {
    const opts = datasource.options as { schema?: string };
    const candidates = [opts.schema, process.env.DB_SCHEMA];
    for (const c of candidates) {
        if (typeof c === 'string' && c.trim().length > 0) return c.trim();
    }
    return 'tenant_finance';
}

export async function findExistingReceptionPairs(
    pairs: Array<{ oc: number; reception: number }>,
): Promise<Set<string>> {
    const found = new Set<string>();
    if (pairs.length === 0) return found;
    const schema = resolveSchema();

    {
        const params: unknown[] = [];
        const tuples = pairs.map((p) => {
            params.push(p.oc, p.reception);
            return `($${params.length - 1}, $${params.length})`;
        });
        const sql = `
            SELECT DISTINCT mdr.nro_oc AS oc, mdr.nro_recepcion AS reception
            FROM "${schema}".migo_document_reception mdr
            JOIN "${schema}".migo_document md ON md.migo_document_id = mdr.migo_document_id
            WHERE md.status <> ${MigoStatus.RECHAZADO}
              AND (mdr.nro_oc, mdr.nro_recepcion) IN (${tuples.join(', ')})
        `;
        const rows = await datasource.query(sql, params);
        for (const r of rows as Array<{ oc: unknown; reception: unknown }>) {
            found.add(`${Number(r.oc)}::${Number(r.reception)}`);
        }
    }

    {
        const params: unknown[] = [];
        const tuples = pairs.map((p) => {
            params.push(String(p.oc), String(p.reception));
            return `($${params.length - 1}, $${params.length})`;
        });
        const sql = `
            SELECT DISTINCT po.order_number AS oc, r.reception_number AS reception
            FROM "${schema}".reception r
            JOIN "${schema}".purchase_order po ON po.purchase_order_uuid = r.purchase_order_uuid
            WHERE (po.order_number, r.reception_number) IN (${tuples.join(', ')})
        `;
        const rows = await datasource.query(sql, params);
        for (const r of rows as Array<{ oc: unknown; reception: unknown }>) {
            found.add(`${Number(r.oc)}::${Number(r.reception)}`);
        }
    }

    return found;
}

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
