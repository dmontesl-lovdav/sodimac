package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateReceptionRequest(

        @NotNull(message = "ERR_FIELD_RECEPCION|ID de recepción obligatorio") Long recepcion,

        @NotNull(message = "ERR_FIELD_ID_ORIGEN_RECEPCION|ID origen obligatorio") Long idOrigen,

        @NotNull(message = "ERR_FIELD_ID_DESTINO_RECEPCION|ID destino obligatorio") Long idDestino,

        @NotNull(message = "ERR_FIELD_IMPORTE_RECEPCION|Importe obligatorio") @Digits(integer = 10, fraction = 2, message = "ERR_FIELD_IMPORTE_RECEPCION|Máximo 2 decimales") BigDecimal importe,

        @NotNull(message = "ERR_FIELD_ESTATUS_RECEPCION|Estatus obligatorio") Integer estatus,

        @Size(max = 256, message = "ERR_FIELD_COMENTARIO|Máximo 256 caracteres") String comentario,

        @NotNull(message = "ERR_FIELD_FECHA_RECEPCION|Fecha recepción obligatoria") LocalDate fechaRecepcion,

        @NotNull(message = "ERR_FIELD_ID_USUARIO_RECEPCION|ID de usuario obligatorio") Long idUsuario,

        @NotNull(message = "ERR_FIELD_FECHA_REGISTRO_RECEPCION|Fecha de registro obligatoria") LocalDate fechaRegistro,

        @NotEmpty(message = "ERR_FIELD_SKUS|Debe incluir al menos un SKU") List<@Valid CreateSkuRequest> skus

) {
}
