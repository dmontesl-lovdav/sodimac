package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.sap.ConceptoEntity;

import java.math.BigDecimal;

public class ConceptoMapper {

    public static ConceptoEntity toEntity(int idComprobante, String claveProdServ,
                                           String noIdentificacion, BigDecimal cantidad,
                                           String claveUnidad, String unidad,
                                           String descripcion, BigDecimal valorUnitario,
                                           BigDecimal importe, BigDecimal descuento,
                                           String objetoImp) {
        ConceptoEntity entity = new ConceptoEntity();
        entity.setIdComprobante(idComprobante);
        entity.setClaveProdServ(claveProdServ);
        entity.setNoIdentificacion(noIdentificacion);
        entity.setCantidad(cantidad);
        entity.setClaveUnidad(claveUnidad);
        entity.setUnidad(unidad);
        entity.setDescripcion(descripcion);
        entity.setValorUnitario(valorUnitario);
        entity.setImporte(importe);
        entity.setDescuento(descuento);
        entity.setObjetoImp(objetoImp);
        return entity;
    }
}
