import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import { DictionaryLang } from '@/entities/DictionaryLang.entity.js';
import * as headerRepo from '@/repositories/catalogHeader.repo.js';
import * as detailRepo from '@/repositories/catalogDetail.repo.js';
import * as dictRepo from '@/repositories/dictionaryLang.repo.js';
import * as elementMapper from '@/mappers/catalogElement.mapper.js';
import { GenericException } from '@/exceptions/GenericException.js';
import type {
    CatalogElementCreateDto,
    CatalogElementUpdateDto,
    CatalogElementDto,
    CatalogElementPageResponse,
    CatalogSimpleDto
} from '@/dto/catalog.dto.js';
import type { PageOptions } from '@/repositories/catalogDetail.repo.js';

const CATALOG_TYPE_PRIMARIO = 'PRIMARIO';
const CATALOG_TYPE_HIERARCHICAL = 'HIERARCHICAL';
const DEFAULT_LANG_ID = 1;

export interface FindElementsParams {
    catalogId: number;
    elementId?: number | null;
    element?: string | null;
    value?: string | null;
    parentCatalogId?: number | null;
    parentElementId?: number | null;
    status?: number | null;
    key?: string | null;
    page: number;
    pageSize: number;
    sortBy: string;
    sortDir: 'ASC' | 'DESC';
}

export async function findElements(params: FindElementsParams): Promise<CatalogElementPageResponse> {
    const catalog = await headerRepo.findById(params.catalogId);
    if (!catalog) {
        throw new GenericException(404, `Catálogo no encontrado con ID: ${params.catalogId}`);
    }

    const pageOptions: PageOptions = {
        page: Math.max(0, params.page - 1),
        pageSize: params.pageSize,
        sortBy: params.sortBy,
        sortDir: params.sortDir
    };

    const normalize = (s?: string | null): string | null => {
        if (s == null) return null;
        const trimmed = s.trim();
        return trimmed === '' ? null : trimmed;
    };

    const result = await detailRepo.findElementsPaged(
        {
            catalogId: params.catalogId,
            elementId: params.elementId,
            element: normalize(params.element),
            value: normalize(params.value),
            parentCatalogId: params.parentCatalogId,
            parentElementId: params.parentElementId,
            status: params.status,
            key: normalize(params.key)
        },
        pageOptions
    );

    const items = await elementMapper.toDtoList(result.items);

    return {
        items,
        page: result.page + 1,
        pageSize: result.pageSize,
        total: result.total,
        totalPages: result.totalPages,
        hasNext: result.page + 1 < result.totalPages,
        hasPrevious: result.page > 0
    };
}

export async function findElementById(catalogId: number, elementId: number): Promise<CatalogElementDto | null> {
    const detail = await detailRepo.findById(elementId);
    if (!detail || detail.header?.id !== catalogId) return null;
    return elementMapper.toDto(detail);
}

function validateDates(validFrom?: string | null, validTo?: string | null): void {
    if (!validFrom) return;

    if (validTo) {
        const from = new Date(validFrom);
        const to = new Date(validTo);
        const now = new Date();
        now.setHours(0, 0, 0, 0);

        if (to < from) {
            throw new GenericException(400, 'La fecha de fin de vigencia no puede ser anterior a la fecha de inicio.');
        }

        if (to <= now) {
            throw new GenericException(400, 'La fecha de fin de vigencia debe ser mayor a la fecha actual.');
        }
    }
}

