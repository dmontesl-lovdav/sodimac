package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "issuer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IssuerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "issuer_uuid", columnDefinition = "uuid")
    private UUID issuerUuid;

    @Column(name = "name", length = 254, nullable = false)
    private String name;

    @Column(name = "rfc", length = 13, nullable = false)
    private String rfc;

    @Column(name = "tax_regime", length = 3, nullable = false)
    private String taxRegime;
}