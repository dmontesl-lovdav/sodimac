package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddendumRepository extends JpaRepository<AddendumEntity, UUID> {

    /**
     * Busca una addenda por UUID de la factura/NC asociada.
     *
     * @param invoiceUuid UUID de la factura/NC
     * @return Optional con la addenda si existe
     */
    Optional<AddendumEntity> findByInvoiceUuid(UUID invoiceUuid);
}