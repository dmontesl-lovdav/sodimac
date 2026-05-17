import * as securityRepo from '@/repositories/security.repo.js';

export type { SecurityUserDetailsResponse, SecurityUserRef } from '@/repositories/security.repo.js';

interface AccessContextEvent {
    key: string;
    name: string;
}

interface AccessContextApplication {
    key: string;
    name: string;
    events: AccessContextEvent[];
}

interface AccessContextProfileRef {
    key: string;
    name: string;
}

interface AccessContextRoleRef {
    key: string;
    name: string;
}

interface AccessContextPermissionRef {
    key: string;
    name: string;
    rol: AccessContextRoleRef;
}

interface AccessContextProviderRef {
    key: string;
    name: string;
    rol: AccessContextRoleRef;
}

interface AccessContextAttributeRef {
    type: {
        key: string;
        name: string;
    };
    value: {
        key: string;
        name: string;
    } | null;
}

export interface AccessContextResponseV1 {
    version: 'v1';
    user: {
        key: string;
        name: string;
        email: string;
    };
    profiles: AccessContextProfileRef[];
    apps: AccessContextApplication[];
    roles: AccessContextRoleRef[];
    permissions: AccessContextPermissionRef[];
    providers: AccessContextProviderRef[];
    attributes: AccessContextAttributeRef[];
}

export interface SecurityFilters {
    startDate: string;
    endDate: string;
    entityId?: string;
    entityName?: string;
    status?: number;
    langId?: number;
}

export interface AssignmentPayload {
    selectedIds: number[];
    actorId: string;
}

export interface CreateUserAttributePayload {
    attributeTypeId: number;
    attributeValueId: number;
    actorId: string;
}

interface AccessContextCacheEntry {
    expiresAt: number;
    data: AccessContextResponseV1;
}

const ACCESS_CONTEXT_CACHE = new Map<string, AccessContextCacheEntry>();

function accessContextCacheTtlMs(): number {
    const ttlSec = Number(process.env.SECURITY_CONTEXT_CACHE_TTL_SEC ?? 300);
    if (!Number.isFinite(ttlSec) || ttlSec <= 0) return 300_000;
    return ttlSec * 1000;
}

function accessContextCacheKey(userKey: string, idProfile?: number, langId?: number): string {
    return `${userKey}::${idProfile ?? 'ALL'}::${langId ?? 1}`;
}

function ensureDateRange(filters: SecurityFilters) {
    if (!filters.startDate || !filters.endDate) {
        throw { status: 400, message: 'Los campos startDate y endDate son obligatorios' };
    }

    if (filters.endDate < filters.startDate) {
        throw { status: 400, message: 'La fecha final debe ser mayor o igual a la fecha inicial.', code: 'MSG_VAL_01' };
    }
}

function withDefaultActiveStatus(filters: SecurityFilters): SecurityFilters {
    return {
        ...filters,
        status: filters.status ?? 1,
    };
}

function normalizeSelectedIds(selectedIds: number[]): number[] {
    return Array.from(
        new Set(
            selectedIds
                .map((value) => Number(value))
                .filter((value) => Number.isInteger(value) && value > 0),
        ),
    );
}

export async function searchProfileUsers(filters: SecurityFilters) {
    ensureDateRange(filters);
    const items = await securityRepo.listProfileUsers(withDefaultActiveStatus(filters));
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function getProfileUserAssignment(idProfile: number) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw { status: 404, message: `No existe el perfil ${idProfile}` };
    }
    return securityRepo.findProfileAssignment(idProfile);
}

export async function saveProfileUserAssignment(idProfile: number, payload: AssignmentPayload) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw { status: 404, message: `No existe el perfil ${idProfile}` };
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const users = await securityRepo.findActiveUsersByIds(selectedIds);

    if (selectedIds.length !== users.length) {
        throw { status: 400, message: 'Uno o mas usuarios no existen o estan inactivos' };
    }

    await securityRepo.syncProfileUsers(idProfile, selectedIds, payload.actorId);
}

