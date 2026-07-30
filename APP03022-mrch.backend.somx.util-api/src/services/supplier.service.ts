import { Supplier } from '@/entities/Supplier.entity.js';
import { PaymentCondition } from '@/entities/PaymentCondition.entity.js';
import * as supplierRepo from '@/repositories/supplier.repo.js';
import * as supplierTypeRepo from '@/repositories/supplierType.repo.js';
import * as paymentConditionRepo from '@/repositories/paymentCondition.repo.js';
import * as supplierBlockRepo from '@/repositories/supplierBlock.repo.js';
import * as supplierMapper from '@/mappers/supplier.mapper.js';
import * as catalogHeaderService from '@/services/catalogHeader.service.js';
import { datasource } from '@/config/typeorm-datasource.js';
import type {
    SupplierCreateDto,
    SupplierUpdateDto,
    SupplierDto,
    SupplierTypeDto,
    PaymentConditionDto,
    SupplierFilterDto
} from '@/dto/supplier.dto.js';

const MSG_SUPPLIER_DUPLICATE = 'BUS200';
const MSG_SUPPLIER_TYPE_NOT_FOUND = 'BUS204';
const MSG_PAYMENT_CONDITION_NOT_FOUND = 'BUS205';
const MSG_SUPPLIER_TYPE_INVALID = 'BUS214';

const MIN_SUPPLIER_TYPE = 0;
const MAX_SUPPLIER_TYPE = 4;
const BLOCK_STATUS_BLOCKED_ONLY = 0;
const BLOCK_STATUS_ACTIVE_ONLY = 1;
const BLOCK_STATUS_ALL = 2;

const DEFAULT_LANG_ID = 1;

async function getMessage(key: string, ...params: string[]): Promise<string> {
    try {
        const detail = await catalogHeaderService.findDetailByKeyFormatted(key, DEFAULT_LANG_ID, params);
        return detail?.description ?? `Error: ${key}`;
    } catch {
        return `Error: ${key}`;
    }
}

export async function findAll(): Promise<SupplierDto[]> {
    const suppliers = await supplierRepo.findAllVisible();
    return supplierMapper.toDtoList(suppliers);
}

export async function findByStatus(status: number): Promise<SupplierDto[]> {
    const suppliers = await supplierRepo.findByStatus(status);
    return supplierMapper.toDtoList(suppliers);
}

export async function findById(id: number): Promise<SupplierDto | null> {
    const entity = await supplierRepo.findById(id);
    return supplierMapper.toDto(entity);
}

export async function findBySupplierNumber(supplierNumber: string): Promise<SupplierDto | null> {
    const entity = await supplierRepo.findBySupplierNumber(supplierNumber);
    return supplierMapper.toDto(entity);
}

export async function findByRfc(rfc: string): Promise<SupplierDto | null> {
    const entity = await supplierRepo.findByRfc(rfc);
    return supplierMapper.toDto(entity);
}

export async function create(dto: SupplierCreateDto, createdBy: string): Promise<SupplierDto> {
    const existingStatus = await supplierRepo.findExistingStatus(dto.supplierNumber);
    if (existingStatus === 'active') {
        throw new Error(await getMessage(MSG_SUPPLIER_DUPLICATE, dto.supplierNumber));
    }
    if (existingStatus === 'inactive') {
        throw new Error(
            `Ya existe un proveedor inactivo con el número ${dto.supplierNumber}. ` +
            `Búscalo en el listado (filtro Estatus = Inactivo) y reactívalo en lugar de crear uno nuevo.`,
        );
    }

    const repo = datasource.getRepository(Supplier);
    const base = supplierMapper.toEntity(dto);
    const entity = repo.create({ ...base, createdBy });

    if (dto.supplierTypeId != null) {
        const type = await supplierTypeRepo.findById(dto.supplierTypeId);
        if (!type) {
            throw new Error(await getMessage(MSG_SUPPLIER_TYPE_NOT_FOUND, String(dto.supplierTypeId)));
        }
        entity.supplierType = type;
        entity.supplierTypeId = type.id;
    }

    if (dto.paymentConditionId != null) {
        const pc = await paymentConditionRepo.findById(dto.paymentConditionId);
        if (!pc) {
            throw new Error(await getMessage(MSG_PAYMENT_CONDITION_NOT_FOUND, String(dto.paymentConditionId)));
        }
        entity.paymentCondition = pc;
        entity.paymentConditionId = pc.id;
    }

    const saved = await repo.save(entity);
    const full = await supplierRepo.findById(saved.id);
    return supplierMapper.toDto(full)!;
}

