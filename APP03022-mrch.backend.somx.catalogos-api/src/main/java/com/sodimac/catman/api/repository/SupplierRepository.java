package com.sodimac.catman.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    Optional<Supplier> findBySupplierNumber(String supplierNumber);

    Optional<Supplier> findByRfc(String rfc);

    List<Supplier> findByStatus(Integer status);

    List<Supplier> findBySupplierTypeId(Integer supplierTypeId);

    List<Supplier> findBySupplierTypeIdAndStatus(Integer supplierTypeId, Integer status);

    boolean existsBySupplierNumber(String supplierNumber);

    boolean existsByRfc(String rfc);

    /**
     * STM-831: Busca proveedores por tipo y estado de bloqueo.
     *
     * @param tipoProveedor ID del tipo de proveedor (0 = todos)
     * @return Lista de proveedores activos filtrados por tipo
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = 1 " +
           "AND (:tipoProveedor = 0 OR s.supplierType.id = :tipoProveedor)")
    List<Supplier> findByTypeFilter(@Param("tipoProveedor") Integer tipoProveedor);
}
