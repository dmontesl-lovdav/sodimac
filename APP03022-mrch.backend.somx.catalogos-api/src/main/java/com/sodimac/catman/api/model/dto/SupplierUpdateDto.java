package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para actualizar un proveedor.
 */
@Schema(description = "Datos para actualizar un proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "RFC del proveedor", example = "ABC123456789")
    @Size(min = 12, max = 13, message = "El RFC debe tener entre 12 y 13 caracteres")
    private String rfc;

    @Schema(description = "Razon social", example = "Proveedor SA de CV")
    @Size(max = 255, message = "La razon social no puede exceder 255 caracteres")
    private String businessName;

    @Schema(description = "ID del tipo de proveedor", example = "1")
    private Integer supplierTypeId;

    @Schema(description = "URL del logo", example = "https://example.com/logo.png")
    @Size(max = 500, message = "La URL del logo no puede exceder 500 caracteres")
    private String logo;

    @Schema(description = "ID de la condicion de pago", example = "1")
    private Integer paymentConditionId;

    @Schema(description = "Estado: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;
}
