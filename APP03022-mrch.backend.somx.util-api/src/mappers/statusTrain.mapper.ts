import { StatusTrain } from '@/entities/StatusTrain.entity.js';
import type { StatusTrainCreateDto, StatusTrainDto } from '@/dto/statusTrain.dto.js';

export function toDto(entity: StatusTrain | null | undefined): StatusTrainDto | null {
    if (!entity) return null;
    return {
        id: entity.id,
        optionId: entity.optionId,
        sourceStatus: entity.sourceStatus,
        targetStatus: entity.targetStatus,
        createdBy: entity.createdBy ?? null,
        createdAt: entity.createdAt ?? null,
        updatedBy: entity.updatedBy ?? null,
        updatedAt: entity.updatedAt ?? null
    };
}

export function toEntity(dto: StatusTrainCreateDto): StatusTrain {
    const entity = new StatusTrain();
    entity.optionId = dto.optionId;
    entity.sourceStatus = dto.sourceStatus;
    entity.targetStatus = dto.targetStatus;
    entity.createdBy = dto.createdBy;
    return entity;
}

