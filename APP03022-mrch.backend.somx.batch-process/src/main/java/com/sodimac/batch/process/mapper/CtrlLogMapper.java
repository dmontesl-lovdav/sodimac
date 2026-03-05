package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.batch.CtrlLogEntity;

import java.time.LocalDateTime;

public class CtrlLogMapper {

    public static CtrlLogEntity toEntity(Integer idEjecucion, String nombre,
                                          String mensaje, String nivel, String fase) {
        CtrlLogEntity entity = new CtrlLogEntity();
        entity.setIdEjecucion(idEjecucion);
        entity.setNombre(nombre);
        entity.setLog(mensaje);
        entity.setNivel(nivel);
        entity.setFase(fase);
        entity.setFechaRegistro(LocalDateTime.now());
        return entity;
    }
}
