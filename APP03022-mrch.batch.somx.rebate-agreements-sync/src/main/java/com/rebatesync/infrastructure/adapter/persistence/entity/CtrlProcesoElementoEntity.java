package com.rebatesync.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla ctrlProcesoElemento en SODIMAC_BATCH_DEV
 * Representa cada elemento/documento procesado (ej: cada agreement)
 */
@Entity
@Table(name = "ctrlProcesoElemento", schema = "dbo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CtrlProcesoElementoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idElemento")
    private Long idElemento;

    @Column(name = "id_ejecucion", nullable = false)
    private Long idEjecucion;

    @Column(name = "valor", nullable = false, length = 50)
    private String valor;

    @Column(name = "valorAlterno", length = 250)
    private String valorAlterno;

    @Column(name = "secuencia", nullable = false)
    private Integer secuencia;

    @Column(name = "estatus", nullable = false, length = 20)
    private String estatus;

    @Column(name = "fechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "detalle_error", length = 500)
    private String detalleError;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (estatus == null) {
            estatus = "PROCESSED";
        }
    }
}
