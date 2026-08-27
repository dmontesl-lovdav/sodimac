package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.DetalleImpuestoEntity;

import java.math.BigDecimal;

public class DetalleImpuestoMapper {

    public static DetalleImpuestoEntity toEntity(String uuid, long idPadre, String claveProdServ,
            String tipoImpuesto, BigDecimal base, String impuesto, String tipoFactor,
            BigDecimal tasaOCuota, BigDecimal importe) {
        DetalleImpuestoEntity e = new DetalleImpuestoEntity();
        e.setUuid(uuid);
        e.setIdPadre(idPadre);
        e.setClaveProdServ(claveProdServ);
        e.setTipoImpuesto(tipoImpuesto);
        e.setBase(base);
        e.setImpuesto(impuesto);
        e.setTipoFactor(tipoFactor);
        e.setTasaOCuota(tasaOCuota);
        e.setImporte(importe);
        return e;
    }
}