async function validateParentRelation(
    catalog: CatalogHeader,
    parentCatalogId: number | null | undefined,
    parentElementId: number | null | undefined
): Promise<void> {
    const isPrimary =
        catalog.catalogType?.toUpperCase() === CATALOG_TYPE_PRIMARIO ||
        catalog.catalogType?.toUpperCase() === CATALOG_TYPE_HIERARCHICAL;

    if (isPrimary) {
        if (parentCatalogId != null || parentElementId != null) {
            throw new GenericException(400, 'Los elementos de catálogos primarios no pueden tener relación padre.');
        }
        return;
    }

    const hasCatalog = parentCatalogId != null;
    const hasElement = parentElementId != null;
    if (hasCatalog !== hasElement) {
        throw new GenericException(
            400,
            'Debe proporcionar tanto el catálogo padre como el elemento padre, o dejar ambos vacíos'
        );
    }

    if (parentCatalogId == null) return;

    const parentElement = await detailRepo.findById(parentElementId!);
    if (!parentElement) {
        throw new GenericException(400, 'El elemento padre seleccionado no existe en el sistema');
    }

    const parentCatalog = await headerRepo.findById(parentCatalogId);
    if (!parentCatalog) {
        throw new GenericException(400, 'El catálogo padre debe ser de tipo primario o jerárquico');
    }

    const pType = parentCatalog.catalogType?.toUpperCase();
    if (pType !== CATALOG_TYPE_PRIMARIO && pType !== CATALOG_TYPE_HIERARCHICAL) {
        throw new GenericException(400, 'El catálogo padre debe ser de tipo primario o jerárquico');
    }

    if (parentElement.header?.id !== parentCatalogId) {
        throw new GenericException(400, 'El elemento padre no pertenece al catálogo padre seleccionado');
    }
}

async function getElementNameFromDict(detail: CatalogDetail): Promise<string> {
    if (detail.dictId != null && detail.dictId > 0) {
        const dict = await dictRepo.findByDictIdAndLangId(detail.dictId, DEFAULT_LANG_ID);
        if (dict) return dict.description ?? detail.key;
    }
    return detail.key;
}

interface DuplicateCheckContext {
    isPrimaryLike: boolean;
    hasParentRelation: boolean;
    parentCatalogId: number | null;
    parentElementId: number | null;
    trimmedTarget: string;
    excludeId: number | null;
}

function throwIfSameParent(el: CatalogDetail, ctx: DuplicateCheckContext): void {
    if (ctx.isPrimaryLike) {
        throw new GenericException(400, 'Ya existe un elemento con este nombre en el catálogo.');
    }
    if (!ctx.hasParentRelation) {
        const existingHasNoParent = el.parentCatalogId == null && el.parentElementId == null;
        if (existingHasNoParent) {
            throw new GenericException(
                400,
                'Ya existe un elemento con este nombre sin relación padre en el catálogo.',
            );
        }
        return;
    }
    if (
        el.parentCatalogId === ctx.parentCatalogId &&
        el.parentElementId === ctx.parentElementId
    ) {
        throw new GenericException(
            400,
            'Ya existe un elemento con este nombre para el elemento padre seleccionado.',
        );
    }
}

async function assertElementNotDuplicate(
    el: CatalogDetail,
    ctx: DuplicateCheckContext,
): Promise<void> {
    if (ctx.excludeId != null && el.id === ctx.excludeId) return;
    const existingName = await getElementNameFromDict(el);
    if (!existingName) return;
    if (existingName.trim().toLowerCase() !== ctx.trimmedTarget) return;
    throwIfSameParent(el, ctx);
}

async function checkDuplicateElementName(
    catalog: CatalogHeader,
    elementName: string,
    parentCatalogId: number | null,
    parentElementId: number | null,
    excludeId: number | null,
): Promise<void> {
    const trimmedTarget = elementName.trim().toLowerCase();
    if (!trimmedTarget) return;

    const catalogType = (catalog.catalogType ?? '').toUpperCase();
    const ctx: DuplicateCheckContext = {
        isPrimaryLike:
            catalogType === CATALOG_TYPE_PRIMARIO || catalogType === CATALOG_TYPE_HIERARCHICAL,
        hasParentRelation: parentCatalogId != null && parentElementId != null,
        parentCatalogId,
        parentElementId,
        trimmedTarget,
        excludeId,
    };

    const existing = await detailRepo.findByHeaderIdOrderBySortOrder(catalog.id);
    for (const el of existing) {
        await assertElementNotDuplicate(el, ctx);
    }
}

async function generateNextKey(catalog: CatalogHeader, startingFrom?: number): Promise<string> {
    const prefix = catalog.prefix ?? 'EL';
    const maxNum = await detailRepo.findMaxKeyNumberByHeaderIdAndPrefix(catalog.id, prefix);
    const nextNum = Math.max(maxNum + 1, startingFrom ?? 1);
    return `${prefix}${String(nextNum).padStart(4, '0')}`;
}

