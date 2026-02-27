package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para Autotransporte en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
public class AutotransporteDto {
    private String permSCT;
    private String numPermisoSCT;
    private IdentificacionVehicularDto identificacionVehicular;
    private SegurosDto seguros;
    private List<RemolqueDto> remolques;
}