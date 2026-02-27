package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Detalle de un catálogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogDetailDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Clave del elemento", example = "EFA001")
    private String key;

    @Schema(description = "ID interno del estatus (definido por negocio)", example = "1")
    private Integer internalStatus;

    @Schema(description = "Clave externa/real del catálogo (ej: G01 del SAT)", example = "G01")
    private String externalKey;

    @Schema(description = "Valor asociado al elemento (ej: 0.16 para IVA 16%)", example = "0.160000")
    private String value;

    @Schema(description = "Descripción del elemento", example = "Factura creada exitosamente")
    private String description;

    @Schema(description = "Color asociado (hexadecimal)", example = "#28a745")
    private String color;

    @Schema(description = "Orden de visualización", example = "1")
    private Integer sortOrder;

    @Schema(description = "Fecha de inicio de vigencia", example = "2024-01-01")
    private LocalDate validFrom;

    @Schema(description = "Fecha de fin de vigencia", example = "2024-12-31")
    private LocalDate validTo;

    @Schema(description = "Atributos adicionales en formato JSON", example = "{\"aplica_pf\": true, \"aplica_pm\": true}")
    private String attributes;
}
