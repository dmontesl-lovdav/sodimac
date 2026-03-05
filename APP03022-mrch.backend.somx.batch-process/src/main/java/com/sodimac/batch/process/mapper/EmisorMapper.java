package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.sap.EmisorEntity;

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
