package com.rebatesync.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla ctrlProcesoDet en SODIMAC_BATCH_DEV
 * Representa los pasos/etapas detallados de un proceso de sincronización
 */
@Entity
@Table(name = "ctrlProcesoDet", schema = "dbo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CtrlProcDetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_flujo")
    private Long idFlujo;

    @Column(name = "id_ejecucion", nullable = false)
    private Long idEjecucion;

    @Column(name = "nombre_paso", nullable = false, length = 100)
    private String nombrePaso;

    @Column(name = "secuencia", nullable = false)
    private Integer secuencia;

    @Column(name = "fecha_inicio_registro", nullable = false)
    private LocalDateTime fechaInicioRegistro;

    @Column(name = "fecha_final_registro")
    private LocalDateTime fechaFinalRegistro;

    @Column(name = "parametros_registro", length = 255)
    private String parametrosRegistro;

    @Column(name = "detalle", length = 255)
    private String detalle;

    @Column(name = "registros_procesados")
    private Integer registrosProcesados;

    @Column(name = "estatus", nullable = false, length = 20)
    private String estatus;

    @PrePersist
    protected void onCreate() {
        if (fechaInicioRegistro == null) {
            fechaInicioRegistro = LocalDateTime.now();
        }
        if (estatus == null) {
            estatus = "IN_PROGRESS";
        }
        if (registrosProcesados == null) {
            registrosProcesados = 0;
        }
    }
}
