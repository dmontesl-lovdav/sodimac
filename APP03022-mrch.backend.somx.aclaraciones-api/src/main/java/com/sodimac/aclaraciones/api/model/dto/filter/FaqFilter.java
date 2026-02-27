package com.sodimac.aclaraciones.api.model.dto.filter;

/**
 * Filtros de búsqueda para FAQ.
 *
 * <ul>
 * <li><b>searchTerm</b> – texto libre (pregunta o respuesta)</li>
 * <li><b>categoryId</b> – id de categoría (0 / null ⇒ todas)</li>
 * <li><b>popularOnly</b> – true ⇒ ordena por <i>views DESC</i></li>
 * <li><b>size</b> – límite de resultados (por defecto 50)</li>
 * </ul>
 *
 * Se incluyen dos helpers:
 * <ul>
 * <li>{@code limit()} – devuelve size normalizado</li>
 * <li>{@code normalizedCategoryId()} – 0 / null → null (todas)</li>
 * </ul>
 */
public record FaqFilter(
        String searchTerm,
        Long categoryId,
        Boolean popularOnly,
        Integer size) {

    /** tamaño máximo (por defecto 50) */
    public int limit() {
        return (size != null && size > 0) ? size : 50;
    }

    /** 0 o null ⇒ “todas las categorías” (retorna null) */
    public Long normalizedCategoryId() {
        return (categoryId != null && categoryId > 0) ? categoryId : null;
    }
}
