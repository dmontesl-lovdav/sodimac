package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.PaymentMapper;
import com.sodimac.fiscal.api.model.dto.PaymentDto;
import com.sodimac.fiscal.api.model.entity.PaymentEntity;
import com.sodimac.fiscal.api.repository.PaymentRepository;
import com.sodimac.fiscal.api.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> findAll(Pageable pageable) {
        log.debug("Finding all payments with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<PaymentEntity> entities = paymentRepository.findAll(pageable);
        return entities.map(paymentMapper::toDto);
    }

    // Find by UUID
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDto> findByUuid(UUID paymentUuid) {
        log.debug("Finding payment by UUID: {}", paymentUuid);
        return paymentRepository.findById(paymentUuid)
                .map(paymentMapper::toDto);
    }
}