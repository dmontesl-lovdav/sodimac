package com.sodimac.aclaraciones.api.model.dto.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReceptionView(
        Long recepcion,
        Long idOrigen,
        Long idDestino,
        BigDecimal importe,
        Integer estatus,
        String comentario,
        LocalDate fechaRecepcion,
        Long idUsuario,
        LocalDate fechaRegistro,
        List<SkuView> skus) {
}
