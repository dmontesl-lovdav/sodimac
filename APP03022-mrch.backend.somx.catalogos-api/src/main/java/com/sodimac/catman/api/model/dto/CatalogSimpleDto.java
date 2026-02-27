package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Catálogo simplificado para dropdowns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSimpleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID del catálogo", example = "1")
    private Integer id;

    @Schema(description = "Código del catálogo", example = "CAT_MODULOS")
    private String code;

    @Schema(description = "Nombre del catálogo", example = "Catálogo de Módulos")
    private String name;

    @Schema(description = "Descripción del catálogo", example = "Lista de módulos del sistema")
    private String description;

    @Schema(description = "Tipo de catálogo: PRIMARIO o SECUNDARIO", example = "PRIMARIO")
    private String catalogType;

    @Schema(description = "Estatus: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;
}







