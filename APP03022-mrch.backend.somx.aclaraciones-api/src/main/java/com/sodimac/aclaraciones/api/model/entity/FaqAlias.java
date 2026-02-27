/* src/main/java/com/sodimac/aclaraciones/api/model/entity/FaqAlias.java */
package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Alias (formas de preguntar) asociados a un FAQ.
 */
@Entity
@Table(name = "FAQ_ALIAS")
public class FaqAlias {

    /* ---------- PK ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- relaciones ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FAQ_ID", nullable = false)
    private Faq faq;

    /* ---------- columnas ---------- */
    @Column(nullable = false, length = 512)
    private String question;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    /* ---------- constructores ---------- */
    /** ctor requerido por JPA */
    protected FaqAlias() {
    }

    public FaqAlias(Faq faq, String question) {
        this.faq = faq;
        this.question = question;
    }

    /* ---------- getters & setters ---------- */
    public Long getId() {
        return id;
    }

    public Faq getFaq() {
        return faq;
    }

    public void setFaq(Faq faq) {
        this.faq = faq;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String q) {
        this.question = q;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean a) {
        this.isActive = a;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
