package com.sodimac.aclaraciones.api.service.feedback.command;

import com.sodimac.aclaraciones.api.model.dto.feedback.FeedbackAnswerDto;
import com.sodimac.aclaraciones.api.model.dto.feedback.FeedbackDto;
import com.sodimac.aclaraciones.api.model.entity.Feedback;
import com.sodimac.aclaraciones.api.model.entity.FeedbackAnswer;
import com.sodimac.aclaraciones.api.repository.FeedbackRepository;
import com.sodimac.aclaraciones.api.security.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackCommandService {

    private final FeedbackRepository repo;

    public FeedbackCommandService(FeedbackRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public FeedbackDto create(FeedbackDto body, Session session) {
        String actor = actorOf(session);
        Feedback f = new Feedback();
        f.setQuestion(trim(body.getQuestion()));
        f.setIsActive(Boolean.TRUE.equals(body.getIsActive()));
        f.setCreatedBy(actor);
        f.setUpdatedBy(actor);

        applyAnswers(f, body.getAnswers());
        return toDto(repo.save(f));
    }

    @Transactional
    public FeedbackDto update(Long id, FeedbackDto body, Session session) {
        String actor = actorOf(session);
        Feedback f = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));

        if (body.getQuestion() != null)
            f.setQuestion(trim(body.getQuestion()));

        f.clearAnswers();
        applyAnswers(f, body.getAnswers());

        f.setUpdatedBy(actor);
        return toDto(repo.save(f));
    }

    @Transactional
    public FeedbackDto updatePublication(Long id, boolean publish, Session session) {
        String actor = actorOf(session);
        Feedback f = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));
        f.setIsActive(publish);
        f.setUpdatedBy(actor);
        return toDto(repo.save(f));
    }

    @Transactional
    public void delete(Long id) {
        Feedback f = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));
        repo.delete(f);
    }

    /* ---------- helpers ---------- */
    private void applyAnswers(Feedback f, List<FeedbackAnswerDto> incoming) {
        if (incoming == null)
            return;
        int pos = 0;
        for (FeedbackAnswerDto dto : incoming) {
            String t = trim(dto.getText());
            if (t == null || t.isEmpty())
                continue;
            FeedbackAnswer a = new FeedbackAnswer();
            a.setText(t);
            a.setPosition(dto.getPosition() != null ? dto.getPosition() : pos++);
            f.addAnswer(a);
        }
    }

    private FeedbackDto toDto(Feedback f) {
        FeedbackDto dto = new FeedbackDto();
        dto.setId(f.getId());
        dto.setQuestion(f.getQuestion());
        dto.setIsActive(f.getIsActive());

        List<FeedbackAnswerDto> ans = new ArrayList<>();
        if (f.getAnswers() != null) {
            for (FeedbackAnswer a : f.getAnswers()) {
                ans.add(new FeedbackAnswerDto(a.getId(), a.getText(), a.getPosition()));
            }
        }
        dto.setAnswers(ans);
        return dto;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * Obtiene el actor desde Session por reflexión: getUserId / getUsername /
     * getUser / getEmail
     */
    private String actorOf(Session s) {
        if (s == null)
            return "system";
        for (String m : new String[] { "getUserId", "getUsername", "getUser", "getEmail" }) {
            try {
                Method mm = s.getClass().getMethod(m);
                Object v = mm.invoke(s);
                if (v != null)
                    return String.valueOf(v);
            } catch (Exception ignored) {
            }
        }
        return "system";
    }
}
