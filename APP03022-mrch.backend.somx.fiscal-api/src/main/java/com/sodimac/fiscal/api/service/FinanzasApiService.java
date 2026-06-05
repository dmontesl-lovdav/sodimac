package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.FinanzasReceptionResponse;

public interface FinanzasApiService {
    FinanzasReceptionResponse getReception(String receptionId);
}
