/*---------------------------------------------------------------------------*/
/* src/main/java/com/sodimac/aclaraciones/api/repository/FaqRepository.java  */
/*---------------------------------------------------------------------------*/
package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.dto.view.FaqView;
import com.sodimac.aclaraciones.api.model.entity.Faq;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FaqRepository extends JpaRepository<Faq, Long> {

        /* ---------- listado filtrado (incluye publicadas y NO publicadas) --- */
        @Query("""
                        SELECT new com.sodimac.aclaraciones.api.model.dto.view.FaqView(
                                   f.id,
                                   f.question,
                                   f.answer,
                                   c.id,
                                   c.description,
                                   c.name,
                                   f.active,
                                   f.views )
                          FROM Faq f
                          JOIN f.category c
                         WHERE c.isActive = true
                           AND (:catId IS NULL OR c.id = :catId)
                           AND (
                                :term IS NULL OR
                                LOWER(f.question) LIKE LOWER(CONCAT('%', :term, '%')) OR
                                LOWER(f.answer)   LIKE LOWER(CONCAT('%', :term, '%'))
                           )
                        ORDER BY
                             CASE WHEN :popular = true THEN f.views END DESC,
                             f.updatedAt DESC
                        """)
        List<FaqView> search(@Param("catId") Long catId,
                        @Param("term") String term,
                        @Param("popular") Boolean popular,
                        Pageable pageable);

        /* ---------- FAQ individual (sin filtrar por active) ----------------- */
        @Query("""
                        SELECT new com.sodimac.aclaraciones.api.model.dto.view.FaqView(
                                   f.id,
                                   f.question,
                                   f.answer,
                                   c.id,
                                   c.description,
                                   c.name,
                                   f.active,
                                   f.views )
                          FROM Faq f
                          JOIN f.category c
                         WHERE f.id = :id
                        """)
        Optional<FaqView> findViewById(@Param("id") Long id);

        /* ---------- publicar / despublicar ---------------------------------- */
        @Modifying
        @Query("UPDATE Faq f SET f.active = :published WHERE f.id = :id")
        int setPublication(@Param("id") Long id,
                        @Param("published") Boolean published);
}
