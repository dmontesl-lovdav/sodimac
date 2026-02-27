package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.AddendumMapper;
import com.sodimac.fiscal.api.model.dto.AddendumDto;
import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import com.sodimac.fiscal.api.repository.AddendumRepository;
import com.sodimac.fiscal.api.service.AddendumService;
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
public class AddendumServiceImpl implements AddendumService {

    private final AddendumRepository addendumRepository;
    private final AddendumMapper addendumMapper;

    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<AddendumDto> findAll(Pageable pageable) {
        log.debug("Finding all addenda with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<AddendumEntity> entities = addendumRepository.findAll(pageable);
        return entities.map(addendumMapper::toDto);
    }
}