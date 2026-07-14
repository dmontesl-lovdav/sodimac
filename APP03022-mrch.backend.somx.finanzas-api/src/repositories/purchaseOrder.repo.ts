import { datasource } from "@/config/typeorm-datasource.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { Between, In, type FindOptionsWhere } from "typeorm";

export const repo = () => datasource.getRepository(PurchaseOrder);

export async function findByVendorAndDateRange(vendorNumber: number, start: Date, end: Date): Promise<PurchaseOrder[]> {
    // Se usa query builder para garantizar la comparación correcta con bigint
    return repo()
        .createQueryBuilder('po')
        .where('po.supplierNumber = :vendorNumber', { vendorNumber })
        .andWhere('po.purchaseOrderDate BETWEEN :start AND :end', { start, end })
        .orderBy('po.purchaseOrderDate', 'ASC')
        .getMany();
}

export async function createOne(data: Partial<PurchaseOrder>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function findById(purchaseOrderId: string) {
    return repo().findOneBy({ purchaseOrderId });
}

export async function findByOrderNumber(filter: FindOptionsWhere<PurchaseOrder>) {
    const entity = await repo().findOne({
        where: filter
    });
    return entity;
}

