package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.CatParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatParameterRepository extends JpaRepository<CatParameterEntity, Integer> {

    /**
     * Última versión ACTIVA de un parámetro por nombre. Un mismo parámetro (ej.
     * "ToleranciaImporteRebate") tiene varias filas versionadas con distinto id_parameter;
     * hay que tomar la de mayor versión con status activo, NO un id fijo. Ivan 2026-09-02.
     */
    Optional<CatParameterEntity> findTopByNameAndStatusOrderByVersionDesc(String name, Integer status);
}
