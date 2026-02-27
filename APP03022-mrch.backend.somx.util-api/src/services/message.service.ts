// src/services/message.service.ts
import { getMessageRepository } from '../repositories/message.repo.js';
import { CatMessage } from '../entities/CatMessage.entity.js';

interface MessageFilters {
    messageCode?: string;
    idMessageType?: number;
}

interface CreateMessageDto {
    messageCode: string;
    idMessageType: number;
    description?: string;
    createdBy?: number;
}

interface UpdateMessageDto {
    messageCode?: string;
    idMessageType?: number;
    description?: string;
    updatedBy?: number;
}

export const messageService = {
    async findAll(filters: MessageFilters = {}): Promise<CatMessage[]> {
        const repo = getMessageRepository();
        const query = repo.createQueryBuilder('m');

        if (filters.messageCode) {
            query.andWhere('m.messageCode ILIKE :messageCode', { messageCode: `%${filters.messageCode}%` });
        }

        if (filters.idMessageType) {
            query.andWhere('m.idMessageType = :idMessageType', { idMessageType: filters.idMessageType });
        }

        query.orderBy('m.idMessage', 'ASC');
        return query.getMany();
    },

    async findById(id: number): Promise<CatMessage | null> {
        const repo = getMessageRepository();
        return repo.findOne({ where: { idMessage: id } });
    },

    async findByCode(code: string): Promise<CatMessage | null> {
        const repo = getMessageRepository();
        return repo.findOne({ where: { messageCode: code } });
    },

    async create(data: CreateMessageDto): Promise<CatMessage> {
        const repo = getMessageRepository();
        const entity = repo.create(data);
        return repo.save(entity);
    },

    async update(id: number, data: UpdateMessageDto): Promise<CatMessage | null> {
        const repo = getMessageRepository();
        const entity = await repo.findOne({ where: { idMessage: id } });

        if (!entity) return null;

        Object.assign(entity, data);
        return repo.save(entity);
    },

    async delete(id: number): Promise<boolean> {
        const repo = getMessageRepository();
        const result = await repo.delete({ idMessage: id });
        return (result.affected ?? 0) > 0;
    }
};
