package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.ReceptorEntity;

public class ReceptorMapper {

    public static ReceptorEntity toEntity(String uuid, String rfc, String nombre,
                                           String usoCfdi, String regimen,
                                           String domicilioFiscal) {
        ReceptorEntity entity = new ReceptorEntity();
        entity.setUuid(uuid);
        entity.setRfc(rfc);
        entity.setNombre(nombre);
        entity.setUsoCfdi(usoCfdi);
        entity.setRegimen(regimen);
        entity.setDomicilioFiscal(domicilioFiscal);
        return entity;
    }
}
