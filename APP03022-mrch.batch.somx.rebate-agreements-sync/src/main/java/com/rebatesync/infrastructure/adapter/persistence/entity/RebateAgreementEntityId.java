package com.rebatesync.infrastructure.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta para RebateAgreementEntity
 * La tabla no tiene columna ID, por lo que usamos NumeroProveedor + NumeroAcuerdo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RebateAgreementEntityId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String supplierNumber;
    private String agreementNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RebateAgreementEntityId that = (RebateAgreementEntityId) o;
        return Objects.equals(supplierNumber, that.supplierNumber) &&
               Objects.equals(agreementNumber, that.agreementNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierNumber, agreementNumber);
    }
}
