package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvoiceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "invoice_uuid", columnDefinition = "uuid")
    private UUID invoiceUuid;  // PK autogenerado

    @Column(name = "fiscal_uuid", columnDefinition = "uuid", unique = true)
    private UUID fiscalUuid;  // UUID del TimbreFiscalDigital del SAT (Folio Fiscal)

    @Column(name = "place_of_issue", length = 5)
    private String placeOfIssue;

    @Column(name = "payment_method", length = 3)
    private String paymentMethod;

    @Column(name = "document_type", length = 1, nullable = false)
    private String documentType;

    @Column(name = "total", precision = 16, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "exchange_rate", precision = 18, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "discount", precision = 16, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 16, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "payment_conditions", length = 255)
    private String paymentConditions;

    @Column(name = "payment_form", length = 10)
    private String paymentForm;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "certification_date")
    private LocalDateTime certificationDate;

    @Column(name = "folio", length = 49)
    private String folio;

    @Column(name = "series", length = 25)
    private String series;

    @Column(name = "version", precision = 6, scale = 3, nullable = false)
    private BigDecimal version;

    @Column(name = "xml_content", columnDefinition = "TEXT")
    private String xmlContent;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "accounting_sent_date")
    private LocalDateTime accountingSentDate;

    /**
     * Fecha de contabilización (STM-410).
     * Se guarda cuando el estatus cambia a 7 (Pendiente de Pago).
     */
    @Column(name = "accounting_date")
    private LocalDate accountingDate;

    @Column(name = "issuer_uuid", nullable = false, columnDefinition = "uuid")
    private UUID issuerUuid;

    @Column(name = "receiver_uuid", nullable = false, columnDefinition = "uuid")
    private UUID receiverUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_uuid", insertable = false, updatable = false)
    private IssuerEntity issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uuid", insertable = false, updatable = false)
    private ReceiverEntity receiver;
}