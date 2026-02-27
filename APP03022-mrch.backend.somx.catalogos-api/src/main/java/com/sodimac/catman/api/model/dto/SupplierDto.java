package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para proveedor.
 */
@Schema(description = "Proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID del proveedor", example = "1")
    private Integer id;

    @Schema(description = "Numero de proveedor", example = "PROV001")
    private String supplierNumber;

    @Schema(description = "RFC del proveedor", example = "ABC123456789")
    private String rfc;

    @Schema(description = "Razon social", example = "Proveedor SA de CV")
    private String businessName;

    @Schema(description = "Tipo de proveedor")
    private SupplierTypeDto supplierType;

    @Schema(description = "URL del logo", example = "https://example.com/logo.png")
    private String logo;

    @Schema(description = "Condicion de pago")
    private PaymentConditionDto paymentCondition;

    @Schema(description = "Estado: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;
}
