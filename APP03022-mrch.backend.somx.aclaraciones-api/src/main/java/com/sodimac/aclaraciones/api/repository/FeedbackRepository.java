package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.Feedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @EntityGraph(attributePaths = "answers")
    List<Feedback> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "answers")
    List<Feedback> findAllByOrderByIdDesc();
}
