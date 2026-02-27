package com.sodimac.fiscal.api.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO de respuesta para el nodo Receptor de documentos fiscales XML.
 *
 * Contiene todos los atributos del elemento Receptor según CFDI v4.0.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptorResponse {

    /**
     * RFC del receptor (obligatorio)
     */
    private String rfc;

    /**
     * Nombre o razón social del receptor (obligatorio)
     */
    private String nombre;

    /**
     * Domicilio fiscal del receptor (obligatorio)
     */
    private String domicilioFiscalReceptor;

    /**
     * Régimen fiscal del receptor (obligatorio)
     */
    private String regimenFiscalReceptor;

    /**
     * Uso del CFDI (obligatorio)
     */
    private String usoCFDI;

    /**
     * Residencia fiscal (opcional, para extranjeros)
     */
    private String residenciaFiscal;

    /**
     * Número de registro de identificación fiscal (opcional, para extranjeros)
     */
    private String numRegIdTrib;
}