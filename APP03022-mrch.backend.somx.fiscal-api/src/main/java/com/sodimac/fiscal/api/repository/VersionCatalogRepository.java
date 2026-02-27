package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.VersionCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface VersionCatalogRepository extends JpaRepository<VersionCatalogEntity, Long> {

    /**
     * Busca una versión por número de versión, tipo de documento y estado.
     *
     * @param version Número de versión
     * @param documentType Tipo de documento ('I', 'E', 'T', 'P', 'N')
     * @param status Estado (1=activo, 0=inactivo)
     * @return Optional con la versión si existe
     */
    Optional<VersionCatalogEntity> findByVersionAndDocumentTypeAndStatus(
            BigDecimal version,
            String documentType,
            Integer status
    );

    /**
     * Busca una versión por tipo de documento y estado.
     *
     * @param documentType Tipo de documento
     * @param status Estado
     * @return Optional con la versión si existe
     */
    Optional<VersionCatalogEntity> findByDocumentTypeAndStatus(String documentType, Integer status);
}