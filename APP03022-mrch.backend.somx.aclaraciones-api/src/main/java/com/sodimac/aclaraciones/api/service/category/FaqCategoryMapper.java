// src/main/java/com/sodimac/aclaraciones/api/service/category/FaqCategoryMapper.java
package com.sodimac.aclaraciones.api.service.category;

import java.util.Base64;

import com.sodimac.aclaraciones.api.model.dto.FaqCategoryDto;
import com.sodimac.aclaraciones.api.model.entity.FaqCategory;

public final class FaqCategoryMapper {

    /** Convierte entidad → DTO */
    public static FaqCategoryDto toDto(FaqCategory e, boolean includeIcon) {
        String iconB64 = null;
        if (includeIcon && e.getImageData() != null && e.getImageData().length > 0) {
            iconB64 = Base64.getEncoder().encodeToString(e.getImageData());
        }
        return new FaqCategoryDto(
                e.getId(),
                e.getName(),
                e.getDescription(),
                iconB64,
                e.getImageName(),
                e.getIsActive());
    }

    /** Aplica campos comunes (no icono) */
    public static void applyCommon(FaqCategory e, FaqCategoryDto d) {
        e.setName(d.name());
        e.setDescription(d.description());
        if (d.isActive() != null) {
            e.setIsActive(d.isActive());
        }
    }

    /**
     * Aplica el icono del DTO a la entidad.
     * - icon == null → no tocar
     * - icon == "" → limpiar imagen
     * - base64 → decodificar y guardar en byte[]
     */
    public static void applyIcon(FaqCategory e, FaqCategoryDto d) {
        String b64 = d.icon();
        if (b64 == null) {
            return; // no modificar
        }
        if (b64.isBlank()) {
            e.setImageData(null);
            e.setImageName(null);
            e.setIconPath(null);
            return;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(b64.replaceAll("\\s+", ""));
            e.setImageData(bytes);
            e.setImageName(d.iconName() != null ? d.iconName() : "icon.bin");
            e.setIconPath(null); // compat: dejamos null porque ya usamos blob
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid Base64 data for icon", ex);
        }
    }

    private FaqCategoryMapper() {
        // utility class, no instanciable
    }
}
