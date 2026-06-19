import { datasource } from '@/config/typeorm-datasource.js';
import { SECURITY_CATALOG_HEADER } from '@/constants/security-shared-catalog.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import {
    ModuleProcess,
    ProfileModule,
    ProfileModuleProcess,
    ProfileUser,
    RolePermission,
    RoleProvider,
    RoleUser,
    UserAttribute,
    UserData,
} from '@/entities/SecurityRelations.entity.js';
import {
    In,
    type EntityManager,
    type EntityTarget,
    type FindOptionsWhere,
    type ObjectLiteral,
    type QueryDeepPartialEntity,
    type SelectQueryBuilder,
} from 'typeorm';

export interface SecuritySearchFilter {
    startDate: string;
    endDate: string;
    entityId?: string;
    entityName?: string;
    /** Filtro opcional específico de catálogo usuario (correo) */
    email?: string;
    /** Filtro opcional: nombre completo / nombre para mostrar */
    fullName?: string;
    status?: number;
    langId?: number;
}

export interface SecuritySummaryRow {
    id: number;
    /** Clave lÃ³gica del detalle en catalog_detail (`key`), p. ej. PERM_PAGO_APROBAR */
    catalogKey: string;
    name: string;
    description: string;
    status: number;
    totalAssigned: number;
    updatedAt: Date | null;
}

export interface AssignableItemRow {
    id: number;
    title: string;
    subtitle?: string | null;
    tags?: string[];
}

export interface UserAttributeRow {
    /** user_attribute_id (unico por fila) */
    id: number;
    userId: number;
    name: string;
    attributeTypeId: number;
    attributeTypeKey: string;
    attributeTypeName: string;
    attributeValueId: number | null;
    attributeValueName: string | null;
    attributeValueKey: string | null;
    status: number;
    createdBy: string;
    createdAt: Date;
    updatedBy?: string | null;
    updatedAt?: Date | null;
}

export interface AttributeValueOption {
    id: number;
    catalogKey: string;
    name: string;
}

function normalizeAttributeDisplayName(value: string): string {
    return String(value ?? '')
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .toLowerCase()
        .replace(/\s+/g, '');
}

/** Identidad en core_security.user_data (respuesta API) */
export interface SecurityUserRef {
    id: number;
    sub: string;
    preferredUsername: string | null;
    givenName: string | null;
    familyName: string | null;
    email: string | null;
    status: number;
}

function mapUserDataToRef(u: UserData): SecurityUserRef {
    return {
        id: u.idUserData,
        sub: u.sub,
        preferredUsername: u.preferredUsername ?? null,
        givenName: u.givenName ?? null,
        familyName: u.familyName ?? null,
        email: u.email ?? null,
        status: u.status,
    };
}

function displayUserTitle(u: UserData): string {
    const full = `${u.givenName ?? ''} ${u.familyName ?? ''}`.trim();
    if (full) return full;
    return u.preferredUsername ?? u.email ?? u.sub;
}

function buildUserAssignableItem(u: UserData): AssignableItemRow {
    return {
        id: u.idUserData,
        title: displayUserTitle(u),
        subtitle: u.email ?? u.sub,
    };
}

/** Referencia normalizada a `shared_catalogs.catalog_detail` */
export interface SecurityCatalogRef {
    id: number;
    catalogKey: string;
    dictId: number;
    label: string;
    status: number;
}

/** Detalle completo de seguridad por usuario (core_security.user_data) */
export interface SecurityUserDetailsResponse {
    user: SecurityUserRef;
    profiles: SecurityCatalogRef[];
    applications: SecurityCatalogRef[];
    applicationModuleProcesses: Array<{
        idModuleProcess: number;
        module: SecurityCatalogRef;
        process: SecurityCatalogRef;
        profile: SecurityCatalogRef;
    }>;
    roles: SecurityCatalogRef[];
    permissions: Array<{
        permission: SecurityCatalogRef;
        role: SecurityCatalogRef;
    }>;
    providers: Array<{
        provider: SecurityCatalogRef;
        role: SecurityCatalogRef;
    }>;
    attributes: Array<{
        idUserAttribute: number;
        attributeType: SecurityCatalogRef;
        attributeValue: SecurityCatalogRef | null;
        status: number;
    }>;
}

/** Texto visible para filas de catalog_detail */
function detailLabelExpr(alias: string): string {
    return `COALESCE(NULLIF(TRIM(${alias}.value), ''), ${alias}.key)`;
}

function resolveLangId(langId?: number): number {
    return Number.isInteger(langId) && Number(langId) > 0 ? Number(langId) : 1;
}

function catalogRefLabelExpr(alias: string, langId: number): string {
    return dictionaryLabelExpr(alias, langId);
}

function dictionaryLabelExpr(alias: string, langId: number): string {
    return `COALESCE((SELECT dl.description FROM shared_catalogs.dictionary_lang dl WHERE dl.dict_id = ${alias}.dict_id AND dl.lang_id = ${langId} LIMIT 1), ${detailLabelExpr(alias)})`;
}

function mapRawToCatalogRef(row: Record<string, unknown>): SecurityCatalogRef {
    return {
        id: Number(row.id),
        catalogKey: String(row.catalogKey ?? ''),
        dictId: Number(row.dictId ?? 0),
        label: String(row.label ?? ''),
        status: Number(row.status ?? 0),
    };
}

function catalogRefFromDetailEntity(d: CatalogDetail): SecurityCatalogRef {
    const label =
        (d.value && String(d.value).trim()) !== ''
            ? String(d.value).trim()
            : d.key;
    return {
        id: d.id,
        catalogKey: d.key,
        dictId: d.dictId,
        label,
        status: d.status,
    };
}

function joinHeader(detailAlias: string, headerAlias: string, headerCode: string): string {
    return `${headerAlias}.id = ${detailAlias}.header_id AND ${headerAlias}.code = '${headerCode}'`;
}

function applyCatalogDetailFilters(
    qb: SelectQueryBuilder<CatalogDetail>,
    alias: string,
    filters: SecuritySearchFilter,
    options?: { skipStatus?: boolean },
) {
    qb.where(`DATE(${alias}.created_at) BETWEEN :startDate AND :endDate`, {
        startDate: filters.startDate,
        endDate: filters.endDate,
    });

    if (filters.entityId) {
        const trim = filters.entityId.trim();
        qb.andWhere(`(CAST(${alias}.id AS TEXT) LIKE :entityId OR CAST(${alias}.dict_id AS TEXT) LIKE :entityId)`, {
            entityId: `%${trim}%`,
        });
    }

    if (filters.entityName) {
        const trim = filters.entityName.trim();
        const langId = resolveLangId(filters.langId);
        const labelExpr = catalogRefLabelExpr(alias, langId);
        qb.andWhere(
            `(LOWER(${alias}.key) LIKE LOWER(:entityName) OR LOWER(COALESCE(${alias}.value, '')) LIKE LOWER(:entityName) OR LOWER(${labelExpr}) LIKE LOWER(:entityName))`,
            { entityName: `%${trim}%` },
        );
    }

    if (!options?.skipStatus && filters.status !== undefined && Number.isFinite(Number(filters.status))) {
        qb.andWhere(`${alias}.status = :status`, { status: Number(filters.status) });
    }
}

function applyUserDataFilters(
    qb: SelectQueryBuilder<UserData>,
    alias: string,
    filters: SecuritySearchFilter,
    options?: { skipStatus?: boolean },
) {
    qb.where(`DATE(${alias}.created_at) BETWEEN :startDate AND :endDate`, {
        startDate: filters.startDate,
        endDate: filters.endDate,
    });

    if (filters.entityId) {
        const trim = filters.entityId.trim();
        qb.andWhere(
            `(CAST(${alias}.user_data_id AS TEXT) LIKE :entityId OR COALESCE(${alias}.sub, '') LIKE :entityId OR COALESCE(${alias}.email, '') LIKE :entityId)`,
            { entityId: `%${trim}%` },
        );
    }

    if (filters.entityName) {
        const trim = filters.entityName.trim();
        qb.andWhere(
            `(LOWER(COALESCE(${alias}.given_name, '')) LIKE LOWER(:entityName) OR LOWER(COALESCE(${alias}.family_name, '')) LIKE LOWER(:entityName) OR LOWER(COALESCE(${alias}.preferred_username, '')) LIKE LOWER(:entityName))`,
            { entityName: `%${trim}%` },
        );
    }

    if (!options?.skipStatus && filters.status !== undefined && Number.isFinite(Number(filters.status))) {
        qb.andWhere(`${alias}.status = :status`, { status: Number(filters.status) });
    }

    if (filters.email?.trim()) {
        const trim = filters.email.trim();
        qb.andWhere(`LOWER(COALESCE(${alias}.email, '')) LIKE LOWER(:emailOnly)`, { emailOnly: `%${trim}%` });
    }

    if (filters.fullName?.trim()) {
        const trim = filters.fullName.trim();
        qb.andWhere(
            `(LOWER(TRIM(CONCAT(COALESCE(${alias}.given_name,''),' ',COALESCE(${alias}.family_name,'')))) LIKE LOWER(:fullNameOnly) OR LOWER(COALESCE(${alias}.preferred_username, '')) LIKE LOWER(:fullNameOnly))`,
            { fullNameOnly: `%${trim}%` },
        );
    }
}

