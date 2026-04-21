package com.sodimac.catman.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.CatalogDetail;

@Repository
public interface CatalogDetailRepository extends JpaRepository<CatalogDetail, Integer>, JpaSpecificationExecutor<CatalogDetail> {

    List<CatalogDetail> findByHeaderIdOrderBySortOrder(Integer headerId);

    List<CatalogDetail> findByHeaderIdAndStatusOrderBySortOrder(Integer headerId, Integer status);

    Optional<CatalogDetail> findByHeaderIdAndKey(Integer headerId, String key);

    @Query("SELECT d FROM CatalogDetail d WHERE d.header.code = :code AND d.status = :status ORDER BY d.sortOrder")
    List<CatalogDetail> findByHeaderCodeAndStatus(@Param("code") String code, @Param("status") Integer status);

    @Query("SELECT d FROM CatalogDetail d WHERE d.header.code = :code ORDER BY d.sortOrder")
    List<CatalogDetail> findByHeaderCode(@Param("code") String code);

    Optional<CatalogDetail> findByKeyAndStatus(String key, Integer status);

    /**
     * Busca detalles activos y vigentes por código de catálogo.
     * Considera status activo Y que la fecha actual esté dentro del rango de vigencia.
     * Si valid_from o valid_to son NULL, se consideran como sin restricción.
     */
    @Query("SELECT d FROM CatalogDetail d WHERE d.header.code = :code " +
           "AND d.status = :status " +
           "AND (d.validFrom IS NULL OR d.validFrom <= :currentDate) " +
           "AND (d.validTo IS NULL OR d.validTo >= :currentDate) " +
           "ORDER BY d.sortOrder")
    List<CatalogDetail> findByHeaderCodeAndStatusAndValidDate(
            @Param("code") String code,
            @Param("status") Integer status,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Busca un detalle específico activo y vigente por clave.
     */
    @Query("SELECT d FROM CatalogDetail d WHERE d.key = :key " +
           "AND d.status = :status " +
           "AND (d.validFrom IS NULL OR d.validFrom <= :currentDate) " +
           "AND (d.validTo IS NULL OR d.validTo >= :currentDate)")
    Optional<CatalogDetail> findByKeyAndStatusAndValidDate(
            @Param("key") String key,
            @Param("status") Integer status,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Busca detalles activos y vigentes por header ID.
     */
    @Query("SELECT d FROM CatalogDetail d WHERE d.header.id = :headerId " +
           "AND d.status = :status " +
           "AND (d.validFrom IS NULL OR d.validFrom <= :currentDate) " +
           "AND (d.validTo IS NULL OR d.validTo >= :currentDate) " +
           "ORDER BY d.sortOrder")
    List<CatalogDetail> findByHeaderIdAndStatusAndValidDate(
            @Param("headerId") Integer headerId,
            @Param("status") Integer status,
            @Param("currentDate") LocalDate currentDate);

    Page<CatalogDetail> findByHeaderId(Integer headerId, Pageable pageable);

    List<CatalogDetail> findByHeaderIdAndStatus(Integer headerId, Integer status);

    boolean existsByHeaderIdAndKeyIgnoreCase(Integer headerId, String key);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM CatalogDetail d WHERE LOWER(d.key) = LOWER(:key)")
    boolean existsByKeyIgnoreCase(@Param("key") String key);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM CatalogDetail d " +
           "WHERE d.header.id = :headerId AND LOWER(d.key) = LOWER(:key) AND d.id <> :excludeId")
    boolean existsByHeaderIdAndKeyIgnoreCaseAndIdNot(
            @Param("headerId") Integer headerId,
            @Param("key") String key,
            @Param("excludeId") Integer excludeId);

    @Query("SELECT d FROM CatalogDetail d WHERE d.parentCatalogId = :parentCatalogId AND d.status = :status ORDER BY d.sortOrder")
    List<CatalogDetail> findByParentCatalogIdAndStatus(
            @Param("parentCatalogId") Integer parentCatalogId,
            @Param("status") Integer status);

    @Query("SELECT d FROM CatalogDetail d WHERE d.parentElementId = :parentElementId AND d.status = :status ORDER BY d.sortOrder")
    List<CatalogDetail> findByParentElementIdAndStatus(
            @Param("parentElementId") Integer parentElementId,
            @Param("status") Integer status);

    Long countByHeaderId(Integer headerId);

    @Query("SELECT MAX(d.key) FROM CatalogDetail d WHERE d.header.id = :headerId")
    String findMaxKeyByHeaderId(@Param("headerId") Integer headerId);
}
