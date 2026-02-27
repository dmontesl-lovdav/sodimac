package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para actualización de addenda de factura o nota de crédito.
 *
 * Los campos son opcionales y solo se actualizan si se proporcionan.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddendaUpdateDto {

    // Común para Factura y NC
    private UUID uuid;
    private BigDecimal idProveedor;
    private String serie;
    private String folio;
    private String rfc;
    private String tipoProveedor;

    // Específico para Factura
    private String idGuiaEntrega;
    private String idViaje;
    private String noOc;
    private String noRecepcion;
    private Integer tipoAddenda;

    // Específico para NC
    private UUID uuidNc;
    private String tipoNotaCredito;
}
