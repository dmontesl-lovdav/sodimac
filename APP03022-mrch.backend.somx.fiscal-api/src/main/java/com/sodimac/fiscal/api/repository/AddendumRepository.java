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
}