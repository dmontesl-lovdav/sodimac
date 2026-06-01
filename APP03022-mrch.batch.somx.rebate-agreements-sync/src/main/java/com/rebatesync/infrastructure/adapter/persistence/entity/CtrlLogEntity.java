package com.rebatesync.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla ctrlLog en SODIMAC_BATCH_DEV
 * Representa los logs técnicos y funcionales del proceso
 */
@Entity
@Table(name = "ctrlLog", schema = "dbo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CtrlLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "id_ejecucion")
    private Long idEjecucion;

    @Column(name = "log", columnDefinition = "TEXT")
    private String log;

    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;

    @Column(name = "fase", length = 50)
    private String fase;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (nivel == null) {
            nivel = "INFO";
        }
    }
}
