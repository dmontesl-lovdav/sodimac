package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "pac_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PacCatalogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pac_id")
    private Long pacId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", length = 254)
    private String description;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "password", length = 254)
    private String password;

    @Column(name = "url", length = 254)
    private String url;

    @Column(name = "license", length = 254)
    private String license;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "catalog_msg_id", precision = 4)
    private BigDecimal catalogMsgId;

    @Column(name = "status")
    private Integer status = 1;
}