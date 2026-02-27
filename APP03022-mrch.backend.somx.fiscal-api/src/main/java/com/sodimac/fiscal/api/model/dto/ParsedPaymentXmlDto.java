package com.sodimac.fiscal.api.model.dto;

import com.sodimac.fiscal.api.model.dto.invoicexml.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene todos los datos parseados de un XML de complemento de pago.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedPaymentXmlDto {

    // Datos del Comprobante CFDI
    private String version;
    private String serie;
    private String folio;
    private String fecha;
    private String sello;
    private String noCertificado;
    private String certificado;
    private String subTotal;
    private String moneda;
    private String total;
    private String tipoDeComprobante;
    private String exportacion;
    private String lugarExpedicion;

    // Emisor
    private String emisorRfc;
    private String emisorNombre;
    private String emisorRegimenFiscal;

    // Receptor
    private String receptorRfc;
    private String receptorNombre;
    private String receptorDomicilioFiscal;
    private String receptorRegimenFiscal;
    private String receptorUsoCFDI;

    // Timbre Fiscal Digital
    private TimbreFiscalDigitalDto timbreFiscalDigital;

    // Complemento Pagos
    private PagosDto pagos;

    // Addenda (si existe)
    private String addendaContent;
}
