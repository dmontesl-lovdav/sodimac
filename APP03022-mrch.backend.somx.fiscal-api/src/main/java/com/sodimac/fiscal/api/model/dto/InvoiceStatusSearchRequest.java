package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para request de búsqueda de facturas por estatus (STM-410).
 *
 * A diferencia de InvoiceSearchRequest, el campo estatus es OBLIGATORIO.
 * Usado para el endpoint POST /invoices/search/by-status.
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para búsqueda de facturas por estatus (estatus obligatorio)")
public class InvoiceStatusSearchRequest {

    // ========== CAMPOS OBLIGATORIOS ==========

    @NotBlank(message = "RFC Emisor es obligatorio")
    @Schema(
            description = "RFC del emisor (proveedor)",
            required = true,
            example = "AAA010101AAA"
    )
    private String rfcEmisor;

    @NotNull(message = "Fecha inicio de recepción es obligatoria")
    @Schema(
            description = "Fecha de inicio para filtrar por fecha de recepción (created_at)",
            required = true,
            example = "2025-01-01"
    )
    private LocalDate fechaInicioRecepcion;

    @NotNull(message = "Fecha final de recepción es obligatoria")
    @Schema(
            description = "Fecha final para filtrar por fecha de recepción (created_at)",
            required = true,
            example = "2025-12-31"
    )
    private LocalDate fechaFinalRecepcion;

    @NotBlank(message = "Tipo de documento es obligatorio")
    @Schema(
            description = "Tipo de documento: I (Factura) o E (Nota de Crédito)",
            required = true,
            example = "I",
            allowableValues = {"I", "E"}
    )
    private String tipoDocumento;

    @NotNull(message = "Estatus es obligatorio para esta búsqueda")
    @Schema(
            description = "Estatus del documento (OBLIGATORIO). Códigos: 1=Pendiente Addenda, 2=Recibido Parcial, 3=Recibido, etc.",
            required = true,
            example = "3"
    )
    private Integer estatus;

    // ========== CAMPOS OPCIONALES ==========

    @Schema(
            description = "RFC del receptor (Sodimac)",
            required = false,
            example = "CSD161207R2A"
    )
    private String rfcReceptor;

    @Schema(
            description = "ID del proveedor (Supplier Number)",
            required = false,
            example = "1234567890"
    )
    private BigDecimal idProveedor;

    @Schema(
            description = "Serie del comprobante",
            required = false,
            example = "A"
    )
    private String serie;

    @Schema(
            description = "Folio del comprobante",
            required = false,
            example = "12345"
    )
    private String folio;

    @Schema(
            description = "UUID fiscal del comprobante (TimbreFiscalDigital)",
            required = false,
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    private UUID uuid;

    @Schema(
            description = "Numero de Orden de Compra para filtrar documentos",
            required = false,
            example = "OC-2025-001234"
    )
    private String noOrdenCompra;

    @Schema(
            description = "Numero de Recepcion para filtrar documentos",
            required = false,
            example = "REC-2025-005678"
    )
    private String noRecepcion;

    // ========== PAGINACIÓN ==========

    @Schema(
            description = "Número de página (0-indexed)",
            required = false,
            example = "0"
    )
    private Integer page = 0;

    @Schema(
            description = "Tamaño de página",
            required = false,
            example = "20"
    )
    private Integer size = 20;

    @Schema(
            description = "Campo para ordenar resultados",
            required = false,
            example = "created_at"
    )
    private String sortBy = "createdAt";

    @Schema(
            description = "Dirección del ordenamiento: ASC o DESC",
            required = false,
            example = "DESC"
    )
    private String sortDirection = "DESC";
}
