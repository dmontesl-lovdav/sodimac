package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.RelatedCfdiDto;
import com.sodimac.fiscal.api.model.entity.RelatedCfdiEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RelatedCfdiMapper {

    public RelatedCfdiDto toDto(RelatedCfdiEntity entity) {
        if (entity == null) {
            return null;
        }

        RelatedCfdiDto dto = new RelatedCfdiDto();
        dto.setRelatedCfdiUuid(entity.getRelatedCfdiUuid());
        dto.setInvoiceUuid(entity.getInvoiceUuid());
        dto.setRelatedInvoiceUuid(entity.getRelatedInvoiceUuid());
        dto.setRelationType(entity.getRelationType());

        return dto;
    }

    public RelatedCfdiEntity toEntity(RelatedCfdiDto dto) {
        if (dto == null) {
            return null;
        }

        RelatedCfdiEntity entity = new RelatedCfdiEntity();
        entity.setRelatedCfdiUuid(dto.getRelatedCfdiUuid());
        entity.setInvoiceUuid(dto.getInvoiceUuid());
        entity.setRelatedInvoiceUuid(dto.getRelatedInvoiceUuid());
        entity.setRelationType(dto.getRelationType());

        return entity;
    }

    public List<RelatedCfdiDto> toDtoList(List<RelatedCfdiEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<RelatedCfdiEntity> toEntityList(List<RelatedCfdiDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}