package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.invoicexml.PagosDto;

/**
 * Servicio especializado para procesar complementos de Pagos v2.0.
 *
 * Maneja complementos de pago (TipoDocumento="P") utilizando el XSD
 * Pagos20.xsd del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
public interface PagosXmlProcessorService {

    /**
     * Procesa un XML de complemento Pagos v2.0.
     *
     * @param xmlContent Contenido XML del complemento de pago
     * @return PagosDto con los datos del pago
     * @throws RuntimeException si hay error en el procesamiento
     */
    PagosDto processPagos(String xmlContent);

    /**
     * Extrae información resumida del complemento Pagos.
     *
     * @param xmlContent Contenido XML
     * @return Información básica del complemento de pagos
     */
    String extractPaymentInfo(String xmlContent);

    /**
     * Calcula el monto total de todos los pagos.
     *
     * @param xmlContent Contenido XML
     * @return Monto total como String
     */
    String calculateTotalAmount(String xmlContent);
}
