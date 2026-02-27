package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para crear un proveedor.
 */
@Schema(description = "Datos para crear un proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Numero de proveedor", example = "PROV001", required = true)
    @NotBlank(message = "El numero de proveedor es requerido")
    @Size(max = 20, message = "El numero de proveedor no puede exceder 20 caracteres")
    private String supplierNumber;

    @Schema(description = "RFC del proveedor", example = "ABC123456789", required = true)
    @NotBlank(message = "El RFC es requerido")
    @Size(min = 12, max = 13, message = "El RFC debe tener entre 12 y 13 caracteres")
    private String rfc;

    @Schema(description = "Razon social", example = "Proveedor SA de CV", required = true)
    @NotBlank(message = "La razon social es requerida")
    @Size(max = 255, message = "La razon social no puede exceder 255 caracteres")
    private String businessName;

    @Schema(description = "ID del tipo de proveedor", example = "1")
    private Integer supplierTypeId;

    @Schema(description = "URL del logo", example = "https://example.com/logo.png")
    @Size(max = 500, message = "La URL del logo no puede exceder 500 caracteres")
    private String logo;

    @Schema(description = "ID de la condicion de pago", example = "1")
    private Integer paymentConditionId;
}
