package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;

/**
 * Servicio especializado para procesar documentos CFDI v4.0.
 *
 * Maneja facturas (TipoDocumento="I") y notas de crédito (TipoDocumento="E")
 * utilizando el XSD cfdv40.xsd del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
public interface CfdiXmlProcessorService {

    /**
     * Procesa un XML CFDI v4.0 (Factura o Nota de Crédito).
     *
     * @param xmlContent Contenido XML del comprobante
     * @param tipoDocumento Tipo específico (FACTURA o NOTA_CREDITO)
     * @return InvoiceXmlDto con los datos del comprobante
     * @throws RuntimeException si hay error en el procesamiento
     */
    InvoiceXmlDto processCfdi(String xmlContent, TipoDocumentoFiscal tipoDocumento);

    /**
     * Extrae información resumida del CFDI sin procesamiento completo.
     *
     * @param xmlContent Contenido XML
     * @return Información básica del comprobante
     */
    String extractBasicInfo(String xmlContent);
}
