package com.sodimac.aclaraciones.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.aclaraciones.api.model.entity.FaqAttachment;

/** CRUD para faq_attachment. */
public interface FaqAttachmentRepository extends JpaRepository<FaqAttachment, Long> {

        // Nota: navegamos la relación: faq.id -> faq_Id
        List<FaqAttachment> findByFaq_IdAndIsActiveTrue(Long faqId);

        @Query("select coalesce(sum(a.sizeKb), 0) " +
                        "from FaqAttachment a " +
                        "where a.faq.id = :faqId and a.isActive = true")
        long sumSizeKbByFaqId(@Param("faqId") Long faqId);
}
