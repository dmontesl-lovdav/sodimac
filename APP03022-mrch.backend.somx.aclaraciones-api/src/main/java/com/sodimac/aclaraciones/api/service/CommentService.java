/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service;

import java.util.List;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.CommentDto;
import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.model.entity.Comment;
import com.sodimac.aclaraciones.api.model.entity.Request;
import com.sodimac.aclaraciones.api.security.Session;

/**
 *
 * @author ggalvan
 */
public interface CommentService {

    public int save(int requestId, CommentDto comment, Session session) throws GenericException;

    public List<CommentDto> retrieve(int requestId);

    public Comment transform(CommentDto comment, Request request, Author author) throws GenericException;

    public CommentDto transform(Comment comment);

}
