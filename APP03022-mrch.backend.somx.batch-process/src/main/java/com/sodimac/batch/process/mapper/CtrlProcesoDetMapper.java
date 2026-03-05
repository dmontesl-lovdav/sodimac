package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.batch.CtrlProcesoDetEntity;

import java.time.LocalDateTime;

public class CtrlProcesoDetMapper {

    public static CtrlProcesoDetEntity toEntity(Integer idEjecucion, String nombrePaso, int secuencia,
                                                 String parametros, String detalle,
                                                 int registrosProcesados, String estatus) {
        CtrlProcesoDetEntity entity = new CtrlProcesoDetEntity();
        entity.setIdEjecucion(idEjecucion);
        entity.setNombrePaso(nombrePaso);
        entity.setSecuencia(secuencia);
        entity.setFechaInicioRegistro(LocalDateTime.now());
        entity.setFechaFinalRegistro(LocalDateTime.now());
        entity.setParametrosRegistro(parametros);
        entity.setDetalle(detalle);
        entity.setRegistrosProcesados(registrosProcesados);
        entity.setEstatus(estatus);
        return entity;
    }
}
