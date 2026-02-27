/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service.attachment;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.AttachmentDto;
import com.sodimac.aclaraciones.api.model.entity.Attachment;
import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.model.entity.Request;
import com.sodimac.aclaraciones.api.repository.AttachmentRepository;
import com.sodimac.aclaraciones.api.repository.AuthorRepository;
import com.sodimac.aclaraciones.api.repository.RequestRepository;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.BinaryFileService;
import com.sodimac.aclaraciones.api.service.cases.AttachmentService;
import com.sodimac.aclaraciones.api.service.impl.AuthorValidatedService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ggalvan
 */
@Component
public class AttachmentServiceImpl extends AuthorValidatedService implements AttachmentService {

    private final Logger logger = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachments;
    private final BinaryFileService bfs;

    @Autowired
    public AttachmentServiceImpl(AttachmentRepository attachments, BinaryFileService bfs, AuthorRepository authors, RequestRepository requests) {
        super(authors, requests);
        this.attachments = attachments;
        this.bfs = bfs;
    }

    @Override
    @Transactional
    public int save(AttachmentDto attachment, int requestId, Session session) throws GenericException {
        Author author = this.validateAuthor(session);
        Request request = this.validateRequest(requestId);

        return this.attachments.save(this.transform(attachment, request, author)).getId();
    }

    @Override
    public List<AttachmentDto> retrieveList(int requestId) {
        List<Attachment> persistence = this.attachments.findByRequestIdAndRequestActiveAndActive(requestId, true, true);
        List<AttachmentDto> result = new ArrayList<>();

        if (persistence == null || persistence.isEmpty()) {
            return result;
        }

        persistence.forEach(attachment -> result.add(this.transform(attachment, false)));
        return result;

    }

    @Override
    public AttachmentDto retrieve(int attachmentId) {
        return this.transform(
                this.attachments.findByIdAndRequestActiveAndActive(attachmentId, true, true),
                true);
    }

    private AttachmentDto transform(Attachment attachment, boolean includeBinary) {
        if (attachment == null) {
            return null;
        }

        AttachmentDto result = new AttachmentDto();

        result.setId(attachment.getId());
        result.setAuthor(attachment.getAuthor().getName());
        result.setCreationTime(attachment.getCreationTime());
        result.setName(attachment.getName());

        if (includeBinary) {
            try {
                result.setContent(this.bfs.retrieveBase64Content(attachment.getPath()));
            } catch (Exception e) {
                this.logger.warn("CAN'T RETRIEVE BINARY FILE {}: {} - CAUSE: {}", attachment.getPath(), e.getMessage(), e.getCause());
            }
        }

        return result;
    }

    public Attachment transform(AttachmentDto attachment, Request request, Author author) throws GenericException {

        this.validate(attachment);
        String path = this.bfs.saveBase64Content(attachment.getContent());
        Attachment result = new Attachment();

        result.setActive(true);
        result.setAuthor(author);
        result.setCreationTime(new Date());
        result.setName(attachment.getName());
        result.setPath(path);
        result.setRequest(request);
        result.setStatus(Attachment.STATUS_DEFAULT);
        result.setType(Attachment.TYPE_DEFAULT);

        return result;
    }

    private void validate(AttachmentDto attachment) throws GenericException {
        if (attachment == null) {
            throw new GenericException("Given entity is null", HttpStatus.BAD_REQUEST.value());
        }

        if (attachment.getContent() == null) {
            throw new GenericException("Invalid `content` value", HttpStatus.BAD_REQUEST.value());
        }
        if (attachment.getName() == null) {
            throw new GenericException("Invalid `name` value", HttpStatus.BAD_REQUEST.value());
        }

    }

}
