package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.ConceptoEntity;

import java.math.BigDecimal;

public class ConceptoMapper {

    public static ConceptoEntity toEntity(String uuid, String claveProdServ, String cantidad,
            String claveUnidad, String unidad, String descripcion,
            BigDecimal valorUnitario, BigDecimal importe) {
        ConceptoEntity e = new ConceptoEntity();
        e.setUuid(uuid);
        e.setClaveProdServ(claveProdServ);
        e.setCantidad(cantidad);
        e.setClaveUnidad(claveUnidad);
        e.setUnidad(unidad);
        e.setDescripcion(descripcion);
        e.setValorUnitario(valorUnitario);
        e.setImporte(importe);
        return e;
    }
}
