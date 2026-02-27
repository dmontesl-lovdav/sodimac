package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos de entrada para registrar la cabecera de una guía de embarque.
 */
public record CreateShipmentGuideRequest(

        // ─── Datos obligatorios ────────────────────────────────────────────────
        @NotNull(message = "ERR_FIELD_PROVIDER_NUMBER|Número de proveedor obligatorio") Long providerNumber,

        @NotBlank(message = "ERR_FIELD_GUIDE_NUMBER|Guía vacía") @Pattern(regexp = "^[A-Z0-9-]{4,50}$", message = "ERR_FIELD_GUIDE_NUMBER|Guía inválida (solo mayúsculas, números y guiones)") String guideNumber,

        String plate,
        String trailerPlate,

        @NotBlank(message = "ERR_FIELD_ORIGIN|Origen obligatorio") String origin,

        @NotBlank(message = "ERR_FIELD_DELIVERY_TYPE|Tipo de entrega obligatorio") String deliveryType,

        @NotNull(message = "ERR_FIELD_AMOUNT|Importe obligatorio") @DecimalMin(value = "0.01", message = "ERR_FIELD_AMOUNT|El importe debe ser positivo") BigDecimal amount,

        @NotNull(message = "ERR_FIELD_DELIVERY_DATE|Fecha de entrega obligatoria") @FutureOrPresent(message = "ERR_FIELD_DELIVERY_DATE|Fecha de entrega en el pasado") LocalDate deliveryDate,

        @NotNull(message = "ERR_FIELD_SHIPPING_DATE|Fecha de envío obligatoria") @PastOrPresent(message = "ERR_FIELD_SHIPPING_DATE|Fecha de envío en el futuro") LocalDate shippingDate,

        @NotEmpty(message = "ERR_FIELD_PURCHASE_ORDERS|Debe incluir al menos una OC") List<Long> purchaseOrders,

        @NotEmpty(message = "ERR_FIELD_RECEIPTS|Debe incluir al menos una recepción") List<Long> receipts,

        // ─── Campo opcional ────────────────────────────────────────────────────
        String comments) {
}
