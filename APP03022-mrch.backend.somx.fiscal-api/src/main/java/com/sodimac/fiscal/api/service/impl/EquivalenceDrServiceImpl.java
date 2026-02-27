package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.EquivalenceDrMapper;
import com.sodimac.fiscal.api.model.dto.EquivalenceDrDto;
import com.sodimac.fiscal.api.model.entity.EquivalenceDrEntity;
import com.sodimac.fiscal.api.repository.EquivalenceDrRepository;
import com.sodimac.fiscal.api.service.EquivalenceDrService;
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
public class EquivalenceDrServiceImpl implements EquivalenceDrService {

    private final EquivalenceDrRepository equivalenceDrRepository;
    private final EquivalenceDrMapper equivalenceDrMapper;

    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<EquivalenceDrDto> findAll(Pageable pageable) {
        log.debug("Finding all equivalence DRs with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<EquivalenceDrEntity> entities = equivalenceDrRepository.findAll(pageable);
        return entities.map(equivalenceDrMapper::toDto);
    }
}