package com.sodimac.fiscal.api.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO de respuesta estructurada para documentos fiscales XML.
 *
 * Contiene únicamente los nodos especificados: Comprobante, Emisor, Receptor,
 * y Complemento->TimbreFiscalDigital con todos sus campos relacionados.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalXmlResponse {

    /**
     * Información del comprobante fiscal
     */
    private ComprobanteResponse comprobante;

    /**
     * Información del emisor del comprobante
     */
    private EmisorResponse emisor;

    /**
     * Información del receptor del comprobante
     */
    private ReceptorResponse receptor;

    /**
     * Información del timbre fiscal digital (complemento)
     */
    private TimbreFiscalDigitalResponse timbreFiscalDigital;

    /**
     * Metadatos adicionales de procesamiento
     */
    private MetadatosResponse metadatos;
}