package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ============================================================================
 * DTO: NCValidationRequest
 * ============================================================================
 * Request para validar que una Nota de Crédito está relacionada con una Factura
 * en el XML del CFDI.
 *
 * PROPÓSITO:
 * - Usado por finanzas-api para validar XMLs antes de guardar rebates
 * - fiscal-api solo VALIDA, no guarda datos de rebates
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para validar relación NC-Factura en XML CFDI")
public class NCValidationRequest {

    @NotNull(message = "El UUID de la factura es obligatorio")
    @Schema(description = "UUID fiscal de la factura (Folio Fiscal del SAT)",
            example = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890",
            required = true)
    private UUID invoiceFiscalUuid;

    @Schema(description = "Nota: El XML de la NC se envía como multipart file en el endpoint",
            example = "Ver endpoint POST /api/invoices/validate-nc-relation")
    private String xmlNote;
}
