package com.sodimac.catman.api.model.dto;

import java.util.List;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversionPageResponse {
    private List<ConversionDto> items;
    private int page;
    private int pageSize;
    private long total;
    private int totalPages;
}

