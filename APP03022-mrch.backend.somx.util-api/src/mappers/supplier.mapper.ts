import type { Supplier } from '@/entities/Supplier.entity.js';
import type { SupplierType } from '@/entities/SupplierType.entity.js';
import type { PaymentCondition } from '@/entities/PaymentCondition.entity.js';
import type { SupplierBlock } from '@/entities/SupplierBlock.entity.js';
import type {
    SupplierDto,
    SupplierTypeDto,
    PaymentConditionDto,
    SupplierFilterDto,
    SupplierBlockInfoDto,
    SupplierCreateDto
} from '@/dto/supplier.dto.js';

export function toSupplierTypeDto(entity: SupplierType | null | undefined): SupplierTypeDto | null {
    if (!entity) return null;
    return {
        id: entity.id,
        code: entity.code,
        description: entity.description
    };
}

export function toPaymentConditionDto(entity: PaymentCondition | null | undefined): PaymentConditionDto | null {
    if (!entity) return null;
    return {
        id: entity.id,
        conditionName: entity.conditionName,
        days: entity.days
    };
}

export function toDto(entity: Supplier | null | undefined): SupplierDto | null {
    if (!entity) return null;
    return {
        id: entity.id,
        supplierNumber: entity.supplierNumber,
        rfc: entity.rfc,
        businessName: entity.businessName,
        supplierType: toSupplierTypeDto(entity.supplierType),
        logo: entity.logo ?? null,
        paymentCondition: toPaymentConditionDto(entity.paymentCondition),
        emailPrincipal: entity.emailPrincipal ?? null,
        emailFinancial: entity.emailFinancial ?? null,
        emailCommercial: entity.emailCommercial ?? null,
        status: entity.status
    };
}

export function toDtoList(entities: Supplier[] | null | undefined): SupplierDto[] {
    if (!entities) return [];
    return entities.map(e => toDto(e)).filter((d): d is SupplierDto => d !== null);
}

export function toSupplierTypeDtoList(entities: SupplierType[] | null | undefined): SupplierTypeDto[] {
    if (!entities) return [];
    return entities.map(e => toSupplierTypeDto(e)).filter((d): d is SupplierTypeDto => d !== null);
}

export function toPaymentConditionDtoList(entities: PaymentCondition[] | null | undefined): PaymentConditionDto[] {
    if (!entities) return [];
    return entities.map(e => toPaymentConditionDto(e)).filter((d): d is PaymentConditionDto => d !== null);
}

export function toEntity(dto: SupplierCreateDto): Partial<Supplier> {
    return {
        supplierNumber: dto.supplierNumber,
        rfc: dto.rfc,
        businessName: dto.businessName,
        logo: dto.logo ?? null,
        emailPrincipal: dto.emailPrincipal,
        emailFinancial: dto.emailFinancial,
        emailCommercial: dto.emailCommercial ?? null
    };
}

export function toBlockInfoDto(entity: SupplierBlock | null | undefined): SupplierBlockInfoDto | null {
    if (!entity) return null;
    return {
        validFrom: entity.validFrom,
        validTo: entity.validTo,
        blockReason: entity.blockReason ?? null
    };
}

export function toFilterDto(
    entity: Supplier,
    blocked: boolean,
    activeBlock: SupplierBlock | null
): SupplierFilterDto {
    const dto: SupplierFilterDto = {
        id: entity.id,
        supplierNumber: entity.supplierNumber,
        rfc: entity.rfc,
        businessName: entity.businessName,
        supplierType: toSupplierTypeDto(entity.supplierType),
        paymentCondition: toPaymentConditionDto(entity.paymentCondition),
        status: entity.status,
        blocked,
        blockInfo: null
    };
    if (blocked && activeBlock) {
        dto.blockInfo = toBlockInfoDto(activeBlock);
    }
    return dto;
}

