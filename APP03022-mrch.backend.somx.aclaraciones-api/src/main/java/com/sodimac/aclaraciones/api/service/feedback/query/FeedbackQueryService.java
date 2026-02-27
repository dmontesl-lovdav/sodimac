package com.sodimac.aclaraciones.api.service.feedback.query;

import com.sodimac.aclaraciones.api.model.dto.feedback.FeedbackAnswerDto;
import com.sodimac.aclaraciones.api.model.dto.feedback.FeedbackDto;
import com.sodimac.aclaraciones.api.model.entity.Feedback;
import com.sodimac.aclaraciones.api.model.entity.FeedbackAnswer;
import com.sodimac.aclaraciones.api.repository.FeedbackRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackQueryService {

    private final FeedbackRepository repo;

    public FeedbackQueryService(FeedbackRepository repo) {
        this.repo = repo;
    }

    public List<FeedbackDto> list(Integer size) {
        List<Feedback> entities = (size != null && size > 0)
                ? repo.findAllByOrderByIdDesc(PageRequest.of(0, size))
                : repo.findAllByOrderByIdDesc();

        List<FeedbackDto> out = new ArrayList<>();
        for (Feedback f : entities)
            out.add(toDto(f, true));
        return out;
    }

    public FeedbackDto findById(Long id) {
        Feedback f = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));
        return toDto(f, true);
    }

    private FeedbackDto toDto(Feedback f, boolean includeAnswers) {
        FeedbackDto dto = new FeedbackDto();
        dto.setId(f.getId());
        dto.setQuestion(f.getQuestion());
        dto.setIsActive(f.getIsActive());

        if (includeAnswers && f.getAnswers() != null) {
            List<FeedbackAnswerDto> ans = new ArrayList<>();
            for (FeedbackAnswer a : f.getAnswers()) {
                ans.add(new FeedbackAnswerDto(a.getId(), a.getText(), a.getPosition()));
            }
            dto.setAnswers(ans);
        }
        return dto;
    }
}
