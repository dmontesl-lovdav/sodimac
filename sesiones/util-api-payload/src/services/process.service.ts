// src/services/process.service.ts
import { getProcessRepository } from '../repositories/process.repo.js';
import { CatProcess } from '../entities/CatProcess.entity.js';

interface ProcessFilters {
    name?: string;
}

interface CreateProcessDto {
    name: string;
    description?: string;
    createdBy?: number;
}

interface UpdateProcessDto {
    name?: string;
    description?: string;
    updatedBy?: number;
}

export const processService = {
    async findAll(filters: ProcessFilters = {}): Promise<CatProcess[]> {
        const repo = getProcessRepository();
        const query = repo.createQueryBuilder('p');

        if (filters.name) {
            query.andWhere('p.name ILIKE :name', { name: `%${filters.name}%` });
        }

        query.orderBy('p.idProcess', 'ASC');
        return query.getMany();
    },

    async findById(id: number): Promise<CatProcess | null> {
        const repo = getProcessRepository();
        return repo.findOne({ where: { idProcess: id } });
    },

    async create(data: CreateProcessDto): Promise<CatProcess> {
        const repo = getProcessRepository();
        const entity = repo.create(data);
        return repo.save(entity);
    },

    async update(id: number, data: UpdateProcessDto): Promise<CatProcess | null> {
        const repo = getProcessRepository();
        const entity = await repo.findOne({ where: { idProcess: id } });

        if (!entity) return null;

        Object.assign(entity, data);
        return repo.save(entity);
    },

    async delete(id: number): Promise<boolean> {
        const repo = getProcessRepository();
        const result = await repo.delete({ idProcess: id });
        return (result.affected ?? 0) > 0;
    }
};
