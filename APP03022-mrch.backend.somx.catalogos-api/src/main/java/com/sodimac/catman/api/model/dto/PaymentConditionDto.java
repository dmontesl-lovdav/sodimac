package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para condicion de pago.
 */
@Schema(description = "Condicion de pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConditionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID de la condicion de pago", example = "1")
    private Integer id;

    @Schema(description = "Nombre de la condicion", example = "30 dias")
    private String conditionName;

    @Schema(description = "Dias de plazo", example = "30")
    private Integer days;
}
