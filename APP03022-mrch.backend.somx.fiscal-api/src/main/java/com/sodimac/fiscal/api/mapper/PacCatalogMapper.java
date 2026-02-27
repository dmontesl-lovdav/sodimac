package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.model.entity.PacCatalogEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PacCatalogMapper {

    public PacCatalogDto toDto(PacCatalogEntity entity) {
        if (entity == null) {
            return null;
        }

        PacCatalogDto dto = new PacCatalogDto();
        dto.setPacId(entity.getPacId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPriority(entity.getPriority());
        dto.setUserName(entity.getUserName());
        dto.setPassword(entity.getPassword());
        dto.setUrl(entity.getUrl());
        dto.setLicense(entity.getLicense());
        dto.setValidFrom(entity.getValidFrom());
        dto.setValidTo(entity.getValidTo());
        dto.setCatalogMsgId(entity.getCatalogMsgId());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    public PacCatalogEntity toEntity(PacCatalogDto dto) {
        if (dto == null) {
            return null;
        }

        PacCatalogEntity entity = new PacCatalogEntity();
        entity.setPacId(dto.getPacId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPriority(dto.getPriority());
        entity.setUserName(dto.getUserName());
        entity.setPassword(dto.getPassword());
        entity.setUrl(dto.getUrl());
        entity.setLicense(dto.getLicense());
        entity.setValidFrom(dto.getValidFrom());
        entity.setValidTo(dto.getValidTo());
        entity.setCatalogMsgId(dto.getCatalogMsgId());
        entity.setStatus(dto.getStatus());

        return entity;
    }

    public List<PacCatalogDto> toDtoList(List<PacCatalogEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PacCatalogEntity> toEntityList(List<PacCatalogDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}