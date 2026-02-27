package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Mercancía individual en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
public class MercanciaDto {
    private String pesoEnKg;
    private String bienesTransp;
    private String descripcion;
    private String cantidad;
    private String claveUnidad;
    private String valorMercancia;
    private String moneda;
}