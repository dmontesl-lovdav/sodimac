package com.rebatesync.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequest {

    private String contractId;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer page;

    @NotNull(message = "Page size is required")
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer pageSize;
}
