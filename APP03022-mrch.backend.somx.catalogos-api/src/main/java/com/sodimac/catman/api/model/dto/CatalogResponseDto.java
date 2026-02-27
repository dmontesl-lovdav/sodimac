package com.sodimac.catman.api.model.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Información completa de un catálogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogResponseDto {

    @Schema(description = "ID del catálogo", example = "1")
    private Integer id;

    @Schema(description = "Código único del catálogo", example = "CAT_PAISES")
    private String code;

    @Schema(description = "Prefijo para claves", example = "PAI")
    private String prefix;

    @Schema(description = "Nombre del catálogo", example = "Catálogo de Países")
    private String name;

    @Schema(description = "Descripción del catálogo")
    private String description;

    @Schema(description = "Módulo al que pertenece", example = "general")
    private String module;

    @Schema(description = "Tipo de catálogo: PRIMARIO o SECUNDARIO", example = "PRIMARIO")
    private String catalogType;

    @Schema(description = "Estado: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;

    @Schema(description = "Usuario que creó el registro")
    private String createdBy;

    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    @Schema(description = "Usuario que actualizó el registro")
    private String updatedBy;

    @Schema(description = "Fecha de actualización")
    private LocalDateTime updatedAt;

    @Schema(description = "Cantidad de elementos en el catálogo")
    private Long elementCount;
}







