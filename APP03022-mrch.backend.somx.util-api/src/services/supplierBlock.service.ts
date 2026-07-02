import { SupplierBlock } from '@/entities/SupplierBlock.entity.js';
import * as supplierBlockRepo from '@/repositories/supplierBlock.repo.js';
import * as supplierRepo from '@/repositories/supplier.repo.js';
import * as catalogHeaderService from '@/services/catalogHeader.service.js';
import { datasource } from '@/config/typeorm-datasource.js';
import type {
    SupplierBlockCreateDto,
    SupplierBlockUpdateDto,
    SupplierBlockDto,
    SupplierBlockResponseDto
} from '@/dto/supplier.dto.js';

const MSG_DATE_INVALID = 'WRN001';
const MSG_SUPPLIER_NOT_FOUND = 'BUS215';
const MSG_BLOCK_OVERLAP = 'BUS216';
const MSG_BLOCK_CREATED = 'RES001';
const DEFAULT_LANG_ID = 1;

async function getMessage(key: string, ...params: string[]): Promise<string> {
    try {
        const detail = await catalogHeaderService.findDetailByKeyFormatted(key, DEFAULT_LANG_ID, params);
        return detail?.description ?? `Error: ${key}`;
    } catch {
        return `Error: ${key}`;
    }
}

function toDto(entity: SupplierBlock): SupplierBlockDto {
    return {
        id: entity.id,
        supplierNumber: entity.supplierNumber,
        validFrom: entity.validFrom,
        validTo: entity.validTo,
        blockReason: entity.blockReason ?? null,
        status: entity.status,
        currentlyBlocked: entity.isCurrentlyBlocked(),
        createdAt: entity.createdAt ?? null,
        createdBy: entity.createdBy ?? null,
        updatedAt: entity.updatedAt ?? null,
        updatedBy: entity.updatedBy ?? null
    };
}

export async function findAll(): Promise<SupplierBlockDto[]> {
    const blocks = await supplierBlockRepo.findAllVisible();
    return blocks.map(toDto);
}

export async function findByStatus(status: number): Promise<SupplierBlockDto[]> {
    const blocks = await supplierBlockRepo.findByStatus(status);
    return blocks.map(toDto);
}

export async function findById(id: number): Promise<SupplierBlockDto | null> {
    const entity = await supplierBlockRepo.findById(id);
    return entity ? toDto(entity) : null;
}

export async function findBySupplierNumber(supplierNumber: string): Promise<SupplierBlockDto[]> {
    const blocks = await supplierBlockRepo.findBySupplierNumber(supplierNumber);
    return blocks.map(toDto);
}

export async function findCurrentActiveBlocks(supplierNumber: string): Promise<SupplierBlockDto[]> {
    const blocks = await supplierBlockRepo.findCurrentActiveBlocks(supplierNumber);
    return blocks.map(toDto);
}

export async function isSupplierCurrentlyBlocked(supplierNumber: string): Promise<boolean> {
    return supplierBlockRepo.isSupplierCurrentlyBlocked(supplierNumber);
}

export async function findActiveBlocksAtDate(supplierNumber: string, date: string): Promise<SupplierBlockDto[]> {
    const blocks = await supplierBlockRepo.findActiveBlocksAtDate(supplierNumber, date);
    return blocks.map(toDto);
}

export async function create(dto: SupplierBlockCreateDto, createdBy: string): Promise<SupplierBlockResponseDto> {
    if (!(await supplierRepo.existsBySupplierNumber(dto.supplierNumber))) {
        throw new Error(await getMessage(MSG_SUPPLIER_NOT_FOUND, dto.supplierNumber));
    }

    if (new Date(dto.validTo) < new Date(dto.validFrom)) {
        throw new Error(await getMessage(MSG_DATE_INVALID));
    }

    const overlapping = await supplierBlockRepo.findOverlappingBlocks(
        dto.supplierNumber,
        dto.validFrom,
        dto.validTo
    );
    if (overlapping.length > 0) {
        throw new Error(await getMessage(MSG_BLOCK_OVERLAP, dto.supplierNumber));
    }

    const repo = datasource.getRepository(SupplierBlock);
    const block = repo.create({
        supplierNumber: dto.supplierNumber,
        validFrom: dto.validFrom,
        validTo: dto.validTo,
        blockReason: dto.blockReason ?? null,
        status: SupplierBlock.STATUS_ACTIVE,
        createdBy
    });

    const saved = await repo.save(block);
    return {
        message: await getMessage(MSG_BLOCK_CREATED, dto.supplierNumber),
        data: toDto(saved)
    };
}

export async function update(
    id: number,
    dto: SupplierBlockUpdateDto,
    updatedBy: string
): Promise<SupplierBlockResponseDto | null> {
    const block = await supplierBlockRepo.findById(id);
    if (!block) return null;

    const validFrom = dto.validFrom ?? block.validFrom;
    const validTo = dto.validTo ?? block.validTo;

    if (new Date(validTo) < new Date(validFrom)) {
        throw new Error(await getMessage(MSG_DATE_INVALID));
    }

    if (dto.validFrom != null || dto.validTo != null) {
        const overlapping = await supplierBlockRepo.findOverlappingBlocks(
            block.supplierNumber,
            validFrom,
            validTo
        );
        const filtered = overlapping.filter(b => b.id !== id);
        if (filtered.length > 0) {
            throw new Error(await getMessage(MSG_BLOCK_OVERLAP, block.supplierNumber));
        }
    }

    if (dto.validFrom != null) block.validFrom = dto.validFrom;
    if (dto.validTo != null) block.validTo = dto.validTo;
    if (dto.blockReason !== undefined) block.blockReason = dto.blockReason ?? null;
    if (dto.status != null) block.status = dto.status;

    block.updatedBy = updatedBy;
    const saved = await supplierBlockRepo.save(block);

    return {
        message: await getMessage(MSG_BLOCK_CREATED, block.supplierNumber),
        data: toDto(saved)
    };
}

export async function deleteBlock(id: number, updatedBy: string): Promise<boolean> {
    const block = await supplierBlockRepo.findById(id);
    if (!block) return false;

    block.status = SupplierBlock.STATUS_DELETED;
    block.updatedBy = updatedBy;
    await supplierBlockRepo.save(block);
    return true;
}

