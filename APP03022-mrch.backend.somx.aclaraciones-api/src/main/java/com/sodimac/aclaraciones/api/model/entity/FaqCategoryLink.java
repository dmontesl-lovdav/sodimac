package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Vínculo muchos-a-muchos entre FAQ y FaqCategory.
 * Permite asignar varias categorías a una misma FAQ.
 */
@Entity
@Table(name = "faq_category_link")
public class FaqCategoryLink {

    @EmbeddedId
    private Key id;

    @MapsId("faqId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faq_id", nullable = false)
    private Faq faq;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FaqCategory category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected FaqCategoryLink() {
        // JPA
    }

    public FaqCategoryLink(Faq faq, FaqCategory category) {
        this.faq = faq;
        this.category = category;
        this.id = new Key(faq.getId(), category.getId());
    }

    public Key getId() {
        return id;
    }

    public Faq getFaq() {
        return faq;
    }

    public FaqCategory getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /* ---------- Clave compuesta ---------- */
    @Embeddable
    public static class Key implements Serializable {
        @Column(name = "faq_id")
        private Long faqId;

        @Column(name = "category_id")
        private Long categoryId;

        protected Key() {
            // JPA
        }

        public Key(Long faqId, Long categoryId) {
            this.faqId = faqId;
            this.categoryId = categoryId;
        }

        public Long getFaqId() {
            return faqId;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Key))
                return false;
            Key key = (Key) o;
            return Objects.equals(faqId, key.faqId) &&
                    Objects.equals(categoryId, key.categoryId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(faqId, categoryId);
        }
    }
}
