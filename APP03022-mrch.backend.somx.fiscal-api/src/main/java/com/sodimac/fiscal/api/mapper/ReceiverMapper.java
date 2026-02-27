package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.ReceiverDto;
import com.sodimac.fiscal.api.model.entity.ReceiverEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReceiverMapper {

    public ReceiverDto toDto(ReceiverEntity entity) {
        if (entity == null) {
            return null;
        }

        ReceiverDto dto = new ReceiverDto();
        dto.setReceiverUuid(entity.getReceiverUuid());
        dto.setName(entity.getName());
        dto.setRfc(entity.getRfc());
        dto.setTaxRegime(entity.getTaxRegime());

        return dto;
    }

    public ReceiverEntity toEntity(ReceiverDto dto) {
        if (dto == null) {
            return null;
        }

        ReceiverEntity entity = new ReceiverEntity();
        entity.setReceiverUuid(dto.getReceiverUuid());
        entity.setName(dto.getName());
        entity.setRfc(dto.getRfc());
        entity.setTaxRegime(dto.getTaxRegime());

        return entity;
    }

    public List<ReceiverDto> toDtoList(List<ReceiverEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ReceiverEntity> toEntityList(List<ReceiverDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}