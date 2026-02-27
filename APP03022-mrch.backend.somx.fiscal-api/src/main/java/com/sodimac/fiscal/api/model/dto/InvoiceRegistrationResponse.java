package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para el registro de facturas y notas de crédito (STM-337).
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRegistrationResponse {

    /**
     * Código de respuesta de negocio (BUS1xxx, BUS2xxx, BUS3xxx)
     */
    private String code;

    /**
     * Mensaje descriptivo de la respuesta
     */
    private String message;

    /**
     * Indica si el registro fue exitoso
     */
    private boolean success;

    /**
     * UUID del invoice registrado (solo si success=true)
     */
    private UUID invoiceUuid;

    /**
     * UUID fiscal del TimbreFiscalDigital (solo si success=true)
     */
    private UUID fiscalUuid;

    /**
     * Serie del comprobante
     */
    private String series;

    /**
     * Folio del comprobante
     */
    private String folio;

    /**
     * Tipo de documento: 'I' (Factura) o 'E' (Nota de Crédito)
     */
    private String documentType;

    /**
     * RFC del emisor
     */
    private String issuerRfc;

    /**
     * RFC del receptor
     */
    private String receiverRfc;

    /**
     * Total del documento
     */
    private String total;

    /**
     * Fecha de emisión
     */
    private LocalDateTime issueDate;

    /**
     * Indica si el documento tiene addenda
     */
    private boolean hasAddenda;

    /**
     * Indica si el documento está pendiente de addenda
     */
    private boolean pendingAddenda;

    /**
     * Advertencias (BUS3xxx) o información adicional
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * Timestamp de procesamiento
     */
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();

    /**
     * Crea una respuesta exitosa con addenda
     */
    public static InvoiceRegistrationResponse success(UUID invoiceUuid, UUID fiscalUuid,
                                                       String series, String folio,
                                                       String documentType,
                                                       String issuerRfc, String receiverRfc,
                                                       String total, LocalDateTime issueDate,
                                                       String code, String message) {
        return InvoiceRegistrationResponse.builder()
                .success(true)
                .code(code)
                .message(message)
                .invoiceUuid(invoiceUuid)
                .fiscalUuid(fiscalUuid)
                .series(series)
                .folio(folio)
                .documentType(documentType)
                .issuerRfc(issuerRfc)
                .receiverRfc(receiverRfc)
                .total(total)
                .issueDate(issueDate)
                .hasAddenda(true)
                .pendingAddenda(false)
                .build();
    }

    /**
     * Crea una respuesta exitosa pendiente de addenda
     */
    public static InvoiceRegistrationResponse successPendingAddenda(UUID invoiceUuid, UUID fiscalUuid,
                                                                     String series, String folio,
                                                                     String documentType,
                                                                     String issuerRfc, String receiverRfc,
                                                                     String total, LocalDateTime issueDate,
                                                                     String code, String message) {
        return InvoiceRegistrationResponse.builder()
                .success(true)
                .code(code)
                .message(message)
                .invoiceUuid(invoiceUuid)
                .fiscalUuid(fiscalUuid)
                .series(series)
                .folio(folio)
                .documentType(documentType)
                .issuerRfc(issuerRfc)
                .receiverRfc(receiverRfc)
                .total(total)
                .issueDate(issueDate)
                .hasAddenda(false)
                .pendingAddenda(true)
                .build();
    }

    /**
     * Crea una respuesta de error/rechazo
     */
    public static InvoiceRegistrationResponse error(String code, String message) {
        return InvoiceRegistrationResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
