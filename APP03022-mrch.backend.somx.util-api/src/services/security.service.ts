import type { Request } from 'express';
import { decodeJwt, jwtVerify, type JWTPayload } from 'jose';
import * as securityRepo from '@/repositories/security.repo.js';
import { HttpException } from '@/exceptions/HttpException.js';

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
    /** Filtro opcional catálogo usuario (correo) */
    email?: string;
    /** Filtro opcional nombre para mostrar / nombre */
    fullName?: string;
    status?: number;
    langId?: number;
    page?: number;
    limit?: number;
    sortBy?: string;
    sortDir?: string;
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
        throw new HttpException(400, 'Los campos startDate y endDate son obligatorios');
    }

    if (filters.endDate < filters.startDate) {
        throw new HttpException(400, 'La fecha final debe ser mayor o igual a la fecha inicial.', 'MSG_VAL_01');
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
        throw new HttpException(404, `No existe el perfil ${idProfile}`);
    }
    return securityRepo.findProfileAssignment(idProfile);
}

export async function saveProfileUserAssignment(idProfile: number, payload: AssignmentPayload) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw new HttpException(404, `No existe el perfil ${idProfile}`);
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const users = await securityRepo.findActiveUsersByIds(selectedIds);

    if (selectedIds.length !== users.length) {
        throw new HttpException(400, 'Uno o mas usuarios no existen o estan inactivos');
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
        throw new HttpException(404, `No existe el rol ${idRole}`);
    }
    return securityRepo.findRoleUserAssignment(idRole);
}

