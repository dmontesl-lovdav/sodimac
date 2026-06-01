package com.rebatesync.infrastructure.adapter.persistence.repository;

import com.rebatesync.infrastructure.adapter.persistence.entity.CtrlLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para ctrlLog
 */
@Repository
public interface JpaCtrlLogRepository extends JpaRepository<CtrlLogEntity, Long> {

    /**
     * Encuentra todos los logs de una ejecución específica
     */
    List<CtrlLogEntity> findByIdEjecucionOrderByFechaRegistroDesc(Long idEjecucion);

    /**
     * Encuentra logs por nivel (ERROR, WARN, INFO, DEBUG)
     */
    List<CtrlLogEntity> findByNivelOrderByFechaRegistroDesc(String nivel);

    /**
     * Encuentra logs de una ejecución específica por nivel
     */
    List<CtrlLogEntity> findByIdEjecucionAndNivelOrderByFechaRegistroDesc(Long idEjecucion, String nivel);

    /**
     * Encuentra logs por fase
     */
    List<CtrlLogEntity> findByFaseOrderByFechaRegistroDesc(String fase);

    /**
     * Encuentra logs en un rango de fechas
     */
    List<CtrlLogEntity> findByFechaRegistroBetweenOrderByFechaRegistroDesc(LocalDateTime inicio, LocalDateTime fin);
}
