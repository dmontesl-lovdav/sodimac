package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.EmisorEntity;

public class EmisorMapper {

    public static EmisorEntity toEntity(String uuid, String rfc, String nombre, String regimen) {
        EmisorEntity e = new EmisorEntity();
        e.setUuid(uuid);
        e.setRfc(rfc);
        e.setNombre(nombre);
        e.setRegimen(regimen);
        return e;
    }
}
