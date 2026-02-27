package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_uuid", columnDefinition = "uuid")
    private UUID paymentUuid;

    @Column(name = "payments_uuid", nullable = false, columnDefinition = "uuid")
    private UUID paymentsUuid;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_method", length = 10, nullable = false)
    private String paymentMethod;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "amount", precision = 16, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "operation_number", length = 100)
    private String operationNumber;

    @Column(name = "exchange_rate", precision = 18, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "payer_bank_rfc", length = 13)
    private String payerBankRfc;

    @Column(name = "payer_account", length = 50)
    private String payerAccount;

    @Column(name = "beneficiary_bank_rfc", length = 13)
    private String beneficiaryBankRfc;

    @Column(name = "beneficiary_account", length = 50)
    private String beneficiaryAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payments_uuid", insertable = false, updatable = false)
    private PaymentsEntity payments;
}