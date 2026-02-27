package com.sodimac.aclaraciones.api.model.dto.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderView(
        Long ordenCompra,
        Long numeroProveedor,
        String nombreProveedor,
        Long idOrigen,
        BigDecimal importe,
        Integer estatus,
        Long idUsuario,
        LocalDate fechaRegistro,
        List<ReceptionView> recepciones
) {}
