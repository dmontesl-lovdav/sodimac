package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Identificación Vehicular en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
public class IdentificacionVehicularDto {
    private String configVehicular;
    private String placaVM;
    private String anioModeloVM;
    private String pesoBrutoVehicular;
}