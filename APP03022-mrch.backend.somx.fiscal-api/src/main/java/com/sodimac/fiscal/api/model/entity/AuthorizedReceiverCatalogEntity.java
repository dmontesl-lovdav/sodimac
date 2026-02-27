package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "authorized_receiver_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthorizedReceiverCatalogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authorized_receiver_id")
    private Long authorizedReceiverId;

    @Column(name = "receiver_uuid", nullable = false, columnDefinition = "uuid")
    private UUID receiverUuid;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "rfc", length = 13, nullable = false)
    private String rfc;

    @Column(name = "tax_regime", length = 3)
    private String taxRegime;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "status")
    private Integer status = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uuid", insertable = false, updatable = false)
    private ReceiverEntity receiver;
}