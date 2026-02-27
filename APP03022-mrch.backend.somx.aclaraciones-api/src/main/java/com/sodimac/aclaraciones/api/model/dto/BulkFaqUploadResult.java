package com.sodimac.aclaraciones.api.model.dto;

import java.util.List;

/**
 * Resultado de la carga masiva de FAQs.
 *
 * @param inserted filas insertadas correctamente
 * @param skipped  filas omitidas
 * @param errors   detalle de errores por línea
 */
public record BulkFaqUploadResult(
        int inserted,
        int skipped,
        List<ErrorRow> errors) {

    /** Fila con error específico. */
    public record ErrorRow(int line, String message) {
    }
}
