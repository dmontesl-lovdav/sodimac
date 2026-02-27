// src/services/item.service.ts
import { getItemRepository } from '../repositories/item.repo.js';
import { CatItem } from '../entities/CatItem.entity.js';

interface ItemFilters {
    name?: string;
}

interface CreateItemDto {
    name: string;
    description?: string;
    createdBy?: number;
}

interface UpdateItemDto {
    name?: string;
    description?: string;
    updatedBy?: number;
}

export const itemService = {
    async findAll(filters: ItemFilters = {}): Promise<CatItem[]> {
        const repo = getItemRepository();
        const query = repo.createQueryBuilder('i');

        if (filters.name) {
            query.andWhere('i.name ILIKE :name', { name: `%${filters.name}%` });
        }

        query.orderBy('i.idItem', 'ASC');
        return query.getMany();
    },

    async findById(id: number): Promise<CatItem | null> {
        const repo = getItemRepository();
        return repo.findOne({ where: { idItem: id } });
    },

    async create(data: CreateItemDto): Promise<CatItem> {
        const repo = getItemRepository();
        const entity = repo.create(data);
        return repo.save(entity);
    },

    async update(id: number, data: UpdateItemDto): Promise<CatItem | null> {
        const repo = getItemRepository();
        const entity = await repo.findOne({ where: { idItem: id } });

        if (!entity) return null;

        Object.assign(entity, data);
        return repo.save(entity);
    },

    async delete(id: number): Promise<boolean> {
        const repo = getItemRepository();
        const result = await repo.delete({ idItem: id });
        return (result.affected ?? 0) > 0;
    }
};
