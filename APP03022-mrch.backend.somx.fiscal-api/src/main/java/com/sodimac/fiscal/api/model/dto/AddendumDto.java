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
public class AddendumDto {
    private UUID addendumUuid;

    @NotNull(message = "El UUID de la factura es obligatorio")
    private UUID invoiceUuid;

    @DecimalMin(value = "0.0", message = "El número de proveedor debe ser mayor o igual que 0")
    @Digits(integer = 10, fraction = 0, message = "El número de proveedor no puede tener más de 10 dígitos")
    private BigDecimal supplierNumber;

    @Size(max = 20, message = "El número de recepción no puede exceder 20 caracteres")
    private String receptionNumber;

    @Size(max = 50, message = "El número de orden de compra no puede exceder 50 caracteres")
    private String purchaseOrderNumber;

    @Size(max = 30, message = "El número de guía de envío no puede exceder 30 caracteres")
    private String shippingGuideNumber;

    private String addendumContent;
}