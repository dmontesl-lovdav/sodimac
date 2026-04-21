import * as r from "@/repositories/vendorBlock.repo.js";
import type {
    ListVendorBlockQuery,
    CreateVendorBlockDto,
    UpdateVendorBlockDto,
} from "@/schemas/vendorBlock.schema.js";
import type { VendorBlock } from "@/entities/VendorBlock.entity.js";
import { FindOptionsWhere } from "typeorm";

export async function list(q: ListVendorBlockQuery) {
    const filter: FindOptionsWhere<VendorBlock> = {};

    if (q.status !== undefined) filter.status = q.status;
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    if (q.blockType !== undefined) filter.blockType = q.blockType;
    if (q.autoUnblock !== undefined) filter.autoUnblock = q.autoUnblock;

    return r.findAll(filter, q.limit ?? 100);
}

export async function get(id: string) {
    return r.findById(id);
}

export async function getByVendorNumber(vendorNumber: number) {
    return r.findByVendorNumber(vendorNumber);
}

export async function getActiveBlocks(vendorNumber: number) {
    return r.findActiveBlocks(vendorNumber);
}

export async function create(dto: CreateVendorBlockDto) {
    const data: Partial<VendorBlock> = {
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    if (dto.vendorNumber !== undefined) data.vendorNumber = dto.vendorNumber;
    if (dto.blockReason !== undefined) data.blockReason = dto.blockReason;
    if (dto.blockDescription !== undefined) data.blockDescription = dto.blockDescription;
    if (dto.startDate !== undefined) data.startDate = dto.startDate;
    if (dto.endDate !== undefined) data.endDate = dto.endDate;
    if (dto.status !== undefined) data.status = dto.status;
    if (dto.autoUnblock !== undefined) data.autoUnblock = dto.autoUnblock;
    if (dto.blockType !== undefined) data.blockType = dto.blockType;
    if (dto.createdBy !== undefined) data.createdBy = dto.createdBy;

    return r.createOne(data);
}

export async function update(id: string, dto: UpdateVendorBlockDto) {
    const patch: Partial<VendorBlock> = {
        updatedAt: new Date(),
    };

    if (dto.vendorNumber !== undefined) patch.vendorNumber = dto.vendorNumber;
    if (dto.blockReason !== undefined) patch.blockReason = dto.blockReason;
    if (dto.blockDescription !== undefined) patch.blockDescription = dto.blockDescription;
    if (dto.startDate !== undefined) patch.startDate = dto.startDate;
    if (dto.endDate !== undefined) patch.endDate = dto.endDate;
    if (dto.status !== undefined) patch.status = dto.status;
    if (dto.autoUnblock !== undefined) patch.autoUnblock = dto.autoUnblock;
    if (dto.blockType !== undefined) patch.blockType = dto.blockType;
    if (dto.updatedBy !== undefined) patch.updatedBy = dto.updatedBy;

    return r.updateOne(id, patch);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
