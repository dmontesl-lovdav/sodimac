package com.sodimac.catman.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.CatalogHeader;

@Repository
public interface CatalogHeaderRepository extends JpaRepository<CatalogHeader, Integer>, JpaSpecificationExecutor<CatalogHeader> {

    Optional<CatalogHeader> findByCode(String code);

    @Query("SELECT h FROM CatalogHeader h WHERE LOWER(h.name) = LOWER(:name)")
    Optional<CatalogHeader> findByName(@Param("name") String name);

    List<CatalogHeader> findByStatus(Integer status);

    List<CatalogHeader> findByModule(String module);

    List<CatalogHeader> findByModuleAndStatus(String module, Integer status);

    Optional<CatalogHeader> findByPrefix(String prefix);

    Optional<CatalogHeader> findByPrefixAndStatus(String prefix, Integer status);

    List<CatalogHeader> findByCatalogTypeAndStatus(String catalogType, Integer status);

    List<CatalogHeader> findByCatalogType(String catalogType);

    Page<CatalogHeader> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM CatalogHeader h " +
           "WHERE LOWER(h.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM CatalogHeader h " +
           "WHERE LOWER(h.name) = LOWER(:name) AND h.id <> :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") Integer excludeId);
}
