package com.sodimac.catman.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.SupplierBlock;

@Repository
public interface SupplierBlockRepository extends JpaRepository<SupplierBlock, Integer> {

    List<SupplierBlock> findBySupplierNumber(String supplierNumber);

    List<SupplierBlock> findBySupplierNumberAndStatus(String supplierNumber, Integer status);

    List<SupplierBlock> findByStatus(Integer status);

    /**
     * Busca bloqueos activos vigentes a una fecha especifica.
     */
    @Query("SELECT sb FROM SupplierBlock sb WHERE sb.supplierNumber = :supplierNumber " +
           "AND sb.status = 1 AND :date >= sb.validFrom AND :date <= sb.validTo")
    List<SupplierBlock> findActiveBlocksAtDate(
            @Param("supplierNumber") String supplierNumber,
            @Param("date") LocalDate date);

    /**
     * Busca bloqueos activos vigentes a la fecha actual.
     */
    @Query("SELECT sb FROM SupplierBlock sb WHERE sb.supplierNumber = :supplierNumber " +
           "AND sb.status = 1 AND CURRENT_DATE >= sb.validFrom AND CURRENT_DATE <= sb.validTo")
    List<SupplierBlock> findCurrentActiveBlocks(@Param("supplierNumber") String supplierNumber);

    /**
     * Verifica si un proveedor tiene bloqueos activos vigentes.
     */
    @Query("SELECT CASE WHEN COUNT(sb) > 0 THEN true ELSE false END FROM SupplierBlock sb " +
           "WHERE sb.supplierNumber = :supplierNumber AND sb.status = 1 " +
           "AND CURRENT_DATE >= sb.validFrom AND CURRENT_DATE <= sb.validTo")
    boolean isSupplierCurrentlyBlocked(@Param("supplierNumber") String supplierNumber);

    /**
     * Busca todos los bloqueos que se solapan con un rango de fechas.
     */
    @Query("SELECT sb FROM SupplierBlock sb WHERE sb.supplierNumber = :supplierNumber " +
           "AND sb.status = 1 AND sb.validFrom <= :endDate AND sb.validTo >= :startDate")
    List<SupplierBlock> findOverlappingBlocks(
            @Param("supplierNumber") String supplierNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
