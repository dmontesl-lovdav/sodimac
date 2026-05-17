// src/controllers/parameter.controller.ts
// STM-1213: Sistema de versionado de parametros
import type { Request, Response, NextFunction } from 'express';
import * as parameterService from '@/services/parameter.service.js';

/**
 * GET /api/parameters
 * Listar parametros con filtros opcionales y paginacion
 */
export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        const { idModule, idType, name, status, includeHistory, page, limit, sort, order } = req.query;

        const filter: parameterService.ParameterFilter = {};
        if (idModule) filter.idModule = Number(idModule);
        if (idType) filter.idType = Number(idType);
        if (name) filter.name = name as string;
        if (status !== undefined) filter.status = Number(status);
        filter.includeHistory = includeHistory === 'true';

        const options: parameterService.ListOptions = {
            page: page ? Number(page) : 1,
            limit: limit ? Number(limit) : 10,
            sort: (sort as 'idParameter' | 'name' | 'createdAt' | 'updatedAt') ?? 'idParameter',
            order: (order as 'asc' | 'desc') ?? 'desc'
        };

        const result = await parameterService.list(filter, options);

        res.json({
            success: true,
            ...result
        });
    } catch (err) {
        next(err);
    }
}

/**
 * GET /api/parameters/:id
 * Obtener parametro por ID
 */
export async function getById(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);

        if (isNaN(id)) {
            return res.status(400).json({
                success: false,
                message: 'Invalid parameter ID'
            });
        }

        const parameter = await parameterService.getById(id);

        if (!parameter) {
            return res.status(404).json({
                success: false,
                message: `Parameter with id ${id} not found`
            });
        }

        res.json({
            success: true,
            data: parameter
        });
    } catch (err) {
        next(err);
    }
}

/**
 * POST /api/parameters
 * Crear nuevo parametro
 */
export async function create(req: Request, res: Response, next: NextFunction) {
    try {
        const { idModule, idType, name, description, value, version, startDate, endDate, status } = req.body;

        // Validacion basica
        if (!idModule || !idType || !name || !value) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields: idModule, idType, name, value'
            });
        }

        const createDto: parameterService.CreateParameterDto = {
            idModule,
            idType,
            name,
            value
        };
        if (description) createDto.description = description;
        if (version) createDto.version = version;
        if (startDate) createDto.startDate = new Date(startDate);
        if (endDate) createDto.endDate = new Date(endDate);
        if (status !== undefined) createDto.status = status;
        if (req.body.createdBy) createDto.createdBy = req.body.createdBy;

        const parameter = await parameterService.create(createDto);

        res.status(201).json({
            success: true,
            data: parameter,
            message: 'Parameter created successfully'
        });
    } catch (err) {
        next(err);
    }
}

/**
 * PATCH /api/parameters/:id
 * Actualizar metadatos del parametro (NO permite cambiar 'value')
 * Para cambiar value usar POST /api/parameters/:id/versions
 */
export async function update(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);

        if (isNaN(id)) {
            return res.status(400).json({
                success: false,
                message: 'Invalid parameter ID'
            });
        }

        // Validar que no se intente cambiar 'value' via PATCH
        if (req.body.value !== undefined) {
            return res.status(400).json({
                success: false,
                message: "No se permite modificar 'value' via PATCH. Use POST /api/parameters/:id/versions para crear nueva version"
            });
        }

        const { idModule, idType, description, startDate, endDate } = req.body;

        const updateDto: parameterService.UpdateParameterDto = {};
        if (idModule !== undefined) updateDto.idModule = idModule;
        if (idType !== undefined) updateDto.idType = idType;
        if (description !== undefined) updateDto.description = description;
        if (startDate) updateDto.startDate = new Date(startDate);
        if (endDate) updateDto.endDate = new Date(endDate);
        if (req.body.updatedBy) updateDto.updatedBy = req.body.updatedBy;

        const parameter = await parameterService.update(id, updateDto);

        res.json({
            success: true,
            data: parameter,
            message: 'Parameter updated successfully'
        });
    } catch (err) {
        next(err);
    }
}

/**
 * GET /api/parameters/:id/versions
 * Obtener historial de versiones de un parametro
 */
export async function getVersions(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);

        if (isNaN(id)) {
            return res.status(400).json({
                success: false,
                message: 'Invalid parameter ID'
            });
        }

        const versions = await parameterService.getVersions(id);

        res.json({
            success: true,
            data: versions,
            count: versions.length
        });
    } catch (err) {
        next(err);
    }
}

/**
 * POST /api/parameters/:id/versions
 * Crear nueva version del parametro (cuando cambia el valor)
 * Cierra la version actual y crea una nueva en transaccion atomica
 */
export async function createVersion(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);

        if (isNaN(id)) {
            return res.status(400).json({
                success: false,
                message: 'Invalid parameter ID'
            });
        }

        const { value, startDate, endDate, changeReason } = req.body;

        if (!value) {
            return res.status(400).json({
                success: false,
                message: "El campo 'value' es requerido para crear nueva version"
            });
        }

        const versionDto: parameterService.CreateVersionDto = { value };
        if (startDate) versionDto.startDate = new Date(startDate);
        if (endDate) versionDto.endDate = new Date(endDate);
        if (changeReason) versionDto.changeReason = changeReason;
        if (req.body.createdBy) versionDto.createdBy = req.body.createdBy;

        const newVersion = await parameterService.createVersion(id, versionDto);

        res.status(201).json({
            success: true,
            data: newVersion,
            message: `Nueva version ${newVersion.version} creada exitosamente`
        });
    } catch (err) {
        next(err);
    }
}

/**
 * PATCH /api/parameters/:id/status
 * Cambiar estatus del parametro (solo ultima version)
 */
export async function updateStatus(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);

        if (isNaN(id)) {
            return res.status(400).json({
                success: false,
                message: 'Invalid parameter ID'
            });
        }

        const { status } = req.body;

        if (status === undefined) {
            return res.status(400).json({
                success: false,
                message: "El campo 'status' es requerido (1=Activo, 0=Inactivo)"
            });
        }

        const statusDto: parameterService.UpdateStatusDto = { status: Number(status) };
        if (req.body.updatedBy) statusDto.updatedBy = req.body.updatedBy;

        const parameter = await parameterService.updateStatus(id, statusDto);

        res.json({
            success: true,
            data: parameter,
            message: `Estatus actualizado a ${status === 1 ? 'Activo' : 'Inactivo'}`
        });
    } catch (err) {
        next(err);
    }
}

/**
 * DELETE /api/parameters/:id
 * Eliminar parametro
 */
export async function remove(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);

        if (isNaN(id)) {
            return res.status(400).json({
                success: false,
                message: 'Invalid parameter ID'
            });
        }

        await parameterService.remove(id);

        res.json({
            success: true,
            message: `Parameter with id ${id} deleted successfully`
        });
    } catch (err) {
        next(err);
    }
}
