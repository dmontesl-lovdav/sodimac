package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.PaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

    Page<PaymentDto> findAll(Pageable pageable);

    Optional<PaymentDto> findByUuid(UUID paymentUuid);

}