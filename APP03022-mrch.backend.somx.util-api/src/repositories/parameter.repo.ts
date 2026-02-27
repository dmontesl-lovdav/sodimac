// src/repositories/parameter.repo.ts
import { datasource } from "@/config/typeorm-datasource.js";
import { CatParameter } from "@/entities/CatParameter.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () => datasource.getRepository(CatParameter);

/**
 * Crear un nuevo parametro
 */
export async function createOne(data: Partial<CatParameter>): Promise<CatParameter> {
    const entity = repo().create(data);
    return repo().save(entity);
}

/**
 * Buscar parametro por ID
 */
export async function findById(idParameter: number): Promise<CatParameter | null> {
    return repo().findOneBy({ idParameter });
}

/**
 * Buscar parametro por nombre
 */
export async function findByName(name: string): Promise<CatParameter | null> {
    return repo().findOneBy({ name });
}

/**
 * Buscar parametro por nombre y version
 */
export async function findByNameAndVersion(name: string, version: number): Promise<CatParameter | null> {
    return repo().findOneBy({ name, version });
}

/**
 * Buscar parametros con filtros
 */
export async function findAll(filter?: FindOptionsWhere<CatParameter>): Promise<CatParameter[]> {
    const options: { order: Record<string, 'ASC' | 'DESC'>; where?: FindOptionsWhere<CatParameter> } = {
        order: { name: 'ASC', version: 'DESC' }
    };
    if (filter) options.where = filter;
    return repo().find(options);
}

/**
 * Buscar parametros por modulo
 */
export async function findByModule(idModule: number): Promise<CatParameter[]> {
    return repo().find({
        where: { idModule },
        order: { name: 'ASC', version: 'DESC' }
    });
}

/**
 * Buscar parametros activos
 */
export async function findActive(): Promise<CatParameter[]> {
    return repo().find({
        where: { status: 1 },
        order: { name: 'ASC', version: 'DESC' }
    });
}

/**
 * Actualizar parametro
 */
export async function updateOne(idParameter: number, patch: Partial<CatParameter>): Promise<CatParameter | null> {
    await repo().update({ idParameter }, patch);
    return findById(idParameter);
}

/**
 * Eliminar parametro
 */
export async function deleteOne(idParameter: number): Promise<void> {
    await repo().delete({ idParameter });
}

/**
 * Contar parametros
 */
export async function count(filter?: FindOptionsWhere<CatParameter>): Promise<number> {
    const options: { where?: FindOptionsWhere<CatParameter> } = {};
    if (filter) options.where = filter;
    return repo().count(options);
}

/**
 * Buscar la ultima version de un parametro por nombre
 */
export async function findLatestVersionByName(name: string): Promise<CatParameter | null> {
    return repo().findOne({
        where: { name },
        order: { version: 'DESC' }
    });
}

/**
 * Buscar todas las versiones de un parametro por nombre
 */
export async function findAllVersionsByName(name: string): Promise<CatParameter[]> {
    return repo().find({
        where: { name },
        order: { version: 'DESC' }
    });
}

/**
 * Verificar si un parametro es la ultima version
 */
export async function isLatestVersion(idParameter: number): Promise<boolean> {
    const param = await findById(idParameter);
    if (!param) return false;

    const latest = await findLatestVersionByName(param.name);
    return latest?.idParameter === idParameter;
}

/**
 * Obtener parametros con paginacion
 */
export interface PaginationOptions {
    page: number;
    limit: number;
    sort: 'idParameter' | 'name' | 'createdAt' | 'updatedAt';
    order: 'ASC' | 'DESC';
}

export interface PaginatedResult<T> {
    data: T[];
    total: number;
    page: number;
    limit: number;
    totalPages: number;
}

export async function findPaginated(
    filter?: FindOptionsWhere<CatParameter>,
    options?: PaginationOptions
): Promise<PaginatedResult<CatParameter>> {
    const page = options?.page ?? 1;
    const limit = options?.limit ?? 10;
    const sort = options?.sort ?? 'idParameter';
    const order = options?.order ?? 'DESC';

    const findOptions: {
        order: Record<string, 'ASC' | 'DESC'>;
        skip: number;
        take: number;
        where?: FindOptionsWhere<CatParameter>;
    } = {
        order: { [sort]: order },
        skip: (page - 1) * limit,
        take: limit
    };
    if (filter) findOptions.where = filter;

    const [data, total] = await repo().findAndCount(findOptions);

    return {
        data,
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
    };
}

/**
 * Buscar solo la ultima version activa de cada parametro (para grid)
 */
export async function findLatestActiveVersions(filter?: FindOptionsWhere<CatParameter>): Promise<CatParameter[]> {
    // Subconsulta para obtener la ultima version de cada parametro
    const qb = repo().createQueryBuilder('p1');

    // Subconsulta correcta usando subQuery builder
    qb.where(qb2 => {
        const subQuery = qb2
            .subQuery()
            .select('MAX(p2.version)')
            .from(CatParameter, 'p2')
            .where('p2.name = p1.name')
            .getQuery();
        return 'p1.version = ' + subQuery;
    });

    if (filter?.status !== undefined) {
        qb.andWhere('p1.status = :status', { status: filter.status });
    }
    if (filter?.idModule) {
        qb.andWhere('p1.id_module = :idModule', { idModule: filter.idModule });
    }
    if (filter?.idType) {
        qb.andWhere('p1.id_type = :idType', { idType: filter.idType });
    }
    if (filter?.name) {
        qb.andWhere('LOWER(p1.name) LIKE LOWER(:name)', { name: `%${filter.name}%` });
    }

    qb.orderBy('p1.name', 'ASC');

    return qb.getMany();
}
