import { datasource } from '@/config/typeorm-datasource.js';
import { StatusTrain } from '@/entities/StatusTrain.entity.js';

export const repo = () => datasource.getRepository(StatusTrain);

export async function findById(id: number): Promise<StatusTrain | null> {
    return repo().findOne({ where: { id } });
}

export async function findByOptionId(optionId: number): Promise<StatusTrain[]> {
    return repo().find({ where: { optionId } });
}

export async function findByOptionIdAndSourceStatus(
    optionId: number,
    sourceStatus: number
): Promise<StatusTrain[]> {
    return repo().find({ where: { optionId, sourceStatus } });
}

export async function findByOptionIdAndSourceStatusAndTargetStatus(
    optionId: number,
    sourceStatus: number,
    targetStatus: number
): Promise<StatusTrain | null> {
    return repo().findOne({ where: { optionId, sourceStatus, targetStatus } });
}

export async function existsByOptionIdAndSourceStatus(
    optionId: number,
    sourceStatus: number
): Promise<boolean> {
    const count = await repo().count({ where: { optionId, sourceStatus } });
    return count > 0;
}

export async function existsByOptionIdAndSourceStatusAndTargetStatus(
    optionId: number,
    sourceStatus: number,
    targetStatus: number
): Promise<boolean> {
    const count = await repo().count({ where: { optionId, sourceStatus, targetStatus } });
    return count > 0;
}

export async function save(entity: StatusTrain): Promise<StatusTrain> {
    return repo().save(entity);
}

export async function existsById(id: number): Promise<boolean> {
    const count = await repo().count({ where: { id } });
    return count > 0;
}

export async function deleteById(id: number): Promise<void> {
    await repo().delete({ id });
}

