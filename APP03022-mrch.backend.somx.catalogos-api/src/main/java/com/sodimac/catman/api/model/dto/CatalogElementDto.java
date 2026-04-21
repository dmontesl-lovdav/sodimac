package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Elemento de un catálogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogElementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único del elemento", example = "1")
    private Integer id;

    @Schema(description = "ID del catálogo al que pertenece", example = "5")
    private Integer catalogId;

    @Schema(description = "Código del catálogo al que pertenece", example = "CAT_MOTIVOS")
    private String catalogCode;

    @Schema(description = "Nombre del elemento", example = "Motivo A")
    private String element;

    @Schema(description = "Valor del elemento", example = "100.00")
    private String value;

    @Schema(description = "Clave interna del elemento", example = "MOT001")
    private String key;

    @Schema(description = "Fecha de inicio de vigencia", example = "2025-01-01")
    private LocalDate validFrom;

    @Schema(description = "Fecha de fin de vigencia", example = "2025-12-31")
    private LocalDate validTo;

    @Schema(description = "Estatus del elemento: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;

    @Schema(description = "Descripción del estatus", example = "Activo")
    private String statusDescription;

    @Schema(description = "ID del catálogo padre (solo para catálogos secundarios)", example = "3")
    private Integer parentCatalogId;

    @Schema(description = "Nombre del catálogo padre", example = "Catálogo de Módulos")
    private String parentCatalogName;

    @Schema(description = "ID del elemento padre (solo para catálogos secundarios)", example = "10")
    private Integer parentElementId;

    @Schema(description = "Nombre del elemento padre", example = "Finanzas")
    private String parentElementName;

    @Schema(description = "ID del usuario que creó el registro", example = "admin")
    private String createdBy;

    @Schema(description = "Fecha de creación", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID del usuario que actualizó el registro", example = "admin")
    private String updatedBy;

    @Schema(description = "Fecha de actualización", example = "2025-01-16T14:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Valor de conversión para sistemas externos", example = "MX")
    private String externalKey;

    @Schema(description = "Orden de visualización", example = "1")
    private Integer sortOrder;

    @Schema(description = "Atributos adicionales en formato JSON")
    private String attributes;
}







