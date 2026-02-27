package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "related_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RelatedDocumentsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "related_document_uuid", columnDefinition = "uuid")
    private UUID relatedDocumentUuid;

    @Column(name = "payment_uuid", nullable = false, columnDefinition = "uuid")
    private UUID paymentUuid;

    @Column(name = "document_uuid", nullable = false, columnDefinition = "uuid")
    private UUID documentUuid;

    @Column(name = "amount_paid", precision = 16, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "previous_balance", precision = 16, scale = 2, nullable = false)
    private BigDecimal previousBalance;

    @Column(name = "remaining_balance", precision = 16, scale = 2, nullable = false)
    private BigDecimal remainingBalance;

    @Column(name = "installment_number", precision = 3)
    private BigDecimal installmentNumber;

    @Column(name = "series", length = 25)
    private String series;

    @Column(name = "folio", length = 49)
    private String folio;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "exchange_rate", precision = 18, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_uuid", insertable = false, updatable = false)
    private PaymentEntity payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_uuid", insertable = false, updatable = false)
    private InvoiceEntity document;
}