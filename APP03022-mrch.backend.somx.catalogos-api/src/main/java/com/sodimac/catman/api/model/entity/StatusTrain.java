package com.sodimac.catman.api.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una regla de transición de estatus permitida.
 * Define qué cambios de estatus son válidos para cada tipo de documento (opción).
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Entity
@Table(name = "status_train",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_status_train", columnNames = {"option_id", "source_status", "target_status"})
    },
    indexes = {
        @Index(name = "idx_status_train_option", columnList = "option_id"),
        @Index(name = "idx_status_train_source", columnList = "option_id, source_status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTrain implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constantes para option_id
     */
    public static final int OPTION_INVOICE = 1;           // Factura (I)
    public static final int OPTION_CREDIT_NOTE = 2;       // Nota de Crédito (E)
    public static final int OPTION_PAYMENT_COMPLEMENT = 3; // Complemento de Pago (P)
    public static final int OPTION_CARTA_PORTE = 4;       // CartaPorte (T)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "option_id", nullable = false)
    private Integer optionId;

    @Column(name = "source_status", nullable = false)
    private Integer sourceStatus;

    @Column(name = "target_status", nullable = false)
    private Integer targetStatus;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
