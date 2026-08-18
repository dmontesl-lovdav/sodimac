package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.RelatedDocumentsDto;
import com.sodimac.fiscal.api.model.entity.RelatedDocumentsEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RelatedDocumentsMapper {

    public RelatedDocumentsDto toDto(RelatedDocumentsEntity entity) {
        if (entity == null) {
            return null;
        }

        RelatedDocumentsDto dto = new RelatedDocumentsDto();
        dto.setRelatedDocumentUuid(entity.getRelatedDocumentUuid());
        dto.setPaymentUuid(entity.getPaymentUuid());
        dto.setDocumentUuid(entity.getDocumentUuid());
        if (entity.getDocument() != null) {
            dto.setFiscalUuid(entity.getDocument().getFiscalUuid());
        }
        dto.setAmountPaid(entity.getAmountPaid());
        dto.setPreviousBalance(entity.getPreviousBalance());
        dto.setRemainingBalance(entity.getRemainingBalance());
        dto.setInstallmentNumber(entity.getInstallmentNumber());
        dto.setSeries(entity.getSeries());
        dto.setFolio(entity.getFolio());
        dto.setCurrency(entity.getCurrency());
        dto.setExchangeRate(entity.getExchangeRate());

        return dto;
    }

    public RelatedDocumentsEntity toEntity(RelatedDocumentsDto dto) {
        if (dto == null) {
            return null;
        }

        RelatedDocumentsEntity entity = new RelatedDocumentsEntity();
        entity.setRelatedDocumentUuid(dto.getRelatedDocumentUuid());
        entity.setPaymentUuid(dto.getPaymentUuid());
        entity.setDocumentUuid(dto.getDocumentUuid());
        entity.setAmountPaid(dto.getAmountPaid());
        entity.setPreviousBalance(dto.getPreviousBalance());
        entity.setRemainingBalance(dto.getRemainingBalance());
        entity.setInstallmentNumber(dto.getInstallmentNumber());
        entity.setSeries(dto.getSeries());
        entity.setFolio(dto.getFolio());
        entity.setCurrency(dto.getCurrency());
        entity.setExchangeRate(dto.getExchangeRate());

        return entity;
    }

    public List<RelatedDocumentsDto> toDtoList(List<RelatedDocumentsEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<RelatedDocumentsEntity> toEntityList(List<RelatedDocumentsDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}