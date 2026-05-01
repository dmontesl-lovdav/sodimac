import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogDetailRelation } from '@/entities/CatalogDetailRelation.entity.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';

export const repo = () => datasource.getRepository(CatalogDetailRelation);

export async function findBySourceDetailIdAndStatus(
    sourceDetailId: number,
    status: number
): Promise<CatalogDetailRelation[]> {
    return repo().find({ where: { sourceDetailId, status } });
}

export async function findByTargetDetailIdAndStatus(
    targetDetailId: number,
    status: number
): Promise<CatalogDetailRelation[]> {
    return repo().find({ where: { targetDetailId, status } });
}

export async function findByRelationTypeAndStatus(
    relationType: string,
    status: number
): Promise<CatalogDetailRelation[]> {
    return repo().find({ where: { relationType, status } });
}

export async function findSourceIdsByTargetAndType(
    targetDetailId: number,
    relationType: string,
    status: number
): Promise<number[]> {
    const rows = await repo()
        .createQueryBuilder('r')
        .select('r.sourceDetailId', 'sourceId')
        .where('r.targetDetailId = :targetDetailId', { targetDetailId })
        .andWhere('r.relationType = :relationType', { relationType })
        .andWhere('r.status = :status', { status })
        .getRawMany<{ sourceId: number }>();
    return rows.map(r => r.sourceId);
}

export async function findTargetIdsBySourceAndType(
    sourceDetailId: number,
    relationType: string,
    status: number
): Promise<number[]> {
    const rows = await repo()
        .createQueryBuilder('r')
        .select('r.targetDetailId', 'targetId')
        .where('r.sourceDetailId = :sourceDetailId', { sourceDetailId })
        .andWhere('r.relationType = :relationType', { relationType })
        .andWhere('r.status = :status', { status })
        .getRawMany<{ targetId: number }>();
    return rows.map(r => r.targetId);
}

export async function findSourceDetailsByTargetCatalogAndKey(
    targetCatalogCode: string,
    targetExternalKey: string,
    relationType: string,
    status: number
): Promise<CatalogDetail[]> {
    const rows = await repo()
        .createQueryBuilder('r')
        .leftJoinAndSelect('r.sourceDetail', 'sd')
        .leftJoinAndSelect('sd.header', 'sh')
        .leftJoinAndSelect('r.targetDetail', 'td')
        .leftJoinAndSelect('td.header', 'th')
        .where('th.code = :targetCatalogCode', { targetCatalogCode })
        .andWhere('td.externalKey = :targetExternalKey', { targetExternalKey })
        .andWhere('r.relationType = :relationType', { relationType })
        .andWhere('r.status = :status', { status })
        .andWhere('sd.status = :status', { status })
        .getMany();
    return rows.map(r => r.sourceDetail).filter((d): d is CatalogDetail => d != null);
}

export async function findSourceDetailsByTargetCatalogAndKeyAndValidDate(
    targetCatalogCode: string,
    targetExternalKey: string,
    relationType: string,
    status: number,
    currentDate: string
): Promise<CatalogDetail[]> {
    const rows = await repo()
        .createQueryBuilder('r')
        .leftJoinAndSelect('r.sourceDetail', 'sd')
        .leftJoinAndSelect('sd.header', 'sh')
        .leftJoinAndSelect('r.targetDetail', 'td')
        .leftJoinAndSelect('td.header', 'th')
        .where('th.code = :targetCatalogCode', { targetCatalogCode })
        .andWhere('td.externalKey = :targetExternalKey', { targetExternalKey })
        .andWhere('r.relationType = :relationType', { relationType })
        .andWhere('r.status = :status', { status })
        .andWhere('sd.status = :status', { status })
        .andWhere('(sd.validFrom IS NULL OR sd.validFrom <= :currentDate)', { currentDate })
        .andWhere('(sd.validTo IS NULL OR sd.validTo >= :currentDate)', { currentDate })
        .getMany();
    return rows.map(r => r.sourceDetail).filter((d): d is CatalogDetail => d != null);
}

export async function existsBySourceTargetAndType(
    sourceDetailId: number,
    targetDetailId: number,
    relationType: string
): Promise<boolean> {
    const count = await repo().count({ where: { sourceDetailId, targetDetailId, relationType } });
    return count > 0;
}

