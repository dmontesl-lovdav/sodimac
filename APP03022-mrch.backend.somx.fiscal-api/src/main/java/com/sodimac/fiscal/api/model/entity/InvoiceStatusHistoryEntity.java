package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad para registrar el historial de cambios de estatus de facturas/NC.
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
@Entity
@Table(name = "invoice_status_history", schema = "tenant_fiscal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "history_id", columnDefinition = "uuid")
    private UUID historyId;

    @Column(name = "invoice_uuid", nullable = false, columnDefinition = "uuid")
    private UUID invoiceUuid;

    @Column(name = "fiscal_uuid", nullable = false, columnDefinition = "uuid")
    private UUID fiscalUuid;

    @Column(name = "status_from")
    private Integer statusFrom;

    @Column(name = "status_to", nullable = false)
    private Integer statusTo;

    @Column(name = "changed_by")
    private Integer changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
