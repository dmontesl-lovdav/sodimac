package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.TotalsMapper;
import com.sodimac.fiscal.api.model.dto.TotalsDto;
import com.sodimac.fiscal.api.model.entity.TotalsEntity;
import com.sodimac.fiscal.api.repository.TotalsRepository;
import com.sodimac.fiscal.api.service.TotalsService;
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
public class TotalsServiceImpl implements TotalsService {

    private final TotalsRepository totalsRepository;
    private final TotalsMapper totalsMapper;

    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<TotalsDto> findAll(Pageable pageable) {
        log.debug("Finding all totals with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<TotalsEntity> entities = totalsRepository.findAll(pageable);
        return entities.map(totalsMapper::toDto);
    }
}