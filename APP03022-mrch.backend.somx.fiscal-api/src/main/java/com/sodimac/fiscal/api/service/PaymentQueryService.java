package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.request.PaymentSearchRequest;
import com.sodimac.fiscal.api.model.dto.response.PaymentSearchResponse;
import org.springframework.data.domain.Page;

/**
 * Servicio para consulta de complementos de pago.
 *
 * Proporciona métodos para buscar complementos de pago con filtros
 * dinámicos para el portal de proveedores.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
public interface PaymentQueryService {

    /**
     * Busca complementos de pago aplicando filtros opcionales.
     *
     * Utiliza JPA Criteria para construir queries dinámicas
     * basadas en los filtros proporcionados.
     *
     * @param searchRequest Filtros de búsqueda
     * @return Página de complementos de pago que cumplen los criterios
     */
    Page<PaymentSearchResponse> searchPayments(PaymentSearchRequest searchRequest);

    Page<PaymentSearchResponse> searchPayments(PaymentSearchRequest searchRequest, java.util.List<String> allowedVendors);
}
