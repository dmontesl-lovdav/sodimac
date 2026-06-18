package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(name = "reception_date")
    private LocalDate receptionDate;
}
