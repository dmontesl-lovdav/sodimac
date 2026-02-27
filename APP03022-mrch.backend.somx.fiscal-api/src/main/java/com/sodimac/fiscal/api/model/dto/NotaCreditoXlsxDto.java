package com.sodimac.fiscal.api.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para exportacion de Notas de Credito en archivo XLSX.
 *
 * Contiene los datos de la NC y la factura relacionada para
 * generar la hoja "Notas de Credito" del archivo Excel.
 *
 * @author Sodimac Tech Team
 * @since 2025
 */
@Data
@Builder
public class NotaCreditoXlsxDto {

    private String serie;
    private String folio;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String tipoRelacionNombre;
    private String fiscalUuid;
    private LocalDate fechaEmision;
    private LocalDateTime fechaRecepcion;
    private LocalDateTime accountingSentDate;

    // Datos de la Factura relacionada
    private String facturaSerie;
    private String facturaFolio;
    private String facturaUuid;
}