function isUniqueKeyViolation(err: unknown): boolean {
    const e = err as { code?: string; driverError?: { code?: string }; detail?: string };
    const code = e?.code ?? e?.driverError?.code;
    if (code !== '23505') return false;
    const detail = (e?.detail ?? '').toLowerCase();
    return detail.includes('header_id') && detail.includes('key');
}

function extractKeyNumberFromError(err: unknown, prefix: string): number | null {
    const detail = (err as { detail?: string })?.detail ?? '';
    const match = detail.match(new RegExp(`${prefix.replace(/[.*+?^${}()|[\\]\\\\]/g, '\\$&')}(\\d+)`));
    if (!match?.[1]) return null;
    const parsed = Number.parseInt(match[1], 10);
    return Number.isFinite(parsed) ? parsed : null;
}

async function createDictionaryEntry(elementName: string): Promise<number> {
    const MAX_ATTEMPTS = 8;
    let lastErr: unknown = null;

    for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
        try {
            return await datasource.transaction(async (manager) => {
                const dictRepoInstance = manager.getRepository(DictionaryLang);

                const maxRow = await manager
                    .createQueryBuilder()
                    .select('COALESCE(MAX(d.dict_id), 0)', 'max')
                    .from(DictionaryLang, 'd')
                    .where('d.lang_id = :langId', { langId: DEFAULT_LANG_ID })
                    .getRawOne<{ max: number | string }>();

                const rawMax = maxRow?.max;
                const currentMax =
                    typeof rawMax === 'string' ? Number.parseInt(rawMax, 10) : (rawMax ?? 0);
                const bump = attempt === 0 ? 1 : 1 + Math.floor(Math.random() * attempt * 4);
                const nextDictId = (Number.isFinite(currentMax) ? currentMax : 0) + bump;

                const entry = dictRepoInstance.create({
                    dictId: nextDictId,
                    langId: DEFAULT_LANG_ID,
                    description: elementName,
                });
                const saved = await dictRepoInstance.save(entry);
                return saved.dictId;
            });
        } catch (err) {
            lastErr = err;
            if (isDictionaryUniqueViolation(err)) continue;
            throw err;
        }
    }

    throw lastErr ?? new Error('No fue posible asignar un dict_id único después de múltiples intentos.');
}

function isDictionaryUniqueViolation(err: unknown): boolean {
    const e = err as { code?: string; driverError?: { code?: string }; detail?: string };
    const code = e?.code ?? e?.driverError?.code;
    if (code !== '23505') return false;
    const detail = (e?.detail ?? '').toLowerCase();
    return detail.includes('dict_id') && detail.includes('lang_id');
}

async function updateDictionaryEntry(dictId: number | null | undefined, newName: string): Promise<void> {
    if (dictId == null || dictId <= 0) return;
    const entry = await dictRepo.findByDictIdAndLangId(dictId, DEFAULT_LANG_ID);
    if (!entry) return;
    entry.description = newName;
    await dictRepo.save(entry);
}

async function resolveParentIdsForCreate(
    dto: CatalogElementCreateDto,
): Promise<{ parentCatId: number | null; parentElemId: number | null }> {
    let parentCatId = dto.parentCatalogId ?? null;
    const parentElemId = dto.parentElementId ?? null;
    if (parentElemId != null && parentCatId == null) {
        const parent = await detailRepo.findById(parentElemId);
        if (parent?.header) {
            parentCatId = parent.header.id;
        }
    }
    return { parentCatId, parentElemId };
}

function normalizeExternalKey(value: string | null | undefined): string | null {
    return value?.trim() ? value : null;
}

function buildNewDetail(
    catalog: CatalogHeader,
    dto: CatalogElementCreateDto,
    generatedKey: string,
    dictId: number,
    parentCatId: number | null,
    parentElemId: number | null,
    userId: string,
): CatalogDetail {
    const detailRepoInstance = datasource.getRepository(CatalogDetail);
    return detailRepoInstance.create({
        header: catalog,
        headerId: catalog.id,
        key: generatedKey,
        value: dto.value ?? null,
        validFrom: dto.validFrom ?? null,
        validTo: dto.validTo ?? null,
        parentCatalogId: parentCatId,
        parentElementId: parentElemId,
        externalKey: normalizeExternalKey(dto.externalKey),
        sortOrder: dto.sortOrder ?? 0,
        attributes: dto.attributes ?? null,
        status: CatalogDetail.STATUS_ACTIVE,
        dictId,
        createdBy: userId,
    });
}

