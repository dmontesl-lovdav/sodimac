package com.sodimac.aclaraciones.api.model.dto;

/** DTO de retorno para operaciones de escritura sobre Categoría. */
public record FaqCategoryResponse(
        Long id,
        String name,
        String description,
        Boolean isActive) {
}
