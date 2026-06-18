package com.sodimac.fiscal.api.model.dto;

import com.sodimac.fiscal.api.model.enums.CreditNoteStatus;
import com.sodimac.fiscal.api.model.enums.InvoiceStatus;
import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para response de búsqueda de facturas y notas de crédito (STM-338).
 *
 * Contiene información de:
 * - Comprobante (datos del CFDI)
 * - Emisor (proveedor)
 * - Receptor (Sodimac)
 * - Addenda (si existe)
 * - Datos de negocio de addenda: OC, Recepción, Proveedor (STM-1169)
 * - Notas de Crédito relacionadas (STM-1168) - solo para Facturas tipo I
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de búsqueda de factura o nota de crédito")
public class InvoiceSearchResponse {

    // ========== COMPROBANTE ==========

    @Schema(description = "UUID interno del invoice")
    private UUID invoiceUuid;

    @Schema(description = "UUID fiscal del TimbreFiscalDigital (Folio Fiscal)")
    private UUID fiscalUuid;

    @Schema(description = "Tipo de documento: I (Factura) o E (Nota de Crédito)")
    private String documentType;

    @Schema(description = "Serie del comprobante")
    private String series;

    @Schema(description = "Folio del comprobante")
    private String folio;

    @Schema(description = "Versión del CFDI", example = "4.0")
    private BigDecimal version;

    @Schema(description = "Fecha de emisión del comprobante")
    private LocalDate issueDate;

    @Schema(description = "Fecha de certificación del timbre fiscal")
    private LocalDateTime certificationDate;

    @Schema(description = "Total del comprobante")
    private BigDecimal total;

    @Schema(description = "Subtotal del comprobante")
    private BigDecimal subtotal;

    @Schema(description = "Descuento aplicado")
    private BigDecimal discount;

    @Schema(description = "Moneda del comprobante", example = "MXN")
    private String currency;

    @Schema(description = "Tipo de cambio")
    private BigDecimal exchangeRate;

    @Schema(description = "Método de pago", example = "PUE")
    private String paymentMethod;

    @Schema(description = "Forma de pago", example = "01")
    private String paymentForm;

    @Schema(description = "Condiciones de pago")
    private String paymentConditions;

    @Schema(description = "Lugar de expedición (código postal)")
    private String placeOfIssue;

    @Schema(description = "Estatus actual del documento")
    private Integer status;

    @Schema(description = "Nombre del estatus actual")
    private String statusName;

    @Schema(description = "Fecha de recepción en el sistema")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;

    // ========== EMISOR ==========

    @Schema(description = "RFC del emisor (proveedor)")
    private String emisorRfc;

    @Schema(description = "Nombre o razón social del emisor")
    private String emisorName;

    @Schema(description = "Régimen fiscal del emisor")
    private String emisorTaxRegime;

    // ========== RECEPTOR ==========

    @Schema(description = "RFC del receptor (Sodimac)")
    private String receptorRfc;

    @Schema(description = "Nombre o razón social del receptor")
    private String receptorName;

    @Schema(description = "Régimen fiscal del receptor")
    private String receptorTaxRegime;

    // ========== ADDENDA ==========

    @Schema(description = "Indica si el comprobante tiene addenda")
    private Boolean hasAddenda;

    @Schema(description = "UUID de la addenda")
    private UUID addendaUuid;

    @Schema(description = "Tipo de addenda")
    private Integer addendaType;

    @Schema(description = "Nombre del tipo de addenda")
    private String addendaTypeName;

    // ========== DATOS DE NEGOCIO ADDENDA (STM-1169) ==========

    @Schema(description = "Número de Orden de Compra asociada a la factura")
    private String noOrdenCompra;

    @Schema(description = "Número de Recepción asociada a la factura")
    private String noRecepcion;

    @Schema(description = "Fecha de recepción real (tenant_finance.reception.reception_date)")
    private LocalDate fechaRecepcion;

    @Schema(description = "Número de proveedor")
    private BigDecimal numeroProveedor;

    @Schema(description = "Tipo de proveedor")
    private String tipoProveedor;

    @Schema(description = "Número de guía de entrega")
    private String guiaEntrega;

    // ========== XML CONTENT (STM-771) ==========

    @Schema(description = "Contenido XML completo del documento fiscal (CFDI)")
    private String xmlContent;

    // ========== NOTAS DE CRÉDITO RELACIONADAS (STM-1168) ==========

    @Schema(description = "Lista de Notas de Crédito relacionadas a esta Factura (solo aplica para documentType='I')")
    private List<NotaCreditoRelacionadaDto> notasCreditoRelacionadas;

    @Schema(description = "Cantidad de Notas de Crédito relacionadas a esta Factura (solo aplica para documentType='I')")
    private Integer creditNotesCount;

    // ========== DATOS DE FACTURA RELACIONADA (STM-396) - Solo para NC (documentType='E') ==========

    @Schema(description = "Tipo de relación de la NC con la factura (01=NC bienes, 02=NC servicios, etc.) - Solo para NC")
    private String relationType;

    @Schema(description = "Nombre del tipo de relación - Solo para NC")
    private String relationTypeName;

    @Schema(description = "Serie de la factura relacionada - Solo para NC")
    private String relatedInvoiceSeries;

    @Schema(description = "Folio de la factura relacionada - Solo para NC")
    private String relatedInvoiceFolio;

    @Schema(description = "UUID fiscal de la factura relacionada - Solo para NC")
    private UUID relatedInvoiceUuid;

    @Schema(description = "Subtotal de la factura relacionada - Solo para NC")
    private BigDecimal relatedInvoiceSubtotal;

    @Schema(description = "Total de la factura relacionada - Solo para NC")
    private BigDecimal relatedInvoiceTotal;

    // ========== DATOS ADICIONALES PROVEEDOR (STM-396) ==========

    @Schema(description = "Nombre o razón social del proveedor (emisor)")
    private String supplierName;

    @Schema(description = "Fecha de envío del documento a contabilizar")
    private LocalDateTime accountingSentDate;

    /**
     * Método helper para determinar el nombre del estatus según el tipo de documento.
     * Utiliza los enums InvoiceStatus y CreditNoteStatus para obtener los nombres.
     */
    public static String getStatusName(String documentType, Integer statusCode) {
        if (statusCode == null) {
            return null;
        }

        try {
            if (TipoDocumentoFiscal.FACTURA.getCodigo().equals(documentType)) {
                return InvoiceStatus.fromCodigo(statusCode).getNombre();
            } else if (TipoDocumentoFiscal.NOTA_CREDITO.getCodigo().equals(documentType)) {
                return CreditNoteStatus.fromCodigo(statusCode).getNombre();
            }
        } catch (IllegalArgumentException e) {
            // Codigo de estatus no encontrado en el enum
            return null;
        }
        return null;
    }
}
