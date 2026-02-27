package com.sodimac.fiscal.api.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de entrada para búsqueda de complementos de pago.
 *
 * Permite filtrar complementos de pago por múltiples criterios
 * para el portal de proveedores.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSearchRequest {

    /**
     * UUID del complemento de pago (folio fiscal).
     */
    private UUID paymentsUuid;

    /**
     * RFC del emisor (proveedor).
     */
    private String rfcEmisor;

    /**
     * RFC del receptor (Sodimac/Falabella).
     */
    private String rfcReceptor;

    /**
     * Número de folio del complemento de pago.
     */
    private String folio;

    /**
     * Serie del complemento de pago.
     */
    private String serie;

    /**
     * Número de proveedor.
     */
    private Long numeroProveedor;

    /**
     * Fecha de pago inicial (rango de búsqueda).
     */
    private LocalDate fechaPagoInicio;

    /**
     * Fecha de pago final (rango de búsqueda).
     */
    private LocalDate fechaPagoFin;

    /**
     * Estado del complemento de pago.
     * 1=Vigente, 0=Cancelado, 2=Pendiente, 3=Rechazado
     */
    private Integer status;

    /**
     * Monto mínimo del pago.
     */
    private Double montoMinimo;

    /**
     * Monto máximo del pago.
     */
    private Double montoMaximo;

    /**
     * Número de página para paginación (base 0).
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * Tamaño de página para paginación.
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * Campo para ordenamiento.
     * Valores: paymentDate, folio, serie, createdAt
     */
    @Builder.Default
    private String sortBy = "paymentDate";

    /**
     * Dirección del ordenamiento.
     * Valores: ASC, DESC
     */
    @Builder.Default
    private String sortDirection = "DESC";
}