function toSummaryRows(rows: Array<Record<string, unknown>>): SecuritySummaryRow[] {
    return rows.map((row) => ({
        id: Number(row.id),
        catalogKey: String(row.catalogKey ?? ''),
        name: String(row.name ?? ''),
        description: String(row.description ?? ''),
        status: Number(row.status ?? 0),
        totalAssigned: Number(row.totalAssigned ?? 0),
        updatedAt: row.updatedAt ? new Date(String(row.updatedAt)) : null,
    }));
}

export async function listProfileUsers(filters: SecuritySearchFilter): Promise<SecuritySummaryRow[]> {
    const langId = resolveLangId(filters.langId);
    const qb = datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('profile')
        .innerJoin(CatalogHeader, 'hProf', joinHeader('profile', 'hProf', SECURITY_CATALOG_HEADER.perfil))
        .leftJoin(
            ProfileUser,
            'profileUser',
            'profileUser.catalog_detail_profile_id = profile.id AND profileUser.status = 1',
        )
        .select('profile.id', 'id')
        .addSelect(dictionaryLabelExpr('profile', langId), 'name')
        .addSelect(`''`, 'description')
        .addSelect('profile.status', 'status')
        .addSelect('COUNT(DISTINCT profileUser.user_data_id)', 'totalAssigned')
        .addSelect('profile.updated_at', 'updatedAt')
        .addSelect('profile.key', 'catalogKey')
        .groupBy('profile.id');

    applyCatalogDetailFilters(qb, 'profile', filters);
    qb.addGroupBy('profile.value')
        .addGroupBy('profile.key')
        .addGroupBy('profile.status')
        .addGroupBy('profile.updated_at')
        .orderBy(dictionaryLabelExpr('profile', langId), 'ASC');

    return toSummaryRows(await qb.getRawMany());
}

export async function listRoleUsers(filters: SecuritySearchFilter): Promise<SecuritySummaryRow[]> {
    const langId = resolveLangId(filters.langId);
    const qb = datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('role')
        .innerJoin(CatalogHeader, 'hRol', joinHeader('role', 'hRol', SECURITY_CATALOG_HEADER.rol))
        .leftJoin(RoleUser, 'roleUser', 'roleUser.catalog_detail_role_id = role.id AND roleUser.status = 1')
        .select('role.id', 'id')
        .addSelect(dictionaryLabelExpr('role', langId), 'name')
        .addSelect(`''`, 'description')
        .addSelect('role.status', 'status')
        .addSelect('COUNT(DISTINCT roleUser.user_data_id)', 'totalAssigned')
        .addSelect('role.updated_at', 'updatedAt')
        .addSelect('role.key', 'catalogKey')
        .groupBy('role.id');

    applyCatalogDetailFilters(qb, 'role', filters);
    qb.addGroupBy('role.value')
        .addGroupBy('role.key')
        .addGroupBy('role.status')
        .addGroupBy('role.updated_at')
        .orderBy(dictionaryLabelExpr('role', langId), 'ASC');

    return toSummaryRows(await qb.getRawMany());
}

export async function listRolePermissions(filters: SecuritySearchFilter): Promise<SecuritySummaryRow[]> {
    const langId = resolveLangId(filters.langId);
    const qb = datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('role')
        .innerJoin(CatalogHeader, 'hRol', joinHeader('role', 'hRol', SECURITY_CATALOG_HEADER.rol))
        .leftJoin(
            RolePermission,
            'rolePermission',
            'rolePermission.catalog_detail_role_id = role.id AND rolePermission.status = 1',
        )
        .select('role.id', 'id')
        .addSelect(dictionaryLabelExpr('role', langId), 'name')
        .addSelect(`''`, 'description')
        .addSelect('role.status', 'status')
        .addSelect('COUNT(DISTINCT rolePermission.catalog_detail_permission_id)', 'totalAssigned')
        .addSelect('role.updated_at', 'updatedAt')
        .addSelect('role.key', 'catalogKey')
        .groupBy('role.id');

    applyCatalogDetailFilters(qb, 'role', filters);
    qb.addGroupBy('role.value')
        .addGroupBy('role.key')
        .addGroupBy('role.status')
        .addGroupBy('role.updated_at')
        .orderBy(dictionaryLabelExpr('role', langId), 'ASC');

    return toSummaryRows(await qb.getRawMany());
}

export async function listUsersWithAttributes(filters: SecuritySearchFilter): Promise<SecuritySummaryRow[]> {
    const rawCounts = await datasource
        .getRepository(UserAttribute)
        .createQueryBuilder('ua')
        .where('ua.status = 1')
        .select('ua.user_data_id', 'userId')
        .addSelect('COUNT(DISTINCT ua.user_attribute_id)', 'totalAssigned')
        .addSelect('MAX(ua.updated_at)', 'updatedAt')
        .groupBy('ua.user_data_id')
        .getRawMany<Record<string, unknown>>();

    const countByUser = new Map<number, { totalAssigned: number; updatedAt: Date | null }>();
    for (const row of rawCounts) {
        countByUser.set(Number(row.userId), {
            totalAssigned: Number(row.totalAssigned ?? 0),
            updatedAt: row.updatedAt ? new Date(String(row.updatedAt)) : null,
        });
    }

    const qb = datasource.getRepository(UserData).createQueryBuilder('ud');
    applyUserDataFilters(qb, 'ud', filters);
    qb.orderBy('ud.user_data_id', 'ASC');
    const users = await qb.getMany();

    return users
        .map((user) => {
            const countInfo = countByUser.get(user.idUserData);
            return {
                id: user.idUserData,
                catalogKey: user.preferredUsername ?? user.sub,
                name: displayUserTitle(user),
                description: user.email ?? '',
                status: user.status,
                totalAssigned: countInfo?.totalAssigned ?? 0,
                updatedAt: countInfo?.updatedAt ?? null,
            };
        })
        .sort((a, b) => a.name.localeCompare(b.name, 'es'));
}

export async function findProfileAssignment(
    profileId: number,
): Promise<{ available: AssignableItemRow[]; assigned: AssignableItemRow[] }> {
    const assignedRows = await datasource.getRepository(ProfileUser).find({
        where: { idCatalogDetailProfile: profileId, status: 1 },
    });
    const activeProfileRows = await datasource.getRepository(ProfileUser).find({
        where: { status: 1 },
    });
    const activeUsers = await datasource.getRepository(UserData).find({
        where: { status: 1 },
        order: { idUserData: 'ASC' },
    });
    const assignedIds = new Set(assignedRows.map((row) => row.userDataId));
    const usersWithAnyProfile = new Set(activeProfileRows.map((row) => row.userDataId));
    const assigned = activeUsers
        .filter((user) => assignedIds.has(user.idUserData))
        .map(buildUserAssignableItem)
        .sort((a, b) => a.title.localeCompare(b.title, 'es'));
    const available = activeUsers
        .filter((user) => !usersWithAnyProfile.has(user.idUserData))
        .map(buildUserAssignableItem)
        .sort((a, b) => a.title.localeCompare(b.title, 'es'));
    return { available, assigned };
}

export async function findRoleUserAssignment(roleId: number): Promise<{ available: AssignableItemRow[]; assigned: AssignableItemRow[] }> {
    const assignedRows = await datasource.getRepository(RoleUser).find({
        where: { idCatalogDetailRole: roleId, status: 1 },
    });
    const activeUsers = await datasource.getRepository(UserData).find({
        where: { status: 1 },
        order: { idUserData: 'ASC' },
    });
    const assignedIds = new Set(assignedRows.map((row) => row.userDataId));
    const assigned = activeUsers
        .filter((user) => assignedIds.has(user.idUserData))
        .map(buildUserAssignableItem)
        .sort((a, b) => a.title.localeCompare(b.title, 'es'));
    const available = activeUsers
        .filter((user) => !assignedIds.has(user.idUserData))
        .map(buildUserAssignableItem)
        .sort((a, b) => a.title.localeCompare(b.title, 'es'));
    return { available, assigned };
}

