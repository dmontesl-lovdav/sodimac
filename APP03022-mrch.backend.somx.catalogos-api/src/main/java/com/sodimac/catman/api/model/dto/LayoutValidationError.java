package com.sodimac.catman.api.model.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LayoutValidationError {
    private int row;
    private String cell;
    private String column;
    private String message;
}

