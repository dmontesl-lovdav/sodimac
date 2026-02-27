package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.RelatedCfdiMapper;
import com.sodimac.fiscal.api.model.dto.RelatedCfdiDto;
import com.sodimac.fiscal.api.model.entity.RelatedCfdiEntity;
import com.sodimac.fiscal.api.repository.RelatedCfdiRepository;
import com.sodimac.fiscal.api.service.RelatedCfdiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RelatedCfdiServiceImpl implements RelatedCfdiService {

    private final RelatedCfdiRepository relatedCfdiRepository;
    private final RelatedCfdiMapper relatedCfdiMapper;

    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<RelatedCfdiDto> findAll(Pageable pageable) {
        log.debug("Finding all related CFDIs with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<RelatedCfdiEntity> entities = relatedCfdiRepository.findAll(pageable);
        return entities.map(relatedCfdiMapper::toDto);
    }
}