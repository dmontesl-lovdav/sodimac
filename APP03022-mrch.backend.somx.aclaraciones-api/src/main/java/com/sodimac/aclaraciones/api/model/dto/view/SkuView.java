package com.sodimac.aclaraciones.api.model.dto.view;

import java.math.BigDecimal;

public record SkuView(
        String sku,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        BigDecimal costoTotal,
        Integer estatus) {
}
