import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogHeader } from '@/entities/CatalogHeader.entity.js';
import * as headerRepo from '@/repositories/catalogHeader.repo.js';
import * as detailRepo from '@/repositories/catalogDetail.repo.js';
import { GenericException } from '@/exceptions/GenericException.js';
import type {
    CatalogCreateDto,
    CatalogUpdateDto,
    CatalogResponseDto,
    CatalogPageResponse
} from '@/dto/catalog.dto.js';
import type { SelectQueryBuilder } from 'typeorm';

export interface FindCatalogsParams {
    id?: number | null;
    name?: string | null;
    description?: string | null;
    catalogType?: string | null;
    status?: number | null;
    code?: string | null;
    prefix?: string | null;
    page: number;
    pageSize: number;
    sortBy: string;
    sortDir: 'ASC' | 'DESC';
}

const SORT_MAP: Record<string, string> = {
    createdAt: 'h.createdAt',
    updatedAt: 'h.updatedAt',
    id: 'h.id',
    name: 'h.name',
    code: 'h.code',
    status: 'h.status'
};

async function toResponseDto(header: CatalogHeader): Promise<CatalogResponseDto> {
    const elementCount = await detailRepo.countByHeaderId(header.id);
    return {
        id: header.id,
        code: header.code,
        prefix: header.prefix,
        name: header.name,
        description: header.description ?? null,
        module: header.module ?? null,
        catalogType: header.catalogType ?? null,
        status: header.status,
        createdBy: header.createdBy ?? null,
        createdAt: header.createdAt ?? null,
        updatedBy: header.updatedBy ?? null,
        updatedAt: header.updatedAt ?? null,
        elementCount
    };
}

function applyFilters(qb: SelectQueryBuilder<CatalogHeader>, p: FindCatalogsParams): SelectQueryBuilder<CatalogHeader> {
    if (p.id != null) qb.andWhere('h.id = :id', { id: p.id });
    if (p.name && p.name.trim() !== '') {
        qb.andWhere('LOWER(h.name) LIKE LOWER(:name)', { name: `%${p.name}%` });
    }
    if (p.description && p.description.trim() !== '') {
        qb.andWhere('LOWER(h.description) LIKE LOWER(:desc)', { desc: `%${p.description}%` });
    }
    if (p.catalogType && p.catalogType.trim() !== '') {
        qb.andWhere('UPPER(h.catalogType) = UPPER(:ct)', { ct: p.catalogType });
    }
    if (p.status != null) qb.andWhere('h.status = :status', { status: p.status });
    if (p.code && p.code.trim() !== '') {
        qb.andWhere('LOWER(h.code) LIKE LOWER(:code)', { code: `%${p.code}%` });
    }
    if (p.prefix && p.prefix.trim() !== '') {
        qb.andWhere('LOWER(h.prefix) LIKE LOWER(:prefix)', { prefix: `%${p.prefix}%` });
    }
    return qb;
}

export async function findCatalogs(p: FindCatalogsParams): Promise<CatalogPageResponse> {
    const repo = datasource.getRepository(CatalogHeader);
    const qb = repo.createQueryBuilder('h');
    applyFilters(qb, p);

    const sortColumn = SORT_MAP[p.sortBy] ?? 'h.createdAt';
    qb.orderBy(sortColumn, p.sortDir);

    const pageIndex = Math.max(0, p.page - 1);
    qb.skip(pageIndex * p.pageSize).take(p.pageSize);

    const [headers, total] = await qb.getManyAndCount();

    const items = await Promise.all(headers.map(h => toResponseDto(h)));
    const totalPages = Math.ceil(total / p.pageSize);

    return {
        items,
        page: pageIndex + 1,
        pageSize: p.pageSize,
        total,
        totalPages,
        hasNext: pageIndex + 1 < totalPages,
        hasPrevious: pageIndex > 0
    };
}

export async function findById(id: number): Promise<CatalogResponseDto | null> {
    const header = await headerRepo.findById(id);
    if (!header) return null;
    return toResponseDto(header);
}

function normalizeType(type: string | null | undefined): string {
    if (!type || type.trim() === '') {
        throw new GenericException(400, 'El tipo de catálogo es obligatorio.');
    }
    const normalized = type.toUpperCase().trim();
    if (normalized !== 'PRIMARIO' && normalized !== 'SECUNDARIO') {
        throw new GenericException(400, 'El tipo de catálogo debe ser PRIMARIO o SECUNDARIO.');
    }
    return normalized;
}

async function generateCode(name: string): Promise<string> {
    const base =
        'CAT_' +
        name
            .toUpperCase()
            .replace(/[^A-Z0-9]/g, '_')
            .replace(/_+/g, '_')
            .substring(0, Math.min(name.length, 50));

    let code = base;
    let suffix = 1;
    while (await headerRepo.findByCode(code)) {
        code = `${base}_${suffix++}`;
    }
    return code;
}

function generatePrefix(name: string): string {
    const clean = name.toUpperCase().replace(/[^A-Z]/g, '');
    if (clean.length >= 3) return clean.substring(0, 3);
    if (clean.length > 0) return clean.padEnd(3, 'X');
    return 'CAT';
}

export async function createCatalog(dto: CatalogCreateDto, _userId: string): Promise<CatalogResponseDto> {
    const normalizedType = normalizeType(dto.catalogType);

    const code =
        dto.code && dto.code.trim() !== ''
            ? dto.code.toUpperCase().trim()
            : await generateCode(dto.name);
    const prefix =
        dto.prefix && dto.prefix.trim() !== ''
            ? dto.prefix.toUpperCase().trim()
            : generatePrefix(dto.name);

    if (await headerRepo.findByCode(code)) {
        throw new GenericException(409, `Ya existe un catálogo con el código: ${code}`);
    }
    if (await headerRepo.findByPrefix(prefix)) {
        throw new GenericException(409, `Ya existe un catálogo con el prefijo: ${prefix}`);
    }

    const repo = datasource.getRepository(CatalogHeader);
    const header = repo.create({
        code,
        prefix,
        name: dto.name,
        description: dto.description ?? null,
        catalogType: normalizedType,
        module: dto.module ?? 'general',
        status: CatalogHeader.STATUS_INACTIVE
    });

    const saved = await repo.save(header);
    return toResponseDto(saved);
}

export async function updateCatalog(
    id: number,
    dto: CatalogUpdateDto,
    _userId: string,
): Promise<CatalogResponseDto> {
    const header = await headerRepo.findById(id);
    if (!header) {
        throw new GenericException(404, `Catálogo no encontrado con ID: ${id}`);
    }

    if (dto.name && dto.name.trim() !== '') header.name = dto.name;
    if (dto.description != null) header.description = dto.description;
    if (dto.catalogType && dto.catalogType.trim() !== '') {
        header.catalogType = normalizeType(dto.catalogType);
    }
    if (dto.status != null) header.status = dto.status;
    if (dto.module != null) header.module = dto.module;

    header.updatedAt = new Date();

    const saved = await headerRepo.save(header);
    return toResponseDto(saved);
}

