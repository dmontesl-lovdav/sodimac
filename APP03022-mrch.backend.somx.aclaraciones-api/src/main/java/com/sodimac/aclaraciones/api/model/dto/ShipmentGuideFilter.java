package com.sodimac.aclaraciones.api.model.dto;

import java.time.LocalDate;

public record ShipmentGuideFilter(
        Long supplierNumber,
        String guideNumber,
        Long purchaseOrder,
        Integer status,
        LocalDate fromShipDate,
        LocalDate toShipDate,
        LocalDate fromRegDate,
        LocalDate toRegDate) {
}
