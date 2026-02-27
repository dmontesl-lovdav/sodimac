package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * DTO: NCValidationResponse
 * ============================================================================
 * Response de validación de relación NC-Factura.
 *
 * PROPÓSITO:
 * - Informa a finanzas-api si el XML de NC es válido
 * - Retorna datos fiscales necesarios (UUIDs, tipo relación)
 * - NO registra nada en BD (solo validación)
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response de validación de relación NC-Factura")
public class NCValidationResponse {

    @Schema(description = "Indica si la validación fue exitosa", example = "true")
    private boolean isValid;

    @Schema(description = "UUID fiscal de la Nota de Crédito (del XML)",
            example = "NC123456-7890-ABCD-EF12-34567890ABCD")
    private UUID ncFiscalUuid;

    @Schema(description = "Lista de UUIDs de facturas relacionadas encontradas en el XML")
    private List<UUID> relatedInvoiceUuids;

    @Schema(description = "Tipo de relación CFDI según catálogo SAT", example = "01")
    private String relationshipType;

    @Schema(description = "Mensaje de error si isValid = false")
    private String errorMessage;

    @Schema(description = "Código de error de negocio", example = "VALID_001")
    private String businessCode;

    // Factory methods
    public static NCValidationResponse success(UUID ncFiscalUuid, List<UUID> relatedUuids, String relationshipType) {
        return NCValidationResponse.builder()
                .isValid(true)
                .ncFiscalUuid(ncFiscalUuid)
                .relatedInvoiceUuids(relatedUuids)
                .relationshipType(relationshipType)
                .businessCode("VALID_001")
                .build();
    }

    public static NCValidationResponse error(String errorMessage, String businessCode) {
        return NCValidationResponse.builder()
                .isValid(false)
                .errorMessage(errorMessage)
                .businessCode(businessCode)
                .build();
    }
}
