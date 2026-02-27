package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing transferred taxes (IVA, IEPS) from CFDI.
 *
 * Stores information about taxes that are transferred from the seller
 * to the buyer, such as VAT (IVA) and special production taxes (IEPS).
 *
 * @author Sodimac Tech Team
 * @since 2025-11-11
 */
@Entity
@Table(name = "tax_transfer", schema = "tenant_fiscal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxTransferEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tax_transfer_uuid", columnDefinition = "uuid")
    private UUID taxTransferUuid;

    @Column(name = "tax_uuid", nullable = false, columnDefinition = "uuid")
    private UUID taxUuid;

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
    @Column(name = "factor_type", length = 10, nullable = false)
    private String factorType;

    /**
     * Tax rate or quota (e.g., 0.160000 for IVA 16%)
     */
    @Column(name = "rate_or_quota", precision = 6, scale = 6)
    private BigDecimal rateOrQuota;

    /**
     * Tax amount transferred
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
