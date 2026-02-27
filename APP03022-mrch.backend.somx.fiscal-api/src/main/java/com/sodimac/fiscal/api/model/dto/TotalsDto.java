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
public class TotalsDto {
    private UUID totalsUuid;

    @NotNull(message = "El UUID de pagos es obligatorio")
    private UUID paymentsUuid;

    @NotNull(message = "El total de pagos es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El total de pagos debe ser mayor que 0")
    @Digits(integer = 14, fraction = 2, message = "El total de pagos no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalPaymentsAmount;

    @DecimalMin(value = "0.0", message = "La base del IVA 16% debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "La base del IVA 16% no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalBaseIva16;

    @DecimalMin(value = "0.0", message = "El impuesto del IVA 16% debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "El impuesto del IVA 16% no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalTaxIva16;

    @DecimalMin(value = "0.0", message = "La base del IVA 8% debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "La base del IVA 8% no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalBaseIva8;

    @DecimalMin(value = "0.0", message = "El impuesto del IVA 8% debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "El impuesto del IVA 8% no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalTaxIva8;

    @DecimalMin(value = "0.0", message = "La base del IVA 0% debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "La base del IVA 0% no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalBaseIva0;

    @DecimalMin(value = "0.0", message = "La retención del IVA debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "La retención del IVA no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalWithholdingIva;

    @DecimalMin(value = "0.0", message = "La retención del ISR debe ser mayor o igual que 0")
    @Digits(integer = 14, fraction = 2, message = "La retención del ISR no puede tener más de 14 dígitos enteros y 2 decimales")
    private BigDecimal totalWithholdingIsr;
}