export async function searchRoleUsers(filters: SecurityFilters) {
    ensureDateRange(filters);
    const items = await securityRepo.listRoleUsers(withDefaultActiveStatus(filters));
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function getRoleUserAssignment(idRole: number) {
    const role = await securityRepo.findActiveRoleById(idRole);
    if (!role) {
        throw { status: 404, message: `No existe el rol ${idRole}` };
    }
    return securityRepo.findRoleUserAssignment(idRole);
}

export async function saveRoleUserAssignment(idRole: number, payload: AssignmentPayload) {
    const role = await securityRepo.findActiveRoleById(idRole);
    if (!role) {
        throw { status: 404, message: `No existe el rol ${idRole}` };
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const users = await securityRepo.findActiveUsersByIds(selectedIds);

    if (selectedIds.length !== users.length) {
        throw { status: 400, message: 'Uno o mas usuarios no existen o estan inactivos' };
    }

    await securityRepo.syncRoleUsers(idRole, selectedIds, payload.actorId);
}

export async function searchRolePermissions(filters: SecurityFilters) {
    ensureDateRange(filters);
    const items = await securityRepo.listRolePermissions(withDefaultActiveStatus(filters));
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function getRolePermissionAssignment(idRole: number, langId?: number) {
    const role = await securityRepo.findActiveRoleById(idRole);
    if (!role) {
        throw { status: 404, message: `No existe el rol ${idRole}` };
    }
    return securityRepo.findRolePermissionAssignment(idRole, langId);
}

export async function saveRolePermissionAssignment(idRole: number, payload: AssignmentPayload) {
    const role = await securityRepo.findActiveRoleById(idRole);
    if (!role) {
        throw { status: 404, message: `No existe el rol ${idRole}` };
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const permissions = await securityRepo.findActivePermissionsByIds(selectedIds);

    if (selectedIds.length !== permissions.length) {
        throw { status: 400, message: 'Uno o mas permisos no existen o estan inactivos' };
    }

    await securityRepo.syncRolePermissions(idRole, selectedIds, payload.actorId);
}

export async function searchUserAttributes(filters: SecurityFilters) {
    ensureDateRange(filters);
    const items = await securityRepo.listUsersWithAttributes(withDefaultActiveStatus(filters));
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function listUserAttributes(idUser: number, page = 1, limit = 10, langId?: number) {
    const user = await securityRepo.findActiveUserById(idUser);
    if (!user) {
        throw { status: 404, message: `No existe el usuario ${idUser}` };
    }

    return securityRepo.listUserAttributes(idUser, page, limit, langId);
}

export async function createUserAttribute(idUser: number, payload: CreateUserAttributePayload) {
    const user = await securityRepo.findActiveUserById(idUser);
    if (!user) {
        throw { status: 404, message: `No existe el usuario ${idUser}` };
    }

    const attributeType = await securityRepo.findActiveAttributeTypeById(payload.attributeTypeId);
    if (!attributeType) {
        throw { status: 404, message: `No existe el tipo de atributo ${payload.attributeTypeId}` };
    }

    if (!Number.isInteger(payload.attributeValueId) || Number(payload.attributeValueId) <= 0) {
        throw { status: 400, message: 'attributeValueId debe ser un entero positivo' };
    }

    const attributeValue = await securityRepo.findActiveCatalogDetailById(payload.attributeValueId);
    if (!attributeValue) {
        throw { status: 404, message: `No existe el valor de atributo ${payload.attributeValueId}` };
    }

    await securityRepo.createUserAttributes(idUser, payload.attributeTypeId, payload.attributeValueId, payload.actorId);
}

export async function removeUserAttribute(idUser: number, attributeId: number, actorId: string) {
    const user = await securityRepo.findActiveUserById(idUser);
    if (!user) {
        throw { status: 404, message: `No existe el usuario ${idUser}` };
    }

    const attributeRow = await securityRepo.findActiveUserAttributeForUser(idUser, attributeId);
    if (!attributeRow) {
        throw { status: 404, message: `No existe el atributo ${attributeId} para el usuario ${idUser}` };
    }

    await securityRepo.deleteUserAttribute(idUser, attributeId, String(actorId));
}

export async function listAttributeTypes(langId?: number) {
    return securityRepo.getAttributeTypes(langId);
}

export async function listAttributeValuesByType(idAttributeType: number, langId?: number) {
    const type = await securityRepo.findActiveAttributeTypeById(idAttributeType);
    if (!type) {
        throw { status: 404, message: `No existe el tipo de atributo ${idAttributeType}` };
    }

    const items = await securityRepo.getAttributeValuesByType(idAttributeType, langId);
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length
            ? undefined
            : 'No existe configuracion de valores para el tipo de atributo seleccionado.',
    };
}

export async function searchProfileModules(filters: SecurityFilters) {
    ensureDateRange(filters);
    const items = await securityRepo.listProfileModules(withDefaultActiveStatus(filters));
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function getProfileModuleAssignment(idProfile: number, langId?: number) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw { status: 404, message: `No existe el perfil ${idProfile}` };
    }
    return securityRepo.findProfileModuleAssignment(idProfile, langId);
}

export async function saveProfileModuleAssignment(idProfile: number, payload: AssignmentPayload) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw { status: 404, message: `No existe el perfil ${idProfile}` };
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const modules = await securityRepo.findModulesByIds(selectedIds);

    if (selectedIds.length !== modules.length) {
        throw { status: 400, message: 'Uno o mas modulos no existen' };
    }

    await securityRepo.syncProfileModules(idProfile, selectedIds, payload.actorId);
}

export async function searchProfileModuleProcesses(filters: SecurityFilters) {
    ensureDateRange(filters);
    const items = await securityRepo.listProfileModuleProcesses(withDefaultActiveStatus(filters));
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function getProfileModuleProcessAssignment(idProfile: number, langId?: number) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw { status: 404, message: `No existe el perfil ${idProfile}` };
    }
    return securityRepo.findProfileModuleProcessAssignment(idProfile, langId);
}

export async function saveProfileModuleProcessAssignment(idProfile: number, payload: AssignmentPayload) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw { status: 404, message: `No existe el perfil ${idProfile}` };
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const links = await securityRepo.findActiveModuleProcessesByIds(selectedIds);

    if (selectedIds.length !== links.length) {
        throw { status: 400, message: 'Uno o mas aplicativo-evento no existen o estan inactivos' };
    }

    await securityRepo.syncProfileModuleProcesses(idProfile, selectedIds, payload.actorId);
}

