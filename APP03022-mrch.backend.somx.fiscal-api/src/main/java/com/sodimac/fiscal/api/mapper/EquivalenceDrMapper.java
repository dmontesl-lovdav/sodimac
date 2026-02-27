package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.EquivalenceDrDto;
import com.sodimac.fiscal.api.model.entity.EquivalenceDrEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EquivalenceDrMapper {

    public EquivalenceDrDto toDto(EquivalenceDrEntity entity) {
        if (entity == null) {
            return null;
        }

        EquivalenceDrDto dto = new EquivalenceDrDto();
        dto.setEquivalenceUuid(entity.getEquivalenceUuid());
        dto.setRelatedDocumentUuid(entity.getRelatedDocumentUuid());
        dto.setFolio(entity.getFolio());
        dto.setAmountPaid(entity.getAmountPaid());
        dto.setPreviousBalance(entity.getPreviousBalance());
        dto.setRemainingBalance(entity.getRemainingBalance());
        dto.setCurrency(entity.getCurrency());
        dto.setInstallmentNumber(entity.getInstallmentNumber());
        dto.setTaxObject(entity.getTaxObject());
        dto.setSeries(entity.getSeries());

        return dto;
    }

    public EquivalenceDrEntity toEntity(EquivalenceDrDto dto) {
        if (dto == null) {
            return null;
        }

        EquivalenceDrEntity entity = new EquivalenceDrEntity();
        entity.setEquivalenceUuid(dto.getEquivalenceUuid());
        entity.setRelatedDocumentUuid(dto.getRelatedDocumentUuid());
        entity.setFolio(dto.getFolio());
        entity.setAmountPaid(dto.getAmountPaid());
        entity.setPreviousBalance(dto.getPreviousBalance());
        entity.setRemainingBalance(dto.getRemainingBalance());
        entity.setCurrency(dto.getCurrency());
        entity.setInstallmentNumber(dto.getInstallmentNumber());
        entity.setTaxObject(dto.getTaxObject());
        entity.setSeries(dto.getSeries());

        return entity;
    }

    public List<EquivalenceDrDto> toDtoList(List<EquivalenceDrEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<EquivalenceDrEntity> toEntityList(List<EquivalenceDrDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}