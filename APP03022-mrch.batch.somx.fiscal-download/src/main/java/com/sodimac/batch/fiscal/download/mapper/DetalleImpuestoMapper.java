package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.DetalleImpuestoEntity;

import java.math.BigDecimal;

public class DetalleImpuestoMapper {

    public static DetalleImpuestoEntity toEntity(int idConcepto, String tipo,
                                                  BigDecimal base, String impuesto,
                                                  String tipoFactor, BigDecimal tasaOCuota,
                                                  BigDecimal importe) {
        DetalleImpuestoEntity entity = new DetalleImpuestoEntity();
        entity.setIdConcepto(idConcepto);
        entity.setTipo(tipo);
        entity.setBase(base);
        entity.setImpuesto(impuesto);
        entity.setTipoFactor(tipoFactor);
        entity.setTasaOCuota(tasaOCuota);
        entity.setImporte(importe);
        return entity;
    }
}
