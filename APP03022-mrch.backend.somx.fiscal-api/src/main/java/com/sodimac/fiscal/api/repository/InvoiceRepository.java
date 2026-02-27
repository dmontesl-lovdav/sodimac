package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para entidad Invoice.
 *
 * Extiende JpaSpecificationExecutor para permitir búsquedas dinámicas con filtros (STM-338).
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID>, JpaSpecificationExecutor<InvoiceEntity> {

    /**
     * Busca una factura por su UUID fiscal (TimbreFiscalDigital del SAT).
     *
     * @param fiscalUuid UUID del TimbreFiscalDigital
     * @return Optional con la factura si existe
     */
    Optional<InvoiceEntity> findByFiscalUuid(UUID invoiceUuid);

    /**
     * Verifica si existe una factura con el UUID fiscal dado.
     *
     * Más eficiente que findByFiscalUuid() para validaciones de duplicidad
     * porque solo hace COUNT sin cargar la entidad ni sus relaciones.
     *
     * @param fiscalUuid UUID del TimbreFiscalDigital
     * @return true si existe, false si no existe
     */
    boolean existsByFiscalUuid(UUID fiscalUuid);

    /**
     * Verifica si existe un documento con la misma serie, folio, emisor y tipo.
     * Usado para validar duplicidad por serie+folio del mismo proveedor (STM-395/STM-397).
     *
     * @param series Serie del documento
     * @param folio Folio del documento
     * @param issuerUuid UUID del emisor (proveedor)
     * @param documentType Tipo de documento (I=Factura, E=Nota de Crédito)
     * @return true si existe, false si no
     */
    boolean existsBySeriesAndFolioAndIssuerUuidAndDocumentType(
            String series, String folio, UUID issuerUuid, String documentType);

    /**
     * Verifica si existe un documento con el mismo UUID fiscal, emisor y tipo.
     * Usado para validar duplicidad por UUID del mismo proveedor (STM-395/STM-397).
     *
     * @param fiscalUuid UUID fiscal del documento
     * @param issuerUuid UUID del emisor (proveedor)
     * @param documentType Tipo de documento (I=Factura, E=Nota de Crédito)
     * @return true si existe, false si no
     */
    boolean existsByFiscalUuidAndIssuerUuidAndDocumentType(
            UUID fiscalUuid, UUID issuerUuid, String documentType);

}