package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.TaxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing tax records for fiscal invoices.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-11
 */
@Repository
public interface TaxRepository extends JpaRepository<TaxEntity, UUID> {

    /**
     * Finds taxes associated with an invoice.
     *
     * @param invoiceUuid Invoice UUID
     * @return Invoice taxes if they exist
     */
    Optional<TaxEntity> findByInvoiceUuid(UUID invoiceUuid);
}
