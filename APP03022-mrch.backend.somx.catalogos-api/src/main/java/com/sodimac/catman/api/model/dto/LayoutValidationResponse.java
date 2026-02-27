package com.sodimac.catman.api.model.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LayoutValidationResponse {
    private boolean isValid;
    private int errorCount;
    private List<LayoutValidationError> errors;
    private boolean reportAvailable;
    private String reportId;
    private int rowsProcessed;
}

