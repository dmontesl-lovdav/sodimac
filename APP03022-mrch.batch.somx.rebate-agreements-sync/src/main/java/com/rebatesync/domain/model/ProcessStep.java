package com.rebatesync.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un paso/etapa del proceso de sincronización (ctrlProcesoDet)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStep {
    private Long idFlujo;
    private Long idEjecucion;
    private String nombrePaso;
    private Integer secuencia;
    private LocalDateTime fechaInicioRegistro;
    private LocalDateTime fechaFinalRegistro;
    private String parametrosRegistro;
    private String detalle;
    private Integer registrosProcesados;
    private String estatus; // IN_PROGRESS, SUCCESS, FAILED
}
