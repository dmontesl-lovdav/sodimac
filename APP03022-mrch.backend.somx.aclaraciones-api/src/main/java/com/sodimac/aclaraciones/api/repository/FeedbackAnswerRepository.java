package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.FeedbackAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackAnswerRepository extends JpaRepository<FeedbackAnswer, Long> {
}
