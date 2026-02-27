package com.sodimac.catman.api.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversionCreateDto {
    @NotNull private Integer sourceElementId;
    @NotNull private Integer targetElementId;
    private LocalDate validFrom;
    private LocalDate validTo;
    @Builder.Default private Boolean isPrincipal = false;
}

