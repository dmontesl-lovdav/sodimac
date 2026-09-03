package com.sodimac.fiscal.api.model.enums;

import lombok.Getter;

/**
 * IDs de parámetros en core_utils.cat_parameter.
 * El valor numérico corresponde al id_parameter en BD (estable, confirmado local + UAT).
 */
@Getter
public enum CatParameterKey {

    TOLERANCIA_IMPORTE(3),
    TOLERANCIA_PORCENTAJE(4),
    // TOLERANCIA_IMPORTE_REBATE(16) ELIMINADO: se leía por id fijo (siempre v1.0), no seguía el
    // versionado. Ahora se lee la última versión activa por nombre. Ver readActiveParamValueByName.
    PDF_FACTURA_OPCIONAL(10),
    PDF_NOTA_CREDITO_OPCIONAL(11),
    PDF_COMPLEMENTO_PAGO_OPCIONAL(12);

    private final int id;

    CatParameterKey(int id) {
        this.id = id;
    }
}
