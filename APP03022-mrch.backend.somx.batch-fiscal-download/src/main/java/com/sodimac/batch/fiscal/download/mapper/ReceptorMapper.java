package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.ReceptorEntity;

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
