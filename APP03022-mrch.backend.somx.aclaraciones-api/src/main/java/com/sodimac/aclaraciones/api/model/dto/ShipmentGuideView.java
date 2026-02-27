package com.sodimac.aclaraciones.api.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShipmentGuideView(
        Long id,
        Long supplierNumber,
        String supplierName,
        String guideNumber,
        String plate,
        String trailerPlate,
        String origin,
        String deliveryType,
        BigDecimal amount,
        LocalDate shippingDate,
        LocalDate createdAt,
        Integer status,
        String statusDesc) {
}
