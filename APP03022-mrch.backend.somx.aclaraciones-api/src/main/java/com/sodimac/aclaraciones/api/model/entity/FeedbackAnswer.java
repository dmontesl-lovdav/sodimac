package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback_answer", schema = "public", indexes = {
        @Index(name = "ix_feedback_answer_feedback", columnList = "feedback_id") })
public class FeedbackAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feedback_answer_feedback"))
    private Feedback feedback;

    @Column(nullable = false, length = 200)
    private String text;

    @Column(nullable = false)
    private Integer position = 0;

    @PrePersist
    public void prePersist() {
        if (position == null)
            position = 0;
    }

    /* getters/setters */
    public Long getId() {
        return id;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
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
