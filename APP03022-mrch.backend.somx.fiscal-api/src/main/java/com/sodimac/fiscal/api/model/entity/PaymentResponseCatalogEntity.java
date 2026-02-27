package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad para el catálogo de respuestas del servicio multipac.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "payment_response_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentResponseCatalogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_catalog_id")
    private Long responseCatalogId;

    @Column(name = "response_code", length = 10, nullable = false)
    private String responseCode;

    @Column(name = "response_type", length = 20, nullable = false)
    private String responseType;

    @Column(name = "description", length = 254, nullable = false)
    private String description;

    @Column(name = "message_template", columnDefinition = "TEXT")
    private String messageTemplate;

    @Column(name = "status")
    private Integer status;
}
