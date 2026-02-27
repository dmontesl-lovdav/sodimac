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
 * DTO para request de búsqueda de facturas y notas de crédito (STM-338).
 *
 * Filtros de búsqueda:
 * - Obligatorios: Fecha Inicio/Fin, Tipo Documento
 * - Opcionales: RFC Emisor (si vacío retorna todos), RFC Receptor, ID Proveedor, Serie, Folio, UUID
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para búsqueda de facturas y notas de crédito")
public class InvoiceSearchRequest {

    // ========== CAMPOS OPCIONALES DE FILTRO ==========

    @Schema(
            description = "RFC del emisor (proveedor). Si se envía vacío o en blanco, retorna todos los proveedores.",
            required = false,
            example = "AAA010101AAA"
    )
    private String rfcEmisor;

    // ========== CAMPOS OBLIGATORIOS ==========

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
            description = "Estatus del documento (1=Pendiente Addenda, 2=Recibido Parcial, etc.)",
            required = false,
            example = "1"
    )
    private Integer estatus;

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
