package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.PaymentResponseCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para el catálogo de respuestas multipac.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Repository
public interface PaymentResponseCatalogRepository extends JpaRepository<PaymentResponseCatalogEntity, Long> {

    /**
     * Busca una respuesta por su código.
     *
     * @param responseCode Código de respuesta
     * @return Optional con la respuesta si existe
     */
    Optional<PaymentResponseCatalogEntity> findByResponseCode(String responseCode);

    /**
     * Busca una respuesta por su código y que esté activa.
     *
     * @param responseCode Código de respuesta
     * @param status Estado (1=activo)
     * @return Optional con la respuesta si existe
     */
    Optional<PaymentResponseCatalogEntity> findByResponseCodeAndStatus(String responseCode, Integer status);
}
