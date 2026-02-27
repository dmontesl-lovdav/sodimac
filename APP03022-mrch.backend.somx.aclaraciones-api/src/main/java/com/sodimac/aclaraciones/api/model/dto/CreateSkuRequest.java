package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSkuRequest(

        @NotBlank(message = "ERR_FIELD_SKU|SKU obligatorio") @Size(max = 15, message = "ERR_FIELD_SKU|SKU máximo 15 caracteres") String sku,

        @NotBlank(message = "ERR_FIELD_DESCRIPTION|Descripción obligatoria") @Size(max = 256, message = "ERR_FIELD_DESCRIPTION|Máximo 256 caracteres") String descripcion,

        @NotNull(message = "ERR_FIELD_CANTIDAD|Cantidad obligatoria") @DecimalMin(value = "0.000001", inclusive = true, message = "ERR_FIELD_CANTIDAD|Debe ser mayor a cero") @Digits(integer = 12, fraction = 6, message = "ERR_FIELD_CANTIDAD|Máximo 6 decimales") BigDecimal cantidad,

        @NotNull(message = "ERR_FIELD_COSTO_UNITARIO|Costo unitario obligatorio") @DecimalMin(value = "0.01", inclusive = true, message = "ERR_FIELD_COSTO_UNITARIO|Debe ser mayor a cero") @Digits(integer = 10, fraction = 2, message = "ERR_FIELD_COSTO_UNITARIO|Máximo 2 decimales") BigDecimal costoUnitario,

        @NotNull(message = "ERR_FIELD_COSTO_TOTAL|Costo total obligatorio") @DecimalMin(value = "0.01", inclusive = true, message = "ERR_FIELD_COSTO_TOTAL|Debe ser mayor a cero") @Digits(integer = 12, fraction = 2, message = "ERR_FIELD_COSTO_TOTAL|Máximo 2 decimales") BigDecimal costoTotal,

        @NotNull(message = "ERR_FIELD_ESTATUS_SKU|Estatus obligatorio") @Min(value = 0, message = "ERR_FIELD_ESTATUS_SKU|Valor mínimo 0") @Max(value = 2147483647, message = "ERR_FIELD_ESTATUS_SKU|Valor demasiado alto") Integer estatus,

        @Positive(message = "ERR_FIELD_ID_USUARIO|ID de usuario inválido") Long idUsuarioActualizacion,

        @PastOrPresent(message = "ERR_FIELD_FECHA_ACTUALIZACION|La fecha no puede ser futura") LocalDate fechaActualizacion) {
}
