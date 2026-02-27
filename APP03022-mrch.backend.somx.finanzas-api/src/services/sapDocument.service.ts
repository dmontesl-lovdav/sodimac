import * as r from "@/repositories/sapDocument.repo.js";
import type {
    ListSapDocumentQuery,
    CreateSapDocumentDto,
    UpdateSapDocumentDto,
} from "@/schemas/sapDocument.schema.js";
import type { SapDocument } from "@/entities/SapDocument.entity.js";
import { FindOptionsWhere } from "typeorm";

export async function list(q: ListSapDocumentQuery) {
    const filter: FindOptionsWhere<SapDocument> = {};

    if (q.sapStatus !== undefined) filter.sapStatus = q.sapStatus;
    if (q.vendorNumber !== undefined) filter.supplierNumber = q.vendorNumber;
    if (q.documentType !== undefined) filter.documentType = q.documentType;
    // Note: source field doesn't exist in SapDocument entity, commenting out
    // if (q.source !== undefined) filter.source = q.source;

    return r.findAll(filter, q.limit ?? 100);
}

export async function get(id: string) {
    return r.findById(id);
}

export async function getByDocumentAndReference(documentNumber: string, documentReference: string) {
    return r.findByDocumentAndReference(documentNumber, documentReference);
}

export async function create(dto: CreateSapDocumentDto) {
    const data = {
        ...dto,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    return r.createOne(data as any);
}

export async function update(id: string, dto: UpdateSapDocumentDto) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
    };

    return r.updateOne(id, patch as any);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
