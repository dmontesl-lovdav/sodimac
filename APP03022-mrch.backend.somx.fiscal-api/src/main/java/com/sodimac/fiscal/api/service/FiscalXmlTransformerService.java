package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.response.FiscalXmlResponse;

/**
 * Servicio para transformar documentos XML fiscales a respuesta JSON estructurada.
 *
 * Extrae específicamente los nodos: Comprobante, Emisor, Receptor y
 * Complemento->TimbreFiscalDigital con todos sus campos relacionados.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
public interface FiscalXmlTransformerService {

    /**
     * Transforma un XML fiscal a respuesta JSON estructurada.
     *
     * @param xmlContent Contenido XML del documento fiscal
     * @return FiscalXmlResponse con los nodos especificados
     * @throws RuntimeException si hay error en el procesamiento
     */
    FiscalXmlResponse transformToStructuredResponse(String xmlContent);
}
