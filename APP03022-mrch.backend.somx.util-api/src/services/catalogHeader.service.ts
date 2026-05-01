import * as headerRepo from '@/repositories/catalogHeader.repo.js';
import * as detailRepo from '@/repositories/catalogDetail.repo.js';
import * as relationRepo from '@/repositories/catalogDetailRelation.repo.js';
import * as dictRepo from '@/repositories/dictionaryLang.repo.js';
import * as headerMapper from '@/mappers/catalogHeader.mapper.js';
import * as detailMapper from '@/mappers/catalogDetail.mapper.js';
import { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import { CatalogDetail } from '@/entities/CatalogDetail.entity.js';
import { CatalogDetailRelation } from '@/entities/CatalogDetailRelation.entity.js';
import type { CatalogDetailDto, CatalogHeaderDto } from '@/dto/catalog.dto.js';

const DEFAULT_LANG_ID = 1;

function today(): string {
    return new Date().toISOString().slice(0, 10);
}

async function loadDictionary(langId: number): Promise<Map<number, string>> {
    const rows = await dictRepo.findByLangId(langId);
    const map = new Map<number, string>();
    for (const r of rows) {
        if (!map.has(r.dictId)) {
            map.set(r.dictId, r.description);
        }
    }
    return map;
}

export async function findAllCatalogs(langId?: number | null): Promise<CatalogHeaderDto[]> {
    void langId;
    const headers = await headerRepo.findByStatus(CatalogHeader.STATUS_ACTIVE);
    return headerMapper.toDtoList(headers);
}

export async function findCatalogsByModule(module: string, langId?: number | null): Promise<CatalogHeaderDto[]> {
    void langId;
    const headers = await headerRepo.findByModuleAndStatus(module, CatalogHeader.STATUS_ACTIVE);
    return headerMapper.toDtoList(headers);
}

export async function findCatalogByCode(code: string, langId?: number | null): Promise<CatalogHeaderDto | null> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);
    const currentDate = today();

    const header = await headerRepo.findByCode(code);
    if (!header || header.status !== CatalogHeader.STATUS_ACTIVE) return null;

    const dto = headerMapper.toDto(header);
    if (!dto) return null;

    const details = await detailRepo.findByHeaderIdAndStatusAndValidDate(
        header.id,
        CatalogDetail.STATUS_ACTIVE,
        currentDate
    );
    dto.details = detailMapper.toDtoList(details, dictionary);
    return dto;
}

export async function findDetailsByCode(code: string, langId?: number | null): Promise<CatalogDetailDto[]> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);
    const currentDate = today();

    const details = await detailRepo.findByHeaderCodeAndStatusAndValidDate(
        code,
        CatalogDetail.STATUS_ACTIVE,
        currentDate
    );
    return detailMapper.toDtoList(details, dictionary);
}

export async function findDetailByCodeAndKey(
    code: string,
    key: string,
    langId?: number | null
): Promise<CatalogDetailDto | null> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);

    const header = await headerRepo.findByCode(code);
    if (!header) return null;

    const detail = await detailRepo.findByHeaderIdAndKey(header.id, key);
    if (!detail || detail.status !== CatalogDetail.STATUS_ACTIVE) return null;

    return detailMapper.toDto(detail, dictionary);
}

export async function findDetailByKey(key: string, langId?: number | null): Promise<CatalogDetailDto | null> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);
    const currentDate = today();

    const detail = await detailRepo.findByKeyAndStatusAndValidDate(
        key,
        CatalogDetail.STATUS_ACTIVE,
        currentDate
    );
    if (!detail) return null;
    return detailMapper.toDto(detail, dictionary);
}

export async function findCatalogById(id: number, langId?: number | null): Promise<CatalogHeaderDto | null> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);
    const currentDate = today();

    const header = await headerRepo.findById(id);
    if (!header || header.status !== CatalogHeader.STATUS_ACTIVE) return null;

    const dto = headerMapper.toDto(header);
    if (!dto) return null;

    const details = await detailRepo.findByHeaderIdAndStatusAndValidDate(
        header.id,
        CatalogDetail.STATUS_ACTIVE,
        currentDate
    );
    dto.details = detailMapper.toDtoList(details, dictionary);
    return dto;
}

export async function findCatalogByPrefix(prefix: string, langId?: number | null): Promise<CatalogHeaderDto | null> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);
    const currentDate = today();

    const header = await headerRepo.findByPrefixAndStatus(prefix, CatalogHeader.STATUS_ACTIVE);
    if (!header) return null;

    const dto = headerMapper.toDto(header);
    if (!dto) return null;

    const details = await detailRepo.findByHeaderIdAndStatusAndValidDate(
        header.id,
        CatalogDetail.STATUS_ACTIVE,
        currentDate
    );
    dto.details = detailMapper.toDtoList(details, dictionary);
    return dto;
}

function countPlaceholders(message: string): number {
    let count = 0;
    let index = 0;
    while (message.includes(`{${index}}`)) {
        count++;
        index++;
    }
    return count;
}

function formatMessage(message: string | null | undefined, params: string[] | null | undefined): string | null | undefined {
    if (!message || !params || params.length === 0) return message;

    const expected = countPlaceholders(message);
    if (expected === 0) return message;

    if (params.length !== expected) {
        throw new Error(`El mensaje espera ${expected} parámetro(s) pero se enviaron ${params.length}`);
    }

    try {
        return message.replace(/\{(\d+)\}/g, (_match, idx) => {
            const i = Number(idx);
            return params[i] ?? '';
        });
    } catch (e) {
        throw new Error(`Error formateando mensaje: ${(e as Error).message}`);
    }
}

export async function findDetailByKeyFormatted(
    key: string,
    langId?: number | null,
    params?: string[] | null
): Promise<CatalogDetailDto | null> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);
    const currentDate = today();

    const detail = await detailRepo.findByKeyAndStatusAndValidDate(
        key,
        CatalogDetail.STATUS_ACTIVE,
        currentDate
    );
    if (!detail) return null;

    const dto = detailMapper.toDto(detail, dictionary);
    if (!dto) return null;

    const formatted = formatMessage(dto.description, params ?? null);
    dto.description = formatted ?? null;
    return dto;
}

export async function findDetailsByCodeWithDependency(
    code: string,
    targetCatalogCode: string,
    targetExternalKey: string,
    langId?: number | null
): Promise<CatalogDetailDto[]> {
    const lang = langId ?? DEFAULT_LANG_ID;
    const dictionary = await loadDictionary(lang);
    const currentDate = today();

    const filtered = await relationRepo.findSourceDetailsByTargetCatalogAndKeyAndValidDate(
        targetCatalogCode,
        targetExternalKey,
        CatalogDetailRelation.RELATION_DEPENDS_ON,
        CatalogDetail.STATUS_ACTIVE,
        currentDate
    );

    const detailsInCatalog = filtered
        .filter(d => d.header?.code === code)
        .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));

    if (detailsInCatalog.length > 0) {
        return detailMapper.toDtoList(detailsInCatalog, dictionary);
    }

    return findDetailsByCode(code, lang);
}

