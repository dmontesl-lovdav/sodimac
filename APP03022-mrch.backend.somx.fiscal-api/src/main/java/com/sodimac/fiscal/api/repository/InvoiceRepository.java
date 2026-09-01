package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
     * Igual que {@link #existsBySeriesAndFolioAndIssuerUuidAndDocumentType}, pero ignora
     * registros cuyo estatus esté en la lista (Rechazo Comercial y Rechazo Contable).
     */
    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
            FROM InvoiceEntity i
            WHERE i.series = :series
              AND i.folio = :folio
              AND i.issuerUuid = :issuerUuid
              AND i.documentType = :documentType
              AND i.status NOT IN :excludedStatuses
            """)
    boolean existsBySeriesAndFolioAndIssuerUuidAndDocumentTypeExcludingStatuses(
            @Param("series") String series,
            @Param("folio") String folio,
            @Param("issuerUuid") UUID issuerUuid,
            @Param("documentType") String documentType,
            @Param("excludedStatuses") Collection<Integer> excludedStatuses);

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

    /**
     * Existe algún documento con el UUID fiscal cuyo estatus no esté en la lista
     * (permite reintentar si solo hay Rechazo Comercial y/o Rechazo Contable).
     */
    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
            FROM InvoiceEntity i
            WHERE i.fiscalUuid = :fiscalUuid
              AND i.status NOT IN :excludedStatuses
            """)
    boolean existsByFiscalUuidExcludingStatuses(
            @Param("fiscalUuid") UUID fiscalUuid,
            @Param("excludedStatuses") Collection<Integer> excludedStatuses);

    /**
     * Existe documento con UUID fiscal + emisor + tipo, excluyendo los estatus indicados.
     */
    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
            FROM InvoiceEntity i
            WHERE i.fiscalUuid = :fiscalUuid
              AND i.issuerUuid = :issuerUuid
              AND i.documentType = :documentType
              AND i.status NOT IN :excludedStatuses
            """)
    boolean existsByFiscalUuidAndIssuerUuidAndDocumentTypeExcludingStatuses(
            @Param("fiscalUuid") UUID fiscalUuid,
            @Param("issuerUuid") UUID issuerUuid,
            @Param("documentType") String documentType,
            @Param("excludedStatuses") Collection<Integer> excludedStatuses);
}