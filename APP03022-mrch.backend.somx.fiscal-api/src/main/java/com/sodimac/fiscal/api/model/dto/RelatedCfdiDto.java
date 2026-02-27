package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatedCfdiDto {
    private UUID relatedCfdiUuid;

    @NotNull(message = "El UUID de la factura es obligatorio")
    private UUID invoiceUuid;

    @NotNull(message = "El UUID de la factura relacionada es obligatorio")
    private UUID relatedInvoiceUuid;

    @NotBlank(message = "El tipo de relación es obligatorio")
    @Size(max = 3, message = "El tipo de relación no puede exceder 3 caracteres")
    private String relationType;
}