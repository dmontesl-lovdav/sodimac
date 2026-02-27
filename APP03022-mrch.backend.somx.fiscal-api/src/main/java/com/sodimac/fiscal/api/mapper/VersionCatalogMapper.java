package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.VersionCatalogDto;
import com.sodimac.fiscal.api.model.entity.VersionCatalogEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VersionCatalogMapper {

    public VersionCatalogDto toDto(VersionCatalogEntity entity) {
        if (entity == null) {
            return null;
        }

        VersionCatalogDto dto = new VersionCatalogDto();
        dto.setVersionId(entity.getVersionId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        dto.setDocumentType(entity.getDocumentType());
        dto.setPacId(entity.getPacId());
        dto.setValidFrom(entity.getValidFrom());
        dto.setValidTo(entity.getValidTo());
        dto.setStructureUrl(entity.getStructureUrl());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    public VersionCatalogEntity toEntity(VersionCatalogDto dto) {
        if (dto == null) {
            return null;
        }

        VersionCatalogEntity entity = new VersionCatalogEntity();
        entity.setVersionId(dto.getVersionId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setVersion(dto.getVersion());
        entity.setDocumentType(dto.getDocumentType());
        entity.setPacId(dto.getPacId());
        entity.setValidFrom(dto.getValidFrom());
        entity.setValidTo(dto.getValidTo());
        entity.setStructureUrl(dto.getStructureUrl());
        entity.setStatus(dto.getStatus());

        return entity;
    }

    public List<VersionCatalogDto> toDtoList(List<VersionCatalogEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<VersionCatalogEntity> toEntityList(List<VersionCatalogDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}