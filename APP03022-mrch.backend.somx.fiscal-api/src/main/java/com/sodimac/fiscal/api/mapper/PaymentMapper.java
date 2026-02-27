package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.PaymentDto;
import com.sodimac.fiscal.api.model.entity.PaymentEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public PaymentDto toDto(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        PaymentDto dto = new PaymentDto();
        dto.setPaymentUuid(entity.getPaymentUuid());
        dto.setPaymentsUuid(entity.getPaymentsUuid());
        dto.setPaymentDate(entity.getPaymentDate());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setCurrency(entity.getCurrency());
        dto.setAmount(entity.getAmount());
        dto.setOperationNumber(entity.getOperationNumber());
        dto.setExchangeRate(entity.getExchangeRate());
        dto.setPayerBankRfc(entity.getPayerBankRfc());
        dto.setPayerAccount(entity.getPayerAccount());
        dto.setBeneficiaryBankRfc(entity.getBeneficiaryBankRfc());
        dto.setBeneficiaryAccount(entity.getBeneficiaryAccount());

        return dto;
    }

    public PaymentEntity toEntity(PaymentDto dto) {
        if (dto == null) {
            return null;
        }

        PaymentEntity entity = new PaymentEntity();
        entity.setPaymentUuid(dto.getPaymentUuid());
        entity.setPaymentsUuid(dto.getPaymentsUuid());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setCurrency(dto.getCurrency());
        entity.setAmount(dto.getAmount());
        entity.setOperationNumber(dto.getOperationNumber());
        entity.setExchangeRate(dto.getExchangeRate());
        entity.setPayerBankRfc(dto.getPayerBankRfc());
        entity.setPayerAccount(dto.getPayerAccount());
        entity.setBeneficiaryBankRfc(dto.getBeneficiaryBankRfc());
        entity.setBeneficiaryAccount(dto.getBeneficiaryAccount());

        return entity;
    }

    public List<PaymentDto> toDtoList(List<PaymentEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentEntity> toEntityList(List<PaymentDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}