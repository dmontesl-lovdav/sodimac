package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "reception", schema = "tenant_finance")
public class ReceptionEntity {

    @Id
    @Column(name = "reception_id")
    private UUID receptionId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "reception_number")
    private String receptionNumber;

    /**
     * Estatus de la recepción (catálogo CatEstatusRecepcion): 0=Disponible, 1=Consumida,
     * 2=Consumida manual, 7=Cancelada... fiscal lo marca a Consumida (1) al publicar una
     * factura que cuadra con la recepción (dentro de tolerancia o mayor). QA filas 54-57.
     */
    @Setter
    @Column(name = "status")
    private BigDecimal status;
}
