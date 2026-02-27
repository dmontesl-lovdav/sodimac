package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Seguros en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
public class SegurosDto {
    private String aseguraRespCivil;
    private String polizaRespCivil;
    private String aseguraMedAmbiente;
    private String polizaMedAmbiente;
    private String aseguraCarga;
    private String polizaCarga;
    private String primaSeguro;
}