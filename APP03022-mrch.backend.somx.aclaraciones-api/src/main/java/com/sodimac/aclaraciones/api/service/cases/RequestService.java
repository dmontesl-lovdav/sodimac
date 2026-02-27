package com.sodimac.aclaraciones.api.service.cases;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.RequestDto;
import com.sodimac.aclaraciones.api.security.Session;

import java.util.Date;
import java.util.List;

public interface RequestService {

        int save(RequestDto request, Session session) throws GenericException;

        int configure(int requestId, RequestDto request, Session session) throws GenericException;

        RequestDto retrieve(int requestId, Session session);

        List<RequestDto> retrieve(
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz,
                        Session session);

        List<RequestDto> retrieve(
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz,
                        String role,
                        Session session);

        RequestDto delete(int requestId, Session session) throws GenericException;

        // 🔹 Versión anterior sin paginación (mantener por compatibilidad)
        List<RequestDto> retrieveByModule(
                        int moduleId,
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz);

        List<RequestDto> retrieveByModule(
                        int moduleId,
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz,
                        int page,
                        int size);

        long countByModule(
                        int moduleId,
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz);

        List<RequestDto> retrievePaged(
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz,
                        int page,
                        int size,
                        Session session);

        long count(
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz,
                        Session session);

        List<RequestDto> retrieveByModules(
                        List<Integer> moduleIds,
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz,
                        String userEmail,
                        int page,
                        int size);

        long countByModules(
                        List<Integer> moduleIds,
                        String criteria,
                        Date dateFrom,
                        Date dateTo,
                        Integer reason,
                        Integer clazz,
                        String userEmail);

}
