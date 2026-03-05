package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.sap.ImpuestosEntity;

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
