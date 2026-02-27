package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Remolque en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
public class RemolqueDto {
    private String subTipoRem;
    private String placa;
}