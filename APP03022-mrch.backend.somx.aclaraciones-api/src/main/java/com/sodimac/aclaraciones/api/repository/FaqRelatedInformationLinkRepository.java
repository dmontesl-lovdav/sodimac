/*---------------------------------------------------------------------------*/
/* src/main/java/com/sodimac/aclaraciones/api/repository/FaqRelatedInformationLinkRepository.java */
/*---------------------------------------------------------------------------*/
package com.sodimac.aclaraciones.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.aclaraciones.api.model.entity.FaqRelatedInformationLink;
import com.sodimac.aclaraciones.api.model.entity.FaqRelatedInformationLinkKey;

public interface FaqRelatedInformationLinkRepository
        extends JpaRepository<FaqRelatedInformationLink, FaqRelatedInformationLinkKey> {

    /** Todos los links de una FAQ. */
    @Query("select l from FaqRelatedInformationLink l where l.id.faqId = :faqId")
    List<FaqRelatedInformationLink> findByFaqId(@Param("faqId") Long faqId);

    /** IDs de related-information para una FAQ. */
    @Query("select l.id.relatedInformationId from FaqRelatedInformationLink l where l.id.faqId = :faqId")
    List<Long> findRelatedInformationIdsByFaqId(@Param("faqId") Long faqId);

    /** ¿Existe el vínculo puntual? */
    boolean existsByFaq_IdAndRelatedInformation_Id(Long faqId, Long relatedInformationId);

    /** Borra todos los vínculos de una FAQ. */
    @Modifying
    @Query("delete from FaqRelatedInformationLink l where l.id.faqId = :faqId")
    void deleteAllByFaqId(@Param("faqId") Long faqId);

    /** Borra solo un subconjunto (cuando haces diff). */
    @Modifying
    @Query("delete from FaqRelatedInformationLink l where l.id.faqId = :faqId and l.id.relatedInformationId in (:ids)")
    void deleteByFaqIdAndRelatedInformationIdIn(@Param("faqId") Long faqId,
            @Param("ids") Collection<Long> relatedInfoIds);
}
