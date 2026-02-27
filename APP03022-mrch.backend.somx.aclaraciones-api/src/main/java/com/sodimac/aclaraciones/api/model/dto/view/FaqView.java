/*───────────────────────────────────────────────────────────
 * src/main/java/com/sodimac/aclaraciones/api/model/dto/view/FaqView.java
 *───────────────────────────────────────────────────────────*/
package com.sodimac.aclaraciones.api.model.dto.view;

/**
 * Proyección ligera usada en el grid de FAQ.
 * published = f.active
 * moduleName = category.name
 * categoryName = category.description
 */
public class FaqView {

    /* ---------- campos ---------- */
    private final Long id;
    private final String question;
    private final String answer;
    private final Long categoryId;
    private final String categoryName; // description
    private final String moduleName; // ← NUEVO: category.name
    private final Boolean published;
    private final Integer views;

    /* ---------- constructor principal (wrappers) ---------- */
    public FaqView(Long id,
            String question,
            String answer,
            Long categoryId,
            String categoryName,
            String moduleName, // ← NUEVO
            Boolean published,
            Integer views) {

        this.id = id;
        this.question = question;
        this.answer = answer;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.moduleName = moduleName; // ← NUEVO
        this.published = published;
        this.views = views;
    }

    /* ---------- constructor alterno (primitivos) ---------- */
    public FaqView(long id,
            String question,
            String answer,
            long categoryId,
            String categoryName,
            String moduleName, // ← NUEVO
            boolean published,
            int views) {
        this(Long.valueOf(id),
                question,
                answer,
                Long.valueOf(categoryId),
                categoryName,
                moduleName,
                Boolean.valueOf(published),
                Integer.valueOf(views));
    }

    /* ---------- getters ---------- */
    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getModuleName() {
        return moduleName;
    } // ← NUEVO

    public Boolean getPublished() {
        return published;
    }

    public Integer getViews() {
        return views;
    }
}
