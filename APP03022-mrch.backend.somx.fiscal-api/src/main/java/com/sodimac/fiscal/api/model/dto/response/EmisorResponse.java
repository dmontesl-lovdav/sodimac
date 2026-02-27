package com.sodimac.fiscal.api.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO de respuesta para el nodo Emisor de documentos fiscales XML.
 *
 * Contiene todos los atributos del elemento Emisor según CFDI v4.0.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmisorResponse {

    /**
     * RFC del emisor (obligatorio)
     */
    private String rfc;

    /**
     * Nombre o razón social del emisor (obligatorio)
     */
    private String nombre;

    /**
     * Régimen fiscal del emisor (obligatorio)
     */
    private String regimenFiscal;

    /**
     * Facultad del adquiriente para expedir (opcional)
     */
    private String facAtrAdquirente;
}