package com.sodimac.fiscal.api.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO de respuesta para el nodo Comprobante de documentos fiscales XML.
 *
 * Contiene todos los atributos del elemento Comprobante según CFDI v4.0.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteResponse {

    // Atributos obligatorios del Comprobante
    private String version;
    private String serie;
    private String folio;
    private String fecha;
    private String subTotal;
    private String total;
    private String tipoDeComprobante;
    private String metodoPago;
    private String formaPago;
    private String moneda;
    private String noCertificado;
    private String certificado;
    private String sello;
    private String lugarExpedicion;
    private String exportacion;

    // Atributos opcionales
    private String condicionesDePago;
    private String tipoCambio;
    private String descuento;

    // Información global (si aplica)
    private String periodicidad;
    private String meses;
    private String año;

    // Relaciones CFDI (si aplica)
    private String tipoRelacion;

    // Observaciones adicionales
    private String observaciones;
}