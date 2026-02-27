import { getDataSource } from "@/config/typeorm-datasource.js";
import { ShippingGuidePurchaseOrder } from "@/entities/ShippingGuidePurchaseOrder.entity.js";
import { ListShippingGuideQuery } from "@/schemas/shippingGuide.schema.js";
import { Between, LessThanOrEqual, MoreThanOrEqual, type FindOptionsWhere } from "typeorm";
import { logger } from "@/utils/logger.js";

export const repo = () => getDataSource().getRepository(ShippingGuidePurchaseOrder);


export async function createOne(data: Partial<ShippingGuidePurchaseOrder>) {
    const entity =  repo().create(data);
    const entityCreated = await repo().save(entity);
    logger.info("✅ ShippingGuidePurchaseOrder Entity Created → data={}", entityCreated); 
    return entityCreated;
}

export async function createMany(data: Partial<ShippingGuidePurchaseOrder>[]) {
    const entity =  repo().create(data);
    const entityCreatedList = await repo().save(entity);
    logger.info("✅ ShippingGuidePurchaseOrder Entity Created → data={}", entityCreatedList); 
    return entityCreatedList;
}