export async function update(id: number, dto: SupplierUpdateDto, updatedBy: string): Promise<SupplierDto | null> {
    const supplier = await supplierRepo.findById(id);
    if (!supplier) return null;

    if (dto.rfc != null) supplier.rfc = dto.rfc;
    if (dto.businessName != null) supplier.businessName = dto.businessName;
    if (dto.logo !== undefined) supplier.logo = dto.logo ?? null;
    if (dto.emailPrincipal !== undefined) supplier.emailPrincipal = dto.emailPrincipal ?? null;
    if (dto.emailFinancial !== undefined) supplier.emailFinancial = dto.emailFinancial ?? null;
    if (dto.emailCommercial !== undefined) supplier.emailCommercial = dto.emailCommercial ?? null;
    if (dto.status != null) supplier.status = dto.status;

    if (dto.supplierTypeId != null) {
        const type = await supplierTypeRepo.findById(dto.supplierTypeId);
        if (!type) {
            throw new Error(await getMessage(MSG_SUPPLIER_TYPE_NOT_FOUND, String(dto.supplierTypeId)));
        }
        supplier.supplierType = type;
        supplier.supplierTypeId = type.id;
    }

    if (dto.paymentConditionId != null) {
        const pc = await paymentConditionRepo.findById(dto.paymentConditionId);
        if (!pc) {
            throw new Error(await getMessage(MSG_PAYMENT_CONDITION_NOT_FOUND, String(dto.paymentConditionId)));
        }
        supplier.paymentCondition = pc;
        supplier.paymentConditionId = pc.id;
    }

    supplier.updatedBy = updatedBy;
    await supplierRepo.save(supplier);
    const full = await supplierRepo.findById(id);
    return supplierMapper.toDto(full);
}

export async function deleteSupplier(id: number, updatedBy: string): Promise<boolean> {
    const supplier = await supplierRepo.findById(id);
    if (!supplier) return false;
    supplier.status = Supplier.STATUS_DELETED;
    supplier.updatedBy = updatedBy;
    await supplierRepo.save(supplier);
    return true;
}

export async function findAllSupplierTypes(): Promise<SupplierTypeDto[]> {
    const rows = await supplierTypeRepo.findActiveFromCatTipoProveedor();
    return rows.map((r) => ({
        id: r.id,
        code: r.code,
        description: r.description,
    }));
}

export async function findAllPaymentConditions(): Promise<PaymentConditionDto[]> {
    const conditions = await paymentConditionRepo.findByStatus(PaymentCondition.STATUS_ACTIVE);
    return supplierMapper.toPaymentConditionDtoList(conditions);
}

/**
 * Indica si el TIPO del proveedor está bloqueado para publicar facturas (BUS2028).
 *
 * Consulta el catálogo CatBloqueoTipoProveedor: si existe un elemento ACTIVO (status=1)
 * que referencia (parent_element_id) al elemento de CatTipoProveedor correspondiente al
 * tipo del proveedor, el tipo está bloqueado. Si no existe o está inactivo, permite continuar.
 */
export async function isSupplierTypeBlocked(supplierNumber: string): Promise<boolean> {
    const sql = `
        SELECT 1
        FROM shared_catalogs.supplier s
        JOIN shared_catalogs.supplier_type st
            ON st.id = s.supplier_type_id
        JOIN shared_catalogs.catalog_header ch_tp
            ON ch_tp.code = 'CatTipoProveedor'
        JOIN shared_catalogs.catalog_detail cd_tp
            ON cd_tp.header_id = ch_tp.id
            AND cd_tp.key = CASE st.code
                WHEN 'MERCANCIA'  THEN 'TPR001'
                WHEN 'TRANSPORTE' THEN 'TPR002'
                WHEN 'INDIRECTOS' THEN 'TPR003'
                WHEN 'SERVICIOS'  THEN 'TPR004'
            END
        JOIN shared_catalogs.catalog_header ch_blk
            ON ch_blk.code = 'CatBloqueoTipoProveedor'
        JOIN shared_catalogs.catalog_detail cd_blk
            ON cd_blk.header_id = ch_blk.id
            AND cd_blk.parent_element_id = cd_tp.id
            AND cd_blk.status = 1
        WHERE s.supplier_number = $1
        LIMIT 1
    `;
    const rows = await datasource.query(sql, [supplierNumber]);
    return (rows ?? []).length > 0;
}

export async function findByTypeAndBlockStatus(
    tipoProveedor: number,
    estatusBloqueo: number
): Promise<SupplierFilterDto[]> {
    if (tipoProveedor < MIN_SUPPLIER_TYPE || tipoProveedor > MAX_SUPPLIER_TYPE) {
        throw new Error(await getMessage(MSG_SUPPLIER_TYPE_INVALID, String(tipoProveedor)));
    }

    const suppliers = await supplierRepo.findByTypeFilter(tipoProveedor);
    const result: SupplierFilterDto[] = [];

    for (const supplier of suppliers) {
        const activeBlocks = await supplierBlockRepo.findCurrentActiveBlocks(supplier.supplierNumber);
        const isBlocked = activeBlocks.length > 0;
        const activeBlock = isBlocked ? activeBlocks[0]! : null;

        let includeSupplier: boolean;
        switch (estatusBloqueo) {
            case BLOCK_STATUS_BLOCKED_ONLY:
                includeSupplier = isBlocked;
                break;
            case BLOCK_STATUS_ACTIVE_ONLY:
                includeSupplier = !isBlocked;
                break;
            case BLOCK_STATUS_ALL:
                includeSupplier = true;
                break;
            default:
                includeSupplier = true;
        }

        if (includeSupplier) {
            result.push(supplierMapper.toFilterDto(supplier, isBlocked, activeBlock));
        }
    }

    return result;
}

