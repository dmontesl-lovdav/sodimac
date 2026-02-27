package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersionCatalogDto {
    private Long versionId;
    private String name;
    private String description;
    private BigDecimal version;
    private String documentType;
    private Integer pacId;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String structureUrl;
    private Integer status;
}