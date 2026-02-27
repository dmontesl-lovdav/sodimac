package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.IssuerDto;
import com.sodimac.fiscal.api.model.entity.IssuerEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssuerMapper {

    public IssuerDto toDto(IssuerEntity entity) {
        if (entity == null) {
            return null;
        }

        IssuerDto dto = new IssuerDto();
        dto.setIssuerUuid(entity.getIssuerUuid());
        dto.setName(entity.getName());
        dto.setRfc(entity.getRfc());
        dto.setTaxRegime(entity.getTaxRegime());

        return dto;
    }

    public IssuerEntity toEntity(IssuerDto dto) {
        if (dto == null) {
            return null;
        }

        IssuerEntity entity = new IssuerEntity();
        entity.setIssuerUuid(dto.getIssuerUuid());
        entity.setName(dto.getName());
        entity.setRfc(dto.getRfc());
        entity.setTaxRegime(dto.getTaxRegime());

        return entity;
    }

    public List<IssuerDto> toDtoList(List<IssuerEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssuerEntity> toEntityList(List<IssuerDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}