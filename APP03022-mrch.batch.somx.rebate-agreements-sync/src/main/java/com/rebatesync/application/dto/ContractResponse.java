package com.rebatesync.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {
    private String contractId;
    private String vendorTaxId;
    private String vendorName;
    private String status;
    private String productCategoryId;
    private List<String> agreementIds;
}
