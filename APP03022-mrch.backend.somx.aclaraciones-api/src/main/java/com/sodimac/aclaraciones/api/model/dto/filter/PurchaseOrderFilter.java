package com.sodimac.aclaraciones.api.model.dto.filter;

import java.time.LocalDate;

public record PurchaseOrderFilter(
        Long ordenCompra,
        Long recepcion,
        Long numeroProveedor,
        Integer estatus,
        LocalDate fechaRecDesde,
        LocalDate fechaRecHasta,
        LocalDate fechaRegDesde,
        LocalDate fechaRegHasta) {
}