export async function searchApplicationEvents(filters: SecurityFilters) {
    ensureDateRange(filters);
    const items = await securityRepo.listApplicationEvents(filters);
    return {
        items,
        warningCode: items.length ? undefined : 'WRN7002',
        warningMessage: items.length ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function getModuleProcessAssignment(idModule: number, langId?: number) {
    const moduleRow = await securityRepo.findModuleById(idModule);
    if (!moduleRow) {
        throw { status: 404, message: `No existe el modulo ${idModule}` };
    }
    return securityRepo.findModuleProcessAssignment(idModule, langId);
}

export async function saveModuleProcessAssignment(idModule: number, payload: AssignmentPayload) {
    const moduleRow = await securityRepo.findModuleById(idModule);
    if (!moduleRow) {
        throw { status: 404, message: `No existe el modulo ${idModule}` };
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const processes = await securityRepo.findProcessesByIds(selectedIds);

    if (selectedIds.length !== processes.length) {
        throw { status: 400, message: 'Uno o mas procesos no existen' };
    }

    await securityRepo.syncModuleProcessesForModule(idModule, selectedIds, payload.actorId);
}

/** Detalle de seguridad por usuario (core_security.user_data): preferred_username, sub, email o id. */
export async function getUserDetailsByCatalogKey(userKey: string, idProfile?: number, langId?: number) {
    const key = String(userKey ?? '').trim();
    if (!key) {
        throw { status: 400, message: 'userKey es obligatorio' };
    }
    if (idProfile !== undefined && (!Number.isInteger(idProfile) || Number(idProfile) <= 0)) {
        throw { status: 400, message: 'idPerfil debe ser entero positivo' };
    }

    const cacheKey = accessContextCacheKey(key, idProfile, langId);
    const now = Date.now();
    const cached = ACCESS_CONTEXT_CACHE.get(cacheKey);
    if (cached && cached.expiresAt > now) {
        return cached.data;
    }

    const data = await securityRepo.getSecurityUserDetailsByCatalogKey(key, langId);
    if (!data) {
        throw { status: 404, message: `No existe usuario activo con la clave indicada '${key}'` };
    }

    const profiles: Array<AccessContextProfileRef & { id: number }> = data.profiles.map((p) => ({
        id: p.id,
        key: p.catalogKey,
        name: p.label,
    }));
    if (profiles.length === 0) {
        throw {
            status: 400,
            code: 'WRN7030',
            message: 'El usuario no tiene configurado un perfil, favor de validar.',
        };
    }

    const selectedProfile = idProfile === undefined ? undefined : profiles.find((p) => p.id === idProfile);
    if (idProfile !== undefined && !selectedProfile) {
        throw {
            status: 400,
            code: 'WRN7030',
            message: 'El usuario no tiene configurado un perfil, favor de validar.',
        };
    }

    const eventsByAppKey = new Map<string, { appName: string; events: Map<string, string> }>();
    for (const row of data.applicationModuleProcesses) {
        if (idProfile !== undefined && row.profile.id !== idProfile) continue;
        const appKey = row.module.catalogKey;
        const appName = row.module.label;
        const eventKey = row.process.catalogKey;
        const eventName = row.process.label;
        if (!eventsByAppKey.has(appKey)) {
            eventsByAppKey.set(appKey, { appName, events: new Map() });
        }
        eventsByAppKey.get(appKey)?.events.set(eventKey, eventName);
    }

    if (eventsByAppKey.size === 0) {
        throw {
            status: 400,
            code: 'WRN7031',
            message: 'El usuario no tiene configurado un aplicativo, favor de validar.',
        };
    }

    const apps: AccessContextApplication[] = [...eventsByAppKey.entries()]
        .map(([appKey, appInfo]) => ({
            key: appKey,
            name: appInfo.appName,
            events: [...appInfo.events.entries()]
                .map(([eventKey, eventName]) => ({
                    key: eventKey,
                    name: eventName,
                }))
                .sort((a, b) => a.name.localeCompare(b.name, 'es')),
        }))
        .sort((a, b) => a.name.localeCompare(b.name, 'es'));

    const roles: AccessContextRoleRef[] = [...new Map(
        data.roles.map((r) => [r.catalogKey, { key: r.catalogKey, name: r.label }]),
    ).values()].sort((a, b) => a.name.localeCompare(b.name, 'es'));

    const permissions: AccessContextPermissionRef[] = data.permissions
        .map((p) => ({
            key: p.permission.catalogKey,
            name: p.permission.label,
            rol: {
                key: p.role.catalogKey,
                name: p.role.label,
            },
        }))
        .sort((a, b) =>
            a.rol.name.localeCompare(b.rol.name, 'es') ||
            a.name.localeCompare(b.name, 'es'),
        );

    const providers: AccessContextProviderRef[] = data.providers
        .map((p) => ({
            key: p.provider.catalogKey,
            name: p.provider.label,
            rol: {
                key: p.role.catalogKey,
                name: p.role.label,
            },
        }))
        .sort((a, b) =>
            a.rol.name.localeCompare(b.rol.name, 'es') ||
            a.name.localeCompare(b.name, 'es'),
        );

    const attributes: AccessContextAttributeRef[] = data.attributes
        .map((a) => ({
            type: {
                key: a.attributeType.catalogKey,
                name: a.attributeType.label,
            },
            value: a.attributeValue
                ? {
                      key: a.attributeValue.catalogKey,
                      name: a.attributeValue.label,
                  }
                : null,
        }))
        .sort((a, b) => a.type.name.localeCompare(b.type.name, 'es'));

    const response: AccessContextResponseV1 = {
        version: 'v1',
        user: {
            key: key,
            name: data.user.givenName+' '+data.user.familyName,
            email: data.user.email || '',
        },
        profiles: profiles
            .map((p) => ({
                key: p.key,
                name: p.name,
            }))
            .sort((a, b) => a.name.localeCompare(b.name, 'es')),
        apps,
        roles,
        permissions,
        providers,
        attributes,
    };

    ACCESS_CONTEXT_CACHE.set(cacheKey, {
        data: response,
        expiresAt: now + accessContextCacheTtlMs(),
    });

    return response;
}

export async function invalidateUserDetailsCache(userKey?: string, idProfile?: number, langId?: number) {
    if (!userKey) {
        const cleared = ACCESS_CONTEXT_CACHE.size;
        ACCESS_CONTEXT_CACHE.clear();
        return { cleared };
    }

    if (idProfile !== undefined && (!Number.isInteger(idProfile) || Number(idProfile) <= 0)) {
        throw { status: 400, message: 'idPerfil debe ser entero positivo' };
    }

    if (idProfile === undefined) {
        const prefix = `${String(userKey).trim()}::`;
        let cleared = 0;
        for (const key of ACCESS_CONTEXT_CACHE.keys()) {
            if (key.startsWith(prefix)) {
                ACCESS_CONTEXT_CACHE.delete(key);
                cleared += 1;
            }
        }
        return { cleared };
    }

    const cacheKey = accessContextCacheKey(String(userKey).trim(), idProfile, langId);
    const existed = ACCESS_CONTEXT_CACHE.delete(cacheKey);
    return { cleared: existed ? 1 : 0 };
}

/** Atributos de un usuario por userKey (sub, preferred_username, email o id). Uso BFF. */
export async function getUserAttributesByKey(userKey: string, langId?: number) {
    const key = String(userKey ?? '').trim();
    if (!key) throw { status: 400, message: 'userKey es obligatorio' };

    const user = await securityRepo.findUserByLookupKey(key);
    if (!user) throw { status: 404, message: `No existe usuario activo con la clave '${key}'` };

    const result = await securityRepo.listUserAttributes(user.idUserData, 1, 1000, langId);

    return {
        userDataId: user.idUserData,
        sub: user.sub,
        preferredUsername: user.preferredUsername,
        email: user.email,
        attributes: result.items.map((a) => ({
            typeKey:  a.attributeTypeKey ?? String(a.attributeTypeId),
            valueKey: a.attributeValueKey ?? null,
        })),
    };
}
