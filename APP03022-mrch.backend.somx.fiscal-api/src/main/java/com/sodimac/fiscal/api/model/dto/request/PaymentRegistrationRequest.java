package com.sodimac.fiscal.api.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para el registro de complementos de pago.
 *
 * Contiene los parámetros requeridos para registrar un complemento de pago
 * según las reglas de negocio establecidas.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRegistrationRequest {

    /**
     * ID del proveedor (número de proveedor).
     * Se guarda en la tabla addendum.
     */
    @NotNull(message = "El ID de proveedor es requerido")
    @Positive(message = "El ID de proveedor debe ser un número positivo")
    private Long idProveedor;

    /**
     * Tipo de addenda.
     * Para complementos de pago siempre debe ser 5.
     */
    @NotNull(message = "El tipo de addenda es requerido")
    private Integer tipoAddenda;

    /**
     * Tipo de proveedor.
     * Se guarda en la tabla addendum.
     */
    @NotNull(message = "El tipo de proveedor es requerido")
    private String tipoProveedor;

    /**
     * ID del usuario que registra el complemento de pago.
     */
    @NotNull(message = "El ID de usuario es requerido")
    @Positive(message = "El ID de usuario debe ser un número positivo")
    private Long idUsuario;

    /**
     * Archivo XML del complemento de pago.
     * Debe ser un XML válido según el XSD Pagos 2.0 del SAT.
     */
    @NotNull(message = "El archivo XML es requerido")
    private MultipartFile xmlFile;

    public MultipartFile getXmlFile() {
        return xmlFile;
    }
}
