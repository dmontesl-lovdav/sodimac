package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "equivalence_dr")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EquivalenceDrEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "equivalence_uuid", columnDefinition = "uuid")
    private UUID equivalenceUuid;

    @Column(name = "related_document_uuid", nullable = false, columnDefinition = "uuid")
    private UUID relatedDocumentUuid;

    @Column(name = "folio", length = 49)
    private String folio;

    @Column(name = "amount_paid", precision = 16, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "previous_balance", precision = 16, scale = 2, nullable = false)
    private BigDecimal previousBalance;

    @Column(name = "remaining_balance", precision = 16, scale = 2, nullable = false)
    private BigDecimal remainingBalance;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "installment_number", precision = 3)
    private BigDecimal installmentNumber;

    @Column(name = "tax_object", length = 10)
    private String taxObject;

    @Column(name = "series", length = 25)
    private String series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_document_uuid", insertable = false, updatable = false)
    private RelatedDocumentsEntity relatedDocument;
}