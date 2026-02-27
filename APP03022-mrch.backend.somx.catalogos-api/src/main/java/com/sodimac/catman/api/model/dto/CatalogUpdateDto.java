package com.sodimac.catman.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Datos para actualizar un catálogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogUpdateDto {

    @Schema(description = "Nombre del catálogo", example = "Catálogo de Países Actualizado")
    @Size(max = 128, message = "El nombre no debe exceder 128 caracteres")
    private String name;

    @Schema(description = "Descripción del catálogo", example = "Lista actualizada de países")
    @Size(max = 250, message = "La descripción no debe exceder 250 caracteres")
    private String description;

    @Schema(description = "Tipo de catálogo: PRIMARIO o SECUNDARIO", example = "PRIMARIO")
    private String catalogType;

    @Schema(description = "Estado: 1=Activo, 0=Inactivo")
    private Integer status;

    @Schema(description = "Módulo al que pertenece", example = "general")
    private String module;
}







