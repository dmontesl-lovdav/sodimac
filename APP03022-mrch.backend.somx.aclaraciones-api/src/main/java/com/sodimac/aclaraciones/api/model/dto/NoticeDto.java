package com.sodimac.aclaraciones.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeDto(
                @Schema(description = "Unique numeric identifier.", example = "6") Integer id,
                @Schema(description = "Business Unit. See `catalogs` methods for further info.", example = "6") @NotNull Integer businessUnit,
                @Schema(description = "Country of origin. See `catalogs` methods for further info.", example = "6") @NotNull Integer country,
                @Schema(description = "Info's title", example = "6") @Size(min = 2, max = 64) String name,
                @Schema(description = "Info's long description - paragraph.", example = "6") @Size(min = 2, max = 128) String description,
                @Schema(description = "Info's hyperlink", example = "6") @Size(min = 2, max = 256) String link,
                @Schema(description = "Info's position or location", example = "6") @NotNull Integer position,
                @Schema(description = "Publishing status", example = "true") boolean published) {
}