export async function findRolePermissionAssignment(
    roleId: number,
    langIdParam?: number,
): Promise<{ available: AssignableItemRow[]; assigned: AssignableItemRow[] }> {
    const langId = resolveLangId(langIdParam);
    const baseSelect = datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('permission')
        .innerJoin(CatalogHeader, 'hPerm', joinHeader('permission', 'hPerm', SECURITY_CATALOG_HEADER.permiso))
        .where('permission.status = 1')
        .select('permission.id', 'id')
        .addSelect(dictionaryLabelExpr('permission', langId), 'title')
        .addSelect(`COALESCE(permission.value, permission.key)`, 'subtitle')
        .addSelect(`ARRAY[permission."key"]::varchar[]`, 'tags');

    const assigned = await baseSelect
        .clone()
        .innerJoin(
            RolePermission,
            'rolePermission',
            'rolePermission.catalog_detail_permission_id = permission.id AND rolePermission.catalog_detail_role_id = :roleId AND rolePermission.status = 1',
            { roleId },
        )
        .orderBy(dictionaryLabelExpr('permission', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    const available = await baseSelect
        .clone()
        .andWhere((qb) => {
            const sub = qb
                .subQuery()
                .select('rolePermission.catalog_detail_permission_id')
                .from(RolePermission, 'rolePermission')
                .where('rolePermission.catalog_detail_role_id = :roleId')
                .andWhere('rolePermission.status = 1')
                .getQuery();
            return `permission.id NOT IN ${sub}`;
        })
        .setParameter('roleId', roleId)
        .orderBy(dictionaryLabelExpr('permission', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    return { available, assigned };
}

async function upsertComposite<T extends ObjectLiteral>(
    manager: EntityManager,
    entity: EntityTarget<T>,
    where: FindOptionsWhere<T>,
    createData: Partial<T>,
    updateData: Partial<T>,
) {
    const repo = manager.getRepository(entity);
    const existing = await repo.findOneBy(where);
    if (existing) {
        await repo.update(where, updateData as QueryDeepPartialEntity<T>);
        return;
    }
    await repo.save(repo.create(createData as T));
}

export async function syncProfileUsers(profileId: number, userIds: number[], actorId: string): Promise<void> {
    await datasource.transaction(async (manager) => {
        const profileRepo = manager.getRepository(ProfileUser);

        await profileRepo
            .createQueryBuilder()
            .update(ProfileUser)
            .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
            .where('catalog_detail_profile_id = :profileId', { profileId })
            .andWhere('status = 1')
            .andWhere(userIds.length ? 'user_data_id NOT IN (:...userIds)' : '1=1', { userIds })
            .execute();

        if (userIds.length) {
            await profileRepo
                .createQueryBuilder()
                .update(ProfileUser)
                .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
                .where('user_data_id IN (:...userIds)', { userIds })
                .andWhere('catalog_detail_profile_id <> :profileId', { profileId })
                .andWhere('status = 1')
                .execute();
        }

        for (const userId of userIds) {
            await upsertComposite(
                manager,
                ProfileUser,
                { idCatalogDetailProfile: profileId, userDataId: userId },
                {
                    idCatalogDetailProfile: profileId,
                    userDataId: userId,
                    status: 1,
                    createdBy: actorId,
                },
                { status: 1, updatedBy: actorId, updatedAt: new Date() },
            );
        }
    });
}

export async function syncRoleUsers(roleId: number, userIds: number[], actorId: string): Promise<void> {
    await datasource.transaction(async (manager) => {
        const repo = manager.getRepository(RoleUser);

        await repo
            .createQueryBuilder()
            .update(RoleUser)
            .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
            .where('catalog_detail_role_id = :roleId', { roleId })
            .andWhere('status = 1')
            .andWhere(userIds.length ? 'user_data_id NOT IN (:...userIds)' : '1=1', { userIds })
            .execute();

        for (const userId of userIds) {
            await upsertComposite(
                manager,
                RoleUser,
                { idCatalogDetailRole: roleId, userDataId: userId },
                {
                    idCatalogDetailRole: roleId,
                    userDataId: userId,
                    status: 1,
                    createdBy: actorId,
                },
                { status: 1, updatedBy: actorId, updatedAt: new Date() },
            );
        }
    });
}

export async function syncRolePermissions(roleId: number, permissionIds: number[], actorId: string): Promise<void> {
    await datasource.transaction(async (manager) => {
        const repo = manager.getRepository(RolePermission);

        await repo
            .createQueryBuilder()
            .update(RolePermission)
            .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
            .where('catalog_detail_role_id = :roleId', { roleId })
            .andWhere('status = 1')
            .andWhere(permissionIds.length ? 'catalog_detail_permission_id NOT IN (:...permissionIds)' : '1=1', {
                permissionIds,
            })
            .execute();

        for (const permissionId of permissionIds) {
            await upsertComposite(
                manager,
                RolePermission,
                { idCatalogDetailRole: roleId, idCatalogDetailPermission: permissionId },
                {
                    idCatalogDetailRole: roleId,
                    idCatalogDetailPermission: permissionId,
                    status: 1,
                    createdBy: actorId,
                },
                { status: 1, updatedBy: actorId, updatedAt: new Date() },
            );
        }
    });
}

export async function listUserAttributes(
    userId: number,
    page: number,
    limit: number,
    langIdParam?: number,
): Promise<{ items: UserAttributeRow[]; total: number }> {
    const langId = resolveLangId(langIdParam);
    const repo = datasource.getRepository(UserAttribute);
    const raw = await repo
        .createQueryBuilder('userAttribute')
        .innerJoin(
            CatalogDetail,
            'attributeType',
            'attributeType.id = userAttribute.catalog_detail_attribute_type_id',
        )
        .innerJoin(CatalogHeader, 'hAtt', joinHeader('attributeType', 'hAtt', SECURITY_CATALOG_HEADER.tipoAtributo))
        .leftJoin(
            CatalogDetail,
            'attributeValue',
            'attributeValue.id = userAttribute.catalog_detail_attribute_value_id',
        )
        .where('userAttribute.user_data_id = :userId', { userId })
        .andWhere('userAttribute.status = 1')
        .select('userAttribute.user_attribute_id', 'id')
        .addSelect('userAttribute.user_data_id', 'userId')
        .addSelect(dictionaryLabelExpr('attributeType', langId), 'name')
        .addSelect('userAttribute.catalog_detail_attribute_type_id', 'attributeTypeId')
        .addSelect('attributeType.key', 'attributeTypeKey')
        .addSelect(dictionaryLabelExpr('attributeType', langId), 'attributeTypeName')
        .addSelect('userAttribute.catalog_detail_attribute_value_id', 'attributeValueId')
        .addSelect(dictionaryLabelExpr('attributeValue', langId), 'attributeValueName')
        .addSelect('attributeValue.key', 'attributeValueKey')
        .addSelect('userAttribute.status', 'status')
        .addSelect('userAttribute.created_by', 'createdBy')
        .addSelect('userAttribute.created_at', 'createdAt')
        .addSelect('userAttribute.updated_by', 'updatedBy')
        .addSelect('userAttribute.updated_at', 'updatedAt')
        .orderBy(dictionaryLabelExpr('attributeType', langId), 'DESC')
        .addOrderBy('userAttribute.user_attribute_id', 'DESC')
        .offset((page - 1) * limit)
        .limit(limit)
        .getRawMany<Record<string, unknown>>();

    const total = await repo
        .createQueryBuilder('userAttribute')
        .where('userAttribute.user_data_id = :userId', { userId })
        .andWhere('userAttribute.status = 1')
        .getCount();

    return {
        items: raw.map((item) => ({
            id: Number(item.id),
            userId: Number(item.userId),
            name: String(item.name),
            attributeTypeId: Number(item.attributeTypeId),
            attributeTypeKey: String(item.attributeTypeKey),
            attributeTypeName: String(item.attributeTypeName),
            attributeValueId: item.attributeValueId == null ? null : Number(item.attributeValueId),
            attributeValueName: item.attributeValueName == null ? null : String(item.attributeValueName),
            attributeValueKey: item.attributeValueKey == null ? null : String(item.attributeValueKey),
            status: Number(item.status),
            createdBy: String(item.createdBy),
            createdAt: new Date(String(item.createdAt)),
            updatedBy: item.updatedBy == null ? null : String(item.updatedBy),
            updatedAt: item.updatedAt ? new Date(String(item.updatedAt)) : null,
        })),
        total,
    };
}

export async function getAttributeTypes(langIdParam?: number): Promise<Array<{ id: number; name: string }>> {
    const langId = resolveLangId(langIdParam);
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('t')
        .innerJoin(CatalogHeader, 'h', joinHeader('t', 'h', SECURITY_CATALOG_HEADER.tipoAtributo))
        .where('t.status = 1')
        .select('t.id', 'id')
        .addSelect(dictionaryLabelExpr('t', langId), 'name')
        .orderBy(dictionaryLabelExpr('t', langId), 'ASC')
        .getRawMany<{ id: number; name: string }>();
}

export async function getAttributeValuesByType(
    idAttributeType: number,
    langIdParam?: number,
): Promise<AttributeValueOption[]> {
    const langId = resolveLangId(langIdParam);
    const attributeTypeRaw = await datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('t')
        .innerJoin(CatalogHeader, 'h', joinHeader('t', 'h', SECURITY_CATALOG_HEADER.tipoAtributo))
        .where('t.id = :id', { id: idAttributeType })
        .andWhere('t.status = 1')
        .select('t.id', 'id')
        .addSelect('t.key', 'catalogKey')
        .addSelect(dictionaryLabelExpr('t', langId), 'displayName')
        .getRawOne<Record<string, unknown>>();

    if (!attributeTypeRaw) {
        return [];
    }

    const attributeName = String(attributeTypeRaw.displayName ?? '');
    const normalizedName = normalizeAttributeDisplayName(attributeName);
    const targetHeaderId = (
        idAttributeType === 556 ||
        normalizedName === 'tipoproveedor' ||
        normalizedName === 'tipoprovedor'
    )
        ? 22
        : undefined;
    if (!targetHeaderId) {
        return [];
    }

    const rows = await datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('v')
        .where('v.status = 1')
        .andWhere('v.header_id = :headerId', { headerId: targetHeaderId })
        .select('v.id', 'id')
        .addSelect('v.key', 'catalogKey')
        .addSelect(dictionaryLabelExpr('v', langId), 'name')
        .orderBy(dictionaryLabelExpr('v', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    return rows.map((r) => ({
        id: Number(r.id),
        catalogKey: String(r.catalogKey ?? ''),
        name: String(r.name ?? ''),
    }));
}

export async function createUserAttributes(
    userId: number,
    attributeTypeId: number,
    attributeValueId: number,
    actorId: string,
): Promise<void> {
    const repo = datasource.getRepository(UserAttribute);
    const existing = await repo.findOneBy({
        userDataId: userId,
        idCatalogDetailAttributeType: attributeTypeId,
    });
    if (existing) {
        await repo.update(
            { idUserAttribute: existing.idUserAttribute },
            {
                status: 1,
                idCatalogDetailAttributeValue: attributeValueId,
                updatedBy: actorId,
                updatedAt: new Date(),
            },
        );
        return;
    }
    await repo.save(
        repo.create({
            userDataId: userId,
            idCatalogDetailAttributeType: attributeTypeId,
            idCatalogDetailAttributeValue: attributeValueId,
            status: 1,
            createdBy: actorId,
        }),
    );
}

export async function deleteUserAttribute(idUser: number, idUserAttribute: number, actorId: string): Promise<void> {
    const repo = datasource.getRepository(UserAttribute);
    await repo
        .createQueryBuilder()
        .update(UserAttribute)
        .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
        .where('user_attribute_id = :id', { id: idUserAttribute })
        .andWhere('user_data_id = :userId', { userId: idUser })
        .andWhere('status = 1')
        .execute();
}

export async function listProfileModules(filters: SecuritySearchFilter): Promise<SecuritySummaryRow[]> {
    const langId = resolveLangId(filters.langId);
    const qb = datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('profile')
        .innerJoin(CatalogHeader, 'hProf', joinHeader('profile', 'hProf', SECURITY_CATALOG_HEADER.perfil))
        .leftJoin(
            ProfileModule,
            'pm',
            'pm.catalog_detail_profile_id = profile.id AND pm.status = 1',
        )
        .select('profile.id', 'id')
        .addSelect(dictionaryLabelExpr('profile', langId), 'name')
        .addSelect(`''`, 'description')
        .addSelect('profile.status', 'status')
        .addSelect('COUNT(DISTINCT pm.catalog_detail_module_id)', 'totalAssigned')
        .addSelect('profile.updated_at', 'updatedAt')
        .addSelect('profile.key', 'catalogKey')
        .groupBy('profile.id');

    applyCatalogDetailFilters(qb, 'profile', filters);
    qb.addGroupBy('profile.value')
        .addGroupBy('profile.key')
        .addGroupBy('profile.status')
        .addGroupBy('profile.updated_at')
        .orderBy(dictionaryLabelExpr('profile', langId), 'ASC');

    return toSummaryRows(await qb.getRawMany());
}

export async function findProfileModuleAssignment(
    profileId: number,
    langIdParam?: number,
): Promise<{ available: AssignableItemRow[]; assigned: AssignableItemRow[] }> {
    const langId = resolveLangId(langIdParam);
    const assigned = await datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('m')
        .innerJoin(CatalogHeader, 'hMod', joinHeader('m', 'hMod', SECURITY_CATALOG_HEADER.aplicativo))
        .innerJoin(
            ProfileModule,
            'pm',
            'pm.catalog_detail_module_id = m.id AND pm.catalog_detail_profile_id = :profileId AND pm.status = 1',
            { profileId },
        )
        .select('m.id', 'id')
        .addSelect(dictionaryLabelExpr('m', langId), 'title')
        .addSelect(`''`, 'subtitle')
        .orderBy(dictionaryLabelExpr('m', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    const available = await datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('m')
        .innerJoin(CatalogHeader, 'hMod', joinHeader('m', 'hMod', SECURITY_CATALOG_HEADER.aplicativo))
        .leftJoin(
            ProfileModule,
            'pm',
            'pm.catalog_detail_module_id = m.id AND pm.catalog_detail_profile_id = :profileId AND pm.status = 1',
            { profileId },
        )
        .where('pm.catalog_detail_module_id IS NULL')
        .select('m.id', 'id')
        .addSelect(dictionaryLabelExpr('m', langId), 'title')
        .addSelect(`''`, 'subtitle')
        .orderBy(dictionaryLabelExpr('m', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    return { available, assigned };
}

export async function syncProfileModules(profileId: number, moduleIds: number[], actorId: string): Promise<void> {
    await datasource.transaction(async (manager) => {
        const repo = manager.getRepository(ProfileModule);

        await repo
            .createQueryBuilder()
            .update(ProfileModule)
            .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
            .where('catalog_detail_profile_id = :profileId', { profileId })
            .andWhere('status = 1')
            .andWhere(moduleIds.length ? 'catalog_detail_module_id NOT IN (:...moduleIds)' : '1=1', { moduleIds })
            .execute();

        for (const moduleId of moduleIds) {
            await upsertComposite(
                manager,
                ProfileModule,
                { idCatalogDetailProfile: profileId, idCatalogDetailModule: moduleId },
                {
                    idCatalogDetailProfile: profileId,
                    idCatalogDetailModule: moduleId,
                    status: 1,
                    createdBy: actorId,
                },
                { status: 1, updatedBy: actorId, updatedAt: new Date() },
            );
        }
    });
}

export async function listProfileModuleProcesses(filters: SecuritySearchFilter): Promise<SecuritySummaryRow[]> {
    const langId = resolveLangId(filters.langId);
    const qb = datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('profile')
        .innerJoin(CatalogHeader, 'hProf', joinHeader('profile', 'hProf', SECURITY_CATALOG_HEADER.perfil))
        .leftJoin(
            ProfileModuleProcess,
            'pmp',
            'pmp.catalog_detail_profile_id = profile.id AND pmp.status = 1',
        )
        .select('profile.id', 'id')
        .addSelect(dictionaryLabelExpr('profile', langId), 'name')
        .addSelect(`''`, 'description')
        .addSelect('profile.status', 'status')
        .addSelect('COUNT(DISTINCT pmp.module_process_id)', 'totalAssigned')
        .addSelect('profile.updated_at', 'updatedAt')
        .addSelect('profile.key', 'catalogKey')
        .groupBy('profile.id');

    applyCatalogDetailFilters(qb, 'profile', filters);
    qb.addGroupBy('profile.value')
        .addGroupBy('profile.key')
        .addGroupBy('profile.status')
        .addGroupBy('profile.updated_at')
        .orderBy(dictionaryLabelExpr('profile', langId), 'ASC');

    return toSummaryRows(await qb.getRawMany());
}

export async function findProfileModuleProcessAssignment(
    profileId: number,
    langIdParam?: number,
): Promise<{ available: AssignableItemRow[]; assigned: AssignableItemRow[] }> {
    const langId = resolveLangId(langIdParam);

    const assigned = await datasource
        .getRepository(ModuleProcess)
        .createQueryBuilder('mp')
        .innerJoin(CatalogDetail, 'cm', 'cm.id = mp.catalog_detail_module_id')
        .innerJoin(CatalogHeader, 'hCm', joinHeader('cm', 'hCm', SECURITY_CATALOG_HEADER.aplicativo))
        .innerJoin(CatalogDetail, 'cp', 'cp.id = mp.catalog_detail_process_id')
        .innerJoin(CatalogHeader, 'hCp', joinHeader('cp', 'hCp', SECURITY_CATALOG_HEADER.evento))
        .innerJoin(
            ProfileModuleProcess,
            'pmp',
            'pmp.module_process_id = mp.module_process_id AND pmp.catalog_detail_profile_id = :profileId AND pmp.status = 1',
            { profileId },
        )
        .where('mp.status = 1')
        .select('MIN(mp.module_process_id)', 'id')
        .addSelect(`CONCAT(${dictionaryLabelExpr('cm', langId)}, ' | ', ${dictionaryLabelExpr('cp', langId)})`, 'title')
        .addSelect(`CONCAT('M', cm.id, ' Â· E', cp.id)`, 'subtitle')
        .groupBy('cm.id')
        .addGroupBy('cp.id')
        .addGroupBy(dictionaryLabelExpr('cm', langId))
        .addGroupBy(dictionaryLabelExpr('cp', langId))
        .orderBy(dictionaryLabelExpr('cm', langId), 'ASC')
        .addOrderBy(dictionaryLabelExpr('cp', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    const available = await datasource
        .getRepository(ModuleProcess)
        .createQueryBuilder('mp')
        .innerJoin(CatalogDetail, 'cm', 'cm.id = mp.catalog_detail_module_id')
        .innerJoin(CatalogHeader, 'hCm', joinHeader('cm', 'hCm', SECURITY_CATALOG_HEADER.aplicativo))
        .innerJoin(CatalogDetail, 'cp', 'cp.id = mp.catalog_detail_process_id')
        .innerJoin(CatalogHeader, 'hCp', joinHeader('cp', 'hCp', SECURITY_CATALOG_HEADER.evento))
        .where('mp.status = 1')
        .andWhere(
            `NOT EXISTS (
                SELECT 1 FROM core_security.profile_module_process pmp
                WHERE pmp.module_process_id = mp.module_process_id
                  AND pmp.catalog_detail_profile_id = :profileId AND pmp.status = 1
            )`,
            { profileId },
        )
        .select('MIN(mp.module_process_id)', 'id')
        .addSelect(`CONCAT(${dictionaryLabelExpr('cm', langId)}, ' | ', ${dictionaryLabelExpr('cp', langId)})`, 'title')
        .addSelect(`CONCAT('M', cm.id, ' Â· E', cp.id)`, 'subtitle')
        .groupBy('cm.id')
        .addGroupBy('cp.id')
        .addGroupBy(dictionaryLabelExpr('cm', langId))
        .addGroupBy(dictionaryLabelExpr('cp', langId))
        .orderBy(dictionaryLabelExpr('cm', langId), 'ASC')
        .addOrderBy(dictionaryLabelExpr('cp', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    return { available, assigned };
}

export async function syncProfileModuleProcesses(profileId: number, moduleProcessIds: number[], actorId: string): Promise<void> {
    await datasource.transaction(async (manager) => {
        const repo = manager.getRepository(ProfileModuleProcess);

        await repo
            .createQueryBuilder()
            .update(ProfileModuleProcess)
            .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
            .where('catalog_detail_profile_id = :profileId', { profileId })
            .andWhere('status = 1')
            .andWhere(moduleProcessIds.length ? 'module_process_id NOT IN (:...moduleProcessIds)' : '1=1', {
                moduleProcessIds,
            })
            .execute();

        for (const idMp of moduleProcessIds) {
            await upsertComposite(
                manager,
                ProfileModuleProcess,
                { idCatalogDetailProfile: profileId, idModuleProcess: idMp },
                {
                    idCatalogDetailProfile: profileId,
                    idModuleProcess: idMp,
                    status: 1,
                    createdBy: actorId,
                },
                { status: 1, updatedBy: actorId, updatedAt: new Date() },
            );
        }
    });
}

export async function listApplicationEvents(filters: SecuritySearchFilter): Promise<SecuritySummaryRow[]> {
    const langId = resolveLangId(filters.langId);
    const qb = datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('mod')
        .innerJoin(CatalogHeader, 'hMod', joinHeader('mod', 'hMod', SECURITY_CATALOG_HEADER.aplicativo))
        .leftJoin(
            ModuleProcess,
            'mp',
            'mp.catalog_detail_module_id = mod.id AND mp.status = 1',
        )
        .select('mod.id', 'id')
        .addSelect(dictionaryLabelExpr('mod', langId), 'name')
        .addSelect(`''`, 'description')
        .addSelect('1', 'status')
        .addSelect('COUNT(DISTINCT mp.module_process_id)', 'totalAssigned')
        .addSelect('mod.updated_at', 'updatedAt')
        .addSelect('mod.key', 'catalogKey')
        .groupBy('mod.id');

    applyCatalogDetailFilters(qb, 'mod', filters, { skipStatus: true });
    qb.addGroupBy('mod.value')
        .addGroupBy('mod.key')
        .addGroupBy('mod.dictId')
        .addGroupBy('mod.updated_at')
        .orderBy(dictionaryLabelExpr('mod', langId), 'ASC');

    return toSummaryRows(await qb.getRawMany());
}

export async function findModuleProcessAssignment(
    moduleId: number,
    langIdParam?: number,
): Promise<{ available: AssignableItemRow[]; assigned: AssignableItemRow[] }> {
    const langId = resolveLangId(langIdParam);
    const assigned = await datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('proc')
        .innerJoin(CatalogHeader, 'hEv', joinHeader('proc', 'hEv', SECURITY_CATALOG_HEADER.evento))
        .innerJoin(
            ModuleProcess,
            'mp',
            'mp.catalog_detail_process_id = proc.id AND mp.catalog_detail_module_id = :moduleId AND mp.status = 1',
            { moduleId },
        )
        .select('proc.id', 'id')
        .addSelect(dictionaryLabelExpr('proc', langId), 'title')
        .addSelect(`''`, 'subtitle')
        .orderBy(dictionaryLabelExpr('proc', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    const available = await datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('proc')
        .innerJoin(CatalogHeader, 'hEv', joinHeader('proc', 'hEv', SECURITY_CATALOG_HEADER.evento))
        .where('(proc.parent_element_id = :moduleId OR proc.parent_element_id IS NULL)', { moduleId })
        .andWhere(
            `NOT EXISTS (
                SELECT 1 FROM core_security.module_process mp
                WHERE mp.catalog_detail_module_id = :moduleId AND mp.catalog_detail_process_id = proc.id AND mp.status = 1
            )`,
            { moduleId },
        )
        .select('proc.id', 'id')
        .addSelect(dictionaryLabelExpr('proc', langId), 'title')
        .addSelect(`''`, 'subtitle')
        .orderBy(dictionaryLabelExpr('proc', langId), 'ASC')
        .getRawMany<AssignableItemRow>();

    return { available, assigned };
}

export async function syncModuleProcessesForModule(moduleId: number, processIds: number[], actorId: string): Promise<void> {
    await datasource.transaction(async (manager) => {
        const repo = manager.getRepository(ModuleProcess);

        await repo
            .createQueryBuilder()
            .update(ModuleProcess)
            .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
            .where('catalog_detail_module_id = :moduleId', { moduleId })
            .andWhere('status = 1')
            .andWhere(processIds.length ? 'catalog_detail_process_id NOT IN (:...processIds)' : '1=1', { processIds })
            .execute();

        for (const processId of processIds) {
            const existing = await repo.findOne({
                where: { idCatalogDetailModule: moduleId, idCatalogDetailProcess: processId },
            });
            if (existing) {
                await repo.update(
                    { idModuleProcess: existing.idModuleProcess },
                    { status: 1, updatedBy: actorId, updatedAt: new Date() },
                );
            } else {
                await repo.save(
                    repo.create({
                        idCatalogDetailModule: moduleId,
                        idCatalogDetailProcess: processId,
                        status: 1,
                        createdBy: actorId,
                    }),
                );
            }
        }
    });
}

export async function findModulesByIds(moduleIds: number[]): Promise<CatalogDetail[]> {
    if (!moduleIds.length) return [];
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('m')
        .innerJoin(CatalogHeader, 'h', joinHeader('m', 'h', SECURITY_CATALOG_HEADER.aplicativo))
        .where('m.id IN (:...moduleIds)', { moduleIds })
        .getMany();
}

export async function findActiveModuleProcessesByIds(ids: number[]): Promise<ModuleProcess[]> {
    if (!ids.length) return [];
    return datasource
        .getRepository(ModuleProcess)
        .createQueryBuilder('mp')
        .where('mp.module_process_id IN (:...ids)', { ids })
        .andWhere('mp.status = 1')
        .getMany();
}

export async function findProcessesByIds(processIds: number[]): Promise<CatalogDetail[]> {
    if (!processIds.length) return [];
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('p')
        .innerJoin(CatalogHeader, 'h', joinHeader('p', 'h', SECURITY_CATALOG_HEADER.evento))
        .where('p.id IN (:...processIds)', { processIds })
        .getMany();
}

export async function findModuleById(idModule: number): Promise<CatalogDetail | null> {
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('m')
        .innerJoin(CatalogHeader, 'h', joinHeader('m', 'h', SECURITY_CATALOG_HEADER.aplicativo))
        .where('m.id = :id', { id: idModule })
        .getOne();
}

export async function findActiveProfileById(idProfile: number): Promise<CatalogDetail | null> {
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('p')
        .innerJoin(CatalogHeader, 'h', joinHeader('p', 'h', SECURITY_CATALOG_HEADER.perfil))
        .where('p.id = :id', { id: idProfile })
        .andWhere('p.status = :st', { st: 1 })
        .getOne();
}

export async function findActiveRoleById(idRole: number): Promise<CatalogDetail | null> {
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('r')
        .innerJoin(CatalogHeader, 'h', joinHeader('r', 'h', SECURITY_CATALOG_HEADER.rol))
        .where('r.id = :id', { id: idRole })
        .andWhere('r.status = :st', { st: 1 })
        .getOne();
}

export async function findActiveUserById(idUser: number): Promise<UserData | null> {
    return datasource.getRepository(UserData).findOne({
        where: { idUserData: idUser },
    });
}

/** Alta o toque de updated_at al entrar a utilería (JWT Bearer). */
export async function upsertUtilityCatalogUser(payload: {
    sub: string;
    email?: string | null;
    preferredUsername?: string | null;
    givenName?: string | null;
    familyName?: string | null;
}): Promise<void> {
    const repo = datasource.getRepository(UserData);
    const existing = await repo.findOne({ where: { sub: payload.sub } });
    const now = new Date();

    if (existing) {
        await repo.update({ idUserData: existing.idUserData }, { updatedAt: now });
        return;
    }

    await repo.insert({
        sub: payload.sub,
        email: payload.email ?? null,
        preferredUsername: payload.preferredUsername ?? null,
        givenName: payload.givenName ?? null,
        familyName: payload.familyName ?? null,
        status: 1,
    });
}

export async function findActiveAttributeTypeById(idAttributeType: number): Promise<CatalogDetail | null> {
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('t')
        .innerJoin(CatalogHeader, 'h', joinHeader('t', 'h', SECURITY_CATALOG_HEADER.tipoAtributo))
        .where('t.id = :id', { id: idAttributeType })
        .andWhere('t.status = :st', { st: 1 })
        .getOne();
}

export async function findActiveCatalogDetailById(idDetail: number): Promise<CatalogDetail | null> {
    return datasource.getRepository(CatalogDetail).findOne({
        where: { id: idDetail, status: 1 },
    });
}

export async function findActiveUserAttributeForUser(
    idUser: number,
    idUserAttribute: number,
): Promise<UserAttribute | null> {
    return datasource
        .getRepository(UserAttribute)
        .createQueryBuilder('ua')
        .where('ua.user_attribute_id = :id', { id: idUserAttribute })
        .andWhere('ua.status = 1')
        .andWhere('ua.user_data_id = :userId', { userId: idUser })
        .getOne();
}

export async function findActiveUsersByIds(userIds: number[]): Promise<UserData[]> {
    if (!userIds.length) return [];
    return datasource.getRepository(UserData).find({
        where: { idUserData: In(userIds), status: 1 },
    });
}

export async function findActivePermissionsByIds(permissionIds: number[]): Promise<CatalogDetail[]> {
    if (!permissionIds.length) return [];
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('permission')
        .innerJoin(CatalogHeader, 'h', joinHeader('permission', 'h', SECURITY_CATALOG_HEADER.permiso))
        .where('permission.id IN (:...permissionIds)', { permissionIds })
        .andWhere('permission.status = 1')
        .getMany();
}

/** Busca por preferred_username, sub, email o user_data_id textual */
export async function findUserByLookupKey(loginKey: string): Promise<UserData | null> {
    const k = loginKey.trim();
    if (!k) return null;
    return datasource
        .getRepository(UserData)
        .createQueryBuilder('ud')
        .where('ud.status = 1')
        .andWhere(
            '(ud.preferred_username = :k OR ud.sub = :k OR ud.email = :k OR CAST(ud.user_data_id AS TEXT) = :k)',
            { k },
        )
        .getOne();
}

/**
 * Resumen estructural: perfiles, aplicativos (vÃ­a perfil), pares app+evento, roles, permisos, proveedores y atributos.
 */
export async function getSecurityUserDetailsByCatalogKey(
    userKey: string,
    langIdParam?: number,
): Promise<SecurityUserDetailsResponse | null> {
    const langId = resolveLangId(langIdParam);
    const user = await findUserByLookupKey(userKey);
    if (!user) return null;

    const userId = user.idUserData;
    const userRef = mapUserDataToRef(user);

    const profilesRaw = await datasource
        .getRepository(ProfileUser)
        .createQueryBuilder('pu')
        .innerJoin(CatalogDetail, 'p', 'p.id = pu.catalog_detail_profile_id')
        .innerJoin(CatalogHeader, 'hp', joinHeader('p', 'hp', SECURITY_CATALOG_HEADER.perfil))
        .where('pu.user_data_id = :uid', { uid: userId })
        .andWhere('pu.status = 1')
        .andWhere('p.status = 1')
        .select('p.id', 'id')
        .addSelect('p.key', 'catalogKey')
        .addSelect('p.dictId', 'dictId')
        .addSelect(catalogRefLabelExpr('p', langId), 'label')
        .addSelect('p.status', 'status')
        .orderBy(catalogRefLabelExpr('p', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    const applicationsRaw = await datasource
        .getRepository(ProfileModule)
        .createQueryBuilder('pm')
        .innerJoin(
            ProfileUser,
            'pu',
            'pu.catalog_detail_profile_id = pm.catalog_detail_profile_id AND pu.user_data_id = :uid AND pu.status = 1',
            { uid: userId },
        )
        .innerJoin(CatalogDetail, 'm', 'm.id = pm.catalog_detail_module_id')
        .innerJoin(CatalogHeader, 'hm', joinHeader('m', 'hm', SECURITY_CATALOG_HEADER.aplicativo))
        .where('pm.status = 1')
        .andWhere('m.status = 1')
        .select('m.id', 'id')
        .addSelect('m.key', 'catalogKey')
        .addSelect('m.dictId', 'dictId')
        .addSelect(catalogRefLabelExpr('m', langId), 'label')
        .addSelect('m.status', 'status')
        .orderBy(catalogRefLabelExpr('m', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    const applicationsById = new Map<number, SecurityCatalogRef>();
    for (const row of applicationsRaw) {
        const ref = mapRawToCatalogRef(row);
        applicationsById.set(ref.id, ref);
    }
    const applicationsSorted = [...applicationsById.values()].sort((a, b) =>
        a.label.localeCompare(b.label, 'es'),
    );

    const pmpRaw = await datasource
        .getRepository(ProfileModuleProcess)
        .createQueryBuilder('pmp')
        .innerJoin(
            ProfileUser,
            'pu',
            'pu.catalog_detail_profile_id = pmp.catalog_detail_profile_id AND pu.user_data_id = :uid AND pu.status = 1',
            { uid: userId },
        )
        .innerJoin(ModuleProcess, 'mp', 'mp.module_process_id = pmp.module_process_id AND mp.status = 1')
        .innerJoin(CatalogDetail, 'cm', 'cm.id = mp.catalog_detail_module_id')
        .innerJoin(CatalogHeader, 'hcm', joinHeader('cm', 'hcm', SECURITY_CATALOG_HEADER.aplicativo))
        .innerJoin(CatalogDetail, 'cp', 'cp.id = mp.catalog_detail_process_id')
        .innerJoin(CatalogHeader, 'hcp', joinHeader('cp', 'hcp', SECURITY_CATALOG_HEADER.evento))
        .innerJoin(CatalogDetail, 'pf', 'pf.id = pmp.catalog_detail_profile_id')
        .innerJoin(CatalogHeader, 'hpf', joinHeader('pf', 'hpf', SECURITY_CATALOG_HEADER.perfil))
        .where('pmp.status = 1')
        .andWhere('pf.status = 1')
        .select('mp.module_process_id', 'idModuleProcess')
        .addSelect('cm.id', 'mid')
        .addSelect('cm.key', 'mCatalogKey')
        .addSelect('cm.dictId', 'mDictId')
        .addSelect(catalogRefLabelExpr('cm', langId), 'mLabel')
        .addSelect('cm.status', 'mStatus')
        .addSelect('cp.id', 'pid')
        .addSelect('cp.key', 'pCatalogKey')
        .addSelect('cp.dictId', 'pDictId')
        .addSelect(catalogRefLabelExpr('cp', langId), 'pLabel')
        .addSelect('cp.status', 'pStatus')
        .addSelect('pf.id', 'pfid')
        .addSelect('pf.key', 'pfCatalogKey')
        .addSelect('pf.dictId', 'pfDictId')
        .addSelect(catalogRefLabelExpr('pf', langId), 'pfLabel')
        .addSelect('pf.status', 'pfStatus')
        .orderBy(catalogRefLabelExpr('pf', langId), 'ASC')
        .addOrderBy(catalogRefLabelExpr('cm', langId), 'ASC')
        .addOrderBy(catalogRefLabelExpr('cp', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    const rolesRaw = await datasource
        .getRepository(RoleUser)
        .createQueryBuilder('ru')
        .innerJoin(CatalogDetail, 'r', 'r.id = ru.catalog_detail_role_id')
        .innerJoin(CatalogHeader, 'hr', joinHeader('r', 'hr', SECURITY_CATALOG_HEADER.rol))
        .where('ru.user_data_id = :uid', { uid: userId })
        .andWhere('ru.status = 1')
        .andWhere('r.status = 1')
        .select('r.id', 'id')
        .addSelect('r.key', 'catalogKey')
        .addSelect('r.dictId', 'dictId')
        .addSelect(catalogRefLabelExpr('r', langId), 'label')
        .addSelect('r.status', 'status')
        .orderBy(catalogRefLabelExpr('r', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    const permissionsRaw = await datasource
        .getRepository(RolePermission)
        .createQueryBuilder('rp')
        .innerJoin(
            RoleUser,
            'ru',
            'ru.catalog_detail_role_id = rp.catalog_detail_role_id AND ru.user_data_id = :uid AND ru.status = 1',
            { uid: userId },
        )
        .innerJoin(CatalogDetail, 'perm', 'perm.id = rp.catalog_detail_permission_id')
        .innerJoin(CatalogHeader, 'hp', joinHeader('perm', 'hp', SECURITY_CATALOG_HEADER.permiso))
        .innerJoin(CatalogDetail, 'role', 'role.id = rp.catalog_detail_role_id')
        .innerJoin(CatalogHeader, 'hr', joinHeader('role', 'hr', SECURITY_CATALOG_HEADER.rol))
        .where('rp.status = 1')
        .andWhere('perm.status = 1')
        .andWhere('role.status = 1')
        .select('perm.id', 'permId')
        .addSelect('perm.key', 'permCatalogKey')
        .addSelect('perm.dictId', 'permDictId')
        .addSelect(catalogRefLabelExpr('perm', langId), 'permLabel')
        .addSelect('perm.status', 'permStatus')
        .addSelect('role.id', 'roleId')
        .addSelect('role.key', 'roleCatalogKey')
        .addSelect('role.dictId', 'roleDictId')
        .addSelect(catalogRefLabelExpr('role', langId), 'roleLabel')
        .addSelect('role.status', 'roleStatus')
        .orderBy(catalogRefLabelExpr('role', langId), 'ASC')
        .addOrderBy(catalogRefLabelExpr('perm', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    const providersRaw = await datasource
        .getRepository(RoleProvider)
        .createQueryBuilder('rpr')
        .innerJoin(
            RoleUser,
            'ru',
            'ru.catalog_detail_role_id = rpr.catalog_detail_role_id AND ru.user_data_id = :uid AND ru.status = 1',
            { uid: userId },
        )
        .innerJoin(CatalogDetail, 'prv', 'prv.id = rpr.catalog_detail_provider_id')
        .innerJoin(CatalogHeader, 'hpv', joinHeader('prv', 'hpv', SECURITY_CATALOG_HEADER.proveedor))
        .innerJoin(CatalogDetail, 'role', 'role.id = rpr.catalog_detail_role_id')
        .innerJoin(CatalogHeader, 'hr', joinHeader('role', 'hr', SECURITY_CATALOG_HEADER.rol))
        .where('rpr.status = 1')
        .andWhere('prv.status = 1')
        .andWhere('role.status = 1')
        .select('prv.id', 'prvId')
        .addSelect('prv.key', 'prvCatalogKey')
        .addSelect('prv.dictId', 'prvDictId')
        .addSelect(catalogRefLabelExpr('prv', langId), 'prvLabel')
        .addSelect('prv.status', 'prvStatus')
        .addSelect('role.id', 'roleId')
        .addSelect('role.key', 'roleCatalogKey')
        .addSelect('role.dictId', 'roleDictId')
        .addSelect(catalogRefLabelExpr('role', langId), 'roleLabel')
        .addSelect('role.status', 'roleStatus')
        .orderBy(catalogRefLabelExpr('role', langId), 'ASC')
        .addOrderBy(catalogRefLabelExpr('prv', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    const uaRepo = datasource.getRepository(UserAttribute);
    const uaHeaders = await uaRepo.find({
        where: { userDataId: userId, status: 1 },
        order: { idUserAttribute: 'ASC' },
    });

    const attrTypeIds = [...new Set(uaHeaders.map((u) => u.idCatalogDetailAttributeType))];
    const attrValueIds = [...new Set(uaHeaders.map((u) => u.idCatalogDetailAttributeValue).filter((id): id is number => !!id))];
    const typesRaw =
        attrTypeIds.length === 0
            ? []
            : await datasource
                  .getRepository(CatalogDetail)
                  .createQueryBuilder('t')
                  .innerJoin(CatalogHeader, 'ht', joinHeader('t', 'ht', SECURITY_CATALOG_HEADER.tipoAtributo))
                  .where('t.id IN (:...ids)', { ids: attrTypeIds })
                  .select('t.id', 'id')
                  .addSelect('t.key', 'catalogKey')
                  .addSelect('t.dictId', 'dictId')
                  .addSelect(catalogRefLabelExpr('t', langId), 'label')
                  .addSelect('t.status', 'status')
                  .getRawMany<Record<string, unknown>>();

    const valuesRaw =
        attrValueIds.length === 0
            ? []
            : await datasource
                  .getRepository(CatalogDetail)
                  .createQueryBuilder('v')
                  .where('v.id IN (:...ids)', { ids: attrValueIds })
                  .andWhere('v.status = 1')
                  .select('v.id', 'id')
                  .addSelect('v.key', 'catalogKey')
                  .addSelect('v.dictId', 'dictId')
                  .addSelect(catalogRefLabelExpr('v', langId), 'label')
                  .addSelect('v.status', 'status')
                  .getRawMany<Record<string, unknown>>();

    const typeById = new Map(typesRaw.map((t) => [Number(t.id), mapRawToCatalogRef(t)]));
    const valueById = new Map(valuesRaw.map((v) => [Number(v.id), mapRawToCatalogRef(v)]));

    const attributes: SecurityUserDetailsResponse['attributes'] = uaHeaders.map((ua) => {
        const t = typeById.get(ua.idCatalogDetailAttributeType);
        const attributeType: SecurityCatalogRef = t
            ? t
            : {
                  id: ua.idCatalogDetailAttributeType,
                  catalogKey: '',
                  dictId: 0,
                  label: '',
                  status: 0,
              };
        return {
            idUserAttribute: ua.idUserAttribute,
            attributeType,
            attributeValue: ua.idCatalogDetailAttributeValue
                ? valueById.get(ua.idCatalogDetailAttributeValue) ?? {
                      id: ua.idCatalogDetailAttributeValue,
                      catalogKey: '',
                      dictId: 0,
                      label: '',
                      status: 0,
                  }
                : null,
            status: ua.status,
        };
    });

    const applicationModuleProcesses = pmpRaw.map((row) => ({
        idModuleProcess: Number(row.idModuleProcess),
        module: mapRawToCatalogRef({
            id: row.mid,
            catalogKey: row.mCatalogKey,
            dictId: row.mDictId,
            label: row.mLabel,
            status: row.mStatus,
        }),
        process: mapRawToCatalogRef({
            id: row.pid,
            catalogKey: row.pCatalogKey,
            dictId: row.pDictId,
            label: row.pLabel,
            status: row.pStatus,
        }),
        profile: mapRawToCatalogRef({
            id: row.pfid,
            catalogKey: row.pfCatalogKey,
            dictId: row.pfDictId,
            label: row.pfLabel,
            status: row.pfStatus,
        }),
    }));

    type PermRow = SecurityUserDetailsResponse['permissions'][number];
    type ProvRow = SecurityUserDetailsResponse['providers'][number];
    const permissionsSet = new Map<string, PermRow>();
    for (const row of permissionsRaw) {
        const permission = mapRawToCatalogRef({
            id: row.permId,
            catalogKey: row.permCatalogKey,
            dictId: row.permDictId,
            label: row.permLabel,
            status: row.permStatus,
        });
        const role = mapRawToCatalogRef({
            id: row.roleId,
            catalogKey: row.roleCatalogKey,
            dictId: row.roleDictId,
            label: row.roleLabel,
            status: row.roleStatus,
        });
        permissionsSet.set(`${role.id}-${permission.id}`, { permission, role });
    }

    const providersSet = new Map<string, ProvRow>();
    for (const row of providersRaw) {
        const provider = mapRawToCatalogRef({
            id: row.prvId,
            catalogKey: row.prvCatalogKey,
            dictId: row.prvDictId,
            label: row.prvLabel,
            status: row.prvStatus,
        });
        const role = mapRawToCatalogRef({
            id: row.roleId,
            catalogKey: row.roleCatalogKey,
            dictId: row.roleDictId,
            label: row.roleLabel,
            status: row.roleStatus,
        });
        providersSet.set(`${role.id}-${provider.id}`, { provider, role });
    }

    return {
        user: userRef,
        profiles: profilesRaw.map(mapRawToCatalogRef),
        applications: applicationsSorted,
        applicationModuleProcesses,
        roles: rolesRaw.map(mapRawToCatalogRef),
        permissions: [...permissionsSet.values()],
        providers: [...providersSet.values()],
        attributes,
    };
}

/** Fila del listado Catálogo Usuarios */
export interface UserCatalogListRow {
    id: number;
    username: string;
    fullName: string;
    email: string | null;
    status: number;
    createdAt: Date;
    modifiedAt: Date;
}

export interface CatalogDetailGridRow {
    id: number;
    name: string;
    description: string;
}

export interface UserModuleProcessEventRow {
    moduleProcessId: number;
    processId: number;
    name: string;
    description: string;
    assigned: boolean;
}

function buildUserCatalogBaseQuery(filters: SecuritySearchFilter) {
    const qb = datasource.getRepository(UserData).createQueryBuilder('ud');
    applyUserDataFilters(qb, 'ud', filters);
    return qb;
}

const USER_CATALOG_SORT_FIELDS = new Set(['id', 'createdAt', 'username', 'fullName']);

export async function listUsersCatalogPaginated(
    filters: SecuritySearchFilter,
    page: number,
    limit: number,
    sortBy: string,
    sortDir: 'ASC' | 'DESC',
): Promise<{ items: UserCatalogListRow[]; total: number }> {
    const safeLimit = Math.min(100, Math.max(1, limit));
    const safePage = Math.max(1, page);
    const sort = USER_CATALOG_SORT_FIELDS.has(sortBy) ? sortBy : 'id';
    const dir = sortDir === 'DESC' ? 'DESC' : 'ASC';

    const base = buildUserCatalogBaseQuery(filters);
    const total = await base.clone().getCount();

    const modifiedSub = `(SELECT MAX(m) FROM (
        SELECT MAX(pu.updated_at) AS m FROM core_security.profile_user pu WHERE pu.user_data_id = ud.user_data_id
        UNION ALL
        SELECT MAX(ru.updated_at) FROM core_security.role_user ru WHERE ru.user_data_id = ud.user_data_id
        UNION ALL
        SELECT MAX(ua.updated_at) FROM core_security.user_attribute ua WHERE ua.user_data_id = ud.user_data_id
    ) s)`;

    let orderExpr: string;
    switch (sort) {
        case 'createdAt':
            orderExpr = 'ud.created_at';
            break;
        case 'username':
            orderExpr = 'COALESCE(ud.preferred_username, ud.sub)';
            break;
        case 'fullName':
            orderExpr = `LOWER(TRIM(CONCAT(COALESCE(ud.given_name,''),' ',COALESCE(ud.family_name,''))))`;
            break;
        default:
            orderExpr = 'ud.user_data_id';
    }

    const rows = await base
        .clone()
        .select('ud.user_data_id', 'id')
        .addSelect('COALESCE(ud.preferred_username, ud.sub)', 'username')
        .addSelect(`TRIM(CONCAT(COALESCE(ud.given_name,''),' ',COALESCE(ud.family_name,'')))`, 'fullName')
        .addSelect('ud.email', 'email')
        .addSelect('ud.status', 'status')
        .addSelect('ud.created_at', 'createdAt')
        .addSelect(`COALESCE(${modifiedSub}, ud.created_at)`, 'modifiedAt')
        .orderBy(orderExpr, dir)
        .skip((safePage - 1) * safeLimit)
        .take(safeLimit)
        .getRawMany<Record<string, unknown>>();

    const items: UserCatalogListRow[] = rows.map((row) => ({
        id: Number(row.id),
        username: String(row.username ?? ''),
        fullName: String(row.fullName ?? '').trim() || String(row.username ?? ''),
        email: row.email == null || row.email === '' ? null : String(row.email),
        status: Number(row.status ?? 0),
        createdAt: new Date(String(row.createdAt)),
        modifiedAt: new Date(String(row.modifiedAt ?? row.createdAt)),
    }));

    return { items, total };
}

export async function listUsersCatalogForExport(
    filters: SecuritySearchFilter,
    maxRows: number,
): Promise<{ items: UserCatalogListRow[]; total: number; truncated: boolean }> {
    const cap = Math.min(50_000, Math.max(1, maxRows));
    const base = buildUserCatalogBaseQuery(filters);
    const total = await base.clone().getCount();

    const modifiedSub = `(SELECT MAX(m) FROM (
        SELECT MAX(pu.updated_at) AS m FROM core_security.profile_user pu WHERE pu.user_data_id = ud.user_data_id
        UNION ALL
        SELECT MAX(ru.updated_at) FROM core_security.role_user ru WHERE ru.user_data_id = ud.user_data_id
        UNION ALL
        SELECT MAX(ua.updated_at) FROM core_security.user_attribute ua WHERE ua.user_data_id = ud.user_data_id
    ) s)`;

    const rows = await base
        .clone()
        .select('ud.user_data_id', 'id')
        .addSelect('COALESCE(ud.preferred_username, ud.sub)', 'username')
        .addSelect(`TRIM(CONCAT(COALESCE(ud.given_name,''),' ',COALESCE(ud.family_name,'')))`, 'fullName')
        .addSelect('ud.email', 'email')
        .addSelect('ud.status', 'status')
        .addSelect('ud.created_at', 'createdAt')
        .addSelect(`COALESCE(${modifiedSub}, ud.created_at)`, 'modifiedAt')
        .orderBy('ud.user_data_id', 'ASC')
        .take(cap)
        .getRawMany<Record<string, unknown>>();

    const items: UserCatalogListRow[] = rows.map((row) => ({
        id: Number(row.id),
        username: String(row.username ?? ''),
        fullName: String(row.fullName ?? '').trim() || String(row.username ?? ''),
        email: row.email == null || row.email === '' ? null : String(row.email),
        status: Number(row.status ?? 0),
        createdAt: new Date(String(row.createdAt)),
        modifiedAt: new Date(String(row.modifiedAt ?? row.createdAt)),
    }));

    return { items, total, truncated: total > cap };
}

export async function findModuleProcessRow(idModuleProcess: number): Promise<ModuleProcess | null> {
    return datasource.getRepository(ModuleProcess).findOne({
        where: { idModuleProcess, status: 1 },
    });
}

export async function findProfileIdsLinkingUserToModule(userId: number, moduleCatalogId: number): Promise<number[]> {
    const raw = await datasource
        .getRepository(ProfileModule)
        .createQueryBuilder('pm')
        .innerJoin(
            ProfileUser,
            'pu',
            'pu.catalog_detail_profile_id = pm.catalog_detail_profile_id AND pu.user_data_id = :userId AND pu.status = 1',
            { userId },
        )
        .where('pm.catalog_detail_module_id = :moduleId', { moduleId: moduleCatalogId })
        .andWhere('pm.status = 1')
        .select('pm.catalog_detail_profile_id', 'pid')
        .distinct(true)
        .getRawMany<Record<string, unknown>>();
    return raw.map((r) => Number(r.pid));
}

export async function setUserModuleProcessForProfiles(
    profileIds: number[],
    moduleProcessId: number,
    assign: boolean,
    actorId: string,
): Promise<void> {
    if (!profileIds.length) return;
    await datasource.transaction(async (manager) => {
        const repo = manager.getRepository(ProfileModuleProcess);
        if (assign) {
            for (const profileId of profileIds) {
                await upsertComposite(
                    manager,
                    ProfileModuleProcess,
                    { idCatalogDetailProfile: profileId, idModuleProcess: moduleProcessId },
                    {
                        idCatalogDetailProfile: profileId,
                        idModuleProcess: moduleProcessId,
                        status: 1,
                        createdBy: actorId,
                    },
                    { status: 1, updatedBy: actorId, updatedAt: new Date() },
                );
            }
        } else {
            await repo
                .createQueryBuilder()
                .update(ProfileModuleProcess)
                .set({ status: 0, updatedBy: actorId, updatedAt: new Date() })
                .where('module_process_id = :mpid', { mpid: moduleProcessId })
                .andWhere('catalog_detail_profile_id IN (:...pids)', { pids: profileIds })
                .andWhere('status = 1')
                .execute();
        }
    });
}

export async function listModuleProcessesWithUserAssignment(
    userId: number,
    moduleCatalogId: number,
    langIdParam?: number,
): Promise<UserModuleProcessEventRow[]> {
    const langId = resolveLangId(langIdParam);

    const rows = await datasource
        .getRepository(ModuleProcess)
        .createQueryBuilder('mp')
        .innerJoin(CatalogDetail, 'cp', 'cp.id = mp.catalog_detail_process_id')
        .innerJoin(CatalogHeader, 'hcp', joinHeader('cp', 'hcp', SECURITY_CATALOG_HEADER.evento))
        .where('mp.catalog_detail_module_id = :moduleId', { moduleId: moduleCatalogId })
        .andWhere('mp.status = 1')
        .andWhere('cp.status = 1')
        .select('MIN(mp.module_process_id)', 'moduleProcessId')
        .addSelect('cp.id', 'processId')
        .addSelect(catalogRefLabelExpr('cp', langId), 'name')
        .addSelect(`COALESCE(NULLIF(TRIM(cp.value), ''), cp.key)`, 'description')
        .addSelect(
            `EXISTS (
                SELECT 1 FROM core_security.profile_module_process pmp
                INNER JOIN core_security.module_process mp2
                    ON mp2.module_process_id = pmp.module_process_id AND mp2.status = 1
                INNER JOIN core_security.profile_user pu ON pu.catalog_detail_profile_id = pmp.catalog_detail_profile_id
                    AND pu.user_data_id = :userId AND pu.status = 1
                INNER JOIN core_security.profile_module pm ON pm.catalog_detail_profile_id = pmp.catalog_detail_profile_id
                    AND pm.catalog_detail_module_id = :moduleId AND pm.status = 1
                WHERE mp2.catalog_detail_module_id = :moduleId
                    AND mp2.catalog_detail_process_id = cp.id
                    AND pmp.status = 1
            )`,
            'assigned',
        )
        .groupBy('cp.id')
        .addGroupBy(catalogRefLabelExpr('cp', langId))
        .addGroupBy(`COALESCE(NULLIF(TRIM(cp.value), ''), cp.key)`)
        .setParameter('userId', userId)
        .orderBy(catalogRefLabelExpr('cp', langId), 'ASC')
        .getRawMany<Record<string, unknown>>();

    return rows.map((row) => ({
        moduleProcessId: Number(row.moduleProcessId),
        processId: Number(row.processId),
        name: String(row.name ?? ''),
        description: String(row.description ?? ''),
        assigned: row.assigned === true || row.assigned === 'true' || String(row.assigned) === '1',
    }));
}

export async function findActiveApplicationById(moduleId: number): Promise<CatalogDetail | null> {
    return datasource
        .getRepository(CatalogDetail)
        .createQueryBuilder('m')
        .innerJoin(CatalogHeader, 'hm', joinHeader('m', 'hm', SECURITY_CATALOG_HEADER.aplicativo))
        .where('m.id = :id', { id: moduleId })
        .andWhere('m.status = 1')
        .getOne();
}

export async function linkProfileUserOnly(userId: number, profileId: number, actorId: string): Promise<void> {
    await datasource.transaction(async (manager) => {
        await upsertComposite(
            manager,
            ProfileUser,
            { idCatalogDetailProfile: profileId, userDataId: userId },
            {
                idCatalogDetailProfile: profileId,
                userDataId: userId,
                status: 1,
                createdBy: actorId,
            },
            { status: 1, updatedBy: actorId, updatedAt: new Date() },
        );
    });
}

export function userDataLookupKey(user: UserData): string {
    return String(user.preferredUsername ?? user.sub ?? user.idUserData);
}

export async function getUserCatalogHeaderById(userId: number): Promise<UserCatalogListRow | null> {
    const modifiedSub = `(SELECT MAX(m) FROM (
        SELECT MAX(pu.updated_at) AS m FROM core_security.profile_user pu WHERE pu.user_data_id = ud.user_data_id
        UNION ALL
        SELECT MAX(ru.updated_at) FROM core_security.role_user ru WHERE ru.user_data_id = ud.user_data_id
        UNION ALL
        SELECT MAX(ua.updated_at) FROM core_security.user_attribute ua WHERE ua.user_data_id = ud.user_data_id
    ) s)`;

    const row = await datasource
        .getRepository(UserData)
        .createQueryBuilder('ud')
        .where('ud.user_data_id = :id', { id: userId })
        .select('ud.user_data_id', 'id')
        .addSelect('COALESCE(ud.preferred_username, ud.sub)', 'username')
        .addSelect(`TRIM(CONCAT(COALESCE(ud.given_name,''),' ',COALESCE(ud.family_name,'')))`, 'fullName')
        .addSelect('ud.email', 'email')
        .addSelect('ud.status', 'status')
        .addSelect('ud.created_at', 'createdAt')
        .addSelect(`COALESCE(${modifiedSub}, ud.created_at)`, 'modifiedAt')
        .getRawOne<Record<string, unknown>>();

    if (!row) return null;
    return {
        id: Number(row.id),
        username: String(row.username ?? ''),
        fullName: String(row.fullName ?? '').trim() || String(row.username ?? ''),
        email: row.email == null || row.email === '' ? null : String(row.email),
        status: Number(row.status ?? 0),
        createdAt: new Date(String(row.createdAt)),
        modifiedAt: new Date(String(row.modifiedAt ?? row.createdAt)),
    };
}
