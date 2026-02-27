/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service.cases.command.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.AttachmentDto;
import com.sodimac.aclaraciones.api.model.dto.CommentDto;
import com.sodimac.aclaraciones.api.model.dto.RequestDto;
import com.sodimac.aclaraciones.api.model.entity.Attachment;
import com.sodimac.aclaraciones.api.model.entity.Author;
import com.sodimac.aclaraciones.api.model.entity.Catalog;
import com.sodimac.aclaraciones.api.model.entity.Company;
import com.sodimac.aclaraciones.api.model.entity.Request;
import com.sodimac.aclaraciones.api.repository.AuthorRepository;
import com.sodimac.aclaraciones.api.repository.CatalogRepository;
import com.sodimac.aclaraciones.api.repository.RequestRepository;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.CommentService;
import com.sodimac.aclaraciones.api.service.cases.AttachmentService;
import com.sodimac.aclaraciones.api.service.cases.RequestService;
import com.sodimac.aclaraciones.api.service.impl.AuthorValidatedService;

/**
 *
 * @author ggalvan
 */
@Service
public class RequestServiceImpl extends AuthorValidatedService implements RequestService {

    private final RequestRepository requests;
    private final CatalogRepository catalogs;
    private final AttachmentService attachments;
    private final CommentService comments;
    private final long maximumTimeThreshold;

    public RequestServiceImpl(
            @Autowired AuthorRepository authors,
            @Autowired RequestRepository requests,
            @Autowired CatalogRepository catalog,
            @Autowired AttachmentService attachments,
            @Autowired CommentService comments,
            @Value("${aclaraciones.requests.maximumTimeThreshold:864000000}") long maximumTimeThreshold) {
        super(authors, requests);
        this.requests = requests;
        this.catalogs = catalog;
        this.attachments = attachments;
        this.comments = comments;
        this.maximumTimeThreshold = maximumTimeThreshold;
    }

    @Override
    @Transactional
    public int save(RequestDto request, Session session) throws GenericException {
        return this.requests.save(this.transform(request, this.validateAuthor(session))).getId();
    }

    @Override
    @Transactional
    public int configure(int requestId, RequestDto request, Session session) throws GenericException {

        // 🔹 Asegura que el author exista (auto-alta si no)
        this.validateAuthor(session);

        Request persistence = this.requests.findByIdAndActive(requestId, true);
        if (persistence == null) {
            return 0;
        }
        return this.requests.save(this.merge(persistence, request, session)).getId();
    }

    @Override
    public RequestDto retrieve(int requestId, Session session) {

        // 🔹 Asegura que el author exista antes de permitir acciones posteriores
        this.validateAuthor(session);

        return this.transform(this.requests.findByIdAndActive(requestId, true));
    }

    /*
     * ------------------------------------------------------------------
     * FIRMA ANTIGUA (de la interfaz) → delega a la nueva pasando role = null
     * ------------------------------------------------------------------
     */
    @Override
    public List<RequestDto> retrieve(String criteria, Date dateFrom, Date dateTo, Integer reason, Integer clazz,
            Session session) {
        List<Request> persistence = this.retrieveHelper(criteria, dateFrom, dateTo, reason, clazz, null, session);
        List<RequestDto> result = new ArrayList<>();
        if (persistence == null || persistence.isEmpty()) {
            return result;
        }
        persistence.forEach(r -> result.add(this.transform(r)));
        return result;
    }

    /*
     * ------------------------------------------------------------------
     * NUEVA SOBRECARGA con role (para que el controller la use ya mismo)
     * ------------------------------------------------------------------
     */
    public List<RequestDto> retrieve(String criteria, Date dateFrom, Date dateTo, Integer reason, Integer clazz,
            String role, Session session) {
        List<Request> persistence = this.retrieveHelper(criteria, dateFrom, dateTo, reason, clazz, role, session);
        List<RequestDto> result = new ArrayList<>();
        if (persistence == null || persistence.isEmpty()) {
            return result;
        }
        persistence.forEach(r -> result.add(this.transform(r)));
        return result;
    }

