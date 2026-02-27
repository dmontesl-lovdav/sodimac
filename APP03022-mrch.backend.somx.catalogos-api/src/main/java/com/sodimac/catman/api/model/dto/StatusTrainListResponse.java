package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para lista de transiciones permitidas.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Schema(description = "Respuesta con lista de transiciones permitidas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTrainListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;

    @Schema(description = "Código de error si la operación falla", example = "WRN7010")
    private String code;

    @Schema(description = "Mensaje descriptivo")
    private String message;

    @Schema(description = "Cantidad de registros encontrados", example = "3")
    private Integer count;

    @Schema(description = "Lista de transiciones permitidas")
    private List<StatusTrainDto> data;

    /**
     * Crea una respuesta exitosa con la lista de transiciones.
     */
    public static StatusTrainListResponse success(List<StatusTrainDto> data) {
        return StatusTrainListResponse.builder()
                .success(true)
                .count(data != null ? data.size() : 0)
                .data(data)
                .build();
    }

    /**
     * Crea una respuesta de error cuando el estatus origen no está catalogado.
     */
    public static StatusTrainListResponse sourceNotFound() {
        return StatusTrainListResponse.builder()
                .success(false)
                .code("WRN7010")
                .message("El estatus origen no existe catalogado. Por favor, valide la información antes de continuar.")
                .build();
    }
}
