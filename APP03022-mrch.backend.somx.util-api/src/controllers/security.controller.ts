import type { NextFunction, Request, Response } from 'express';
import * as securityService from '@/services/security.service.js';

function parseActorId(req: Request): string {
    const headerValue = req.header('X-User-Id');
    const bodyValue = req.body?.createdBy ?? req.body?.updatedBy;
    const actorId = String(bodyValue ?? headerValue ?? 'SYSTEM').trim();
    return actorId || 'SYSTEM';
}

function parseSearchFilters(req: Request): securityService.SecurityFilters {
    const filters: securityService.SecurityFilters = {
        startDate: String(req.query.startDate ?? ''),
        endDate: String(req.query.endDate ?? ''),
    };
    if (req.query.entityId) filters.entityId = String(req.query.entityId);
    if (req.query.entityName) filters.entityName = String(req.query.entityName);
    if (req.query.status !== undefined && req.query.status !== '') {
        filters.status = Number(req.query.status);
    }
    if (req.query.langId !== undefined && req.query.langId !== '') {
        filters.langId = Number(req.query.langId);
    }
    return filters;
}

function parseLangId(req: Request): number | undefined {
    const raw = req.query.langId;
    if (raw === undefined || raw === '') return undefined;
    const parsed = Number(raw);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export async function searchProfileUsers(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.searchProfileUsers(parseSearchFilters(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function getProfileUserAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const data = await securityService.getProfileUserAssignment(id);
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function saveProfileUserAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await securityService.saveProfileUserAssignment(id, {
            selectedIds: Array.isArray(req.body?.selectedIds) ? req.body.selectedIds : [],
            actorId: parseActorId(req),
        });
        res.json({ success: true, message: 'Relacion perfil-usuario actualizada correctamente' });
    } catch (error) {
        next(error);
    }
}

export async function searchRoleUsers(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.searchRoleUsers(parseSearchFilters(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function getRoleUserAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const data = await securityService.getRoleUserAssignment(id);
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function saveRoleUserAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await securityService.saveRoleUserAssignment(id, {
            selectedIds: Array.isArray(req.body?.selectedIds) ? req.body.selectedIds : [],
            actorId: parseActorId(req),
        });
        res.json({ success: true, message: 'Relacion rol-usuario actualizada correctamente' });
    } catch (error) {
        next(error);
    }
}

export async function searchRolePermissions(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.searchRolePermissions(parseSearchFilters(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function getRolePermissionAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const data = await securityService.getRolePermissionAssignment(id, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function saveRolePermissionAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await securityService.saveRolePermissionAssignment(id, {
            selectedIds: Array.isArray(req.body?.selectedIds) ? req.body.selectedIds : [],
            actorId: parseActorId(req),
        });
        res.json({ success: true, message: 'Relacion rol-permiso actualizada correctamente' });
    } catch (error) {
        next(error);
    }
}

export async function searchUserAttributes(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.searchUserAttributes(parseSearchFilters(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function listUserAttributes(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const page = req.query.page ? Number(req.query.page) : 1;
        const limit = req.query.limit ? Number(req.query.limit) : 10;
        const data = await securityService.listUserAttributes(id, page, limit, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function createUserAttribute(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await securityService.createUserAttribute(id, {
            attributeTypeId: Number(req.body?.attributeTypeId),
            attributeValueId: Number(req.body?.attributeValueId),
            actorId: parseActorId(req),
        });
        res.status(201).json({ success: true, message: 'Atributo registrado correctamente' });
    } catch (error) {
        next(error);
    }
}

export async function removeUserAttribute(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const attributeId = Number(req.params.attributeId);
        await securityService.removeUserAttribute(id, attributeId, parseActorId(req));
        res.json({ success: true, message: 'Atributo eliminado correctamente' });
    } catch (error) {
        next(error);
    }
}

export async function listAttributeTypes(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.listAttributeTypes(parseLangId(req));
        res.json({ success: true, data, count: data.length });
    } catch (error) {
        next(error);
    }
}

export async function listAttributeValuesByType(req: Request, res: Response, next: NextFunction) {
    try {
        const attributeTypeId = Number(req.query.attributeTypeId);
        const data = await securityService.listAttributeValuesByType(attributeTypeId, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function searchProfileModules(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.searchProfileModules(parseSearchFilters(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function getProfileModuleAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const data = await securityService.getProfileModuleAssignment(id, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function saveProfileModuleAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await securityService.saveProfileModuleAssignment(id, {
            selectedIds: Array.isArray(req.body?.selectedIds) ? req.body.selectedIds : [],
            actorId: parseActorId(req),
        });
        res.json({ success: true, message: 'Relacion perfil-modulo actualizada correctamente' });
    } catch (error) {
        next(error);
    }
}

export async function searchProfileModuleProcesses(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.searchProfileModuleProcesses(parseSearchFilters(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function getProfileModuleProcessAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const data = await securityService.getProfileModuleProcessAssignment(id, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function saveProfileModuleProcessAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await securityService.saveProfileModuleProcessAssignment(id, {
            selectedIds: Array.isArray(req.body?.selectedIds) ? req.body.selectedIds : [],
            actorId: parseActorId(req),
        });
        res.json({ success: true, message: 'Relacion perfil-aplicativo evento actualizada correctamente' });
    } catch (error) {
        next(error);
    }
}

export async function searchApplicationEvents(req: Request, res: Response, next: NextFunction) {
    try {
        const data = await securityService.searchApplicationEvents(parseSearchFilters(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function getModuleProcessAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        const data = await securityService.getModuleProcessAssignment(id, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function saveModuleProcessAssignment(req: Request, res: Response, next: NextFunction) {
    try {
        const id = Number(req.params.id);
        await securityService.saveModuleProcessAssignment(id, {
            selectedIds: Array.isArray(req.body?.selectedIds) ? req.body.selectedIds : [],
            actorId: parseActorId(req),
        });
        res.json({ success: true, message: 'Relacion modulo-proceso actualizada correctamente' });
    } catch (error) {
        next(error);
    }
}

/** GET /api/security/user-details/:userKey — detalle por user_data (preferred_username, sub, email o id). */
export async function getUserAttributesByKey(req: Request, res: Response, next: NextFunction) {
    try {
        const userKey = decodeURIComponent(String(req.params.userKey ?? ''));
        const data = await securityService.getUserAttributesByKey(userKey, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

export async function getUserDetailsByCatalogKey(req: Request, res: Response, next: NextFunction) {
    try {
        const userKey = decodeURIComponent(String(req.params.userKey ?? ''));
        const idPerfilRaw = req.query.idPerfil;
        const idProfile =
            idPerfilRaw === undefined || String(idPerfilRaw).trim() === ''
                ? undefined
                : Number(idPerfilRaw);
        const data = await securityService.getUserDetailsByCatalogKey(userKey, idProfile, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}

/** DELETE /api/security/user-details/cache — invalidacion explicita de cache del contexto */
export async function invalidateUserDetailsCache(req: Request, res: Response, next: NextFunction) {
    try {
        const userKeyRaw = req.query.userKey;
        const userKey = userKeyRaw == null || String(userKeyRaw).trim() === '' ? undefined : String(userKeyRaw);
        const idPerfilRaw = req.query.idPerfil;
        const idProfile =
            idPerfilRaw == null || String(idPerfilRaw).trim() === '' ? undefined : Number(idPerfilRaw);
        const data = await securityService.invalidateUserDetailsCache(userKey, idProfile, parseLangId(req));
        res.json({ success: true, data });
    } catch (error) {
        next(error);
    }
}
