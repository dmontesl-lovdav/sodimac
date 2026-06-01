package com.rebatesync.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementResponse {
    private String agreementId;
    private String productTypeSelector;
    private String rebateType;
    private String currencyCode;
    private BigDecimal contributionAmount;
    private String contributionType;
    private BigDecimal agreedFillRate;
    private String paymentPeriodLabel;
    private String brand;
}
