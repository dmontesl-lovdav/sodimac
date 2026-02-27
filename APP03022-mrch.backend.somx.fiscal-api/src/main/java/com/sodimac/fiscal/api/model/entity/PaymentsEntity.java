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
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payments_uuid", columnDefinition = "uuid")
    private UUID paymentsUuid;  // PK autogenerado

    @Column(name = "fiscal_uuid", columnDefinition = "uuid", unique = true)
    private UUID fiscalUuid;  // UUID del TimbreFiscalDigital del SAT (Folio Fiscal)

    @Column(name = "version", precision = 6, scale = 3, nullable = false)
    private BigDecimal version;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "certification_date")
    private LocalDateTime certificationDate;

    @Column(name = "issuer_uuid", nullable = false, columnDefinition = "uuid")
    private UUID issuerUuid;

    @Column(name = "receiver_uuid", nullable = false, columnDefinition = "uuid")
    private UUID receiverUuid;

    @Column(name = "folio", length = 49)
    private String folio;

    @Column(name = "series", length = 25)
    private String series;

    @Column(name = "xml_content", columnDefinition = "TEXT")
    private String xmlContent;

    @Column(name = "status")
    private Integer status = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_uuid", insertable = false, updatable = false)
    private IssuerEntity issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uuid", insertable = false, updatable = false)
    private ReceiverEntity receiver;
}