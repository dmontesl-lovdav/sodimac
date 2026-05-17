// src/services/applicationMsg.service.ts
import { getApplicationMsgRepository } from '../repositories/applicationMsg.repo.js';
import { ApplicationMsg } from '../entities/ApplicationMsg.entity.js';

interface ApplicationMsgFilters {
    idApplication?: number;
    idMessage?: number;
}

interface CreateApplicationMsgDto {
    idMessage: number;
    idApplication: number;
    createdBy?: number;
}

export const applicationMsgService = {
    async findAll(filters: ApplicationMsgFilters = {}): Promise<ApplicationMsg[]> {
        const repo = getApplicationMsgRepository();
        const query = repo.createQueryBuilder('am');

        if (filters.idApplication) {
            query.andWhere('am.idApplication = :idApplication', { idApplication: filters.idApplication });
        }

        if (filters.idMessage) {
            query.andWhere('am.idMessage = :idMessage', { idMessage: filters.idMessage });
        }

        query.orderBy('am.idApplicationMsg', 'ASC');
        return query.getMany();
    },

    async findById(id: number): Promise<ApplicationMsg | null> {
        const repo = getApplicationMsgRepository();
        return repo.findOne({ where: { idApplicationMsg: id } });
    },

    async findByApplicationAndMessage(idApplication: number, idMessage: number): Promise<ApplicationMsg | null> {
        const repo = getApplicationMsgRepository();
        return repo.findOne({ where: { idApplication, idMessage } });
    },

    async create(data: CreateApplicationMsgDto): Promise<ApplicationMsg> {
        const repo = getApplicationMsgRepository();
        const entity = repo.create(data);
        return repo.save(entity);
    },

    async delete(id: number): Promise<boolean> {
        const repo = getApplicationMsgRepository();
        const result = await repo.delete({ idApplicationMsg: id });
        return (result.affected ?? 0) > 0;
    }
};
