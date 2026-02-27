package com.sodimac.aclaraciones.api.model.dto.feedback;

public class FeedbackAnswerDto {
    private Long id;
    private String text;
    private Integer position;

    public FeedbackAnswerDto() {
    }

    public FeedbackAnswerDto(Long id, String text, Integer position) {
        this.id = id;
        this.text = text;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
