package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogDto {
    private UUID logId;

    private Integer pacId;

    private Integer versionId;   //CAMBIARLO A STRING O DECIMAL

    private UUID cfdiUuid;

    @NotBlank(message = "El tipo de operación es obligatorio")
    @Size(max = 20, message = "El tipo de operación no puede exceder 20 caracteres")
    private String operationType;

    private LocalDateTime transactionDate;

    private LocalDateTime recordStartDate;

    private LocalDateTime recordEndDate;

    @Size(max = 10, message = "El código de estado no puede exceder 10 caracteres")
    private String statusCode;

    private String statusMessage;

    private String requestData;

    private String responseData;
}