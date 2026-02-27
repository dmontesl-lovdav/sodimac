package com.sodimac.aclaraciones.api.security;

import java.io.IOException;
import java.util.Date;

import org.springframework.web.filter.OncePerRequestFilter;

import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.repository.AuthorRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorAutoRegisterFilter extends OncePerRequestFilter {

    private final AuthorRepository authorRepository;

    public AuthorAutoRegisterFilter(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

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
            }
        }

        filterChain.doFilter(request, response);
    }
}
