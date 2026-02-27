package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta para la tabla faq_related_information_link.
 * Representa la relación FAQ ↔ RelatedInformation.
 */
@Embeddable
public class FaqRelatedInformationLinkKey implements Serializable {

    @Column(name = "faq_id")
    private Long faqId;

    @Column(name = "related_information_id")
    private Long relatedInformationId;

    protected FaqRelatedInformationLinkKey() {
        // JPA
    }

    public FaqRelatedInformationLinkKey(Long faqId, Long relatedInformationId) {
        this.faqId = faqId;
        this.relatedInformationId = relatedInformationId;
    }

    public Long getFaqId() {
        return faqId;
    }

    public Long getRelatedInformationId() {
        return relatedInformationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FaqRelatedInformationLinkKey))
            return false;
        FaqRelatedInformationLinkKey key = (FaqRelatedInformationLinkKey) o;
        return Objects.equals(faqId, key.faqId) &&
                Objects.equals(relatedInformationId, key.relatedInformationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faqId, relatedInformationId);
    }
}
