package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsDto {
    private UUID paymentsUuid;

    @NotNull(message = "La versión es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La versión debe ser mayor que 0")
    @Digits(integer = 3, fraction = 3, message = "La versión no puede tener más de 3 dígitos enteros y 3 decimales")
    private BigDecimal version;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate paymentDate;

    private LocalDateTime certificationDate;

    @NotNull(message = "El UUID del emisor es obligatorio")
    private UUID issuerUuid;

    @NotNull(message = "El UUID del receptor es obligatorio")
    private UUID receiverUuid;

    @Size(max = 49, message = "El folio no puede exceder 49 caracteres")
    private String folio;

    @Size(max = 25, message = "La serie no puede exceder 25 caracteres")
    private String series;

    private String xmlContent;

    private Integer status;
}