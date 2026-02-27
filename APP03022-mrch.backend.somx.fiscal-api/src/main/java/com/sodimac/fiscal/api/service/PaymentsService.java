package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.PaymentsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentsService {

    Page<PaymentsDto> findAll(Pageable pageable);

    Optional<PaymentsDto> findByUuid(UUID paymentsUuid);

}