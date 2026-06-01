package com.rebatesync.infrastructure.adapter.persistence.repository;

import com.rebatesync.infrastructure.adapter.persistence.entity.CtrlProcDetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para ctrlProcesoDet
 */
@Repository
public interface JpaCtrlProcDetRepository extends JpaRepository<CtrlProcDetEntity, Long> {

    /**
     * Encuentra todos los pasos de un proceso específico
     */
    List<CtrlProcDetEntity> findByIdEjecucionOrderBySecuenciaAsc(Long idEjecucion);

    /**
     * Encuentra un paso específico por su secuencia en una ejecución
     */
    CtrlProcDetEntity findByIdEjecucionAndSecuencia(Long idEjecucion, Integer secuencia);
}
