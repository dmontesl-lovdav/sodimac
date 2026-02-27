package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para representar una Nota de Crédito relacionada a una Factura.
 *
 * Se utiliza en el response de búsqueda de facturas (InvoiceSearchResponse)
 * para mostrar las NC que están relacionadas con una factura específica.
 *
 * STM-1168: Presentar NC relacionadas
 *
 * @author Sodimac Tech Team
 * @since 2025-12-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nota de Crédito relacionada a una Factura")
public class NotaCreditoRelacionadaDto {

    @Schema(description = "UUID interno de la NC")
    private UUID invoiceUuid;

    @Schema(description = "UUID fiscal de la NC (Folio Fiscal del SAT)")
    private UUID fiscalUuid;

    @Schema(description = "Serie de la NC")
    private String serie;

    @Schema(description = "Folio de la NC")
    private String folio;

    @Schema(description = "Subtotal de la NC")
    private BigDecimal subtotal;

    @Schema(description = "Total de la NC")
    private BigDecimal total;

    @Schema(description = "Tipo de relación SAT (01=NC, 02=Nota Débito, 03=Devolución, etc.)")
    private String tipoRelacion;

    @Schema(description = "Descripción del tipo de relación")
    private String tipoRelacionNombre;

    @Schema(description = "Estatus actual de la NC")
    private Integer status;

    @Schema(description = "Nombre del estatus de la NC")
    private String statusNombre;

    @Schema(description = "Fecha de emisión de la NC")
    private LocalDate fechaEmision;

    @Schema(description = "Fecha de recepción en el sistema")
    private LocalDateTime fechaRecepcion;

    @Schema(description = "Fecha de envío a contabilizar")
    private LocalDateTime accountingSentDate;
}
