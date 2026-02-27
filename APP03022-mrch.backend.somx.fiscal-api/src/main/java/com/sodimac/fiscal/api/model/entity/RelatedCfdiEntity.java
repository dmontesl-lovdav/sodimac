package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "related_cfdi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RelatedCfdiEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "related_cfdi_uuid", columnDefinition = "uuid")
    private UUID relatedCfdiUuid;

    @Column(name = "invoice_uuid", nullable = false, columnDefinition = "uuid")
    private UUID invoiceUuid;

    @Column(name = "related_invoice_uuid", nullable = false, columnDefinition = "uuid")
    private UUID relatedInvoiceUuid;

    @Column(name = "relation_type", length = 3, nullable = false)
    private String relationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_uuid", insertable = false, updatable = false)
    private InvoiceEntity invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_invoice_uuid", insertable = false, updatable = false)
    private InvoiceEntity relatedInvoice;
}