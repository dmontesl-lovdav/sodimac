package com.sodimac.aclaraciones.api.model.dto;

import java.util.List;

public record BulkCategoryUploadResult(
        int inserted,
        int skipped,
        List<ErrorRow> errors) {
    public record ErrorRow(int line, String reason) {
    }
}