export async function saveRoleUserAssignment(idRole: number, payload: AssignmentPayload) {
    const role = await securityRepo.findActiveRoleById(idRole);
    if (!role) {
        throw new HttpException(404, `No existe el rol ${idRole}`);
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const users = await securityRepo.findActiveUsersByIds(selectedIds);

    if (selectedIds.length !== users.length) {
        throw new HttpException(400, 'Uno o mas usuarios no existen o estan inactivos');
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
        throw new HttpException(404, `No existe el rol ${idRole}`);
    }
    return securityRepo.findRolePermissionAssignment(idRole, langId);
}

export async function saveRolePermissionAssignment(idRole: number, payload: AssignmentPayload) {
    const role = await securityRepo.findActiveRoleById(idRole);
    if (!role) {
        throw new HttpException(404, `No existe el rol ${idRole}`);
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const permissions = await securityRepo.findActivePermissionsByIds(selectedIds);

    if (selectedIds.length !== permissions.length) {
        throw new HttpException(400, 'Uno o mas permisos no existen o estan inactivos');
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
        throw new HttpException(404, `No existe el usuario ${idUser}`);
    }

    return securityRepo.listUserAttributes(idUser, page, limit, langId);
}

export async function createUserAttribute(idUser: number, payload: CreateUserAttributePayload) {
    const user = await securityRepo.findActiveUserById(idUser);
    if (!user) {
        throw new HttpException(404, `No existe el usuario ${idUser}`);
    }

    const attributeType = await securityRepo.findActiveAttributeTypeById(payload.attributeTypeId);
    if (!attributeType) {
        throw new HttpException(404, `No existe el tipo de atributo ${payload.attributeTypeId}`);
    }

    if (!Number.isInteger(payload.attributeValueId) || Number(payload.attributeValueId) <= 0) {
        throw new HttpException(400, 'attributeValueId debe ser un entero positivo');
    }

    const attributeValue = await securityRepo.findActiveCatalogDetailById(payload.attributeValueId);
    if (!attributeValue) {
        throw new HttpException(404, `No existe el valor de atributo ${payload.attributeValueId}`);
    }

    await securityRepo.createUserAttributes(idUser, payload.attributeTypeId, payload.attributeValueId, payload.actorId);
}

export async function removeUserAttribute(idUser: number, attributeId: number, actorId: string) {
    const user = await securityRepo.findActiveUserById(idUser);
    if (!user) {
        throw new HttpException(404, `No existe el usuario ${idUser}`);
    }

    const attributeRow = await securityRepo.findActiveUserAttributeForUser(idUser, attributeId);
    if (!attributeRow) {
        throw new HttpException(404, `No existe el atributo ${attributeId} para el usuario ${idUser}`);
    }

    await securityRepo.deleteUserAttribute(idUser, attributeId, String(actorId));
}

export async function listAttributeTypes(langId?: number) {
    return securityRepo.getAttributeTypes(langId);
}

export async function listAttributeValuesByType(idAttributeType: number, langId?: number) {
    const type = await securityRepo.findActiveAttributeTypeById(idAttributeType);
    if (!type) {
        throw new HttpException(404, `No existe el tipo de atributo ${idAttributeType}`);
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
        throw new HttpException(404, `No existe el perfil ${idProfile}`);
    }
    return securityRepo.findProfileModuleAssignment(idProfile, langId);
}

export async function saveProfileModuleAssignment(idProfile: number, payload: AssignmentPayload) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw new HttpException(404, `No existe el perfil ${idProfile}`);
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const modules = await securityRepo.findModulesByIds(selectedIds);

    if (selectedIds.length !== modules.length) {
        throw new HttpException(400, 'Uno o mas modulos no existen');
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
        throw new HttpException(404, `No existe el perfil ${idProfile}`);
    }
    return securityRepo.findProfileModuleProcessAssignment(idProfile, langId);
}

export async function saveProfileModuleProcessAssignment(idProfile: number, payload: AssignmentPayload) {
    const profile = await securityRepo.findActiveProfileById(idProfile);
    if (!profile) {
        throw new HttpException(404, `No existe el perfil ${idProfile}`);
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const links = await securityRepo.findActiveModuleProcessesByIds(selectedIds);

    if (selectedIds.length !== links.length) {
        throw new HttpException(400, 'Uno o mas aplicativo-evento no existen o estan inactivos');
    }

    await securityRepo.syncProfileModuleProcesses(idProfile, selectedIds, payload.actorId);

    await invalidateUserDetailsCache(undefined, idProfile);
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
        throw new HttpException(404, `No existe el modulo ${idModule}`);
    }
    return securityRepo.findModuleProcessAssignment(idModule, langId);
}

export async function saveModuleProcessAssignment(idModule: number, payload: AssignmentPayload) {
    const moduleRow = await securityRepo.findModuleById(idModule);
    if (!moduleRow) {
        throw new HttpException(404, `No existe el modulo ${idModule}`);
    }

    const selectedIds = normalizeSelectedIds(payload.selectedIds);
    const processes = await securityRepo.findProcessesByIds(selectedIds);

    if (selectedIds.length !== processes.length) {
        throw new HttpException(400, 'Uno o mas procesos no existen');
    }

    await securityRepo.syncModuleProcessesForModule(idModule, selectedIds, payload.actorId);
}

/** Detalle de seguridad por usuario (core_security.user_data): preferred_username, sub, email o id. */
function validateUserDetailsInputs(userKey: string, idProfile?: number): string {
    const key = String(userKey ?? '').trim();
    if (!key) {
        throw new HttpException(400, 'userKey es obligatorio');
    }
    if (idProfile !== undefined && (!Number.isInteger(idProfile) || Number(idProfile) <= 0)) {
        throw new HttpException(400, 'idPerfil debe ser entero positivo');
    }
    return key;
}

function ensureProfilesPresent(
    profiles: Array<AccessContextProfileRef & { id: number }>,
    idProfile: number | undefined,
): void {
    if (profiles.length === 0) {
        throw new HttpException(400, 'El usuario no tiene configurado un perfil, favor de validar.', 'WRN7030');
    }
    const selectedProfile = idProfile === undefined ? undefined : profiles.find((p) => p.id === idProfile);
    if (idProfile !== undefined && !selectedProfile) {
        throw new HttpException(400, 'El usuario no tiene configurado un perfil, favor de validar.', 'WRN7030');
    }
}

function buildEventsByAppKey(
    rows: Array<{ profile: { id: number }; module: { catalogKey: string }; process: { catalogKey: string; label: string } }>,
    idProfile: number | undefined,
): Map<string, Map<string, string>> {
    const eventsByAppKey = new Map<string, Map<string, string>>();
    for (const row of rows) {
        if (idProfile !== undefined && row.profile.id !== idProfile) continue;
        const appKey = row.module.catalogKey;
        if (!eventsByAppKey.has(appKey)) eventsByAppKey.set(appKey, new Map());
        eventsByAppKey.get(appKey)!.set(row.process.catalogKey, row.process.label);
    }
    return eventsByAppKey;
}

export async function getUserDetailsByCatalogKey(userKey: string, idProfile?: number, langId?: number) {
    const key = validateUserDetailsInputs(userKey, idProfile);

    const cacheKey = accessContextCacheKey(key, idProfile, langId);
    const now = Date.now();
    const cached = ACCESS_CONTEXT_CACHE.get(cacheKey);
    if (cached && cached.expiresAt > now) {
        return cached.data;
    }

    const data = await securityRepo.getSecurityUserDetailsByCatalogKey(key, langId);
    if (!data) {
        throw new HttpException(404, `No existe usuario activo con la clave indicada '${key}'`);
    }

    const profiles: Array<AccessContextProfileRef & { id: number }> = data.profiles.map((p) => ({
        id: p.id,
        key: p.catalogKey,
        name: p.label,
    }));
    ensureProfilesPresent(profiles, idProfile);

    const eventsByAppKey = buildEventsByAppKey(data.applicationModuleProcesses, idProfile);

    if (data.applications.length === 0) {
        throw new HttpException(400, 'El usuario no tiene configurado un aplicativo, favor de validar.', 'WRN7031');
    }

    const apps: AccessContextApplication[] = data.applications
        .map((app) => {
            const eventsMap = eventsByAppKey.get(app.catalogKey) ?? new Map<string, string>();
            return {
                key: app.catalogKey,
                name: app.label,
                events: [...eventsMap.entries()]
                    .map(([eventKey, eventName]) => ({ key: eventKey, name: eventName }))
                    .sort((a, b) => a.name.localeCompare(b.name, 'es')),
            };
        })
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
            email: data.user.email ?? '',
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

function toUserCatalogApiRow(row: import('@/repositories/security.repo.js').UserCatalogListRow) {
    return {
        id: row.id,
        username: row.username,
        fullName: row.fullName,
        email: row.email,
        status: row.status,
        createdAt: row.createdAt.toISOString(),
        modifiedAt: row.modifiedAt.toISOString(),
    };
}

function toSecuritySearchFilter(filters: SecurityFilters): import('@/repositories/security.repo.js').SecuritySearchFilter {
    const out: import('@/repositories/security.repo.js').SecuritySearchFilter = {
        startDate: filters.startDate,
        endDate: filters.endDate,
    };
    if (filters.entityId !== undefined) out.entityId = filters.entityId;
    if (filters.entityName !== undefined) out.entityName = filters.entityName;
    if (filters.email !== undefined) out.email = filters.email;
    if (filters.fullName !== undefined) out.fullName = filters.fullName;
    if (filters.status !== undefined && Number.isFinite(Number(filters.status))) {
        out.status = Number(filters.status);
    }
    if (filters.langId !== undefined) out.langId = filters.langId;
    return out;
}

function sliceSection<T>(arr: T[], page: number, pageSize: number) {
    const p = Math.max(1, page);
    const size = Math.max(1, pageSize);
    const start = (p - 1) * size;
    return {
        items: arr.slice(start, start + size),
        total: arr.length,
        page: p,
        pageSize: size,
    };
}

export async function searchUserCatalog(filters: SecurityFilters) {
    ensureDateRange(filters);
    const repoFilters = toSecuritySearchFilter(filters);
    const page = Math.max(1, Number(filters.page) || 1);
    const limit = Math.min(100, Math.max(1, Number(filters.limit) || 10));
    const sortBy = filters.sortBy ?? 'id';
    const sortDir = filters.sortDir === 'DESC' ? 'DESC' : ('ASC' as const);
    const { items, total } = await securityRepo.listUsersCatalogPaginated(repoFilters, page, limit, sortBy, sortDir);
    return {
        items: items.map(toUserCatalogApiRow),
        total,
        page,
        limit,
        sortBy,
        sortDir,
        warningCode: total ? undefined : 'WRN7002',
        warningMessage: total ? undefined : 'No existe informacion con los filtros de busqueda capturados.',
    };
}

export async function exportUserCatalogCsvLines(filters: SecurityFilters, maxRows = 10_000) {
    ensureDateRange(filters);
    const repoFilters = toSecuritySearchFilter(filters);
    const { items, total, truncated } = await securityRepo.listUsersCatalogForExport(repoFilters, maxRows);
    return {
        rows: items.map(toUserCatalogApiRow),
        total,
        truncated,
    };
}

export async function getUserCatalogDetail(
    userId: number,
    opts: {
        langId?: number;
        rolesPage?: number;
        applicationsPage?: number;
        attributesPage?: number;
        matrixPage?: number;
        matrixPageSize?: number;
        sectionSize?: number;
    },
) {
    const user = await securityRepo.findActiveUserById(userId);
    if (!user) {
        throw new HttpException(404, `No existe el usuario ${userId}`);
    }
    const key = securityRepo.userDataLookupKey(user);
    const snap = await securityRepo.getSecurityUserDetailsByCatalogKey(key, opts.langId);
    if (!snap) {
        throw new HttpException(404, `No se pudo obtener el detalle de seguridad del usuario ${userId}`);
    }

    const headerRow = await securityRepo.getUserCatalogHeaderById(userId);
    const header = headerRow
        ? toUserCatalogApiRow(headerRow)
        : {
              id: user.idUserData,
              username: user.preferredUsername ?? user.sub,
              fullName: [user.givenName, user.familyName].filter(Boolean).join(' ').trim() || (user.preferredUsername ?? user.sub),
              email: user.email,
              status: user.status,
              createdAt: user.createdAt.toISOString(),
              modifiedAt: user.createdAt.toISOString(),
          };

    const sectionSize = Math.min(50, Math.max(1, opts.sectionSize ?? 5));

    const mapRef = (c: import('@/repositories/security.repo.js').SecurityCatalogRef) => ({
        id: c.id,
        name: c.label,
        description: c.catalogKey ?? '',
    });

    const primaryProfile = snap.profiles.length > 0 ? mapRef(snap.profiles[0]!) : null;
    const multipleProfilesDetected = snap.profiles.length > 1;

    const permissionEventMatrixAll = buildPermissionEventMatrixRows(snap);
    const matrixPage = Math.max(1, opts.matrixPage ?? 1);
    const matrixPageSize = Math.min(200, Math.max(10, opts.matrixPageSize ?? 20));
    const matrixStart = (matrixPage - 1) * matrixPageSize;
    const permissionEventMatrix = {
        items: permissionEventMatrixAll.slice(matrixStart, matrixStart + matrixPageSize),
        total: permissionEventMatrixAll.length,
        page: matrixPage,
        pageSize: matrixPageSize,
    };

    const attrs = await listUserAttributes(userId, Math.max(1, opts.attributesPage ?? 1), sectionSize, opts.langId);

    const attrItems = attrs.items.map((a) => ({
        id: a.id,
        name: a.name || a.attributeTypeName,
        description: [a.attributeTypeName, a.attributeValueName].filter(Boolean).join(' — ') || '',
    }));

    const applicationsSlice = sliceSection(snap.applications.map(mapRef), opts.applicationsPage ?? 1, sectionSize);
    const applicationsItems = await Promise.all(
        applicationsSlice.items.map(async (app) => {
            const events = await securityRepo.listModuleProcessesWithUserAssignment(userId, app.id, opts.langId);
            return {
                ...app,
                events: events.map((e) => ({
                    moduleProcessId: e.moduleProcessId,
                    processId: e.processId,
                    name: e.name,
                    description: e.description,
                    assigned: e.assigned,
                })),
            };
        }),
    );

    return {
        header,
        profile: primaryProfile,
        multipleProfilesDetected,
        roles: sliceSection(snap.roles.map(mapRef), opts.rolesPage ?? 1, sectionSize),
        applications: {
            ...applicationsSlice,
            items: applicationsItems,
        },
        permissionEventMatrix,
        attributes: {
            items: attrItems,
            total: attrs.total,
            page: Math.max(1, opts.attributesPage ?? 1),
            pageSize: sectionSize,
        },
        userLookupKey: key,
    };
}

type PermissionEventMatrixRow = {
    permissionId: number;
    permissionName: string;
    permissionKey: string;
    roleId: number;
    roleName: string;
    moduleId: number;
    moduleName: string;
    processId: number;
    processName: string;
    processKey: string;
    effective: boolean;
};

function buildPermissionEventMatrixRows(
    snap: import('@/repositories/security.repo.js').SecurityUserDetailsResponse,
): PermissionEventMatrixRow[] {
    const eventMap = new Map<
        string,
        { moduleId: number; moduleName: string; processId: number; processName: string; processKey: string }
    >();
    for (const row of snap.applicationModuleProcesses) {
        const k = `${row.module.id}-${row.process.id}`;
        if (!eventMap.has(k)) {
            eventMap.set(k, {
                moduleId: row.module.id,
                moduleName: row.module.label,
                processId: row.process.id,
                processName: row.process.label,
                processKey: row.process.catalogKey,
            });
        }
    }
    const events = [...eventMap.values()].sort(
        (a, b) =>
            a.moduleName.localeCompare(b.moduleName, 'es') || a.processName.localeCompare(b.processName, 'es'),
    );

    const permMap = new Map<
        number,
        { permissionId: number; permissionName: string; permissionKey: string; roleId: number; roleName: string }
    >();
    for (const p of snap.permissions) {
        if (!permMap.has(p.permission.id)) {
            permMap.set(p.permission.id, {
                permissionId: p.permission.id,
                permissionName: p.permission.label,
                permissionKey: p.permission.catalogKey,
                roleId: p.role.id,
                roleName: p.role.label,
            });
        }
    }
    const perms = [...permMap.values()].sort(
        (a, b) =>
            a.roleName.localeCompare(b.roleName, 'es') || a.permissionName.localeCompare(b.permissionName, 'es'),
    );

    const rows: PermissionEventMatrixRow[] = [];
    for (const perm of perms) {
        for (const ev of events) {
            rows.push({
                permissionId: perm.permissionId,
                permissionName: perm.permissionName,
                permissionKey: perm.permissionKey,
                roleId: perm.roleId,
                roleName: perm.roleName,
                moduleId: ev.moduleId,
                moduleName: ev.moduleName,
                processId: ev.processId,
                processName: ev.processName,
                processKey: ev.processKey,
                effective: true,
            });
        }
    }
    return rows;
}

export async function getUserApplicationEventsCatalog(userId: number, moduleId: number, langId?: number) {
    const user = await securityRepo.findActiveUserById(userId);
    if (!user) {
        throw new HttpException(404, `No existe el usuario ${userId}`);
    }
    const mod = await securityRepo.findActiveApplicationById(moduleId);
    if (!mod) {
        throw new HttpException(404, `No existe el aplicativo ${moduleId}`);
    }
    const lang = resolveLangId(langId);
    const label =
        (mod.value && String(mod.value).trim()) !== ''
            ? String(mod.value).trim()
            : "";
    const events = await securityRepo.listModuleProcessesWithUserAssignment(userId, moduleId, lang);
    return {
        application: {
            id: mod.id,
            name: label,
            description: "",
        },
        events,
    };
}

function resolveLangId(langId?: number): number {
    return Number.isInteger(langId) && Number(langId) > 0 ? Number(langId) : 1;
}

export async function setUserModuleProcessAssigned(userId: number, moduleProcessId: number, assign: boolean, actorId: string) {
    const user = await securityRepo.findActiveUserById(userId);
    if (!user) {
        throw new HttpException(404, `No existe el usuario ${userId}`);
    }
    const mp = await securityRepo.findModuleProcessRow(moduleProcessId);
    if (!mp) {
        throw new HttpException(404, `No existe el aplicativo-evento ${moduleProcessId}`);
    }
    const moduleCatalogId = mp.idCatalogDetailModule;
    const profiles = await securityRepo.findProfileIdsLinkingUserToModule(userId, moduleCatalogId);
    if (assign && profiles.length === 0) {
        throw new HttpException(400, 'El usuario no tiene el aplicativo en ningún perfil; asigne primero el aplicativo al perfil correspondiente.');
    }
    if (!assign && profiles.length === 0) {
        return;
    }
    await securityRepo.setUserModuleProcessForProfiles(profiles, moduleProcessId, assign, actorId);
    await invalidateUserDetailsCache(securityRepo.userDataLookupKey(user));
}

export async function appendUserProfileLink(userId: number, profileId: number, actorId: string) {
    const user = await securityRepo.findActiveUserById(userId);
    if (!user) {
        throw new HttpException(404, `No existe el usuario ${userId}`);
    }
    const profile = await securityRepo.findActiveProfileById(profileId);
    if (!profile) {
        throw new HttpException(404, `No existe el perfil ${profileId}`);
    }
    await securityRepo.linkProfileUserOnly(userId, profileId, actorId);
    await invalidateUserDetailsCache(securityRepo.userDataLookupKey(user));
}

export async function invalidateUserDetailsCache(userKey?: string, idProfile?: number, langId?: number) {
    if (!userKey) {
        const cleared = ACCESS_CONTEXT_CACHE.size;
        ACCESS_CONTEXT_CACHE.clear();
        return { cleared };
    }

    if (idProfile !== undefined && (!Number.isInteger(idProfile) || Number(idProfile) <= 0)) {
        throw new HttpException(400, 'idPerfil debe ser entero positivo');
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
    if (!key) throw new HttpException(400, 'userKey es obligatorio');

    const user = await securityRepo.findUserByLookupKey(key);
    if (!user) throw new HttpException(404, `No existe usuario activo con la clave '${key}'`);

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

function parseBearerToken(req: Request): string | null {
    const raw = req.header('authorization') ?? req.header('Authorization');
    if (!raw) return null;
    const m = /^Bearer\s+(.+)$/i.exec(String(raw).trim());
    const token = m?.[1];
    return token ? token.trim() : null;
}

async function readJwtPayloadFromRequest(token: string): Promise<JWTPayload> {
    const secret = process.env.JWT_SECRET?.trim() || process.env.AUTH_JWT_SECRET?.trim();
    if (secret) {
        const { payload } = await jwtVerify(token, new TextEncoder().encode(secret));
        return payload;
    }
    console.warn(
        '[security:user-utility] JWT_SECRET/AUTH_JWT_SECRET no definidos; se decodifica el JWT sin verificar firma.',
    );
    return decodeJwt(token);
}

function stringClaim(payload: JWTPayload, ...keys: string[]): string | null {
    const o = payload as Record<string, unknown>;
    for (const k of keys) {
        const v = o[k];
        if (typeof v === 'string' && v.trim()) return v.trim();
    }
    return null;
}

/**
 * POST /api/security/user-utility — Registra o actualiza `updated_at` en catálogo (core_security.user_data)
 * según claims del JWT enviado en Authorization Bearer.
 */
export async function registerUserFromUtilitySession(req: Request): Promise<void> {
    const token = parseBearerToken(req);
    if (!token) {
        throw new HttpException(401, 'Se requiere encabezado Authorization: Bearer <token>');
    }

    let payload: JWTPayload;
    try {
        payload = await readJwtPayloadFromRequest(token);
    } catch (e) {
        console.error('[security:user-utility] JWT inválido', e);
        throw new HttpException(401, 'Token JWT inválido o no verificable');
    }

    const sub = stringClaim(payload, 'sub');
    if (!sub) {
        throw new HttpException(400, 'El token no contiene sub');
    }

    const email = stringClaim(payload, 'email');
    const preferredUsername = stringClaim(payload, 'preferred_username', 'preferredUsername');
    let givenName = stringClaim(payload, 'given_name', 'givenName');
    let familyName = stringClaim(payload, 'family_name', 'familyName');
    const fullName = stringClaim(payload, 'name');
    if (!givenName && !familyName && fullName) {
        const parts = fullName.split(/\s+/).filter(Boolean);
        givenName = parts[0] ?? null;
        familyName = parts.length > 1 ? parts.slice(1).join(' ') : null;
    }

    await securityRepo.upsertUtilityCatalogUser({
        sub,
        email,
        preferredUsername,
        givenName,
        familyName,
    });
}
