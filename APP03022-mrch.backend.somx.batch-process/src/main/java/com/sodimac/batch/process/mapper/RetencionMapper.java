package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.sap.RetencionEntity;

import java.math.BigDecimal;

public class RetencionMapper {

    public static RetencionEntity toEntity(int idImpuesto, BigDecimal base,
                                            String impuesto, String tipoFactor,
                                            BigDecimal tasaOCuota, BigDecimal importe) {
        RetencionEntity entity = new RetencionEntity();
        entity.setIdImpuesto(idImpuesto);
        entity.setBase(base);
        entity.setImpuesto(impuesto);
        entity.setTipoFactor(tipoFactor);
        entity.setTasaOCuota(tasaOCuota);
        entity.setImporte(importe);
        return entity;
    }
}
