package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.batch.CtrlProcesoElementoEntity;

import java.time.LocalDateTime;

public class CtrlProcesoElementoMapper {

    public static CtrlProcesoElementoEntity toEntity(Integer idEjecucion, String valor,
                                                      String valorAlterno, int secuencia,
                                                      String estatus, String detalleError) {
        CtrlProcesoElementoEntity entity = new CtrlProcesoElementoEntity();
        entity.setIdEjecucion(idEjecucion);
        entity.setValor(valor);
        entity.setValorAlterno(valorAlterno);
        entity.setSecuencia(secuencia);
        entity.setEstatus(estatus);
        entity.setFechaRegistro(LocalDateTime.now());
        entity.setDetalleError(detalleError);
        return entity;
    }
}
