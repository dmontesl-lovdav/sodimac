package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.entity.batch.CtrlProcesoCabEntity;

import java.time.LocalDateTime;

public class CtrlProcesoCabMapper {

    public static CtrlProcesoCabEntity toNewExecution(int processId) {
        CtrlProcesoCabEntity entity = new CtrlProcesoCabEntity();
        entity.setIdProceso(processId);
        entity.setRegistrosOrigen(0);
        entity.setRegistrosDestino(0);
        entity.setFechaInicio(LocalDateTime.now());
        entity.setEstatus("IN_PROGRESS");
        return entity;
    }
}
