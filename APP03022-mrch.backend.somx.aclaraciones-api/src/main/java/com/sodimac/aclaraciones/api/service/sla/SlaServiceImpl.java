package com.sodimac.aclaraciones.api.service.sla;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.SlaDto;
import com.sodimac.aclaraciones.api.model.entity.Catalog;
import com.sodimac.aclaraciones.api.model.entity.Sla;
import com.sodimac.aclaraciones.api.repository.CatalogRepository;
import com.sodimac.aclaraciones.api.repository.SlaRepository;
import com.sodimac.aclaraciones.api.security.Session;

@Component
public class SlaServiceImpl implements SlaService {

    private final SlaRepository slas;
    private final CatalogRepository catalogs;

    public SlaServiceImpl(@Autowired SlaRepository slas, @Autowired CatalogRepository catalogs) {
        this.slas = slas;
        this.catalogs = catalogs;
    }

    @Override
    public int save(SlaDto sla, Session session) throws GenericException {

        // 🔒 Validar duplicado por módulo
        boolean exists = slas.existsByModule_IdAndActive(
                sla.module(),
                true);

        if (exists) {
            throw new GenericException(
                    "SLA_ALREADY_EXISTS_FOR_MODULE",
                    HttpStatus.CONFLICT.value() // 409 → perfecto para el front
            );
        }

        return this.slas.save(this.transform(sla)).getId();
    }

    @Override
    public int update(int slaId, SlaDto sla, Session session) throws GenericException {
        this.slas.findById(slaId).orElseThrow(() -> {
            return new GenericException("GIVEN id IS INVALID", HttpStatus.BAD_REQUEST.value());
        });

        Sla target = this.transform(sla);
        target.setId(slaId);

        return this.slas.save(target).getId();
    }

    @Override
    public int publish(int slaId, Boolean publishStatus) throws GenericException {
        Sla current = this.slas.findById(slaId).orElseThrow(() -> {
            return new GenericException("GIVEN id IS INVALID", HttpStatus.BAD_REQUEST.value());
        });

        current.setStatus(publishStatus ? Sla.STATUS_PUBLISHED : Sla.STATUS_UNPUBLISHED);
        return this.slas.save(current).getId();
    }

    @Override
    public int delete(int slaId) throws GenericException {
        Sla current = this.slas.findById(slaId).orElseThrow(() -> {
            return new GenericException("GIVEN id IS INVALID", HttpStatus.BAD_REQUEST.value());
        });

        current.setActive(false);
        return this.slas.save(current).getId();
    }

    @Override
    public SlaDto retrieve(int slaId, Session session) {
        return this.transform(this.slas.findById(slaId).orElse(null));
    }

    @Override
    public List<SlaDto> retrieve() {
        List<Sla> current = this.slas.findByActive(true);
        if (current == null) {
            return Collections.emptyList();
        }

        return current.stream().map((sla) -> {
            return transform(sla);
        }).toList();
    }

    private SlaDto transform(Sla sla) {
        if (sla == null) {
            return null;
        }

        return new SlaDto(
                sla.getId(),
                sla.getBusinessUnit().getId(),
                sla.getCountry().getId(),
                sla.getModule().getId(),
                sla.getReason().getId(),
                sla.getPriority().getId(),
                sla.getFirstResponseLevel().getId(),
                sla.getResolutionLevel().getId(),
                sla.getManager(),
                sla.getStatus() == Sla.STATUS_PUBLISHED);
    }

    private Sla transform(SlaDto sla) throws GenericException {
        // VALIDATING CATALOGS
        this.validateCatalog(sla.businessUnit(), "INVALID businessUnit VALUE");
        this.validateCatalog(sla.country(), "INVALID country VALUE");
        this.validateCatalog(sla.module(), "INVALID module VALUE");
        this.validateCatalog(sla.reason(), "INVALID reason VALUE");
        this.validateCatalog(sla.priority(), "INVALID priority VALUE");
        this.validateCatalog(sla.firstResponseLevel(), "INVALID firstResponseLevel VALUE");
        this.validateCatalog(sla.resolutionLevel(), "INVALID resolutionLevel VALUE");

        Sla result = new Sla();
        result.setActive(true);
        result.setBusinessUnit(new Catalog(sla.businessUnit()));
        result.setCountry(new Catalog(sla.country()));
        result.setModule(new Catalog(sla.module()));
        result.setReason(new Catalog(sla.reason()));
        result.setPriority(new Catalog(sla.priority()));
        result.setFirstResponseLevel(new Catalog(sla.firstResponseLevel()));
        result.setResolutionLevel(new Catalog(sla.resolutionLevel()));
        result.setCreationTime(new Date());
        result.setManager(sla.manager());
        result.setStatus(Sla.STATUS_PUBLISHED);
        result.setType(Sla.TYPE_DEFAULT);

        return result;
    }

    private void validateCatalog(int catalogId, String message) throws GenericException {
        Catalog catalog = this.catalogs.findByIdAndActive(catalogId, true);
        if (catalog == null) {
            throw new GenericException(message, HttpStatus.BAD_REQUEST.value());
        }

    }

    @Override
    public String getResponseTimeByModule(int moduleId) throws GenericException {
        Sla sla = this.slas.findFirstByModule_IdAndActiveTrueAndStatus(
                moduleId, Sla.STATUS_PUBLISHED);

        if (sla == null) {
            throw new GenericException("SLA NOT FOUND FOR MODULE", HttpStatus.NOT_FOUND.value());
        }

        Catalog responseLevel = this.catalogs.findByIdAndActive(
                sla.getFirstResponseLevel().getId(), true);

        if (responseLevel == null) {
            throw new GenericException("RESPONSE LEVEL NOT FOUND", HttpStatus.NOT_FOUND.value());
        }

        return responseLevel.getDescription();
    }

}
