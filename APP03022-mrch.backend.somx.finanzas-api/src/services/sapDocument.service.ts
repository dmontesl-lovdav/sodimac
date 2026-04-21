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

    return r.findAll(filter, q.limit ?? 100);
}

export async function get(id: string) {
    return r.findById(id);
}

export async function getByDocumentAndReference(
    documentNumber: string,
    documentReference: string
) {
    return r.findByDocumentAndReference(documentNumber, documentReference);
}

export async function create(dto: CreateSapDocumentDto) {
    const data: Partial<SapDocument> = {
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    if (dto.documentNumber !== undefined)
        data.documentNumber = dto.documentNumber;

    if (dto.referenceNumber !== undefined)
        data.documentReference = dto.referenceNumber;

    if (dto.vendorNumber !== undefined)
        data.supplierNumber = dto.vendorNumber;

    if (dto.amount !== undefined)
        data.amount = Number(dto.amount);

    if (dto.docSap !== undefined)
        data.sapCode = dto.docSap;

    if (dto.message != null)
        data.message = dto.message;

    if (dto.sapStatus !== undefined)
        data.sapStatus = dto.sapStatus;

    if (dto.documentType !== undefined)
        data.documentType = dto.documentType;

    if (dto.createdBy != null)
        data.createdBy = dto.createdBy;

    return r.createOne(data);
}


export async function update(id: string, dto: UpdateSapDocumentDto) {
    const patch: Partial<SapDocument> = {
        updatedAt: new Date(),
    };

    if (dto.documentNumber !== undefined)
        patch.documentNumber = dto.documentNumber;

    if (dto.referenceNumber !== undefined)
        patch.documentReference = dto.referenceNumber;

    if (dto.vendorNumber !== undefined)
        patch.supplierNumber = dto.vendorNumber;

    if (dto.amount !== undefined)
        patch.amount = Number(dto.amount);

    if (dto.docSap !== undefined)
        patch.sapCode = dto.docSap;

    if (dto.message != null)
        patch.message = dto.message;

    if (dto.sapStatus !== undefined)
        patch.sapStatus = dto.sapStatus;

    if (dto.documentType !== undefined)
        patch.documentType = dto.documentType;

    if (dto.updatedBy !== undefined)
        patch.updatedBy = dto.updatedBy;

    return r.updateOne(id, patch);
}


export async function remove(id: string) {
    await r.deleteOne(id);
}
