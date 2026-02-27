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
    const data = {
        ...dto,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    return r.createOne(data as any);
}

export async function update(id: string, dto: UpdateVendorBlockDto) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
    };

    return r.updateOne(id, patch as any);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