    // role: ppsOMX-admin | ppsOMX-resolver → todos; ppsOMX-vendor (o null) → solo
    // sus casos
    private List<Request> retrieveHelper(String criteria, Date dateFrom, Date dateTo, Integer reason, Integer clazz,
            String role, Session session) {

        // Validamos autor para requester/email
        Author author = this.validateAuthor(session);

        int pageSize = (dateFrom != null || dateTo != null) ? Integer.MAX_VALUE : 10;

        Date lowerTimeThreshold = (dateFrom != null)
                ? dateFrom
                : new Date(System.currentTimeMillis() - this.maximumTimeThreshold);

        Date upperTimeThreshold = (dateTo != null)
                ? dateTo
                : new Date();

        List<Request> result;

        final String r = role == null ? "" : role.trim();
        final boolean isAdminOrResolver = "ppsOMX-admin".equalsIgnoreCase(r) || "ppsOMX-resolver".equalsIgnoreCase(r);
        final boolean isVendor = "ppsOMX-vendor".equalsIgnoreCase(r) || !isAdminOrResolver; // fallback vendor si no
                                                                                            // mandan role

        if (isAdminOrResolver) {
            // TODOS los casos (global)
            if (criteria != null && !criteria.isBlank()) {
                result = this.requests.findByCriteriaGlobal(
                        criteria.matches("[0-9]+") ? Integer.parseInt(criteria) : 0,
                        '%' + criteria + '%',
                        true,
                        lowerTimeThreshold,
                        upperTimeThreshold);
            } else {
                result = this.requests.findByActiveAndCreationTimeBetweenGlobal(
                        true,
                        lowerTimeThreshold,
                        upperTimeThreshold,
                        PageRequest.of(0, pageSize, Sort.by(Direction.DESC, "creationTime")));
            }
        } else if (isVendor) {
            // Solo sus casos (por email)
            if (criteria != null && !criteria.isBlank()) {
                result = this.requests.findByCriteria(
                        criteria.matches("[0-9]+") ? Integer.parseInt(criteria) : 0,
                        '%' + criteria + '%',
                        true,
                        lowerTimeThreshold,
                        upperTimeThreshold,
                        session.getEmail());
            } else {
                result = this.requests.findByActiveAndCreationTimeGreaterThanAndCreationTimeLessThanAndRequesterEmail(
                        true,
                        lowerTimeThreshold,
                        upperTimeThreshold,
                        session.getEmail(),
                        PageRequest.of(0, pageSize, Sort.by(Direction.DESC, "creationTime")));
            }
        } else {
            result = List.of();
        }

        if (result == null) {
            return null;
        }

        if (reason != null) {
            result = result.stream().filter(item -> item.getReason().getId() == reason.intValue()).toList();
        }
        if (clazz != null) {
            result = result.stream()
                    .filter(item -> item.getClazz() != null && item.getClazz().getId() == clazz.intValue())
                    .toList();
        }
        return result;
    }

    private Request merge(Request persistence, RequestDto request, Session session) throws GenericException {
        this.validateOnConfig(request);

        // actualizar módulo si viene en el RequestDto
        if (request.getModule() != null) {
            persistence.setModule(new Catalog(request.getModule()));
        }

        if (request.getResponsible() != null) {
            persistence.setResponsible(request.getResponsible());
        }

        if (request.getNombreProveedor() != null) {
            persistence.setNombreProveedor(request.getNombreProveedor());
        }

        persistence.setClazz(request.getClazz() != null ? new Catalog(request.getClazz()) : null);
        persistence.setProgress(request.getProgress() != null ? new Catalog(request.getProgress()) : null);
        persistence.setPriority(request.getPriority() != null ? new Catalog(request.getPriority()) : null);
        persistence.setRepeatitiveness(
                request.getRepeatitiveness() != null ? new Catalog(request.getRepeatitiveness()) : null);

        // MERGING ATTACHMENTS
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (AttachmentDto attachment : request.getAttachments()) {
                this.attachments.save(attachment, persistence.getId(), session);
            }
        }

        // MERGING COMMENTS
        if (request.getComments() != null && !request.getComments().isEmpty()) {
            for (CommentDto comment : request.getComments()) {
                this.comments.save(persistence.getId(), comment, session);
            }
        }

