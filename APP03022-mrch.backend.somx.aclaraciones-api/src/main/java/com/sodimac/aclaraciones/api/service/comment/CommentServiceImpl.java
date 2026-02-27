/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service.comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.CommentDto;
import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.model.entity.Comment;
import com.sodimac.aclaraciones.api.model.entity.Request;
import com.sodimac.aclaraciones.api.repository.AuthorRepository;
import com.sodimac.aclaraciones.api.repository.CommentRepository;
import com.sodimac.aclaraciones.api.repository.RequestRepository;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.CommentService;
import com.sodimac.aclaraciones.api.service.impl.AuthorValidatedService;

/**
 *
 * @author ggalvan
 */
@Component
public class CommentServiceImpl extends AuthorValidatedService implements CommentService {

    private final CommentRepository comments;

    @Autowired
    public CommentServiceImpl(CommentRepository comments, RequestRepository requests, AuthorRepository authors) {
        super(authors, requests);
        this.comments = comments;
    }

    @Override
    @Transactional
    public int save(int requestId, CommentDto comment, Session session) throws GenericException {
        return this.comments
                .save(this.transform(comment, this.validateRequest(requestId), this.validateAuthor(session))).getId();
    }

    @Override
    public List<CommentDto> retrieve(int requestId) {
        List<Comment> persistence = this.comments.findByRequestIdAndRequestActiveAndActive(requestId, true, true);
        List<CommentDto> result = new ArrayList<>();

        if (persistence == null || persistence.isEmpty()) {
            return result;
        }

        persistence.forEach(comment -> result.add(this.transform(comment)));
        return result;
    }

    public CommentDto transform(Comment comment) {
        CommentDto result = new CommentDto();

        result.setId(comment.getId());
        result.setAuthor(comment.getAuthor().getEmail());
        result.setComment(comment.getComment());
        result.setCreationTime(comment.getCreationTime());

        return result;
    }

    public Comment transform(CommentDto comment, Request request, Author author) throws GenericException {

        if (comment == null || comment.getComment() == null || comment.getComment().isEmpty()
                || comment.getComment().length() > 256) {
            throw new GenericException("Invalid `comment` value", HttpStatus.BAD_REQUEST.value());
        }

        Comment result = new Comment();
        result.setActive(true);
        result.setAuthor(author);
        result.setComment(comment.getComment());
        result.setCreationTime(new Date());
        result.setRequest(request);
        result.setStatus(Comment.STATUS_DEFAULT);
        result.setType(Comment.TYPE_DEFAULT);

        return result;
    }
}
