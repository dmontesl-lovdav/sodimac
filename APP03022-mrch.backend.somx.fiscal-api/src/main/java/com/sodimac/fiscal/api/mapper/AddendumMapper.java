package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.AddendumDto;
import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddendumMapper {

    public AddendumDto toDto(AddendumEntity entity) {
        if (entity == null) {
            return null;
        }

        AddendumDto dto = new AddendumDto();
        dto.setAddendumUuid(entity.getAddendumUuid());
        dto.setInvoiceUuid(entity.getInvoiceUuid());
        dto.setSupplierNumber(entity.getSupplierNumber());
        dto.setReceptionNumber(entity.getReceptionNumber());
        dto.setPurchaseOrderNumber(entity.getPurchaseOrderNumber());
        dto.setShippingGuideNumber(entity.getShippingGuideNumber());
        dto.setAddendumContent(entity.getAddendumContent());

        return dto;
    }

    public AddendumEntity toEntity(AddendumDto dto) {
        if (dto == null) {
            return null;
        }

        AddendumEntity entity = new AddendumEntity();
        entity.setAddendumUuid(dto.getAddendumUuid());
        entity.setInvoiceUuid(dto.getInvoiceUuid());
        entity.setSupplierNumber(dto.getSupplierNumber());
        entity.setReceptionNumber(dto.getReceptionNumber());
        entity.setPurchaseOrderNumber(dto.getPurchaseOrderNumber());
        entity.setShippingGuideNumber(dto.getShippingGuideNumber());
        entity.setAddendumContent(dto.getAddendumContent());

        return entity;
    }

    public List<AddendumDto> toDtoList(List<AddendumEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AddendumEntity> toEntityList(List<AddendumDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}