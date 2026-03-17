package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.batch.CtrlProcesoCabEntity;

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
