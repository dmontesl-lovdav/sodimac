package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;

/**
 * Interfaz para servicios de detección de tipos de documentos fiscales XML.
 *
 * Define el contrato para implementaciones que puedan identificar automáticamente
 * el tipo de documento fiscal basándose en el contenido XML.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
public interface XmlDocumentTypeDetector {

    /**
     * Detecta el tipo de documento fiscal a partir del contenido XML.
     *
     * @param xmlContent Contenido XML como String
     * @return Tipo de documento fiscal detectado
     * @throws RuntimeException si no se puede parsear el XML o identificar el tipo
     */
    TipoDocumentoFiscal detectDocumentType(String xmlContent);
}