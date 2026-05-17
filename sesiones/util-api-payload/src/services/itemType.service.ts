// src/services/itemType.service.ts
import { getItemTypeRepository } from '../repositories/itemType.repo.js';
import { CatItemType } from '../entities/CatItemType.entity.js';

interface ItemTypeFilters {
    name?: string;
}

interface CreateItemTypeDto {
    name: string;
    description?: string;
    createdBy?: number;
}

interface UpdateItemTypeDto {
    name?: string;
    description?: string;
    updatedBy?: number;
}

export const itemTypeService = {
    async findAll(filters: ItemTypeFilters = {}): Promise<CatItemType[]> {
        const repo = getItemTypeRepository();
        const query = repo.createQueryBuilder('it');

        if (filters.name) {
            query.andWhere('it.name ILIKE :name', { name: `%${filters.name}%` });
        }

        query.orderBy('it.idItemType', 'ASC');
        return query.getMany();
    },

    async findById(id: number): Promise<CatItemType | null> {
        const repo = getItemTypeRepository();
        return repo.findOne({ where: { idItemType: id } });
    },

    async create(data: CreateItemTypeDto): Promise<CatItemType> {
        const repo = getItemTypeRepository();
        const entity = repo.create(data);
        return repo.save(entity);
    },

    async update(id: number, data: UpdateItemTypeDto): Promise<CatItemType | null> {
        const repo = getItemTypeRepository();
        const entity = await repo.findOne({ where: { idItemType: id } });

        if (!entity) return null;

        Object.assign(entity, data);
        return repo.save(entity);
    },

    async delete(id: number): Promise<boolean> {
        const repo = getItemTypeRepository();
        const result = await repo.delete({ idItemType: id });
        return (result.affected ?? 0) > 0;
    }
};
