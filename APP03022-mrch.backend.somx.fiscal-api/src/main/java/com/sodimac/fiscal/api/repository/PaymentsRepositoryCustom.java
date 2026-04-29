package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.dto.request.PaymentSearchRequest;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import org.springframework.data.domain.Page;

/**
 * Repositorio custom para consultas dinámicas de complementos de pago.
 *
 * Utiliza JPA Criteria API para construir queries dinámicas
 * basadas en filtros opcionales.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
public interface PaymentsRepositoryCustom {

    /**
     * Busca complementos de pago aplicando filtros dinámicos.
     *
     * @param searchRequest Filtros de búsqueda
     * @return Página de complementos de pago que cumplen los criterios
     */
    Page<PaymentsEntity> searchPayments(PaymentSearchRequest searchRequest);

    Page<PaymentsEntity> searchPayments(PaymentSearchRequest searchRequest, java.util.List<String> allowedVendors);
}
