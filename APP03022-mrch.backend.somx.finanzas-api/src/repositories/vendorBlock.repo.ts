import { getDataSource } from "@/config/typeorm-datasource.js";
import { VendorBlock } from "@/entities/VendorBlock.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => getDataSource().getRepository(VendorBlock);

export async function findAll(filter: FindOptionsWhere<VendorBlock>, limit = 100) {
    return repo().find({ where: filter, take: limit, order: { startDate: "DESC" } });
}

export async function findById(id: string) {
    return repo().findOneBy({ vendorBlockUuid: id });
}

export async function findByVendorNumber(vendorNumber: number) {
    return repo().find({ where: { vendorNumber }, order: { startDate: "DESC" } });
}

export async function findActiveBlocks(vendorNumber: number) {
    return repo().find({
        where: { vendorNumber, status: 1 },
        order: { startDate: "DESC" }
    });
}

export async function createOne(data: Partial<VendorBlock>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(id: string, patch: Partial<VendorBlock>) {
    await repo().update({ vendorBlockUuid: id }, patch);
    return findById(id);
}

export async function deleteOne(id: string) {
    await repo().delete({ vendorBlockUuid: id });
}
