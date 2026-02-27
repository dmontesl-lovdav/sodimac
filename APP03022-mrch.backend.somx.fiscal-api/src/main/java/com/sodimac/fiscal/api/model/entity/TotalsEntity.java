package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "totals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TotalsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "totals_uuid", columnDefinition = "uuid")
    private UUID totalsUuid;

    @Column(name = "payments_uuid", nullable = false, columnDefinition = "uuid")
    private UUID paymentsUuid;

    @Column(name = "total_payments_amount", precision = 16, scale = 2, nullable = false)
    private BigDecimal totalPaymentsAmount;

    @Column(name = "total_base_iva_16", precision = 16, scale = 2)
    private BigDecimal totalBaseIva16 = BigDecimal.ZERO;

    @Column(name = "total_tax_iva_16", precision = 16, scale = 2)
    private BigDecimal totalTaxIva16 = BigDecimal.ZERO;

    @Column(name = "total_base_iva_8", precision = 16, scale = 2)
    private BigDecimal totalBaseIva8 = BigDecimal.ZERO;

    @Column(name = "total_tax_iva_8", precision = 16, scale = 2)
    private BigDecimal totalTaxIva8 = BigDecimal.ZERO;

    @Column(name = "total_base_iva_0", precision = 16, scale = 2)
    private BigDecimal totalBaseIva0 = BigDecimal.ZERO;

    @Column(name = "total_withholding_iva", precision = 16, scale = 2)
    private BigDecimal totalWithholdingIva = BigDecimal.ZERO;

    @Column(name = "total_withholding_isr", precision = 16, scale = 2)
    private BigDecimal totalWithholdingIsr = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payments_uuid", insertable = false, updatable = false)
    private PaymentsEntity payments;
}