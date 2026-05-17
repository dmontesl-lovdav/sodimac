// src/services/parameter.service.ts
// STM-1213: Sistema de versionado de parametros
import * as parameterRepo from "@/repositories/parameter.repo.js";
import { CatParameter } from "@/entities/CatParameter.entity.js";
import type { FindOptionsWhere } from "typeorm";
import { datasource } from "@/config/typeorm-datasource.js";

export interface CreateParameterDto {
    idModule: number;
    idType: number;
    name: string;
    description?: string;
    value: string;
    version?: number;
    startDate?: Date;
    endDate?: Date;
    status?: number;
    createdBy?: number;
}

export interface UpdateParameterDto {
    idModule?: number;
    idType?: number;
    description?: string;
    startDate?: Date;
    endDate?: Date;
    updatedBy?: number;
    // NOTA: 'value' NO se permite en PATCH, debe usar createVersion
}

export interface CreateVersionDto {
    value: string;
    startDate?: Date;
    endDate?: Date;
    changeReason?: string;
    createdBy?: number;
}

export interface UpdateStatusDto {
    status: number; // 1=Activo, 0=Inactivo
    updatedBy?: number;
}

export interface ParameterFilter {
    idModule?: number;
    idType?: number;
    name?: string;
    status?: number;
    includeHistory?: boolean;
}

export interface ListOptions {
    page?: number;
    limit?: number;
    sort?: 'idParameter' | 'name' | 'createdAt' | 'updatedAt';
    order?: 'asc' | 'desc';
}

/**
 * Listar parametros con filtros opcionales
 * Por defecto solo muestra la ultima version activa de cada parametro
 */
export async function list(filter?: ParameterFilter, options?: ListOptions): Promise<{
    data: CatParameter[];
    total: number;
    page: number;
    limit: number;
    totalPages: number;
}> {
    const where: FindOptionsWhere<CatParameter> = {};

    if (filter?.idModule) where.idModule = filter.idModule;
    if (filter?.idType) where.idType = filter.idType;
    if (filter?.status !== undefined) where.status = filter.status;

    // Si includeHistory=true, mostrar todas las versiones
    if (filter?.includeHistory) {
        if (filter?.name) where.name = filter.name;
        return parameterRepo.findPaginated(where, {
            page: options?.page ?? 1,
            limit: options?.limit ?? 10,
            sort: options?.sort ?? 'idParameter',
            order: (options?.order?.toUpperCase() as 'ASC' | 'DESC') ?? 'DESC'
        });
    }

    // Por defecto: solo ultima version de cada parametro
    const data = await parameterRepo.findLatestActiveVersions({
        ...where,
        name: filter?.name as any // Para busqueda parcial
    });

    return {
        data,
        total: data.length,
        page: 1,
        limit: data.length,
        totalPages: 1
    };
}

/**
 * Obtener parametro por ID
 */
export async function getById(id: number): Promise<CatParameter | null> {
    return parameterRepo.findById(id);
}

/**
 * Obtener parametro por nombre (ultima version)
 */
export async function getByName(name: string): Promise<CatParameter | null> {
    return parameterRepo.findLatestVersionByName(name);
}

/**
 * Obtener todas las versiones de un parametro
 */
export async function getVersions(id: number): Promise<CatParameter[]> {
    const param = await parameterRepo.findById(id);
    if (!param) {
        throw { status: 404, message: `Parameter with id ${id} not found` };
    }
    return parameterRepo.findAllVersionsByName(param.name);
}

/**
 * Crear nuevo parametro (version inicial 1.0)
 */
export async function create(data: CreateParameterDto): Promise<CatParameter> {
    // Verificar unicidad de nombre
    const existing = await parameterRepo.findLatestVersionByName(data.name);
    if (existing) {
        throw { status: 409, message: `Ya existe un parametro con el nombre '${data.name}'` };
    }

    return parameterRepo.createOne({
        ...data,
        version: 1.0,
        status: 1,
        startDate: data.startDate ?? new Date()
    });
}

/**
 * Actualizar metadatos del parametro (SIN crear nueva version)
 * NO permite modificar 'value' - para eso usar createVersion
 */
export async function update(id: number, data: UpdateParameterDto): Promise<CatParameter | null> {
    const existing = await parameterRepo.findById(id);
    if (!existing) {
        throw { status: 404, message: `Parameter with id ${id} not found` };
    }

    // Verificar que sea la ultima version
    const isLatest = await parameterRepo.isLatestVersion(id);
    if (!isLatest) {
        throw { status: 403, message: 'Solo se puede editar la ultima version del parametro' };
    }

    // Actualizar solo campos permitidos (NO value)
    const patch: Partial<CatParameter> = { updatedAt: new Date() };
    if (data.idModule !== undefined) patch.idModule = data.idModule;
    if (data.idType !== undefined) patch.idType = data.idType;
    if (data.description !== undefined) patch.description = data.description;
    if (data.startDate !== undefined) patch.startDate = data.startDate;
    if (data.endDate !== undefined) patch.endDate = data.endDate;
    if (data.updatedBy !== undefined) patch.updatedBy = data.updatedBy;

    return parameterRepo.updateOne(id, patch);
}

