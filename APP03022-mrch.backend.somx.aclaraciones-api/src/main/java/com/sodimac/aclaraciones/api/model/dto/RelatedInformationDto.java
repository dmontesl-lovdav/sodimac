package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada / salida para Información Relacionada.
 * • `image` → Base-64 opcional.
 * • `imageName` → nombre del archivo (opcional).
 * • `isActive == null` implica true al crear.
 */
public record RelatedInformationDto(
        Long id,
        @NotBlank String title,
        @NotBlank String link,
        String image,      // Base-64 (puede ser null)
        String imageName,  // nombre.jpg (puede ser null)
        Boolean isActive,
        Integer businessUnitId,
        Integer countryId
) {
}
