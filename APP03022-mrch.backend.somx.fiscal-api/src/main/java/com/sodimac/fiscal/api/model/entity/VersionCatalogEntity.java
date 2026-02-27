package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "version_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VersionCatalogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long versionId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", length = 254)
    private String description;

    @Column(name = "version", precision = 7, scale = 3, nullable = false)
    private BigDecimal version;

    @Column(name = "document_type", length = 1, nullable = false)
    private String documentType;

    @Column(name = "pac_id", nullable = false)
    private Integer pacId;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "structure_url", length = 254)
    private String structureUrl;

    @Column(name = "status")
    private Integer status = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pac_id", insertable = false, updatable = false)
    private PacCatalogEntity pacCatalog;
}