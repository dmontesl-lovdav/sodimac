package com.sodimac.fiscal.api.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO de respuesta para el complemento TimbreFiscalDigital de documentos fiscales XML.
 *
 * Contiene todos los atributos del complemento TimbreFiscalDigital según las
 * especificaciones del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimbreFiscalDigitalResponse {

    /**
     * Versión del timbre fiscal digital (obligatorio)
     */
    private String version;

    /**
     * UUID del comprobante fiscal (obligatorio)
     */
    private String uuid;

    /**
     * Fecha y hora de timbrado (obligatorio)
     */
    private String fechaTimbrado;

    /**
     * RFC del proveedor de certificación (obligatorio)
     */
    private String rfcProvCertif;

    /**
     * Leyenda del timbre (opcional)
     */
    private String leyenda;

    /**
     * Sello del CFDI (obligatorio)
     */
    private String selloCFD;

    /**
     * Número de certificado del SAT (obligatorio)
     */
    private String noCertificadoSAT;

    /**
     * Sello del SAT (obligatorio)
     */
    private String selloSAT;
}