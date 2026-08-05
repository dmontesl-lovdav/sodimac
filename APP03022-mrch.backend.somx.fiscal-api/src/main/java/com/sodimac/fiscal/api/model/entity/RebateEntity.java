package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapea tenant_finance.rebate (descuento comercial / rebate). Solo lectura desde fiscal-api,
 * igual que ReceptionEntity: fiscal la consulta por su PK para validar la tolerancia del
 * descuento comercial al registrar una NC de tipo 2. No se persiste desde aquí.
 */
@Getter
@Entity
@Table(name = "rebate", schema = "tenant_finance")
public class RebateEntity {

    @Id
    @Column(name = "rebate_uuid")
    private UUID rebateUuid;

    @Column(name = "document_number")
    private String documentNumber;

    /** Valor del descuento comercial. Se compara contra el subtotal de la NC. */
    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "status")
    private BigDecimal status;
}
