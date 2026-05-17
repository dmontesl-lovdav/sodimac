import { getDataSource } from "@/config/typeorm-datasource.js";
import { Rebate } from "@/entities/Rebate.entity.js";
import type { RebateFilterDto } from "@/schemas/rebate.schema.js";
import { Between, type FindOptionsWhere } from "typeorm";

export const repo = () => getDataSource().getRepository(Rebate);

export async function findAll(filter: FindOptionsWhere<Rebate>, limit = 100) {
    return repo().find({
        where: filter,
        take: limit,
        order: { postingDate: "DESC" },
        relations: ['stampedRebate']
    });
}

export async function findById(id: string) {
    return repo().findOne({
        where: { rebateId: id },
        relations: ['stampedRebate']
    });
}

export async function findByDocumentNumber(documentNumber: string) {
    return repo().find({
        where: { documentNumber },
        relations: ['stampedRebate']
    });
}

// Buscar todos los rebates publicados (status = 1)
export async function findAllPublished() {
    return repo().find({
        where: { status: 1 },
        order: { postingDate: "DESC" },
        relations: ['stampedRebate']
    });
}

// Búsqueda con filtros dinámicos
export async function findByVendorAndPostingDateRange(vendorNumber: number, start: Date, end: Date): Promise<Rebate[]> {
    return repo().find({
        where: {
            //supplierNumber: vendorNumber,
            postingDate: Between(start, end) as any,
        },
        order: { postingDate: 'DESC' },
        take: 500,
    });
}

export async function findWithDynamicFilters(filters: RebateFilterDto) {
    const queryBuilder = repo().createQueryBuilder('rebate')
        .leftJoinAndSelect('rebate.stampedRebate', 'stampedRebate');

    // Filtro por vendorNumber (igual)
    if (filters.vendorNumber !== undefined) {
        queryBuilder.andWhere('rebate.vendorNumber = :vendorNumber', {
            vendorNumber: filters.vendorNumber
        });
    }

    // Filtro por documentNumber (like - búsqueda parcial)
    if (filters.documentNumber) {
        queryBuilder.andWhere('rebate.documentNumber LIKE :documentNumber', {
            documentNumber: `%${filters.documentNumber}%`
        });
    }

    // Filtro por sapDocument (like - búsqueda parcial)
    if (filters.sapDocument) {
        queryBuilder.andWhere('rebate.sapDocument LIKE :sapDocument', {
            sapDocument: `%${filters.sapDocument}%`
        });
    }

    // Filtro por status
    if (filters.status !== undefined) {
        queryBuilder.andWhere('rebate.status = :status', {
            status: filters.status
        });
    }

    // Filtro por periodId
    if (filters.periodId !== undefined) {
        queryBuilder.andWhere('rebate.periodId = :periodId', {
            periodId: filters.periodId
        });
    }

    // Ordenamiento y paginación
    queryBuilder
        .orderBy('rebate.postingDate', 'DESC')
        .take(filters.limit ?? 20);

    if (filters.page && filters.page > 0) {
        queryBuilder.skip((filters.page) * (filters.limit ?? 20));
    }

    return queryBuilder.getMany();
}

export async function createOne(data: Partial<Rebate>) {
    const entity = repo().create(data);
    return repo().save(entity);
}

export async function updateOne(id: string, patch: Partial<Rebate>) {
    await repo().update({ rebateId: id }, patch);
    return findById(id);
}

export async function deleteOne(id: string) {
    await repo().delete({ rebateId: id });
}
