package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para request de descarga masiva de documentos (STM-396).
 *
 * Permite descargar multiples documentos XML o PDF en un archivo ZIP.
 *
 * @author Sodimac Tech Team
 * @since 2025-12-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para descarga masiva de documentos fiscales")
public class BulkDownloadRequest {

    @NotEmpty(message = "La lista de UUIDs no puede estar vacia")
    @Schema(
            description = "Lista de invoice_uuid de los documentos a descargar",
            required = true,
            example = "[\"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"b2c3d4e5-f6a7-8901-bcde-f12345678901\"]"
    )
    private List<UUID> invoiceUuids;

    @Schema(
            description = "Tipo de documento: I (Factura) o E (Nota de Credito)",
            required = false,
            example = "E",
            allowableValues = {"I", "E"}
    )
    private String documentType;
}
