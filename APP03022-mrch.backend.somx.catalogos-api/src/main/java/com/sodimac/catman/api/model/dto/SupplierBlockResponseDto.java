package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para operaciones de bloqueo de proveedor.
 * STM-605: Incluye mensaje del catalogo de mensajes + datos del bloqueo.
 */
@Schema(description = "Respuesta de operacion de bloqueo de proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBlockResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Mensaje de la operacion",
            example = "El registro del proveedor 0000100025 se realizó exitosamente.")
    private String message;

    @Schema(description = "Datos del bloqueo")
    private SupplierBlockDto data;
}
