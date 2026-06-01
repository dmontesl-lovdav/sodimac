package com.rebatesync.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio para RebateAgreement
 *
 * Nota: Se adaptó a la estructura real de la tabla en SODIMAC_REBATES_DEV
 * - No tiene ID (usa clave compuesta: supplierNumber + agreementNumber)
 * - No tiene timestamps (createdAt, updatedAt)
 * - value y fillRate son String (VARCHAR en la tabla)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebateAgreement {
    private String supplierNumber;
    private String agreementNumber;
    private String rfc;
    private String businessName;
    private String status;
    private String family;
    private String commercialClassification;
    private String agreementType;
    private String currency;
    private String value;
    private String valueType;
    private String fillRate;
    private String paymentProgram;
    private String brand;

    /**
     * Obtiene un identificador único combinando supplierNumber y agreementNumber
     */
    public String getUniqueId() {
        return supplierNumber + "-" + agreementNumber;
    }
}
