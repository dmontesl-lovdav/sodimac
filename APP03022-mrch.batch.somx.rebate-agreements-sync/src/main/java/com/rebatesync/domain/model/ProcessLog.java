package com.rebatesync.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un log del proceso (ctrlLog)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessLog {
    private Long idLog;
    private String nombre;
    private Long idEjecucion;
    private String log;
    private String nivel; // INFO, WARN, ERROR, DEBUG
    private String fase; // EXTRACT, TRANSFORM, LOAD, VALIDATION
    private LocalDateTime fechaRegistro;
}
