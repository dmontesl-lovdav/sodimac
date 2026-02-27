package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquivalenceDrDto {
    private UUID equivalenceUuid;

    @NotNull(message = "El UUID del documento relacionado es obligatorio")
    private UUID relatedDocumentUuid;

    @Size(max = 49, message = "El folio no puede exceder 49 caracteres")
    private String folio;

    @NotNull(message = "El monto pagado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto pagado debe ser mayor que 0")
    @Digits(integer = 14, fraction = 2, message = "El monto pagado no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal amountPaid;

    @NotNull(message = "El saldo anterior es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo anterior debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "El saldo anterior no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal previousBalance;

    @NotNull(message = "El saldo restante es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo restante debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "El saldo restante no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal remainingBalance;

    @Size(max = 3, message = "La moneda no puede exceder 3 caracteres")
    private String currency;

    @DecimalMin(value = "0.0", message = "El número de parcialidad debe ser mayor o igual que 0")
    @Digits(integer = 3, fraction = 0, message = "El número de parcialidad no puede tener más de 3 dígitos")
    private BigDecimal installmentNumber;

    @Size(max = 10, message = "El objeto del impuesto no puede exceder 10 caracteres")
    private String taxObject;

    @Size(max = 25, message = "La serie no puede exceder 25 caracteres")
    private String series;
}