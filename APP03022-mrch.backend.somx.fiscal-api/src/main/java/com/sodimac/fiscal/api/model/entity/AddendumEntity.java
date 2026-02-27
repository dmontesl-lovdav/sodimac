package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addendum")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddendumEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "addendum_uuid", columnDefinition = "uuid")
    private UUID addendumUuid;

    @Column(name = "invoice_uuid", columnDefinition = "uuid")
    private UUID invoiceUuid;

    @Column(name = "payments_uuid", columnDefinition = "uuid")
    private UUID paymentsUuid;

    @Column(name = "supplier_number", precision = 10)
    private BigDecimal supplierNumber;

    @Column(name = "reception_number", length = 20)
    private String receptionNumber;

    @Column(name = "purchase_order_number", length = 50)
    private String purchaseOrderNumber;

    @Column(name = "shipping_guide_number", length = 30)
    private String shippingGuideNumber;

    @Column(name = "addendum_content", columnDefinition = "TEXT")
    private String addendumContent;

    @Column(name = "supplier_type", length = 10)
    private String supplierType;

    @Column(name = "addenda_type")
    private Integer addendaType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_uuid", insertable = false, updatable = false)
    private InvoiceEntity invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payments_uuid", insertable = false, updatable = false)
    private PaymentsEntity payments;
}