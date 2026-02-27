package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Catálogo con encabezado y detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogHeaderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Código único del catálogo", example = "CatEstatusFactura")
    private String code;

    @Schema(description = "Prefijo para claves de detalle", example = "EFA")
    private String prefix;

    @Schema(description = "Nombre del catálogo", example = "Estatus de Factura")
    private String name;

    @Schema(description = "Descripción del catálogo", example = "Catálogo de estados de factura")
    private String description;

    @Schema(description = "Módulo al que pertenece", example = "FACTURACION")
    private String module;

    @Schema(description = "Tipo de catálogo: SIMPLE, MESSAGE, SAT_FISCAL, HIERARCHICAL, BOOLEAN", example = "SIMPLE")
    private String catalogType;

    @Schema(description = "Lista de elementos del catálogo")
    private List<CatalogDetailDto> details;
}
