package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.AuthorizedReceiverCatalogDto;
import com.sodimac.fiscal.api.model.entity.AuthorizedReceiverCatalogEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorizedReceiverCatalogMapper {

    public AuthorizedReceiverCatalogDto toDto(AuthorizedReceiverCatalogEntity entity) {
        if (entity == null) {
            return null;
        }

        AuthorizedReceiverCatalogDto dto = new AuthorizedReceiverCatalogDto();
        dto.setAuthorizedReceiverId(entity.getAuthorizedReceiverId());
        dto.setReceiverUuid(entity.getReceiverUuid());
        dto.setName(entity.getName());
        dto.setRfc(entity.getRfc());
        dto.setTaxRegime(entity.getTaxRegime());
        dto.setValidFrom(entity.getValidFrom());
        dto.setValidTo(entity.getValidTo());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    public AuthorizedReceiverCatalogEntity toEntity(AuthorizedReceiverCatalogDto dto) {
        if (dto == null) {
            return null;
        }

        AuthorizedReceiverCatalogEntity entity = new AuthorizedReceiverCatalogEntity();
        entity.setAuthorizedReceiverId(dto.getAuthorizedReceiverId());
        entity.setReceiverUuid(dto.getReceiverUuid());
        entity.setName(dto.getName());
        entity.setRfc(dto.getRfc());
        entity.setTaxRegime(dto.getTaxRegime());
        entity.setValidFrom(dto.getValidFrom());
        entity.setValidTo(dto.getValidTo());
        entity.setStatus(dto.getStatus());

        return entity;
    }

    public List<AuthorizedReceiverCatalogDto> toDtoList(List<AuthorizedReceiverCatalogEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AuthorizedReceiverCatalogEntity> toEntityList(List<AuthorizedReceiverCatalogDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}