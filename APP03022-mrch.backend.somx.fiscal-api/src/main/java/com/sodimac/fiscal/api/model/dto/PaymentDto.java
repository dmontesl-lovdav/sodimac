package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private UUID paymentUuid;

    @NotNull(message = "El UUID de pagos es obligatorio")
    private UUID paymentsUuid;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate paymentDate;

    @NotBlank(message = "El método de pago es obligatorio")
    @Size(max = 10, message = "El método de pago no puede exceder 10 caracteres")
    private String paymentMethod;

    @Size(max = 3, message = "La moneda no puede exceder 3 caracteres")
    private String currency;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor que 0")
    @Digits(integer = 14, fraction = 2, message = "El monto no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal amount;

    @Size(max = 100, message = "El número de operación no puede exceder 100 caracteres")
    private String operationNumber;

    @DecimalMin(value = "0.0", message = "El tipo de cambio debe ser mayor que 0")
    @Digits(integer = 12, fraction = 6, message = "El tipo de cambio no puede tener más de 12 dígitos enteros y 6 decimales")
    private BigDecimal exchangeRate;

    @Size(max = 13, message = "El RFC del banco pagador no puede exceder 13 caracteres")
    private String payerBankRfc;

    @Size(max = 50, message = "La cuenta del pagador no puede exceder 50 caracteres")
    private String payerAccount;

    @Size(max = 13, message = "El RFC del banco beneficiario no puede exceder 13 caracteres")
    private String beneficiaryBankRfc;

    @Size(max = 50, message = "La cuenta del beneficiario no puede exceder 50 caracteres")
    private String beneficiaryAccount;
}