async function persistWithUniqueKey(
    catalog: CatalogHeader,
    dto: CatalogElementCreateDto,
    dictId: number,
    parentCatId: number | null,
    parentElemId: number | null,
    userId: string,
): Promise<CatalogDetail | null> {
    const detailRepoInstance = datasource.getRepository(CatalogDetail);
    const MAX_KEY_ATTEMPTS = 5;
    let startingFrom: number | undefined;

    for (let attempt = 1; attempt <= MAX_KEY_ATTEMPTS; attempt++) {
        const generatedKey = await generateNextKey(catalog, startingFrom);
        const detail = buildNewDetail(catalog, dto, generatedKey, dictId, parentCatId, parentElemId, userId);
        try {
            return await detailRepoInstance.save(detail);
        } catch (err) {
            if (!isUniqueKeyViolation(err) || attempt >= MAX_KEY_ATTEMPTS) {
                throw err;
            }
            const collisionNum = extractKeyNumberFromError(err, catalog.prefix ?? 'EL');
            startingFrom = collisionNum != null ? collisionNum + 1 : (startingFrom ?? 1) + 1;
        }
    }
    return null;
}

async function reactivateCatalogIfInactive(catalog: CatalogHeader): Promise<void> {
    if (catalog.status !== 0) return;
    catalog.status = 1;
    await headerRepo.save(catalog);
}

export async function createElement(
    catalogId: number,
    dto: CatalogElementCreateDto,
    userId: string,
): Promise<CatalogElementDto> {
    const catalog = await headerRepo.findById(catalogId);
    if (!catalog) {
        throw new GenericException(404, `Catálogo no encontrado con ID: ${catalogId}`);
    }

    validateDates(dto.validFrom, dto.validTo);

    const { parentCatId, parentElemId } = await resolveParentIdsForCreate(dto);
    await validateParentRelation(catalog, parentCatId, parentElemId);
    await checkDuplicateElementName(catalog, dto.element, parentCatId, parentElemId, null);

    const dictId = await createDictionaryEntry(dto.element);
    const saved = await persistWithUniqueKey(catalog, dto, dictId, parentCatId, parentElemId, userId);
    if (!saved) {
        throw new GenericException(
            500,
            'No fue posible generar una clave única para el elemento después de varios intentos. Inténtalo nuevamente.',
        );
    }

    await reactivateCatalogIfInactive(catalog);

    const result = await elementMapper.toDto(saved);
    if (!result) {
        throw new GenericException(500, 'Error creando elemento');
    }
    return result;
}

function applyValueAndDates(detail: CatalogDetail, dto: CatalogElementUpdateDto): void {
    if (dto.value != null) detail.value = dto.value;
    if (dto.validFrom != null) detail.validFrom = dto.validFrom;
    if (dto.validTo != null) detail.validTo = dto.validTo;
}

async function resolveEffectiveParent(
    catalog: CatalogHeader,
    detail: CatalogDetail,
    dto: CatalogElementUpdateDto,
): Promise<{ dtoTouchesParent: boolean; parentCatId: number | null; parentElemId: number | null }> {
    const dtoTouchesParent =
        dto.parentCatalogId !== undefined || dto.parentElementId !== undefined;
    if (!dtoTouchesParent) {
        return {
            dtoTouchesParent,
            parentCatId: detail.parentCatalogId ?? null,
            parentElemId: detail.parentElementId ?? null,
        };
    }
    let nextCatId = dto.parentCatalogId ?? null;
    const nextElemId = dto.parentElementId ?? null;
    if (nextElemId != null && nextCatId == null) {
        const parent = await detailRepo.findById(nextElemId);
        if (parent?.header) {
            nextCatId = parent.header.id;
        }
    }
    await validateParentRelation(catalog, nextCatId, nextElemId);
    return { dtoTouchesParent, parentCatId: nextCatId, parentElemId: nextElemId };
}

