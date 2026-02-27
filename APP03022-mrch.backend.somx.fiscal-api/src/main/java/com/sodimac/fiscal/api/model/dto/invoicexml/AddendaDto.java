package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para las adendas de las facturas XML CFDI v4.0
 * Las adendas son elementos personalizados específicos de cada proveedor
  * @author g_dco018
  * @version 1.0
  * @since 2025
 */
@Data
@NoArgsConstructor
public class AddendaDto {
    private String tipoAddenda; // Tipo de addenda (ej: BOVEDAFISCAL, AddendaK, etc.)
    private String namespace; // Namespace de la addenda
    private Map<String, Object> contenido; // Contenido dinámico de la addenda

    // Campos específicos comunes extraídos
    private String numeroCliente;
    private String razonSocialCliente;
    private String numeroFactura;
    private String numeroPedido;
    private String contrato;
    private String ordenCompra;
    private String importeLetra;
}