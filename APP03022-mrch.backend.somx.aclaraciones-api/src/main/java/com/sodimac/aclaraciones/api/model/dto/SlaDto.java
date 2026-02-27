package com.sodimac.aclaraciones.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SlaDto(
        @Schema(description = "SLA unique identifier.", example = "6") Integer id,
        @Schema(description = "Business unit catalog value.", example = "6") @NotBlank @Max(32) int businessUnit,
        @Schema(description = "Country catalog value.", example = "6") @NotBlank @Max(32) int country,
        @Schema(description = "Module catalog value.", example = "6") @NotBlank @Max(32) int module,
        @Schema(description = "Reason catalog value.", example = "6") @NotBlank @Max(32) int reason,
        @Schema(description = "Priority catalog value.", example = "6") @NotBlank @Max(32) int priority,
        @Schema(description = "First Response SLA catalog value.", example = "6") @NotBlank @Max(32) int firstResponseLevel,
        @Schema(description = "Resolution SLA catalog value.", example = "6") @NotBlank @Max(32) int resolutionLevel,
        @Schema(description = "Manager email", example = "develop@sodimac.com.mx") @NotBlank @Min(3) @Max(128) String manager,
        @Schema(description = "Publishing status", example = "true") @NotBlank Boolean published) {
}
