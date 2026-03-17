package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.ImpuestosEntity;

import java.math.BigDecimal;

public class ImpuestosMapper {

    public static ImpuestosEntity toEntity(int idComprobante,
                                            BigDecimal totalImpuestosTrasladados,
                                            BigDecimal totalImpuestosRetenidos) {
        ImpuestosEntity entity = new ImpuestosEntity();
        entity.setIdComprobante(idComprobante);
        entity.setTotalImpuestosTrasladados(totalImpuestosTrasladados);
        entity.setTotalImpuestosRetenidos(totalImpuestosRetenidos);
        return entity;
    }
}
