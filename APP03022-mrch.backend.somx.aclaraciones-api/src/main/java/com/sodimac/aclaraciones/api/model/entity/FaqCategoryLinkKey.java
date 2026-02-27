package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FaqCategoryLinkKey implements Serializable {
    @Column(name = "faq_id")
    private Long faqId;

    @Column(name = "category_id")
    private Long categoryId;

    public FaqCategoryLinkKey() {
    }

    public FaqCategoryLinkKey(Long faqId, Long categoryId) {
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
        if (!(o instanceof FaqCategoryLinkKey k))
            return false;
        return Objects.equals(faqId, k.faqId) && Objects.equals(categoryId, k.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faqId, categoryId);
    }
}
