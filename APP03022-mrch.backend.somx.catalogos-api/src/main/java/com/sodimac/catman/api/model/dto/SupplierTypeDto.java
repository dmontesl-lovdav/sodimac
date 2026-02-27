package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para tipo de proveedor.
 */
@Schema(description = "Tipo de proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierTypeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID del tipo de proveedor", example = "1")
    private Integer id;

    @Schema(description = "Codigo del tipo", example = "NAC")
    private String code;

    @Schema(description = "Descripcion del tipo", example = "Proveedor Nacional")
    private String description;
}
