package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.RelatedDocumentsMapper;
import com.sodimac.fiscal.api.model.dto.RelatedDocumentsDto;
import com.sodimac.fiscal.api.model.entity.RelatedDocumentsEntity;
import com.sodimac.fiscal.api.repository.RelatedDocumentsRepository;
import com.sodimac.fiscal.api.service.RelatedDocumentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RelatedDocumentsServiceImpl implements RelatedDocumentsService {

    private final RelatedDocumentsRepository relatedDocumentsRepository;
    private final RelatedDocumentsMapper relatedDocumentsMapper;

    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<RelatedDocumentsDto> findAll(Pageable pageable) {
        log.debug("Finding all related documents with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<RelatedDocumentsEntity> entities = relatedDocumentsRepository.findAll(pageable);
        return entities.map(relatedDocumentsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RelatedDocumentsDto> findByPaymentsUuid(UUID paymentsUuid, Pageable pageable) {
        log.info("Buscando documentos relacionados para complemento de pago UUID: {}, página: {}, tamaño: {}",
                paymentsUuid, pageable.getPageNumber(), pageable.getPageSize());

        Page<RelatedDocumentsEntity> entities = relatedDocumentsRepository.findByPaymentsUuid(paymentsUuid, pageable);

        log.info("Se encontraron {} documentos relacionados (Total: {}, Página: {}/{})",
                entities.getNumberOfElements(),
                entities.getTotalElements(),
                entities.getNumber() + 1,
                entities.getTotalPages());

        return entities.map(relatedDocumentsMapper::toDto);
    }
}