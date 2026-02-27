package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.RelatedCfdiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RelatedCfdiRepository extends JpaRepository<RelatedCfdiEntity, UUID> {

    /**
     * Busca todas las NC relacionadas a una Factura específica.
     *
     * @param relatedInvoiceUuid UUID de la Factura (invoice_uuid de la factura relacionada)
     * @return Lista de relaciones donde la Factura es el documento relacionado
     */
    List<RelatedCfdiEntity> findByRelatedInvoiceUuid(UUID relatedInvoiceUuid);

    /**
     * Busca todas las relaciones de una NC específica.
     *
     * @param invoiceUuid UUID de la NC que declara las relaciones
     * @return Lista de relaciones declaradas por la NC
     */
    List<RelatedCfdiEntity> findByInvoiceUuid(UUID invoiceUuid);

}