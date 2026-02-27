package com.sodimac.aclaraciones.api.model.dto.feedback;

import java.util.ArrayList;
import java.util.List;

/* Nota: no exponemos createdBy/updatedBy al front por ahora */
public class FeedbackDto {
    private Long id;
    private String question;
    private Boolean isActive;
    private List<FeedbackAnswerDto> answers = new ArrayList<>();

    public FeedbackDto() {
    }

    public FeedbackDto(Long id, String question, Boolean isActive, List<FeedbackAnswerDto> answers) {
        this.id = id;
        this.question = question;
        this.isActive = isActive;
        if (answers != null)
            this.answers = answers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<FeedbackAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<FeedbackAnswerDto> answers) {
        this.answers = answers;
    }
}
