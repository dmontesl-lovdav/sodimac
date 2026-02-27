package com.sodimac.catman.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sodimac.catman.api.model.dto.PaymentConditionDto;
import com.sodimac.catman.api.model.dto.SupplierBlockInfoDto;
import com.sodimac.catman.api.model.dto.SupplierCreateDto;
import com.sodimac.catman.api.model.dto.SupplierDto;
import com.sodimac.catman.api.model.dto.SupplierFilterDto;
import com.sodimac.catman.api.model.dto.SupplierTypeDto;
import com.sodimac.catman.api.model.entity.PaymentCondition;
import com.sodimac.catman.api.model.entity.Supplier;
import com.sodimac.catman.api.model.entity.SupplierBlock;
import com.sodimac.catman.api.model.entity.SupplierType;

@Component
public class SupplierMapper {

    public SupplierDto toDto(Supplier entity) {
        if (entity == null) {
            return null;
        }

        return SupplierDto.builder()
                .id(entity.getId())
                .supplierNumber(entity.getSupplierNumber())
                .rfc(entity.getRfc())
                .businessName(entity.getBusinessName())
                .supplierType(toSupplierTypeDto(entity.getSupplierType()))
                .logo(entity.getLogo())
                .paymentCondition(toPaymentConditionDto(entity.getPaymentCondition()))
                .status(entity.getStatus())
                .build();
    }

    public Supplier toEntity(SupplierCreateDto dto) {
        if (dto == null) {
            return null;
        }

        return Supplier.builder()
                .supplierNumber(dto.getSupplierNumber())
                .rfc(dto.getRfc())
                .businessName(dto.getBusinessName())
                .logo(dto.getLogo())
                .build();
    }

    public List<SupplierDto> toDtoList(List<Supplier> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public SupplierTypeDto toSupplierTypeDto(SupplierType entity) {
        if (entity == null) {
            return null;
        }

        return SupplierTypeDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .build();
    }

    public PaymentConditionDto toPaymentConditionDto(PaymentCondition entity) {
        if (entity == null) {
            return null;
        }

        return PaymentConditionDto.builder()
                .id(entity.getId())
                .conditionName(entity.getConditionName())
                .days(entity.getDays())
                .build();
    }

    public List<SupplierTypeDto> toSupplierTypeDtoList(List<SupplierType> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toSupplierTypeDto)
                .toList();
    }

    public List<PaymentConditionDto> toPaymentConditionDtoList(List<PaymentCondition> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toPaymentConditionDto)
                .toList();
    }

    /**
     * STM-831: Convierte un Supplier a SupplierFilterDto con informacion de bloqueo.
     */
    public SupplierFilterDto toFilterDto(Supplier entity, boolean blocked, SupplierBlock activeBlock) {
        if (entity == null) {
            return null;
        }

        SupplierFilterDto.SupplierFilterDtoBuilder builder = SupplierFilterDto.builder()
                .id(entity.getId())
                .supplierNumber(entity.getSupplierNumber())
                .rfc(entity.getRfc())
                .businessName(entity.getBusinessName())
                .supplierType(toSupplierTypeDto(entity.getSupplierType()))
                .paymentCondition(toPaymentConditionDto(entity.getPaymentCondition()))
                .status(entity.getStatus())
                .blocked(blocked);

        if (blocked && activeBlock != null) {
            builder.blockInfo(toBlockInfoDto(activeBlock));
        }

        return builder.build();
    }

    /**
     * STM-831: Convierte un SupplierBlock a SupplierBlockInfoDto.
     */
    public SupplierBlockInfoDto toBlockInfoDto(SupplierBlock entity) {
        if (entity == null) {
            return null;
        }

        return SupplierBlockInfoDto.builder()
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .blockReason(entity.getBlockReason())
                .build();
    }
}
