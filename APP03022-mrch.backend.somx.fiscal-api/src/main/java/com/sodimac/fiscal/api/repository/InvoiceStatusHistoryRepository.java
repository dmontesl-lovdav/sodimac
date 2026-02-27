package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.InvoiceStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para el historial de cambios de estatus de facturas/NC.
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
@Repository
public interface InvoiceStatusHistoryRepository extends JpaRepository<InvoiceStatusHistoryEntity, UUID> {

    /**
     * Busca el historial de cambios de estatus por invoice_uuid.
     */
    List<InvoiceStatusHistoryEntity> findByInvoiceUuidOrderByChangedAtDesc(UUID invoiceUuid);

    /**
     * Busca el historial de cambios de estatus por fiscal_uuid.
     */
    List<InvoiceStatusHistoryEntity> findByFiscalUuidOrderByChangedAtDesc(UUID fiscalUuid);
}
