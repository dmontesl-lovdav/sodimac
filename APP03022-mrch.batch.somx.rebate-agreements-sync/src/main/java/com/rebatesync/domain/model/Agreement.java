package com.rebatesync.domain.model;

import com.rebatesync.domain.enums.AgreementType;
import com.rebatesync.domain.enums.ContributionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agreement {
    private String agreementId;
    private String productTypeSelector;
    private AgreementType rebateType;
    private String currencyCode;
    private BigDecimal contributionAmount;
    private ContributionType contributionType;
    private BigDecimal agreedFillRate;
    private String paymentPeriodLabel;
    private String brand;
}
