package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Vínculo muchos-a-muchos entre FAQ y RelatedInformation.
 * Permite asociar varias "Información relacionada" a una misma FAQ.
 */
@Entity
@Table(name = "faq_related_information_link")
public class FaqRelatedInformationLink {

    @EmbeddedId
    private Key id;

    @MapsId("faqId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faq_id", nullable = false)
    private Faq faq;

    @MapsId("relatedInformationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "related_information_id", nullable = false)
    private RelatedInformation relatedInformation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected FaqRelatedInformationLink() {
        /* JPA */
    }

    public FaqRelatedInformationLink(Faq faq, RelatedInformation relatedInformation) {
        this.faq = faq;
        this.relatedInformation = relatedInformation;
        this.id = new Key(faq.getId(), relatedInformation.getId());
    }

    public Key getId() {
        return id;
    }

    public Faq getFaq() {
        return faq;
    }

    public RelatedInformation getRelatedInformation() {
        return relatedInformation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /* ===================== PK compuesta ===================== */
    @Embeddable
    public static class Key implements Serializable {
        @Column(name = "faq_id")
        private Long faqId;

        @Column(name = "related_information_id")
        private Long relatedInformationId;

        protected Key() {
            /* JPA */ }

        public Key(Long faqId, Long relatedInformationId) {
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
            if (!(o instanceof Key key))
                return false;
            return Objects.equals(faqId, key.faqId)
                    && Objects.equals(relatedInformationId, key.relatedInformationId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(faqId, relatedInformationId);
        }
    }
}
