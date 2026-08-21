package com.sodimac.fiscal.api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para búsqueda de complementos de pago.
 *
 * Contiene información resumida del complemento de pago
 * más datos del emisor y receptor.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSearchResponse {

    /**
     * UUID interno del complemento de pago.
     */
    private UUID paymentsUuid;

    /**
     * UUID del TimbreFiscalDigital del SAT (Folio Fiscal).
     * Utilizado para descargar el XML del complemento via /invoices/{fiscalUuid}/xml
     */
    private UUID fiscalUuid;

    /**
     * Versión del complemento de pagos (2.0).
     */
    private BigDecimal version;

    /**
     * Fecha del pago.
     */
    private LocalDate paymentDate;

    /**
     * Fecha de certificación del SAT.
     */
    private LocalDateTime certificationDate;

    /**
     * Folio del complemento de pago.
     */
    private String folio;

    /**
     * Serie del complemento de pago.
     */
    private String series;

    /**
     * Estado del complemento de pago.
     * 0=Cancelado, 1=Vigente, 2=Pendiente, 3=Rechazado
     */
    private Integer status;

    /**
     * Descripción del estado.
     */
    private String statusDescription;

    // Datos del emisor (proveedor)
    private UUID issuerUuid;
    private String issuerRfc;
    private String issuerName;

    // Datos del receptor (Sodimac/Falabella)
    private UUID receiverUuid;
    private String receiverRfc;
    private String receiverName;

    /**
     * Subtotal de los pagos contenidos (suma de montos en {@code payment}).
     */
    private BigDecimal subtotalAmount;

    /**
     * Monto total de los pagos contenidos.
     */
    private BigDecimal totalAmount;

    /**
     * Cantidad de documentos relacionados pagados.
     */
    private Integer relatedDocumentsCount;

    /**
     * Tipo de proveedor: id (1-4 de CatTipoProveedor) y descripción en campos separados;
     * el front decide cuál mostrar. Issue Fer #5.
     */
    private String tipoProveedor;
    private String tipoProveedorDescripcion;

    /**
     * Fecha de creación del registro.
     */
    private LocalDateTime createdAt;

    /**
     * Usuario que creó el registro (UUID del sub del token).
     */
    private UUID createdBy;

    /**
     * XML del complemento (mismo patrón que InvoiceSearchResponse / STM-771).
     * El grid de consulta descarga el XML desde este campo, sin un GET extra.
     */
    private String xmlContent;
}
