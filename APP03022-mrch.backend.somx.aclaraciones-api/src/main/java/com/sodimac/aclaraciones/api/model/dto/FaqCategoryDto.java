package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for FAQ Category.
 * icon: Base64 string (nullable)
 * iconName: original filename or suggested name (nullable)
 * isActive: if null on create -> true
 */
public record FaqCategoryDto(
                Long id,
                @NotBlank String name,
                @NotBlank String description,
                String icon, // Base64 string
                String iconName, // e.g., "category.png" (nullable)
                Boolean isActive) {
}
