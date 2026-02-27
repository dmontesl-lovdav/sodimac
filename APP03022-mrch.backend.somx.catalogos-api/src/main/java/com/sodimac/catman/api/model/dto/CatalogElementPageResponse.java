package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Respuesta paginada de elementos de catálogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogElementPageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Lista de elementos", example = "[]")
    private List<CatalogElementDto> items;

    @Schema(description = "Número de página actual (1-indexed)", example = "1")
    private Integer page;

    @Schema(description = "Tamaño de página", example = "10")
    private Integer pageSize;

    @Schema(description = "Total de elementos encontrados", example = "25")
    private Long total;

    @Schema(description = "Total de páginas", example = "3")
    private Integer totalPages;

    @Schema(description = "Indica si hay página siguiente", example = "true")
    private Boolean hasNext;

    @Schema(description = "Indica si hay página anterior", example = "false")
    private Boolean hasPrevious;
}







