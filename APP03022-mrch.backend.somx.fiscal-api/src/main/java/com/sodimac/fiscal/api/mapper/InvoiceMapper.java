package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.InvoiceDto;
import com.sodimac.fiscal.api.model.entity.InvoiceEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceMapper {

    public InvoiceDto toDto(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }

        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceUuid(entity.getInvoiceUuid());
        dto.setPlaceOfIssue(entity.getPlaceOfIssue());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setDocumentType(entity.getDocumentType());
        dto.setTotal(entity.getTotal());
        dto.setExchangeRate(entity.getExchangeRate());
        dto.setCurrency(entity.getCurrency());
        dto.setDiscount(entity.getDiscount());
        dto.setSubtotal(entity.getSubtotal());
        dto.setPaymentConditions(entity.getPaymentConditions());
        dto.setPaymentForm(entity.getPaymentForm());
        dto.setIssueDate(entity.getIssueDate());
        dto.setCertificationDate(entity.getCertificationDate());
        dto.setFolio(entity.getFolio());
        dto.setSeries(entity.getSeries());
        dto.setVersion(entity.getVersion());
        dto.setXmlContent(entity.getXmlContent());
        dto.setStatus(entity.getStatus());
        dto.setIssuerUuid(entity.getIssuerUuid());
        dto.setReceiverUuid(entity.getReceiverUuid());

        return dto;
    }

    public InvoiceEntity toEntity(InvoiceDto dto) {
        if (dto == null) {
            return null;
        }

        InvoiceEntity entity = new InvoiceEntity();
        entity.setInvoiceUuid(dto.getInvoiceUuid());
        entity.setPlaceOfIssue(dto.getPlaceOfIssue());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setDocumentType(dto.getDocumentType());
        entity.setTotal(dto.getTotal());
        entity.setExchangeRate(dto.getExchangeRate());
        entity.setCurrency(dto.getCurrency());
        entity.setDiscount(dto.getDiscount());
        entity.setSubtotal(dto.getSubtotal());
        entity.setPaymentConditions(dto.getPaymentConditions());
        entity.setPaymentForm(dto.getPaymentForm());
        entity.setIssueDate(dto.getIssueDate());
        entity.setCertificationDate(dto.getCertificationDate());
        entity.setFolio(dto.getFolio());
        entity.setSeries(dto.getSeries());
        entity.setVersion(dto.getVersion());
        entity.setXmlContent(dto.getXmlContent());
        entity.setStatus(dto.getStatus());
        entity.setIssuerUuid(dto.getIssuerUuid());
        entity.setReceiverUuid(dto.getReceiverUuid());

        return entity;
    }

    public List<InvoiceDto> toDtoList(List<InvoiceEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<InvoiceEntity> toEntityList(List<InvoiceDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}