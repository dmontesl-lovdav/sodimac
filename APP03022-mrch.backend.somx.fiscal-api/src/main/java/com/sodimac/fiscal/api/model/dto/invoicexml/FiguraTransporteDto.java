package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Figura de Transporte en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
public class FiguraTransporteDto {
    private String tipoFigura;
    private String rfcFigura;
    private String numLicencia;
    private String nombreFigura;
}