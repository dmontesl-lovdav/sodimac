package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.FinanzasReceptionResponse;
import com.sodimac.fiscal.api.repository.ReceptionRepository;
import com.sodimac.fiscal.api.service.FinanzasApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanzasApiServiceImpl implements FinanzasApiService {

    private final ReceptionRepository receptionRepository;

    @Override
    public FinanzasReceptionResponse getReception(String receptionId) {
        log.debug("Consultando recepción en tenant_finance.reception. receptionId: {}", receptionId);

        return receptionRepository.findById(UUID.fromString(receptionId))
                .map(r -> {
                    FinanzasReceptionResponse resp = new FinanzasReceptionResponse();
                    resp.setReceptionId(r.getReceptionId().toString());
                    resp.setAmount(r.getAmount() != null ? r.getAmount().toPlainString() : null);
                    log.debug("Recepción encontrada. receptionId: {}, amount: {}", receptionId, resp.getAmount());
                    return resp;
                })
                .orElse(null);
    }
}
