/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service.cases;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.AttachmentDto;
import com.sodimac.aclaraciones.api.model.entity.Attachment;
import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.model.entity.Request;
import com.sodimac.aclaraciones.api.security.Session;
import java.util.List;

/**
 *
 * @author ggalvan
 */
public interface AttachmentService {

    public int save(AttachmentDto attachment, int requestId, Session session) throws GenericException;

    public List<AttachmentDto> retrieveList(int requestId);

    public AttachmentDto retrieve(int attachmentId);

    public Attachment transform(AttachmentDto attachment, Request request, Author author) throws GenericException;

}
