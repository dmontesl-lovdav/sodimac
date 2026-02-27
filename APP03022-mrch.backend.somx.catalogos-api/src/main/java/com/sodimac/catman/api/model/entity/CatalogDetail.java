package com.sodimac.catman.api.model.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

@Entity
@Table(name = "catalog_detail",
    uniqueConstraints = @UniqueConstraint(columnNames = {"header_id", "key"}),
    indexes = {
        @Index(columnList = "header_id"),
        @Index(columnList = "key"),
        @Index(columnList = "dict_id"),
        @Index(columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private CatalogHeader header;

    @Column(name = "key", nullable = false, length = 64)
    private String key;

    @Column(name = "dict_id", nullable = false)
    private Integer dictId;

    @Column(length = 16)
    private String color;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer status = STATUS_ACTIVE;

    /**
     * Identificador interno del elemento (idEstatus del Excel/negocio).
     * Corresponde al ID numérico definido por el PM.
     * Para catálogos de estatus: 0=Pendiente, 1=Activo, 2=Procesado, etc.
     */
    @Column(name = "internal_status")
    private Integer internalStatus;

    /**
     * Clave externa o real del catálogo.
     * Para catálogos del SAT es la clave oficial (G01, 601, 0.160000).
     */
    @Column(name = "external_key", length = 50)
    private String externalKey;

    /**
     * Valor asociado al elemento del catálogo.
     * Para tasas de impuestos es el porcentaje (0.16).
     */
    @Column(name = "value", length = 100)
    private String value;

    /**
     * Fecha de inicio de vigencia del elemento.
     */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /**
     * Fecha de fin de vigencia del elemento.
     */
    @Column(name = "valid_to")
    private LocalDate validTo;

    /**
     * Usuario que creó el registro.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Usuario que actualizó el registro.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * ID del catálogo padre (solo para catálogos secundarios).
     */
    @Column(name = "parent_catalog_id")
    private Integer parentCatalogId;

    /**
     * ID del elemento padre (solo para catálogos secundarios).
     */
    @Column(name = "parent_element_id")
    private Integer parentElementId;

    /**
     * Atributos adicionales en formato JSON.
     * Ej: {"aplica_pf": true, "aplica_pm": true, "traslado": true}
     */
    @Column(name = "attributes", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String attributes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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
