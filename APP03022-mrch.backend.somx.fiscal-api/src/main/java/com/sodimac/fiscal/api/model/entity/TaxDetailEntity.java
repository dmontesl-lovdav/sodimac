package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing detailed tax information by invoice concept.
 *
 * Stores granular tax details for each product or service concept in the CFDI,
 * allowing for complete breakdown of transfers and withholdings by line item.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-11
 */
@Entity
@Table(name = "tax_detail", schema = "tenant_fiscal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxDetailEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tax_detail_uuid", columnDefinition = "uuid")
    private UUID taxDetailUuid;

    @Column(name = "tax_uuid", nullable = false, columnDefinition = "uuid")
    private UUID taxUuid;

    /**
     * SAT product or service code
     */
    @Column(name = "product_service_code", length = 20)
    private String productServiceCode;

    /**
     * Tax type: Traslado or Retencion
     */
    @Column(name = "tax_type", length = 10, nullable = false)
    private String taxType;

    /**
     * SAT tax code.
     * 001 = ISR (Income Tax)
     * 002 = IVA (VAT)
     * 003 = IEPS (Special Production Tax)
     */
    @Column(name = "tax_code", length = 3, nullable = false)
    private String taxCode;

    /**
     * Factor type: Tasa, Cuota, Exento
     */
    @Column(name = "factor_type", length = 10)
    private String factorType;

    /**
     * Tax rate or quota
     */
    @Column(name = "rate_or_quota", precision = 6, scale = 6)
    private BigDecimal rateOrQuota;

    /**
     * Tax amount
     */
    @Column(name = "amount", precision = 16, scale = 2)
    private BigDecimal amount;

    /**
     * Taxable base amount
     */
    @Column(name = "base", precision = 16, scale = 2)
    private BigDecimal base;

    // Relationship with Tax
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_uuid", insertable = false, updatable = false)
    private TaxEntity tax;
}
