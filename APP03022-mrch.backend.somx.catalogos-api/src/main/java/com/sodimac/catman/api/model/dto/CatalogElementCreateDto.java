package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Datos para crear un elemento de catálogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogElementCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Nombre del elemento (obligatorio)", example = "Motivo K", required = true)
    @NotBlank(message = "El nombre del elemento es obligatorio")
    @Size(max = 100, message = "El nombre del elemento no puede exceder 100 caracteres")
    private String element;

    @Schema(description = "Valor del elemento (opcional)", example = "250.00")
    @Size(max = 100, message = "El valor del elemento no puede exceder 100 caracteres")
    private String value;

    @Schema(description = "Fecha de inicio de vigencia (obligatoria)", example = "2025-01-01", required = true)
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate validFrom;

    @Schema(description = "Fecha de fin de vigencia (opcional)", example = "2025-12-31")
    private LocalDate validTo;

    @Schema(description = "ID del catálogo padre (solo para catálogos secundarios)", example = "3")
    private Integer parentCatalogId;

    @Schema(description = "ID del elemento padre (solo para catálogos secundarios)", example = "10")
    private Integer parentElementId;

    @Schema(description = "Orden de visualización", example = "1")
    private Integer sortOrder;

    @Schema(description = "Atributos adicionales en formato JSON")
    private String attributes;
}







