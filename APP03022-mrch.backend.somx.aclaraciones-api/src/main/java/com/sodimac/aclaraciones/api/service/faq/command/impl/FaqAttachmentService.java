/*---------------------------------------------------------------------------
 * src/main/java/com/sodimac/aclaraciones/api/service/view/FaqAttachmentService.java
 *---------------------------------------------------------------------------*/
package com.sodimac.aclaraciones.api.service.faq.command.impl;

import com.sodimac.aclaraciones.api.model.entity.FaqAttachment;
import com.sodimac.aclaraciones.api.repository.FaqAttachmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lector sencillo de adjuntos FAQ.
 */
@Service
public class FaqAttachmentService {

    private final FaqAttachmentRepository repo;

    public FaqAttachmentService(FaqAttachmentRepository repo) {
        this.repo = repo;
    }

    /** Devuelve el adjunto o lanza 404. */
    public FaqAttachment findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment " + id + " not found"));
    }
}
