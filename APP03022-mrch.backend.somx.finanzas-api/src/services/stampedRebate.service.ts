import * as r from "@/repositories/stampedRebate.repo.js";
import type {
    ListStampedRebateQuery,
    CreateStampedRebateDto,
    UpdateStampedRebateDto,
} from "@/schemas/stampedRebate.schema.js";
import type { StampedRebate } from "@/entities/StampedRebate.entity.js";
import { FindOptionsWhere } from "typeorm";

export async function list(q: ListStampedRebateQuery) {
    const filter: FindOptionsWhere<StampedRebate> = {};

    if (q.status !== undefined) filter.status = q.status;
    if (q.documentNumber !== undefined) filter.documentNumber = q.documentNumber;

    return r.findAll(filter, q.limit ?? 100);
}

export async function get(id: string) {
    return r.findById(id);
}

export async function getByDocumentNumber(documentNumber: string) {
    return r.findByDocumentNumber(documentNumber);
}

export async function create(dto: CreateStampedRebateDto) {
    const data: Partial<StampedRebate> = {
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    if (dto.documentNumber !== undefined) data.documentNumber = dto.documentNumber;
    if (dto.referenceNumber !== undefined) data.referenceNumber = dto.referenceNumber;
    if (dto.status !== undefined) data.status = dto.status;
    if (dto.createdBy !== undefined) data.createdBy = dto.createdBy;

    return r.createOne(data);
}

export async function update(id: string, dto: UpdateStampedRebateDto) {
    const patch: Partial<StampedRebate> = {
        updatedAt: new Date(),
    };

    if (dto.documentNumber !== undefined) patch.documentNumber = dto.documentNumber;
    if (dto.referenceNumber !== undefined) patch.referenceNumber = dto.referenceNumber;
    if (dto.status !== undefined) patch.status = dto.status;
    if (dto.updatedBy !== undefined) patch.updatedBy = dto.updatedBy;

    return r.updateOne(id, patch);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}

// Generate CSV report
export async function generateCsvReport(): Promise<string> {
    const stampedRebates = await r.findAll({}, 10000);

    const headers = [
        "UUID",
        "Document Number",
        "Reference Number",
        "Status",
    ];

    const rows = stampedRebates.map(sr => [
        sr.stampedRebateUuid,
        sr.documentNumber,
        sr.referenceNumber,
        sr.status,
    ]);

    const csvLines = [
        headers.join(","),
        ...rows.map(row => row.map(cell => `"${cell}"`).join(",")),
    ];

    return csvLines.join("\n");
}
