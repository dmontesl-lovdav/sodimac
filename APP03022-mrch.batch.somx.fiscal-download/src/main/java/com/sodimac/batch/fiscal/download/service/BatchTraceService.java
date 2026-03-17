package com.sodimac.batch.fiscal.download.service;

import com.sodimac.batch.fiscal.download.mapper.CtrlLogMapper;
import com.sodimac.batch.fiscal.download.mapper.CtrlProcesoCabMapper;
import com.sodimac.batch.fiscal.download.mapper.CtrlProcesoDetMapper;
import com.sodimac.batch.fiscal.download.mapper.CtrlProcesoElementoMapper;
import com.sodimac.batch.fiscal.download.model.entity.batch.CtrlProcesoCabEntity;
import com.sodimac.batch.fiscal.download.model.entity.batch.CtrlProcesoDetEntity;
import com.sodimac.batch.fiscal.download.repository.batch.CtrlLogRepository;
import com.sodimac.batch.fiscal.download.repository.batch.CtrlProcesoCabRepository;
import com.sodimac.batch.fiscal.download.repository.batch.CtrlProcesoDetRepository;
import com.sodimac.batch.fiscal.download.repository.batch.CtrlProcesoElementoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BatchTraceService {

    private static final Logger log = LoggerFactory.getLogger(BatchTraceService.class);

    private final CtrlProcesoCabRepository cabRepository;
    private final CtrlProcesoDetRepository detRepository;
    private final CtrlProcesoElementoRepository elementoRepository;
    private final CtrlLogRepository logRepository;

    public BatchTraceService(CtrlProcesoCabRepository cabRepository,
                              CtrlProcesoDetRepository detRepository,
                              CtrlProcesoElementoRepository elementoRepository,
                              CtrlLogRepository logRepository) {
        this.cabRepository = cabRepository;
        this.detRepository = detRepository;
        this.elementoRepository = elementoRepository;
        this.logRepository = logRepository;
    }

    public static final int PROCESS_INVOICE_DOWNLOAD = 5;
    public static final int PROCESS_CREDITNOTE_DOWNLOAD = 6;

    @Transactional("batchTransactionManager")
    public CtrlProcesoCabEntity startExecution(int processId) {
        CtrlProcesoCabEntity cab = CtrlProcesoCabMapper.toNewExecution(processId);
        cab = cabRepository.save(cab);
        log.info("Ejecucion iniciada: id={} proceso={}", cab.getIdEjecucion(), processId);
        return cab;
    }

    @Transactional("batchTransactionManager")
    public void finishExecution(Integer idEjecucion, String estatus,
                                int registrosOrigen, int registrosDestino, String mensajeError) {
        cabRepository.findById(idEjecucion).ifPresent(cab -> {
            cab.setFechaFinal(LocalDateTime.now());
            cab.setEstatus(estatus);
            cab.setRegistrosOrigen(registrosOrigen);
            cab.setRegistrosDestino(registrosDestino);
            if (mensajeError != null) {
                cab.setMensajeError(mensajeError.length() > 1000
                        ? mensajeError.substring(0, 1000) : mensajeError);
            }
            cabRepository.save(cab);
            log.info("Ejecucion finalizada: id={} estatus={} origen={} destino={}",
                    idEjecucion, estatus, registrosOrigen, registrosDestino);
        });
    }

    @Transactional("batchTransactionManager")
    public CtrlProcesoDetEntity addStep(Integer idEjecucion, String nombrePaso, int secuencia,
                                         String parametros, String detalle, int registrosProcesados,
                                         String estatus) {
        CtrlProcesoDetEntity det = CtrlProcesoDetMapper.toEntity(
                idEjecucion, nombrePaso, secuencia,
                parametros, truncate(detalle, 255),
                registrosProcesados, estatus);
        return detRepository.save(det);
    }

    @Transactional("batchTransactionManager")
    public void addElement(Integer idEjecucion, String valor, String valorAlterno,
                           int secuencia, String estatus, String detalleError) {
        elementoRepository.save(CtrlProcesoElementoMapper.toEntity(
                idEjecucion, truncate(valor, 50),
                truncate(valorAlterno, 250), secuencia,
                estatus, truncate(detalleError, 500)));
    }

    @Transactional("batchTransactionManager")
    public void log(Integer idEjecucion, String nombre, String mensaje, String nivel, String fase) {
        logRepository.save(CtrlLogMapper.toEntity(
                idEjecucion, truncate(nombre, 100),
                mensaje, nivel, fase));
    }

    public void logInfo(Integer idEjecucion, String nombre, String mensaje, String fase) {
        log(idEjecucion, nombre, mensaje, "INFO", fase);
    }

    public void logError(Integer idEjecucion, String nombre, String mensaje, String fase) {
        log(idEjecucion, nombre, mensaje, "ERROR", fase);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
