package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.EmisorEntity;

public class EmisorMapper {

    public static EmisorEntity toEntity(int idComprobante, String rfc,
                                         String nombre, String regimenFiscal) {
        EmisorEntity entity = new EmisorEntity();
        entity.setIdComprobante(idComprobante);
        entity.setRfc(rfc);
        entity.setNombre(nombre);
        entity.setRegimenFiscal(regimenFiscal);
        return entity;
    }
}
