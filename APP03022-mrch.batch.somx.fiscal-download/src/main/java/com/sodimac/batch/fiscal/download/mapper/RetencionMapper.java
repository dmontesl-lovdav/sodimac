package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.RetencionEntity;

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
