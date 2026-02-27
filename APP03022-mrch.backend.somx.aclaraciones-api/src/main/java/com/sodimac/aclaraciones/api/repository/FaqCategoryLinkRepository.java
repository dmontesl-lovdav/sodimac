/*---------------------------------------------------------------------------*/
/* src/main/java/com/sodimac/aclaraciones/api/repository/FaqCategoryLinkRepository.java */
/*---------------------------------------------------------------------------*/
package com.sodimac.aclaraciones.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.aclaraciones.api.model.entity.FaqCategoryLink;

public interface FaqCategoryLinkRepository
        extends JpaRepository<FaqCategoryLink, FaqCategoryLink.Key> {

    /** Todos los links de una FAQ (útil para mapear categoryIds). */
    @Query("select l from FaqCategoryLink l where l.id.faqId = :faqId")
    List<FaqCategoryLink> findByFaqId(@Param("faqId") Long faqId);

    /** IDs de categoría para una FAQ (atajo directo). */
    @Query("select l.id.categoryId from FaqCategoryLink l where l.id.faqId = :faqId")
    List<Long> findCategoryIdsByFaqId(@Param("faqId") Long faqId);

    /** ¿Existe el vínculo puntual? */
    boolean existsByFaq_IdAndCategory_Id(Long faqId, Long categoryId);

    /** Borra todos los vínculos de una FAQ. */
    @Modifying
    @Query("delete from FaqCategoryLink l where l.id.faqId = :faqId")
    void deleteAllByFaqId(@Param("faqId") Long faqId);

    /** Borra solo un subconjunto (cuando haces diff). */
    @Modifying
    @Query("delete from FaqCategoryLink l where l.id.faqId = :faqId and l.id.categoryId in (:categoryIds)")
    void deleteByFaqIdAndCategoryIdIn(@Param("faqId") Long faqId,
            @Param("categoryIds") Collection<Long> categoryIds);
}
