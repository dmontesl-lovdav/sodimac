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
     * UUID del complemento (PK interno o folio fiscal SAT). Se compara contra
     * {@code payments_uuid} y {@code fiscal_uuid}.
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
     * Tipo de proveedor (id numérico 1-4 de CatTipoProveedor) para filtrar. Issue Fer #5.
     */
    private String tipoProveedor;

    /**
     * Fecha de pago inicial (rango de búsqueda).
     */
    private LocalDate fechaPagoInicio;

    /**
     * Fecha de pago final (rango de búsqueda).
     */
    private LocalDate fechaPagoFin;

    /**
     * Fecha de registro inicial ({@code created_at}).
     */
    private LocalDate fechaRegistroInicio;

    /**
     * Fecha de registro final ({@code created_at}).
     */
    private LocalDate fechaRegistroFin;

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
