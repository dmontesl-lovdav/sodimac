package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.TrasladoEntity;

import java.math.BigDecimal;

public class TrasladoMapper {

    public static TrasladoEntity toEntity(int idImpuesto, BigDecimal base,
                                           String impuesto, String tipoFactor,
                                           BigDecimal tasaOCuota, BigDecimal importe) {
        TrasladoEntity entity = new TrasladoEntity();
        entity.setIdImpuesto(idImpuesto);
        entity.setBase(base);
        entity.setImpuesto(impuesto);
        entity.setTipoFactor(tipoFactor);
        entity.setTasaOCuota(tasaOCuota);
        entity.setImporte(importe);
        return entity;
    }
}
