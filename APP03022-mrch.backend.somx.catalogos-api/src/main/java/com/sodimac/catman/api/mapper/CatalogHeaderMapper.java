package com.sodimac.catman.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sodimac.catman.api.model.dto.CatalogHeaderDto;
import com.sodimac.catman.api.model.entity.CatalogHeader;

@Component
public class CatalogHeaderMapper {

    public CatalogHeaderDto toDto(CatalogHeader entity) {
        if (entity == null) {
            return null;
        }

        return CatalogHeaderDto.builder()
                .code(entity.getCode())
                .prefix(entity.getPrefix())
                .name(entity.getName())
                .description(entity.getDescription())
                .module(entity.getModule())
                .catalogType(entity.getCatalogType())
                .build();
    }

    public CatalogHeader toEntity(CatalogHeaderDto dto) {
        if (dto == null) {
            return null;
        }

        return CatalogHeader.builder()
                .code(dto.getCode())
                .prefix(dto.getPrefix())
                .name(dto.getName())
                .description(dto.getDescription())
                .module(dto.getModule())
                .catalogType(dto.getCatalogType() != null ? dto.getCatalogType() : "SIMPLE")
                .build();
    }

    public List<CatalogHeaderDto> toDtoList(List<CatalogHeader> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public List<CatalogHeader> toEntityList(List<CatalogHeaderDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }
}
