package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.AuthorizedReceiverCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorizedReceiverCatalogRepository extends JpaRepository<AuthorizedReceiverCatalogEntity, Long> {

    /**
     * Busca un receptor autorizado por RFC y estado.
     *
     * @param rfc RFC del receptor
     * @param status Estado (1=activo, 0=inactivo)
     * @return Optional con el receptor si existe
     */
    Optional<AuthorizedReceiverCatalogEntity> findByRfcAndStatus(String rfc, Integer status);

    /**
     * Busca un receptor autorizado por RFC.
     *
     * @param rfc RFC del receptor
     * @return Optional con el receptor si existe
     */
    Optional<AuthorizedReceiverCatalogEntity> findByRfc(String rfc);
}