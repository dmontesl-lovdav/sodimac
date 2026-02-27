package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;

/**
 * Servicio principal para procesar documentos XML fiscales mexicanos.
 *
 * Detecta automáticamente el tipo de documento y aplica el procesador
 * específico según las reglas de negocio del SAT.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
public interface XmlProcessorService {

    /**
     * Procesa un XML fiscal identificando automáticamente su tipo.
     *
     * @param xmlContent Contenido XML como String
     * @return Objeto procesado según el tipo de documento
     * @throws RuntimeException si no se puede procesar el XML
     */
    Object processXml(String xmlContent);

    /**
     * Obtiene información del tipo de documento sin procesarlo completamente.
     *
     * @param xmlContent Contenido XML
     * @return Información del tipo de documento detectado
     */
    TipoDocumentoFiscal getDocumentType(String xmlContent);

    /**
     * Valida si el XML es un documento fiscal válido.
     *
     * @param xmlContent Contenido XML
     * @return true si es válido, false en caso contrario
     */
    boolean isValidFiscalDocument(String xmlContent);
}
