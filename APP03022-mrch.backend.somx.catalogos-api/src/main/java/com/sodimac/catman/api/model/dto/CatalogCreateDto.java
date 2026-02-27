package com.sodimac.catman.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Datos para crear un nuevo catálogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogCreateDto {

    @Schema(description = "Código del catálogo", example = "CAT_PAISES")
    @Size(max = 64, message = "El código no debe exceder 64 caracteres")
    private String code;

    @Schema(description = "Prefijo del catálogo (3 letras)", example = "PAI")
    @Size(max = 3, message = "El prefijo no debe exceder 3 caracteres")
    private String prefix;

    @Schema(description = "Nombre del catálogo", example = "Catálogo de Países", required = true)
    @NotBlank(message = "El nombre del catálogo es obligatorio")
    @Size(max = 128, message = "El nombre no debe exceder 128 caracteres")
    private String name;

    @Schema(description = "Descripción del catálogo", example = "Lista de países disponibles")
    @Size(max = 250, message = "La descripción no debe exceder 250 caracteres")
    private String description;

    @Schema(description = "Tipo de catálogo: PRIMARIO o SECUNDARIO", example = "PRIMARIO", required = true)
    @NotBlank(message = "El tipo de catálogo es obligatorio")
    private String catalogType;

    @Schema(description = "Módulo al que pertenece", example = "general")
    private String module;
}







