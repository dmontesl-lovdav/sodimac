import { getDataSource } from "@/config/typeorm-datasource.js";
import { ShippingGuide } from "@/entities/ShippingGuide.entity.js";
import { ListShippingGuideQuery } from "@/schemas/shippingGuide.schema.js";
import { Between, LessThanOrEqual, MoreThanOrEqual, In, type FindOptionsWhere } from "typeorm";
import { logger } from "@/utils/logger.js";

export const repo = () => getDataSource().getRepository(ShippingGuide);

export async function findAllByPage(criteria: ListShippingGuideQuery, page: number, size: number) {
    return repo().createQueryBuilder().where(buildCriteria(criteria)).limit(size).offset(page * size).getMany();
}

export async function findAll(criteria: ListShippingGuideQuery, limit = 100) {
    return repo().find({ where: buildCriteria(criteria), take: limit, order: { deliveryDate: "DESC" } });
}

export async function findByAll(filter: FindOptionsWhere<ShippingGuide>) {
    const entity = await repo().find({ where: filter });
    return entity;
}


export async function findAllPaginated(filter: FindOptionsWhere<ShippingGuide>, pageSize: number, pageNumber: number) {
    const skip = (pageNumber - 1) * pageSize; // Calculate the offset
    let [result, total] = await repo().findAndCount({ where: filter, take: pageSize, skip: skip, order: { createdAt: "DESC" } });
    logger.info("✅ shippingGuide List  → data={}", result); 
    return [result, total, result.length];
}



export async function findById(id: string) {
    const entityFinded = await repo().findOne({
        where: { shippingGuideId: id }
    });
    logger.info("✅ shippingGuide finded  → data={}", entityFinded);
    return entityFinded;
}

export async  function findOneByGuideNumber(guideNumber: string) {
    const entityFinded = await repo().findOneBy({ guideNumber: guideNumber });
    logger.info("✅ shippingGuide finded  → data={}", entityFinded);
    return entityFinded;
}

export async function createOne(data: Partial<ShippingGuide>) {
    const entity =  repo().create(data);
    const entityCreated = await repo().save(entity);
    logger.info("✅ shippingGuide Entity Created → data={}", entityCreated); 
    return entityCreated;
}

export async function updateOneByUuid(id: string, patch: Partial<ShippingGuide>) {
    const entityUpdated = await repo().update({ shippingGuideId: id }, patch);
    logger.info("✅ shippingGuide Entity Updated → data={}", entityUpdated); 
    return findById(id);
}

export async function updateOneByGuide(guideNumber: string, patch: Partial<ShippingGuide>) {
    const entityUpdated = await repo().update({ guideNumber: guideNumber }, patch);
    logger.info("✅ shippingGuide Entity Updated → data={}", entityUpdated); 
    return findOneByGuideNumber(guideNumber);
}

export async function deleteOne(id: string) {
    await repo().delete({ shippingGuideId: id });
}

export async function findByAllByIds(shippingGuideIds: string[]) {
     const oneDayInMs = 86400 * 1000;
     const days = -7; //Solo busca desde hace 7 dias
     const newTime  = (new Date()).getTime() + (days * oneDayInMs);
    const entity = await repo().find({
    where: {
        shippingGuideId: In(shippingGuideIds),
        isStatusUpdated: true,
        createdAt: MoreThanOrEqual(new Date(newTime))
    },
    });
    return entity;
}

export async function updateAllStatus(entitiesToUpdateIds: string[]) {
    const entity = await repo().createQueryBuilder().update(ShippingGuide)
            .set({isStatusUpdated: false})
            .whereInIds(entitiesToUpdateIds.map(e => e))
            .execute();
    return entity;
}

export function buildCriteria(criteria: ListShippingGuideQuery): FindOptionsWhere<ShippingGuide> {
    const filter: FindOptionsWhere<ShippingGuide> = {};

    if (criteria.id) filter.shippingGuideId = criteria.id;
    if (criteria.status !== undefined) filter.status = criteria.status;
    if (criteria.vendorNumber !== undefined) filter.vendorNumber = criteria.vendorNumber;
    if (criteria.deliveryType !== undefined) filter.deliveryType = criteria.deliveryType;

    if (criteria.from && criteria.to) filter.deliveryDate = Between(criteria.from, criteria.to);
    else if (criteria.from) filter.deliveryDate = MoreThanOrEqual(criteria.from);
    else if (criteria.to) filter.deliveryDate = LessThanOrEqual(criteria.to);

    return filter;
}
