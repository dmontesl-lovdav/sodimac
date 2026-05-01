import * as statusTrainRepo from '@/repositories/statusTrain.repo.js';
import * as statusTrainMapper from '@/mappers/statusTrain.mapper.js';
import {
    statusTrainListSourceNotFound,
    statusTrainListSuccess,
    statusTrainSourceNotFound,
    statusTrainTransitionNotAllowed,
    statusTrainValidationSuccess
} from '@/dto/statusTrain.dto.js';
import type {
    StatusTrainCreateDto,
    StatusTrainDto,
    StatusTrainListResponse,
    StatusTrainUpdateDto,
    StatusTrainValidationResponse
} from '@/dto/statusTrain.dto.js';

export async function validateTransition(
    optionId: number,
    sourceStatus: number,
    targetStatus: number
): Promise<StatusTrainValidationResponse> {
    const sourceExists = await statusTrainRepo.existsByOptionIdAndSourceStatus(optionId, sourceStatus);
    if (!sourceExists) return statusTrainSourceNotFound();

    const transition = await statusTrainRepo.findByOptionIdAndSourceStatusAndTargetStatus(
        optionId,
        sourceStatus,
        targetStatus
    );

    if (!transition) return statusTrainTransitionNotAllowed();

    return statusTrainValidationSuccess(statusTrainMapper.toDto(transition)!);
}

export async function getAllowedDestinations(
    optionId: number,
    sourceStatus: number
): Promise<StatusTrainListResponse> {
    const sourceExists = await statusTrainRepo.existsByOptionIdAndSourceStatus(optionId, sourceStatus);
    if (!sourceExists) return statusTrainListSourceNotFound();

    const transitions = await statusTrainRepo.findByOptionIdAndSourceStatus(optionId, sourceStatus);
    const dtos = transitions
        .map(t => statusTrainMapper.toDto(t))
        .filter((d): d is StatusTrainDto => d !== null);

    return statusTrainListSuccess(dtos);
}

export async function create(dto: StatusTrainCreateDto): Promise<StatusTrainDto> {
    const entity = statusTrainMapper.toEntity(dto);
    const saved = await statusTrainRepo.save(entity);
    return statusTrainMapper.toDto(saved)!;
}

export async function update(
    id: number,
    optionId: number,
    dto: StatusTrainUpdateDto
): Promise<StatusTrainDto | null> {
    const entity = await statusTrainRepo.findById(id);
    if (!entity || entity.optionId !== optionId) return null;

    entity.sourceStatus = dto.sourceStatus;
    entity.targetStatus = dto.targetStatus;
    entity.updatedBy = dto.updatedBy;

    const saved = await statusTrainRepo.save(entity);
    return statusTrainMapper.toDto(saved);
}

export async function deleteById(id: number): Promise<boolean> {
    if (await statusTrainRepo.existsById(id)) {
        await statusTrainRepo.deleteById(id);
        return true;
    }
    return false;
}

export async function findById(id: number): Promise<StatusTrainDto | null> {
    const entity = await statusTrainRepo.findById(id);
    return statusTrainMapper.toDto(entity);
}