/**
 * Crear nueva version del parametro (cuando cambia el valor)
 * Operacion transaccional: cierra version anterior + crea nueva
 */
export async function createVersion(id: number, data: CreateVersionDto): Promise<CatParameter> {
    const existing = await parameterRepo.findById(id);
    if (!existing) {
        throw { status: 404, message: `Parameter with id ${id} not found` };
    }

    // Verificar que sea la ultima version
    const isLatest = await parameterRepo.isLatestVersion(id);
    if (!isLatest) {
        throw { status: 403, message: 'Solo se puede versionar la ultima version del parametro' };
    }

    // Calcular siguiente version
    const nextVersion = calculateNextVersion(existing.version);

    // Verificar que no exista la nueva version
    const duplicate = await parameterRepo.findByNameAndVersion(existing.name, nextVersion);
    if (duplicate) {
        throw { status: 409, message: `Ya existe version ${nextVersion} para este parametro` };
    }

    // Ejecutar transaccion
    const queryRunner = datasource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        // 1. Cerrar version actual
        const closeUpdate: Record<string, unknown> = {
            status: 0,
            endDate: new Date(),
            updatedAt: new Date()
        };
        if (data.createdBy !== undefined) closeUpdate.updatedBy = data.createdBy;

        await queryRunner.manager.update(CatParameter, { idParameter: id }, closeUpdate);

        // 2. Crear nueva version
        const newParamData: Partial<CatParameter> = {
            idModule: existing.idModule,
            idType: existing.idType,
            name: existing.name,
            value: data.value,
            version: nextVersion,
            status: 1,
            startDate: data.startDate ?? new Date(),
            createdAt: new Date()
        };
        if (existing.description !== undefined) newParamData.description = existing.description;
        if (data.endDate !== undefined) newParamData.endDate = data.endDate;
        if (data.createdBy !== undefined) newParamData.createdBy = data.createdBy;

        const newParam = queryRunner.manager.create(CatParameter, newParamData);

        const saved = await queryRunner.manager.save(newParam);

        await queryRunner.commitTransaction();

        return saved;
    } catch (error) {
        await queryRunner.rollbackTransaction();
        throw error;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Calcular siguiente numero de version
 * 1.0 -> 1.1 -> ... -> 1.9 -> 2.0 -> 2.1 ...
 */
export function calculateNextVersion(currentVersion: number): number {
    const intPart = Math.floor(currentVersion);
    const decPart = Math.round((currentVersion - intPart) * 10);

    if (decPart >= 9) {
        return intPart + 1; // 1.9 -> 2.0
    }
    return Number((intPart + (decPart + 1) / 10).toFixed(1)); // 1.1 -> 1.2
}

/**
 * Cambiar estatus del parametro (solo ultima version)
 */
export async function updateStatus(id: number, data: UpdateStatusDto): Promise<CatParameter | null> {
    const existing = await parameterRepo.findById(id);
    if (!existing) {
        throw { status: 404, message: `Parameter with id ${id} not found` };
    }

    // Verificar que sea la ultima version
    const isLatest = await parameterRepo.isLatestVersion(id);
    if (!isLatest) {
        throw { status: 403, message: 'Solo se puede cambiar el estatus de la ultima version' };
    }

    // Validar estatus
    if (data.status !== 0 && data.status !== 1) {
        throw { status: 400, message: 'Estatus invalido. Debe ser 0 (Inactivo) o 1 (Activo)' };
    }

    const patch: Partial<CatParameter> = {
        status: data.status,
        updatedAt: new Date()
    };
    if (data.updatedBy !== undefined) patch.updatedBy = data.updatedBy;

    return parameterRepo.updateOne(id, patch);
}

/**
 * Eliminar parametro (soft delete - cambiar status a 0)
 */
export async function remove(id: number, updatedBy?: number): Promise<void> {
    const existing = await parameterRepo.findById(id);
    if (!existing) {
        throw { status: 404, message: `Parameter with id ${id} not found` };
    }

    const patch: Partial<CatParameter> = {
        status: 0,
        updatedAt: new Date()
    };
    if (updatedBy !== undefined) patch.updatedBy = updatedBy;

    await parameterRepo.updateOne(id, patch);
}

/**
 * Obtener parametros por modulo
 */
export async function getByModule(idModule: number): Promise<CatParameter[]> {
    return parameterRepo.findByModule(idModule);
}

/**
 * Obtener parametros activos
 */
export async function getActive(): Promise<CatParameter[]> {
    return parameterRepo.findActive();
}
