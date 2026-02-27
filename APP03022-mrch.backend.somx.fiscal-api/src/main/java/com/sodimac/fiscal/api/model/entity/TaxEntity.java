package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing consolidated taxes for CFDI invoices and credit notes.
 *
 * This entity stores the total transferred and withheld taxes for each invoice,
 * along with detailed breakdowns through related transfer, withholding, and
 * detail entities.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-11
 */
@Entity
@Table(name = "tax", schema = "tenant_fiscal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tax_uuid", columnDefinition = "uuid")
    private UUID taxUuid;

    @Column(name = "invoice_uuid", nullable = false, columnDefinition = "uuid")
    private UUID invoiceUuid;

    /**
     * Total amount of transferred taxes (IVA, IEPS).
     */
    @Column(name = "total_transferred_taxes", precision = 16, scale = 2)
    private BigDecimal totalTransferredTaxes;

    /**
     * Total amount of withheld taxes (ISR, IVA).
     */
    @Column(name = "total_withheld_taxes", precision = 16, scale = 2)
    private BigDecimal totalWithheldTaxes;

    // Relationships
    @OneToMany(mappedBy = "tax", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaxTransferEntity> transfers = new ArrayList<>();

    @OneToMany(mappedBy = "tax", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaxWithholdingEntity> withholdings = new ArrayList<>();

    @OneToMany(mappedBy = "tax", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaxDetailEntity> details = new ArrayList<>();

    // Relationship with Invoice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_uuid", insertable = false, updatable = false)
    private InvoiceEntity invoice;

    // Helper methods for bidirectional relationships
    public void addTransfer(TaxTransferEntity transfer) {
        transfers.add(transfer);
        transfer.setTax(this);
    }

    public void addWithholding(TaxWithholdingEntity withholding) {
        withholdings.add(withholding);
        withholding.setTax(this);
    }

    public void addDetail(TaxDetailEntity detail) {
        details.add(detail);
        detail.setTax(this);
    }
}
