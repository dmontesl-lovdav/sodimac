package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para proveedor con informacion de bloqueo.
 * STM-831: Ajustar servicio del catalogo de proveedores.
 */
@Schema(description = "Proveedor con informacion de bloqueo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierFilterDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID del proveedor", example = "1")
    private Integer id;

    @Schema(description = "Numero de proveedor", example = "PROV001")
    private String supplierNumber;

    @Schema(description = "RFC del proveedor", example = "ABC123456789")
    private String rfc;

    @Schema(description = "Razon social", example = "Proveedor ABC S.A. de C.V.")
    private String businessName;

    @Schema(description = "Tipo de proveedor")
    private SupplierTypeDto supplierType;

    @Schema(description = "Condicion de pago")
    private PaymentConditionDto paymentCondition;

    @Schema(description = "Estado: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;

    @Schema(description = "Indica si el proveedor esta bloqueado actualmente", example = "false")
    private Boolean blocked;

    @Schema(description = "Informacion del bloqueo (solo si blocked=true)")
    private SupplierBlockInfoDto blockInfo;
}
