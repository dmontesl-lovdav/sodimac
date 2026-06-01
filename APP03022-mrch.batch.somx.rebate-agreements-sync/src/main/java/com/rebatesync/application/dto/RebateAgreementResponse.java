package com.rebatesync.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebateAgreementResponse {
    private Long id;
    private String supplierNumber;
    private String rfc;
    private String businessName;
    private String status;
    private String family;
    private String commercialClassification;
    private String agreementNumber;
    private String agreementType;
    private String currency;
    private BigDecimal value;
    private String valueType;
    private BigDecimal fillRate;
    private String paymentProgram;
    private String brand;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
