package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Devuelve el id del tipo de proveedor (CatTipoProveedor: 1-4) de un proveedor,
     * leyendo directo de shared_catalogs (sin ir a util-api). Issue Fer #3 (2026-06-19).
     * Mapea supplier_type.code -> key TPR00x del catálogo. null si no se encuentra.
     */
    @Query(value = "SELECT cd.value " +
            "FROM shared_catalogs.supplier s " +
            "JOIN shared_catalogs.supplier_type st ON st.id = s.supplier_type_id " +
            "JOIN shared_catalogs.catalog_header ch ON ch.code = 'CatTipoProveedor' " +
            "JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id AND cd.key = CASE st.code " +
            "  WHEN 'MERCANCIA'  THEN 'TPR001' " +
            "  WHEN 'TRANSPORTE' THEN 'TPR002' " +
            "  WHEN 'INDIRECTOS' THEN 'TPR003' " +
            "  WHEN 'SERVICIOS'  THEN 'TPR004' END " +
            "WHERE s.supplier_number = :supplierNumber LIMIT 1", nativeQuery = true)
    String findTipoProveedorId(@Param("supplierNumber") String supplierNumber);

    /**
     * Descripción (ES, lang_id=1) del tipo de proveedor a partir de su id (value 1-4 de
     * CatTipoProveedor). Para devolver id + descripción en búsquedas. Issue Fer #4 (2026-06-19).
     */
    @Query(value = "SELECT dl.description " +
            "FROM shared_catalogs.catalog_header ch " +
            "JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id " +
            "JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = cd.dict_id AND dl.lang_id = 1 " +
            "WHERE ch.code = 'CatTipoProveedor' AND cd.value = :tipoId LIMIT 1", nativeQuery = true)
    String findTipoProveedorDescripcion(@Param("tipoId") String tipoId);

    /**
     * supplier_type (id del tipo de proveedor) de la addenda de un complemento de pago,
     * por payments_uuid. Para devolver tipo en la búsqueda de complementos (Fer #5).
     */
    @Query(value = "SELECT supplier_type FROM tenant_fiscal.addendum " +
            "WHERE payments_uuid = :paymentsUuid AND supplier_type IS NOT NULL LIMIT 1", nativeQuery = true)
    String findSupplierTypeByPaymentsUuid(@Param("paymentsUuid") UUID paymentsUuid);

    /**
     * Tipo de proveedor (id 1-4 de CatTipoProveedor) resuelto EN VIVO desde el supplier_number
     * de la addenda de un complemento de pago (no del valor guardado). Refleja cambios en el
     * catálogo. Retro Ivan 2026-06-22.
     */
    @Query(value = "SELECT cd.value FROM tenant_fiscal.addendum a " +
            "JOIN shared_catalogs.supplier s ON s.supplier_number = a.supplier_number::text " +
            "JOIN shared_catalogs.supplier_type st ON st.id = s.supplier_type_id " +
            "JOIN shared_catalogs.catalog_header ch ON ch.code = 'CatTipoProveedor' " +
            "JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id AND cd.key = CASE st.code " +
            "  WHEN 'MERCANCIA'  THEN 'TPR001' " +
            "  WHEN 'TRANSPORTE' THEN 'TPR002' " +
            "  WHEN 'INDIRECTOS' THEN 'TPR003' " +
            "  WHEN 'SERVICIOS'  THEN 'TPR004' END " +
            "WHERE a.payments_uuid = :paymentsUuid LIMIT 1", nativeQuery = true)
    String findTipoProveedorIdByPaymentsUuid(@Param("paymentsUuid") UUID paymentsUuid);

    /**
     * Descripción de CUALQUIER catálogo de shared_catalogs por su value (directo a la tabla, sin
     * util-api). El value es la clave numérica (ej. estatus de factura). Las descripciones NO deben
     * salir de enums hardcodeados sino de la BD. Retro Ivan 2026-06-22.
     */
    @Query(value = "SELECT dl.description " +
            "FROM shared_catalogs.catalog_header ch " +
            "JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id " +
            "JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = cd.dict_id AND dl.lang_id = :langId " +
            "WHERE ch.code = :catalogCode AND cd.value = :value LIMIT 1", nativeQuery = true)
    String findCatalogDescription(@Param("catalogCode") String catalogCode,
            @Param("value") String value, @Param("langId") int langId);

    /**
     * ¿El folio fiscal ya está cargado como addenda manual? `tenant_finance.addendum_manual.invoice_uuid`
     * guarda el folio fiscal (lo captura finanzas/Josue en pantalla, antes del registro). Al cargar el
     * XML se valida que no exista ya (si existe → WRN7032). Fila 47 QA (2026-06-23).
     */
    @Query(value = "SELECT EXISTS(SELECT 1 FROM tenant_finance.addendum_manual WHERE invoice_uuid = :fiscalUuid)",
            nativeQuery = true)
    boolean existsAddendaManualByFolioFiscal(@Param("fiscalUuid") UUID fiscalUuid);

    /**
     * ¿El RFC del receptor está autorizado y activo en el catálogo `CatRfcReceptor`?
     * Reemplaza la tabla `authorized_receiver_catalog` (sin pantalla de mantenimiento). El RFC se
     * guarda en `catalog_detail.value`, status=1 = activo. Decisión Ivan 2026-06-23.
     */
    @Query(value = "SELECT EXISTS(" +
            "SELECT 1 FROM shared_catalogs.catalog_header ch " +
            "JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id " +
            "WHERE ch.code = 'CATRFCRECEPTOR' AND ch.status = 1 AND cd.value = :rfc AND cd.status = 1)",
            nativeQuery = true)
    boolean existsRfcReceptorAutorizado(@Param("rfc") String rfc);
}