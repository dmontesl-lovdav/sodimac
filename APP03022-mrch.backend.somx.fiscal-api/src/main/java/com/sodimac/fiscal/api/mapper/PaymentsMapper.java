package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.PaymentsDto;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentsMapper {

    public PaymentsDto toDto(PaymentsEntity entity) {
        if (entity == null) {
            return null;
        }

        PaymentsDto dto = new PaymentsDto();
        dto.setPaymentsUuid(entity.getPaymentsUuid());
        dto.setVersion(entity.getVersion());
        dto.setPaymentDate(entity.getPaymentDate());
        dto.setCertificationDate(entity.getCertificationDate());
        dto.setIssuerUuid(entity.getIssuerUuid());
        dto.setReceiverUuid(entity.getReceiverUuid());
        dto.setFolio(entity.getFolio());
        dto.setSeries(entity.getSeries());
        dto.setXmlContent(entity.getXmlContent());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    public PaymentsEntity toEntity(PaymentsDto dto) {
        if (dto == null) {
            return null;
        }

        PaymentsEntity entity = new PaymentsEntity();
        entity.setPaymentsUuid(dto.getPaymentsUuid());
        entity.setVersion(dto.getVersion());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setCertificationDate(dto.getCertificationDate());
        entity.setIssuerUuid(dto.getIssuerUuid());
        entity.setReceiverUuid(dto.getReceiverUuid());
        entity.setFolio(dto.getFolio());
        entity.setSeries(dto.getSeries());
        entity.setXmlContent(dto.getXmlContent());
        entity.setStatus(dto.getStatus());

        return entity;
    }

    public List<PaymentsDto> toDtoList(List<PaymentsEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentsEntity> toEntityList(List<PaymentsDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}