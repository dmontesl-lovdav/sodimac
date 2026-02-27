package com.sodimac.catman.api.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversionUpdateDto {
    @NotNull(message = "El ID del elemento destino es obligatorio")
    private Integer targetElementId;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer status;
}

