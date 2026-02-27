package com.sodimac.catman.api.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Respuesta paginada de catálogos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogPageResponse {

    @Schema(description = "Lista de catálogos en la página actual")
    private List<CatalogResponseDto> items;

    @Schema(description = "Número de página actual (1-indexed)", example = "1")
    private int page;

    @Schema(description = "Tamaño de página", example = "10")
    private int pageSize;

    @Schema(description = "Total de registros", example = "50")
    private long total;

    @Schema(description = "Total de páginas", example = "5")
    private int totalPages;

    @Schema(description = "Si hay página siguiente")
    private boolean hasNext;

    @Schema(description = "Si hay página anterior")
    private boolean hasPrevious;
}







