package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entidad para el registro de archivos de complementos de pago procesados.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "payment_file_registry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentFileRegistryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_registry_id")
    private Long fileRegistryId;

    @Column(name = "file_name", length = 254, nullable = false)
    private String fileName;

    @Column(name = "payments_uuid")
    private UUID paymentsUuid;

    @Column(name = "processing_status", length = 20, nullable = false)
    private String processingStatus;

    @Column(name = "error_code", length = 10)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payments_uuid", referencedColumnName = "payments_uuid", insertable = false, updatable = false)
    private PaymentsEntity payments;
}