async function ensureNoDuplicateOnUpdate(
    catalog: CatalogHeader,
    detail: CatalogDetail,
    dto: CatalogElementUpdateDto,
    parentCatId: number | null,
    parentElemId: number | null,
): Promise<void> {
    const nameChanging = !!(dto.element && dto.element.trim() !== '');
    const parentChanging =
        parentCatId !== (detail.parentCatalogId ?? null) ||
        parentElemId !== (detail.parentElementId ?? null);
    if (!nameChanging && !parentChanging) return;
    const effectiveName = nameChanging ? dto.element! : await getElementNameFromDict(detail);
    if (!effectiveName || effectiveName.trim() === '') return;
    await checkDuplicateElementName(catalog, effectiveName, parentCatId, parentElemId, detail.id);
}

function applyRemainingFields(detail: CatalogDetail, dto: CatalogElementUpdateDto): void {
    if (dto.sortOrder != null) detail.sortOrder = dto.sortOrder;
    if (dto.attributes != null) detail.attributes = dto.attributes;
    if (dto.externalKey !== undefined) {
        detail.externalKey = normalizeExternalKey(dto.externalKey);
    }
    if (dto.status != null) detail.status = dto.status;
}

export async function updateElement(
    catalogId: number,
    elementId: number,
    dto: CatalogElementUpdateDto,
    userId: string,
): Promise<CatalogElementDto> {
    const catalog = await headerRepo.findById(catalogId);
    if (!catalog) {
        throw new GenericException(404, `Catálogo no encontrado con ID: ${catalogId}`);
    }

    const detail = await detailRepo.findById(elementId);
    if (!detail || detail.header?.id !== catalogId) {
        throw new GenericException(404, `Elemento no encontrado con ID: ${elementId}`);
    }

    applyValueAndDates(detail, dto);
    validateDates(detail.validFrom, detail.validTo);

    const { dtoTouchesParent, parentCatId, parentElemId } = await resolveEffectiveParent(catalog, detail, dto);

    await ensureNoDuplicateOnUpdate(catalog, detail, dto, parentCatId, parentElemId);

    if (dto.element && dto.element.trim() !== '') {
        await updateDictionaryEntry(detail.dictId, dto.element);
    }

    if (dtoTouchesParent) {
        detail.parentCatalogId = parentCatId;
        detail.parentElementId = parentElemId;
    }

    applyRemainingFields(detail, dto);

    detail.updatedBy = userId;
    detail.updatedAt = new Date();

    const saved = await detailRepo.save(detail);
    const result = await elementMapper.toDto(saved);
    if (!result) throw new GenericException(500, 'Error actualizando elemento');
    return result;
}

export async function changeStatus(
    elementId: number,
    newStatus: number,
    userId: string
): Promise<CatalogElementDto> {
    const detail = await detailRepo.findById(elementId);
    if (!detail) {
        throw new GenericException(404, `Elemento no encontrado con ID: ${elementId}`);
    }

    if (newStatus !== 0 && newStatus !== 1) {
        throw new GenericException(400, 'Estatus inválido. Use 0 (Inactivo) o 1 (Activo).');
    }

    detail.status = newStatus;
    detail.updatedBy = userId;
    detail.updatedAt = new Date();

    const saved = await detailRepo.save(detail);
    const result = await elementMapper.toDto(saved);
    if (!result) throw new GenericException(500, 'Error cambiando estatus');
    return result;
}

export async function findPrimaryCatalogs(): Promise<CatalogSimpleDto[]> {
    const primaries = await headerRepo.findByCatalogTypeAndStatus(CATALOG_TYPE_PRIMARIO, 1);
    const hierarchicals = await headerRepo.findByCatalogTypeAndStatus(CATALOG_TYPE_HIERARCHICAL, 1);
    return elementMapper.toSimpleDtoList([...primaries, ...hierarchicals]);
}

export async function findActiveElements(catalogId: number): Promise<CatalogElementDto[]> {
    const elements = await detailRepo.findByHeaderIdAndStatus(catalogId, CatalogDetail.STATUS_ACTIVE);
    return elementMapper.toDtoList(elements);
}

export async function findCatalogById(catalogId: number): Promise<CatalogSimpleDto | null> {
    const header = await headerRepo.findById(catalogId);
    if (!header) return null;
    return elementMapper.toSimpleDto(header);
}

