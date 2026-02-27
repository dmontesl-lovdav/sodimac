/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service.impl;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.model.entity.Request;
import com.sodimac.aclaraciones.api.repository.AuthorRepository;
import com.sodimac.aclaraciones.api.repository.RequestRepository;
import com.sodimac.aclaraciones.api.security.Session;
import java.util.Date;
import org.springframework.http.HttpStatus;

/**
 *
 * @author ggalvan
 */
public abstract class AuthorValidatedService {

    private final AuthorRepository authors;
    private final RequestRepository requests;

    public AuthorValidatedService(AuthorRepository authors, RequestRepository requests) {
        this.authors = authors;
        this.requests = requests;
    }

    public Request validateRequest(int requestId) throws GenericException {
        Request request = this.requests.findByIdAndActive(requestId, true);
        if (request == null) {
            throw new GenericException("Invalid `requestId` value", HttpStatus.BAD_REQUEST.value());
        }

        return request;
    }

    public Author validateAuthor(Session session) {
        Author author = this.authors.findByEmail(session.getEmail());

        if (author == null) {
            author = new Author();
            author.setActive(true);
            author.setCreationTime(new Date());
            author.setEmail(session.getEmail());
            author.setName(session.getName());
            author.setStatus(Author.STATUS_DEFAULT);
            author.setType(session.isOperator() ? Author.TYPE_OPERATOR : Author.TYPE_REGULAR);
        } else {
            author.setUpdateTime(new Date());
        }

        return author;
    }

}
