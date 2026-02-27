package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa la tabla public.faq.
 */
@Entity
@Table(name = "faq")
public class Faq {

    /* ---------- PK ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- columnas ---------- */
    @Column(nullable = false, length = 512)
    private String question;

    @Column(nullable = false, length = 1024)
    private String answer;

    /** Contador de vistas — int4 ⇒ Integer */
    @Column(nullable = false)
    private Integer views = 0;

    // ⇨ 1) CAMBIA el nombre a `active`, usa tipo primitivo boolean
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* ---------- relaciones ---------- */

    /** Categoría principal (n:1) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FaqCategory category;

    /** Formas alternativas de preguntar (1:n) */
    @OneToMany(mappedBy = "faq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaqAlias> aliases = new ArrayList<>();

    /** Archivos adjuntos (1:n) */
    @OneToMany(mappedBy = "faq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaqAttachment> attachments = new ArrayList<>();

    /** FAQs relacionadas (n:m simétrico) */
    @ManyToMany
    @JoinTable(name = "faq_related", joinColumns = @JoinColumn(name = "faq_id"), inverseJoinColumns = @JoinColumn(name = "related_id"))
    private List<Faq> related = new ArrayList<>();

    /* ---------- getters & setters ---------- */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String q) {
        this.question = q;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String a) {
        this.answer = a;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer v) {
        this.views = v;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public FaqCategory getCategory() {
        return category;
    }

    public void setCategory(FaqCategory c) {
        this.category = c;
    }

    public List<FaqAlias> getAliases() {
        return aliases;
    }

    public void setAliases(List<FaqAlias> aliases) {
        this.aliases = aliases;
    }

    public List<FaqAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FaqAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<Faq> getRelated() {
        return related;
    }

    public void setRelated(List<Faq> related) {
        this.related = related;
    }
}