        persistence.setUpdateTime(new Date());
        return persistence;
    }

    private Request transform(RequestDto request, Author requester) throws GenericException {
        this.validate(request);
        Request result = new Request();

        result.setActive(true);
        result.setCreationTime(new Date());
        result.setDescription(request.getDescription());
        result.setDetail(request.getDetail() != null ? new Catalog(request.getDetail()) : null);
        result.setOrder(request.getOrderId());
        result.setReason(new Catalog(request.getReason()));
        result.setModule(new Catalog(request.getModule()));
        result.setRequester(requester);
        result.setResponsible(request.getResponsible());
        result.setNombreProveedor(request.getNombreProveedor());
        result.setStatus(Request.STATUS_UNATTENDED);
        result.setType(Request.TYPE_DEFAULT);
        //
        result.setClazz(request.getClazz() != null ? new Catalog(request.getClazz()) : null);
        result.setProgress(request.getProgress() != null ? new Catalog(request.getProgress()) : null);
        result.setPriority(request.getPriority() != null ? new Catalog(request.getPriority()) : null);
        result.setRepeatitiveness(
                request.getRepeatitiveness() != null ? new Catalog(request.getRepeatitiveness()) : null);
        //
        Company company = new Company();
        company.setActive(true);
        company.setBusinessUnit(new Catalog(request.getBusinessUnit()));
        company.setCountry(new Catalog(request.getCountry()));
        company.setCreationTime(result.getCreationTime());
        company.setName(request.getCompany());
        company.setRut(request.getRut());
        company.setStatus(Company.STATUS_DEFAULT);
        company.setType(Company.TYPE_DEFAULT);
        result.setCompany(company);
        //
        if (request.getAttachments() != null) {
            List<Attachment> attachments = new ArrayList<>();
            for (AttachmentDto attachment : request.getAttachments()) {
                attachments.add(this.attachments.transform(attachment, result, requester));
            }
            result.setAttachments(attachments);
        }

        return result;
    }

    private RequestDto transform(Request persistence) {
        if (persistence == null) {
            return null;
        }

        RequestDto result = new RequestDto();

        result.setCreationTime(persistence.getCreationTime());
        result.setDescription(persistence.getDescription());
        result.setDetail(persistence.getDetail() != null ? persistence.getDetail().getId() : null);
        result.setElapsedTime(
                Long.valueOf(
                        (System.currentTimeMillis() - persistence.getCreationTime().getTime())
                                / (86400000l))
                        .intValue());
        result.setId(persistence.getId());
        result.setResponsible(persistence.getResponsible());
        result.setNombreProveedor(persistence.getNombreProveedor());
        result.setOperator(persistence.getOperator() != null ? persistence.getOperator().getEmail() : null);
        result.setOrderId(persistence.getOrderId());
        result.setReason(persistence.getReason().getId());
        result.setRequester(persistence.getRequester().getEmail());
        result.setStatus(persistence.getStatus());
        result.setModule(persistence.getModule().getId());

        result.setCompany(persistence.getCompany().getName());
        result.setRut(persistence.getCompany().getRut());
        result.setBusinessUnit(persistence.getCompany().getBusinessUnit().getId());
        result.setCountry(persistence.getCompany().getCountry().getId());

        result.setClazz(persistence.getClazz() != null ? persistence.getClazz().getId() : null);
        result.setProgress(persistence.getProgress() != null ? persistence.getProgress().getId() : null);
        result.setPriority(persistence.getPriority() != null ? persistence.getPriority().getId() : null);
        result.setRepeatitiveness(
                persistence.getRepeatitiveness() != null ? persistence.getRepeatitiveness().getId() : null);

        return result;
    }

    private void validate(RequestDto request) throws GenericException {
        if (request == null) {
            throw new GenericException("Given entity is null", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getDescription() == null) {
            throw new GenericException("Invalid `description` value", HttpStatus.BAD_REQUEST.value());
        }
        if (request.getOrderId() == null) {
            throw new GenericException("Invalid `orderId` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getRut() == null) {
            throw new GenericException("Invalid `rut` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getBusinessUnit() == null
                || this.catalogs.findByIdAndActive(request.getBusinessUnit(), true) == null) {
            throw new GenericException("Invalid `businessUnit` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getCountry() == null || this.catalogs.findByIdAndActive(request.getCountry(), true) == null) {
            throw new GenericException("Invalid `country` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getModule() == null || this.catalogs.findByIdAndActive(request.getModule(), true) == null) {
            throw new GenericException("Invalid `module` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getReason() == null || this.catalogs.findByIdAndActive(request.getReason(), true) == null) {
            throw new GenericException("Invalid `reason` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getDetail() != null && this.catalogs.findByIdAndActive(request.getDetail(), true) == null) {
            throw new GenericException("Invalid `detail` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getClazz() != null && this.catalogs.findByIdAndActive(request.getClazz(), true) == null) {
            throw new GenericException("Invalid `clazz` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getPriority() != null && this.catalogs.findByIdAndActive(request.getPriority(), true) == null) {
            throw new GenericException("Invalid `priority` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getProgress() != null && this.catalogs.findByIdAndActive(request.getProgress(), true) == null) {
            throw new GenericException("Invalid `progress` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getRepeatitiveness() != null
                && this.catalogs.findByIdAndActive(request.getRepeatitiveness(), true) == null) {
            throw new GenericException("Invalid `repeatitiveness` value", HttpStatus.BAD_REQUEST.value());
        }
    }

    private void validateOnConfig(RequestDto request) throws GenericException {
        if (request == null) {
            throw new GenericException("Given entity is null", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getClazz() != null && this.catalogs.findByIdAndActive(request.getClazz(), true) == null) {
            throw new GenericException("Invalid `clazz` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getPriority() != null && this.catalogs.findByIdAndActive(request.getPriority(), true) == null) {
            throw new GenericException("Invalid `priority` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getProgress() != null && this.catalogs.findByIdAndActive(request.getProgress(), true) == null) {
            throw new GenericException("Invalid `progress` value", HttpStatus.BAD_REQUEST.value());
        }

        if (request.getRepeatitiveness() != null
                && this.catalogs.findByIdAndActive(request.getRepeatitiveness(), true) == null) {
            throw new GenericException("Invalid `repeatitiveness` value", HttpStatus.BAD_REQUEST.value());
        }
    }

    @Override
    @Transactional
    public RequestDto delete(int requestId, Session session) throws GenericException {
        Request request = this.requests.findByIdAndActive(requestId, true);
        if (request == null) {
            throw new GenericException("Invalid `requestId` value", HttpStatus.BAD_REQUEST.value());
        }

        request.setActive(false);
        request.setUpdateTime(new Date());
        this.requests.save(request);

        return this.transform(request);
    }

    @Override
    public List<RequestDto> retrieveByModule(
            int moduleId,
            String criteria,
            Date dateFrom,
            Date dateTo,
            Integer reason,
            Integer clazz) {
        List<Request> entities = this.requests.findByModuleWithFilters(
                moduleId, criteria, dateFrom, dateTo, reason, clazz);

        return this.transform(entities);
    }

    private List<RequestDto> transform(List<Request> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<RequestDto> result = new ArrayList<>();
        for (Request entity : entities) {
            result.add(this.transform(entity));
        }
        return result;
    }

    @Override
    public List<RequestDto> retrieveByModule(
            int moduleId,
            String criteria,
            Date dateFrom,
            Date dateTo,
            Integer reason,
            Integer clazz,
            int page,
            int size) {

        // Configurar paginación (0-based)
        PageRequest pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Direction.DESC, "creationTime"));

        // 🔹 Obtener resultados paginados
        List<Request> entities = this.requests.findByModuleWithFiltersPaged(
                moduleId, criteria, dateFrom, dateTo, reason, clazz, pageable);

        long total = this.requests.countByModuleWithFilters(
                moduleId, criteria, dateFrom, dateTo, reason, clazz);

        // 🔹 Log opcional para debug
        System.out.println("Page " + page + " / " + size + " → found: " + entities.size() + " of total: " + total);

        return this.transform(entities);
    }

    @Override
    public long countByModule(
            int moduleId,
            String criteria,
            Date dateFrom,
            Date dateTo,
            Integer reason,
            Integer clazz) {
        return this.requests.countByModuleWithFilters(
                moduleId, criteria, dateFrom, dateTo, reason, clazz);
    }

    // =====================================================================================
    // NUEVOS MÉTODOS PARA PAGINAR /requests (homologado con retrieveByModule)
    // =====================================================================================

    @Override
    public List<RequestDto> retrievePaged(
            String criteria,
            Date dateFrom,
            Date dateTo,
            Integer reason,
            Integer clazz,
            int page,
            int size,
            Session session) {

        // Crear paginación (0-based)
        PageRequest pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Direction.DESC, "creationTime"));

        // Filtrar por rol igual que retrieveHelper
        Author author = this.validateAuthor(session);
        String email = author.getEmail();

        // Llamada a repositorio (requiere agregar los métodos en RequestRepository)
        List<Request> entities = this.requests.findPagedRequests(
                criteria,
                dateFrom,
                dateTo,
                reason,
                clazz,
                email,
                pageable);

        return this.transform(entities);
    }

    @Override
    public long count(
            String criteria,
            Date dateFrom,
            Date dateTo,
            Integer reason,
            Integer clazz,
            Session session) {

        Author author = this.validateAuthor(session);
        String email = author.getEmail();

        return this.requests.countRequests(
                criteria,
                dateFrom,
                dateTo,
                reason,
                clazz,
                email);
    }

    @Override
    public List<RequestDto> retrieveByModules(
            List<Integer> moduleIds,
            String criteria,
            Date dateFrom,
            Date dateTo,
            Integer reason,
            Integer clazz,
            String userEmail,
            int page,
            int size) {

        PageRequest pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "creationTime"));

        List<Request> entities = this.requests.findByModulesWithFiltersPaged(
                moduleIds, criteria, dateFrom, dateTo, reason, clazz, userEmail, pageable);

        return this.transform(entities);
    }

    @Override
    public long countByModules(
            List<Integer> moduleIds,
            String criteria,
            Date dateFrom,
            Date dateTo,
            Integer reason,
            Integer clazz,
            String userEmail) {

        return this.requests.countByModulesWithFilters(
                moduleIds, criteria, dateFrom, dateTo, reason, clazz, userEmail);
    }

}
