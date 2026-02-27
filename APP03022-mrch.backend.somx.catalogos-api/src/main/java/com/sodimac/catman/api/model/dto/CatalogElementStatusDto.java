package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Datos para cambiar el estatus de un elemento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogElementStatusDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Nuevo estatus: 1=Activo, 0=Inactivo", example = "1", required = true)
    @NotNull(message = "El estatus es obligatorio")
    private Integer status;
}







