package com.rebatesync.domain.model;

import com.rebatesync.domain.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    private String contractId;
    private String vendorTaxId;
    private String vendorName;
    private ContractStatus status;
    private String productCategoryId;
    private List<String> agreementIds;
}
