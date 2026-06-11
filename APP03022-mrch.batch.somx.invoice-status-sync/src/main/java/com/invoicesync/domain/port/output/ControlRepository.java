package com.invoicesync.domain.port.output;

import com.invoicesync.domain.model.BatchExecutionLog;
import com.invoicesync.domain.model.ControlCifras;
import com.invoicesync.domain.model.StatusTransitionResult;

/**
 * Trazabilidad del batch en SODIMAC_BATCH_DEV usando el modelo estándar
 * CtrlProcesoCab / CtrlProcesoDet / CtrlProcesoElemento / ctrlLog (STM-1309, STM-1167).
 *
 * El id de ejecución es el int identity de ctrlProcesoCab (se obtiene al iniciar).
 */
public interface ControlRepository {

    /** Inserta ctrlProcesoCab (estatus IN_PROGRESS) y devuelve el id_ejecucion generado. */
    int startExecution(int processId);

    /** Actualiza ctrlProcesoCab al cierre (estatus, totales, duración, error). */
    void finishExecution(int idEjecucion, BatchExecutionLog log);

    /** Registra un paso en ctrlProcesoDet. */
    void saveStep(int idEjecucion, String nombrePaso, int secuencia, int registrosProcesados, String estatus);

    /** Registra una factura procesada en ctrlProcesoElemento. */
    void saveTransitionResult(int idEjecucion, StatusTransitionResult result, int secuencia);

    /** Registra una entrada en ctrlLog. */
    void saveLog(int idEjecucion, String nombre, String mensaje, String nivel, String fase);

    /** Registra las cifras control (fotografía) como entrada en ctrlLog. */
    void saveCifras(int idEjecucion, ControlCifras cifras, String phase);
}
