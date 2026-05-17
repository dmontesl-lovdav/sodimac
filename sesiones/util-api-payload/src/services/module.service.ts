// src/services/module.service.ts
import { getModuleRepository } from '../repositories/module.repo.js';
import { CatModule } from '../entities/CatModule.entity.js';

interface ModuleFilters {
    name?: string;
}

interface CreateModuleDto {
    name: string;
    description?: string;
    createdBy?: number;
}

interface UpdateModuleDto {
    name?: string;
    description?: string;
    updatedBy?: number;
}

export const moduleService = {
    async findAll(filters: ModuleFilters = {}): Promise<CatModule[]> {
        const repo = getModuleRepository();
        const query = repo.createQueryBuilder('m');

        if (filters.name) {
            query.andWhere('m.name ILIKE :name', { name: `%${filters.name}%` });
        }

        query.orderBy('m.idModule', 'ASC');
        return query.getMany();
    },

    async findById(id: number): Promise<CatModule | null> {
        const repo = getModuleRepository();
        return repo.findOne({ where: { idModule: id } });
    },

    async create(data: CreateModuleDto): Promise<CatModule> {
        const repo = getModuleRepository();
        const entity = repo.create(data);
        return repo.save(entity);
    },

    async update(id: number, data: UpdateModuleDto): Promise<CatModule | null> {
        const repo = getModuleRepository();
        const entity = await repo.findOne({ where: { idModule: id } });

        if (!entity) return null;

        Object.assign(entity, data);
        return repo.save(entity);
    },

    async delete(id: number): Promise<boolean> {
        const repo = getModuleRepository();
        const result = await repo.delete({ idModule: id });
        return (result.affected ?? 0) > 0;
    }
};
