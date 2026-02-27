package com.sodimac.fiscal.api.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con metadatos del procesamiento de documentos fiscales XML.
 *
 * Contiene información adicional sobre el procesamiento y tipo de documento detectado.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadatosResponse {

    /**
     * Código del tipo de documento detectado
     */
    private String tipoDocumento;

    /**
     * Descripción del tipo de documento
     */
    private String descripcionTipo;

    /**
     * Versión del esquema utilizado
     */
    private String version;

    /**
     * XSD utilizado para el procesamiento
     */
    private String xsdPath;

    /**
     * Namespace del documento XML
     */
    private String namespace;

    /**
     * Indica si el documento tiene complementos
     */
    private Boolean tieneComplementos;

    /**
     * Nombre del complemento principal (si aplica)
     */
    private String complementoPrincipal;

    /**
     * Timestamp del procesamiento
     */
    private LocalDateTime fechaProcesamiento;

    /**
     * Estado del procesamiento (SUCCESS, WARNING, ERROR)
     */
    private String estado;

    /**
     * Mensaje adicional del procesamiento
     */
    private String mensaje;
}