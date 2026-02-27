import { datasource } from "@/config/typeorm-datasource.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { In, type FindOptionsWhere } from "typeorm";

export const repo = () => datasource.getRepository(PurchaseOrder);

export async function createOne(data: Partial<PurchaseOrder>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function findById(purchaseOrderId: string) {
    return repo().findOneBy({ purchaseOrderId });
}

export async function findByOrderNumber(filter: FindOptionsWhere<PurchaseOrder>) {
    const entity = await repo().findOne({ where: filter });
    return entity;
}

