package com.sodimac.aclaraciones.api.service.notice;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.NoticeDto;
import com.sodimac.aclaraciones.api.model.entity.Catalog;
import com.sodimac.aclaraciones.api.model.entity.Notice;
import com.sodimac.aclaraciones.api.repository.CatalogRepository;
import com.sodimac.aclaraciones.api.repository.NoticeRepository;
import com.sodimac.aclaraciones.api.security.Session;

@Component
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository notices;
    private final CatalogRepository catalogs;

    public NoticeServiceImpl(@Autowired NoticeRepository notices, @Autowired CatalogRepository catalogs) {
        this.notices = notices;
        this.catalogs = catalogs;
    }

    @Override
    public int save(NoticeDto notice, Session session) throws GenericException {
        return this.notices.save(this.transform(notice)).getId();
    }

    @Override
    public int update(int noticeId, NoticeDto notice, Session session) throws GenericException {
        this.notices.findById(noticeId).orElseThrow(() -> {
            return new GenericException("GIVEN id IS INVALID", HttpStatus.BAD_REQUEST.value());
        });

        Notice target = this.transform(notice);
        target.setId(noticeId);

        return this.notices.save(target).getId();
    }

    @Override
    public int publish(int noticeId, Boolean publishStatus) throws GenericException {
        Notice current = this.notices.findById(noticeId).orElseThrow(() -> {
            return new GenericException("GIVEN id IS INVALID", HttpStatus.BAD_REQUEST.value());
        });

        current.setStatus(publishStatus ? Notice.STATUS_PUBLISHED : Notice.STATUS_UNPUBLISHED);
        return this.notices.save(current).getId();
    }

    @Override
    public int delete(int noticeId) throws GenericException {
        Notice current = this.notices.findById(noticeId).orElseThrow(() -> {
            return new GenericException("GIVEN id IS INVALID", HttpStatus.BAD_REQUEST.value());
        });

        current.setActive(false);
        return this.notices.save(current).getId();
    }

    @Override
    public NoticeDto retrieve(int noticeId, Session session) {
        return this.transform(this.notices.findById(noticeId).orElse(null));
    }

    @Override
    public List<NoticeDto> retrieve() {
        List<Notice> current = this.notices.findByActive(true);
        if (current == null) {
            return Collections.emptyList();
        }

        return current.stream().map((notice) -> {
            return transform(notice);
        }).toList();
    }

    private NoticeDto transform(Notice notice) {
        if (notice == null) {
            return null;
        }

        return new NoticeDto(notice.getId(), notice.getBusinessUnit().getId(), notice.getCountry().getId(),
                notice.getName(),
                notice.getDescription(), notice.getLink(), notice.getPosition().getId(),
                notice.getStatus() == Notice.STATUS_PUBLISHED);
    }

    private Notice transform(NoticeDto notice) throws GenericException {
        // VALIDATING CATALOGS
        this.validateCatalog(notice.businessUnit(), "INVALID businessUnit VALUE");
        this.validateCatalog(notice.country(), "INVALID country VALUE");
        this.validateCatalog(notice.position(), "INVALID position VALUE");

        Notice result = new Notice();
        result.setActive(true);
        result.setBusinessUnit(new Catalog(notice.businessUnit()));
        result.setCountry(new Catalog(notice.country()));
        result.setCreationTime(new Date());
        result.setDescription(notice.description());
        result.setLink(notice.link());
        result.setName(notice.name());
        result.setPosition(new Catalog(notice.position()));
        result.setStatus(Notice.STATUS_PUBLISHED);
        result.setType(Notice.TYPE_DEFAULT);

        return result;
    }

    private void validateCatalog(int catalogId, String message) throws GenericException {
        Catalog catalog = this.catalogs.findByIdAndActive(catalogId, true);
        if (catalog == null) {
            throw new GenericException(message, HttpStatus.BAD_REQUEST.value());
        }

    }

}
