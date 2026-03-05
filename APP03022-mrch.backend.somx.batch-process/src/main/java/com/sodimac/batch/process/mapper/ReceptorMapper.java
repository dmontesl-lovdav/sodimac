package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.sap.ReceptorEntity;

public class ReceptorMapper {

    public static ReceptorEntity toEntity(int idComprobante, String rfc, String nombre,
                                           String usoCfdi, String regimenFiscal,
                                           String domicilioFiscal) {
        ReceptorEntity entity = new ReceptorEntity();
        entity.setIdComprobante(idComprobante);
        entity.setRfc(rfc);
        entity.setNombre(nombre);
        entity.setUsoCfdi(usoCfdi);
        entity.setRegimenFiscal(regimenFiscal);
        entity.setDomicilioFiscal(domicilioFiscal);
        return entity;
    }
}
