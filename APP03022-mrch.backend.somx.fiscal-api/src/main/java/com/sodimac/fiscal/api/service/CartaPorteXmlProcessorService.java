package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.invoicexml.CartaPorteDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;

/**
 * Servicio especializado para procesar documentos con complemento CartaPorte v3.1.
 *
 * Maneja facturas con complemento de traslado (TipoDocumento="T" o "I" con CartaPorte)
 * utilizando el XSD CartaPorte31.xsd del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
public interface CartaPorteXmlProcessorService {

    /**
     * Procesa un XML CFDI con complemento CartaPorte v3.1.
     *
     * @param xmlContent Contenido XML del comprobante con CartaPorte
     * @return InvoiceXmlDto con los datos del comprobante y complemento
     * @throws RuntimeException si hay error en el procesamiento
     */
    InvoiceXmlDto processCartaPorte(String xmlContent);

    /**
     * Extrae información de transporte del CartaPorte.
     *
     * @param xmlContent Contenido XML
     * @return Información resumida del transporte
     */
    String extractTransportInfo(String xmlContent);

    /**
     * Procesa solo el complemento CartaPorte (sin el CFDI completo).
     *
     * @param xmlContent Contenido XML
     * @return CartaPorteDto con los datos del complemento
     */
    CartaPorteDto processCartaPorteOnly(String xmlContent);
}
