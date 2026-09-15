import * as r from "@/repositories/sapDocument.repo.js";
import {
    collectFiscalUuids,
    type ListSapDocumentQuery,
    type CreateSapDocumentDto,
    type UpdateSapDocumentDto,
} from "@/schemas/sapDocument.schema.js";
import type { SapDocument } from "@/entities/SapDocument.entity.js";
import { FindOptionsWhere } from "typeorm";
import { toSapDocumentResponse } from "@/mappers/sapDocument.mapper.js";

export async function list(q: ListSapDocumentQuery) {
    if (q.fiscalUuid) {
        const rows = await r.findByFiscalUuid(q.fiscalUuid, q.limit ?? 100);
        return rows.filter(Boolean).map(toSapDocumentResponse);
    }

    const filter: FindOptionsWhere<SapDocument> = {};

    if (q.sapStatus !== undefined) filter.sapStatus = q.sapStatus;
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    if (q.documentType !== undefined) filter.documentType = q.documentType;
    if (q.source !== undefined) filter.source = q.source;

    const rows = await r.findAll(filter, q.limit ?? 100);
    return rows.map(toSapDocumentResponse);
}

export async function get(id: string) {
    const row = await r.findById(id);
    return row ? toSapDocumentResponse(row) : null;
}

export async function getByDocumentAndReference(
    documentNumber: string,
    documentReference: string
) {
    const row = await r.findByDocumentAndReference(documentNumber, documentReference);
    return row ? toSapDocumentResponse(row) : null;
}

export async function listByFiscalUuid(fiscalUuid: string, limit = 100) {
    const rows = await r.findByFiscalUuid(fiscalUuid, limit);
    return rows.map(toSapDocumentResponse);
}

export async function create(dto: CreateSapDocumentDto) {
    const fiscalUuids = collectFiscalUuids(dto);
    const data: Partial<SapDocument> = {
        documentNumber: dto.documentNumber,
        referenceNumber: dto.referenceNumber,
        vendorNumber: dto.vendorNumber,
        amount: Number(dto.amount),
        source: dto.source,
        docSap: dto.docSap,
        message: dto.message ?? null,
        sapStatus: dto.sapStatus ?? 1,
        documentType: dto.documentType,
        createdBy: dto.createdBy ?? null,
    };

    const created = await r.createOne(data, fiscalUuids);
    return created ? toSapDocumentResponse(created) : null;
}

export async function update(id: string, dto: UpdateSapDocumentDto) {
    const current = await r.findById(id);
    if (!current) return null;

    const patch: Partial<SapDocument> = {};

    if (dto.documentNumber !== undefined) patch.documentNumber = dto.documentNumber;
    if (dto.referenceNumber !== undefined) patch.referenceNumber = dto.referenceNumber;
    if (dto.vendorNumber !== undefined) patch.vendorNumber = dto.vendorNumber;
    if (dto.amount !== undefined) patch.amount = Number(dto.amount);
    if (dto.source !== undefined) patch.source = dto.source;
    if (dto.docSap !== undefined) patch.docSap = dto.docSap;
    if (dto.message !== undefined) patch.message = dto.message;
    if (dto.sapStatus !== undefined) patch.sapStatus = dto.sapStatus;
    if (dto.documentType !== undefined) patch.documentType = dto.documentType;
    if (dto.updatedBy !== undefined) patch.updatedBy = dto.updatedBy;

    const extraUuids = collectFiscalUuids(dto);
    if (extraUuids.length) {
        await r.addFiscalUuids(id, extraUuids, dto.createdBy ?? dto.updatedBy);
    }

    if (Object.keys(patch).length) {
        const updated = await r.updateOne(id, patch);
        return updated ? toSapDocumentResponse(updated) : null;
    }

    const refreshed = await r.findById(id);
    return refreshed ? toSapDocumentResponse(refreshed) : null;
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
