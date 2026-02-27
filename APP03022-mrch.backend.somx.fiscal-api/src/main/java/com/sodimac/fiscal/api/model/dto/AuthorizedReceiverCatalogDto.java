package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizedReceiverCatalogDto {
    private Long authorizedReceiverId;
    private UUID receiverUuid;
    private String name;
    private String rfc;
    private String taxRegime;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer status;
}