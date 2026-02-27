package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseOrderRequest(

        @NotNull(message = "ERR_FIELD_ORDEN_COMPRA|Orden de compra obligatoria") Long ordenCompra,

        @NotNull(message = "ERR_FIELD_NUMERO_PROVEEDOR|Número de proveedor obligatorio") Long numeroProveedor,

        @NotNull(message = "ERR_FIELD_ID_ORIGEN|ID de origen obligatorio") Long idOrigen,

        @NotNull(message = "ERR_FIELD_IMPORTE|Importe obligatorio") @Digits(integer = 10, fraction = 2, message = "ERR_FIELD_IMPORTE|Importe inválido (hasta 2 decimales)") BigDecimal importe,

        @NotNull(message = "ERR_FIELD_ESTATUS|Estatus obligatorio") Integer estatus,

        @NotNull(message = "ERR_FIELD_ID_USUARIO|ID de usuario obligatorio") Long idUsuario,

        @NotNull(message = "ERR_FIELD_FECHA_REGISTRO|Fecha de registro obligatoria") LocalDate fechaRegistro,

        @NotEmpty(message = "ERR_FIELD_RECEPCIONES|Debe incluir al menos una recepción") List<@Valid CreateReceptionRequest> recepciones

) {
}
