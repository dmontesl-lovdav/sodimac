package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing withheld taxes (ISR, IVA) from CFDI.
 *
 * Stores information about taxes that are withheld by the buyer
 * from payments to the seller, such as income tax (ISR) and VAT (IVA).
 *
 * @author Sodimac Tech Team
 * @since 2025-11-11
 */
@Entity
@Table(name = "tax_withholding", schema = "tenant_fiscal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxWithholdingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tax_withholding_uuid", columnDefinition = "uuid")
    private UUID taxWithholdingUuid;

    @Column(name = "tax_uuid", nullable = false, columnDefinition = "uuid")
    private UUID taxUuid;

    /**
     * SAT tax code.
     * 001 = ISR (Income Tax)
     * 002 = IVA (VAT)
     */
    @Column(name = "tax_code", length = 3, nullable = false)
    private String taxCode;

    /**
     * Withheld tax amount
     */
    @Column(name = "amount", precision = 16, scale = 2, nullable = false)
    private BigDecimal amount;

    // Relationship with Tax
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_uuid", insertable = false, updatable = false)
    private TaxEntity tax;
}
