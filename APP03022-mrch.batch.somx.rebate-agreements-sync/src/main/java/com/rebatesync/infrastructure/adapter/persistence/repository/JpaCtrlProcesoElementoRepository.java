package com.rebatesync.infrastructure.adapter.persistence.repository;

import com.rebatesync.infrastructure.adapter.persistence.entity.CtrlProcesoElementoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para ctrlProcesoElemento
 */
@Repository
public interface JpaCtrlProcesoElementoRepository extends JpaRepository<CtrlProcesoElementoEntity, Long> {

    /**
     * Encuentra todos los elementos procesados en una ejecución
     */
    List<CtrlProcesoElementoEntity> findByIdEjecucionOrderBySecuenciaAsc(Long idEjecucion);

    /**
     * Cuenta elementos por estatus en una ejecución
     */
    Long countByIdEjecucionAndEstatus(Long idEjecucion, String estatus);

    /**
     * Encuentra elementos fallidos en una ejecución
     */
    List<CtrlProcesoElementoEntity> findByIdEjecucionAndEstatus(Long idEjecucion, String estatus);
}
