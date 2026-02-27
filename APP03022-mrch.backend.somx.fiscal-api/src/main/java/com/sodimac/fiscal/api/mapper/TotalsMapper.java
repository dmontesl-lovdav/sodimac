package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.TotalsDto;
import com.sodimac.fiscal.api.model.entity.TotalsEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TotalsMapper {

    public TotalsDto toDto(TotalsEntity entity) {
        if (entity == null) {
            return null;
        }

        TotalsDto dto = new TotalsDto();
        dto.setTotalsUuid(entity.getTotalsUuid());
        dto.setPaymentsUuid(entity.getPaymentsUuid());
        dto.setTotalPaymentsAmount(entity.getTotalPaymentsAmount());
        dto.setTotalBaseIva16(entity.getTotalBaseIva16());
        dto.setTotalTaxIva16(entity.getTotalTaxIva16());
        dto.setTotalBaseIva8(entity.getTotalBaseIva8());
        dto.setTotalTaxIva8(entity.getTotalTaxIva8());
        dto.setTotalBaseIva0(entity.getTotalBaseIva0());
        dto.setTotalWithholdingIva(entity.getTotalWithholdingIva());
        dto.setTotalWithholdingIsr(entity.getTotalWithholdingIsr());

        return dto;
    }

    public TotalsEntity toEntity(TotalsDto dto) {
        if (dto == null) {
            return null;
        }

        TotalsEntity entity = new TotalsEntity();
        entity.setTotalsUuid(dto.getTotalsUuid());
        entity.setPaymentsUuid(dto.getPaymentsUuid());
        entity.setTotalPaymentsAmount(dto.getTotalPaymentsAmount());
        entity.setTotalBaseIva16(dto.getTotalBaseIva16());
        entity.setTotalTaxIva16(dto.getTotalTaxIva16());
        entity.setTotalBaseIva8(dto.getTotalBaseIva8());
        entity.setTotalTaxIva8(dto.getTotalTaxIva8());
        entity.setTotalBaseIva0(dto.getTotalBaseIva0());
        entity.setTotalWithholdingIva(dto.getTotalWithholdingIva());
        entity.setTotalWithholdingIsr(dto.getTotalWithholdingIsr());

        return entity;
    }

    public List<TotalsDto> toDtoList(List<TotalsEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TotalsEntity> toEntityList(List<TotalsDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}