package com.rebatesync.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un elemento individual procesado (ctrlProcesoElemento)
 * Ej: cada agreement procesado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessElement {
    private Long idElemento;
    private Long idEjecucion;
    private String valor; // ID único del elemento (ej: agreement_id)
    private String valorAlterno; // Información adicional
    private Integer secuencia;
    private String estatus; // PROCESSED, FAILED, SKIPPED
    private LocalDateTime fechaRegistro;
    private String detalleError;
}
