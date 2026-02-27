package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.RelatedDocumentsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RelatedDocumentsService {

    Page<RelatedDocumentsDto> findAll(Pageable pageable);

    /**
     * Busca documentos relacionados (facturas) de un complemento de pago específico.
     *
     * @param paymentsUuid UUID del complemento de pago
     * @param pageable Configuración de paginación
     * @return Página de documentos relacionados
     */
    Page<RelatedDocumentsDto> findByPaymentsUuid(UUID paymentsUuid, Pageable pageable);

}