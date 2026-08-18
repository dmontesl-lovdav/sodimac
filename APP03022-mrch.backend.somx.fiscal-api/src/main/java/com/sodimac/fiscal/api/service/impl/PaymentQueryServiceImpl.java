package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.request.PaymentSearchRequest;
import com.sodimac.fiscal.api.model.dto.response.PaymentSearchResponse;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.repository.PaymentRepository;
import com.sodimac.fiscal.api.repository.PaymentsRepository;
import com.sodimac.fiscal.api.service.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del servicio de consulta de complementos de pago.
 *
 * Utiliza el repositorio custom con Criteria API para realizar
 * búsquedas dinámicas y transforma las entidades en DTOs de respuesta.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentRepository paymentRepository;
    private final com.sodimac.fiscal.api.repository.AddendumRepository addendumRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSearchResponse> searchPayments(PaymentSearchRequest searchRequest, java.util.List<String> allowedVendors) {
        log.info("Buscando complementos de pago con filtro de vendors: {}", allowedVendors);
        Page<PaymentsEntity> paymentsPage = paymentsRepository.searchPayments(searchRequest, allowedVendors);
        log.info("Se encontraron {} complementos de pago (Página {}/{})",
                paymentsPage.getTotalElements(), paymentsPage.getNumber() + 1, paymentsPage.getTotalPages());
        return paymentsPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSearchResponse> searchPayments(PaymentSearchRequest searchRequest) {
        log.info("Buscando complementos de pago con filtros: RFC Emisor={}, RFC Receptor={}, Folio={}, Fecha Inicio={}, Fecha Fin={}",
                searchRequest.getRfcEmisor(), searchRequest.getRfcReceptor(), searchRequest.getFolio(),
                searchRequest.getFechaPagoInicio(), searchRequest.getFechaPagoFin());

        // Buscar usando Criteria API
        Page<PaymentsEntity> paymentsPage = paymentsRepository.searchPayments(searchRequest);

        log.info("Se encontraron {} complementos de pago (Página {}/{})",
                paymentsPage.getTotalElements(), paymentsPage.getNumber() + 1, paymentsPage.getTotalPages());

        // Transformar entidades a DTOs
        return paymentsPage.map(this::mapToResponse);
    }

    /**
     * Mapea una entidad PaymentsEntity a un DTO PaymentSearchResponse.
     */
    private PaymentSearchResponse mapToResponse(PaymentsEntity entity) {
        // Calcular monto total sumando todos los pagos individuales
        BigDecimal totalAmount = paymentRepository.sumAmountByPaymentsUuid(entity.getPaymentsUuid())
                .orElse(BigDecimal.ZERO);

        // Contar documentos relacionados
        Integer relatedDocsCount = paymentRepository.countRelatedDocumentsByPaymentsUuid(entity.getPaymentsUuid());

        // Tipo de proveedor (id + descripción) desde la addenda del complemento - Fer #5.
        // Retro Ivan 2026-06-22: se resuelve EN VIVO desde el supplier_number (refleja cambios en
        // CatTipoProveedor); fallback al valor guardado si el proveedor ya no está en el catálogo.
        String tipoProveedorId = addendumRepository.findTipoProveedorIdByPaymentsUuid(entity.getPaymentsUuid());
        if (tipoProveedorId == null) {
            tipoProveedorId = addendumRepository.findSupplierTypeByPaymentsUuid(entity.getPaymentsUuid());
        }
        String tipoProveedorDescripcion = (tipoProveedorId != null && !tipoProveedorId.isBlank())
                ? addendumRepository.findTipoProveedorDescripcion(tipoProveedorId)
                : null;

        return PaymentSearchResponse.builder()
                .paymentsUuid(entity.getPaymentsUuid())
                .fiscalUuid(entity.getFiscalUuid())
                .version(entity.getVersion())
                .paymentDate(entity.getPaymentDate())
                .certificationDate(entity.getCertificationDate())
                .folio(entity.getFolio())
                .series(entity.getSeries())
                .status(entity.getStatus())
                .statusDescription(getStatusDescription(entity.getStatus()))
                .issuerUuid(entity.getIssuerUuid())
                .issuerRfc(entity.getIssuer() != null ? entity.getIssuer().getRfc() : null)
                .issuerName(entity.getIssuer() != null ? entity.getIssuer().getName() : null)
                .receiverUuid(entity.getReceiverUuid())
                .receiverRfc(entity.getReceiver() != null ? entity.getReceiver().getRfc() : null)
                .receiverName(entity.getReceiver() != null ? entity.getReceiver().getName() : null)
                .subtotalAmount(totalAmount)
                .totalAmount(totalAmount)
                .relatedDocumentsCount(relatedDocsCount)
                .tipoProveedor(tipoProveedorId)
                .tipoProveedorDescripcion(tipoProveedorDescripcion)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    /**
     * Obtiene la descripción del estado del complemento de pago.
     */
    private String getStatusDescription(Integer status) {
        if (status == null) {
            return "Desconocido";
        }
        return switch (status) {
            case 0 -> "Cancelado";
            case 1 -> "Vigente";
            case 2 -> "Pendiente";
            case 3 -> "Rechazado";
            default -> "Desconocido";
        };
    }
}
