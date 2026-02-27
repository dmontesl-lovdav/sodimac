/*────────────────────────────────────────────────────────────
 * src/main/java/com/sodimac/aclaraciones/api/repository/FaqCategoryRepository.java
 *────────────────────────────────────────────────────────────*/
package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaqCategoryRepository extends JpaRepository<FaqCategory, Long> {

    // 🔹 Existente: solo activos
    List<FaqCategory> findByIsActiveTrueOrderByNameAsc();

    // 🔹 Nuevo: solo inactivos
    List<FaqCategory> findByIsActiveFalseOrderByNameAsc();

    // 🔹 Nuevo: todos (activos e inactivos)
    List<FaqCategory> findAllByOrderByNameAsc();

    Optional<FaqCategory> findById(Long id);

    long countByNameIgnoreCase(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FaqCategory c SET c.isActive = :published WHERE c.id = :id")
    int setPublication(@Param("id") Long id, @Param("published") Boolean published);

    Optional<FaqCategory> findByNameIgnoreCase(String name);

    Page<FaqCategory> findByIsActiveTrue(Pageable pageable);

    Page<FaqCategory> findByIsActiveFalse(Pageable pageable);

    Page<FaqCategory> findAll(Pageable pageable);

    @Query("""
                SELECT c
                FROM FaqCategory c
                WHERE LOWER(c.name) LIKE :q
                   OR LOWER(c.description) LIKE :q
            """)
    Page<FaqCategory> searchAll(@Param("q") String q, Pageable pageable);

    @Query("""
                SELECT c
                FROM FaqCategory c
                WHERE c.isActive = true
                  AND (LOWER(c.name) LIKE :q OR LOWER(c.description) LIKE :q)
            """)
    Page<FaqCategory> searchActive(@Param("q") String q, Pageable pageable);

    @Query("""
                SELECT c
                FROM FaqCategory c
                WHERE c.isActive = false
                  AND (LOWER(c.name) LIKE :q OR LOWER(c.description) LIKE :q)
            """)
    Page<FaqCategory> searchInactive(@Param("q") String q, Pageable pageable);

}
