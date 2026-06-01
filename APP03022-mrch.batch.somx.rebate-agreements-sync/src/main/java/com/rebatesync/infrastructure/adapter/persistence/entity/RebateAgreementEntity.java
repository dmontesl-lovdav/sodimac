package com.rebatesync.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad JPA para la tabla RebateAcuerdosTemp en SODIMAC_REBATES_DEV
 *
 * Nota: La tabla no tiene columna ID ni timestamps.
 * Se usa una clave compuesta: NumeroProveedor + NumeroAcuerdo
 */
@Entity
@Table(name = "RebateAcuerdosTemp", catalog = "SODIMAC_REBATES_DEV", schema = "dbo")
@IdClass(RebateAgreementEntityId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebateAgreementEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "NumeroProveedor")
    private String supplierNumber;

    @Id
    @Column(name = "NumeroAcuerdo")
    private String agreementNumber;

    @Column(name = "RFC")
    private String rfc;

    @Column(name = "RazonSocial")
    private String businessName;

    @Column(name = "Estado")
    private String status;

    @Column(name = "Familia")
    private String family;

    @Column(name = "ClasificacionComercial")
    private String commercialClassification;

    @Column(name = "TipoAcuerdo")
    private String agreementType;

    @Column(name = "Moneda")
    private String currency;

    @Column(name = "Valor")
    private String value;  // VARCHAR en la tabla real

    @Column(name = "TipoValor")
    private String valueType;

    @Column(name = "FillRate")
    private String fillRate;  // VARCHAR en la tabla real

    @Column(name = "ProgramaPago")
    private String paymentProgram;

    @Column(name = "Marca")
    private String brand;
}
