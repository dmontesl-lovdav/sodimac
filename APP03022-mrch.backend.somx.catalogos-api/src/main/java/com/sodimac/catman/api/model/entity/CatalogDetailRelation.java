package com.sodimac.catman.api.model.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * Entidad que representa las relaciones/dependencias entre elementos de catálogos.
 * Permite definir que un elemento de un catálogo depende de elementos de otro catálogo.
 *
 * Ejemplo: UsoCFDI "G01" depende de RegimenFiscal "601", "603", "606"...
 *
 * Si esta tabla está vacía, los catálogos funcionan de forma independiente.
 */
@Entity
@Table(name = "catalog_detail_relation",
    uniqueConstraints = @UniqueConstraint(columnNames = {"source_detail_id", "target_detail_id", "relation_type"}),
    indexes = {
        @Index(columnList = "source_detail_id"),
        @Index(columnList = "target_detail_id"),
        @Index(columnList = "relation_type"),
        @Index(columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogDetailRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    // Tipos de relación
    public static final String RELATION_DEPENDS_ON = "DEPENDS_ON";
    public static final String RELATION_REQUIRES = "REQUIRES";
    public static final String RELATION_EXCLUDES = "EXCLUDES";
    public static final String RELATION_PARENT_OF = "PARENT_OF";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Detalle origen: el elemento que tiene la dependencia.
     * Ej: G01 de UsoCFDI
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_detail_id", nullable = false)
    private CatalogDetail sourceDetail;

    /**
     * Detalle destino: el elemento del que depende.
     * Ej: 601 de RegimenFiscal
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_detail_id", nullable = false)
    private CatalogDetail targetDetail;

    /**
     * Tipo de relación entre los elementos.
     * Valores: DEPENDS_ON, REQUIRES, EXCLUDES, PARENT_OF
     */
    @Column(name = "relation_type", nullable = false, length = 20)
    @Builder.Default
    private String relationType = RELATION_DEPENDS_ON;

    /**
     * Estado de la relación: 1=Activa, 0=Inactiva
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = STATUS_ACTIVE;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
