package com.sodimac.aclaraciones.api.security;

import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.repository.AuthorRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthorAutoRegisterInterceptor implements HandlerInterceptor {

    private final AuthorRepository authorRepository;

    public AuthorAutoRegisterInterceptor(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        Object s = request.getAttribute("session");
        if (s instanceof Session session && session.getEmail() != null) {

            Author author = authorRepository.findByEmail(session.getEmail());
            if (author == null) {
                Author newAuthor = new Author();
                newAuthor.setEmail(session.getEmail());
                newAuthor.setName(session.getName());
                newAuthor.setActive(true);
                newAuthor.setCreationTime(new Date());

                authorRepository.save(newAuthor);

                System.out.println("✔ Author creado automáticamente: " + session.getEmail());
            }
        }
        return true;
    }
}
