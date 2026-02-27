package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.dto.CreateFaqRequest;
import com.sodimac.aclaraciones.api.model.dto.FaqResponse;

public interface FaqCommandService {

    /**
     * Crea la FAQ, sus alias, relaciones y adjuntos.
     * Devuelve un pequeño echo para el cliente.
     */
    FaqResponse createFaq(CreateFaqRequest req);
}
