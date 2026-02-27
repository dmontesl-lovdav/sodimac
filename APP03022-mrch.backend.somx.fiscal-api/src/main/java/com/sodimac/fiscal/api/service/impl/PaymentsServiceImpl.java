package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.PaymentsMapper;
import com.sodimac.fiscal.api.model.dto.PaymentsDto;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.repository.PaymentsRepository;
import com.sodimac.fiscal.api.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentsServiceImpl implements PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentsMapper paymentsMapper;

    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentsDto> findAll(Pageable pageable) {
        log.debug("Finding all payments documents with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<PaymentsEntity> entities = paymentsRepository.findAll(pageable);
        return entities.map(paymentsMapper::toDto);
    }

    // Find by UUID
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentsDto> findByUuid(UUID paymentsUuid) {
        log.debug("Finding payment document by UUID: {}", paymentsUuid);
        return paymentsRepository.findById(paymentsUuid)
                .map(paymentsMapper::toDto);
    }
}