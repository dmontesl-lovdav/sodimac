package com.invoicesync.infrastructure.adapter.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.invoicesync.domain.enums.InvoiceFlowStatus;
import com.invoicesync.domain.model.FbcInvoice;
import com.invoicesync.domain.port.output.FbcPortalClient;
import com.invoicesync.infrastructure.adapter.external.dto.FbcStatusUpdateRequest;
import com.invoicesync.infrastructure.adapter.external.dto.FbcStatusUpdateResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cliente del portal FBC = fiscal-api (contrato real, sin mock).
 *
 * - Consulta por estatus: POST /invoices/search (rfcEmisor vacío = todos los proveedores).
 * - Actualización: PUT /invoices/{uuid}/status (fiscal-api valida la transición contra el tren v1.0).
 */
@Component
public class FbcPortalClientAdapter implements FbcPortalClient {

    private static final Logger log = LoggerFactory.getLogger(FbcPortalClientAdapter.class);

    private static final String DOC_TYPE_FACTURA = "I";
    private static final int PAGE_SIZE = 100;
    private static final int SEARCH_MONTHS_BACK = 12;
    private static final int ID_USUARIO_BATCH = 1;

    private final RestClient restClient;

    public FbcPortalClientAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${fbc.portal.url:http://localhost:8082}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    @Retry(name = "fbcPortal")
    public List<FbcInvoice> fetchInvoicesByStatus(InvoiceFlowStatus status) {
        log.debug("Consultando facturas en estatus {} a fiscal-api", status.getCode());

        LocalDate dateTo = LocalDate.now();
        LocalDate dateFrom = dateTo.minusMonths(SEARCH_MONTHS_BACK);
        List<FbcInvoice> all = new java.util.ArrayList<>();
        int page = 0;
        boolean hasMore = true;

        try {
            while (hasMore) {
                InvoiceSearchRequest body = new InvoiceSearchRequest(
                        null, dateFrom.toString(), dateTo.toString(),
                        DOC_TYPE_FACTURA, status.getCode(), page, PAGE_SIZE);

                PageResponse response = restClient.post()
                        .uri("/invoices/search")
                        .body(body)
                        .retrieve()
                        .body(PageResponse.class);

                if (response == null || response.content() == null || response.content().isEmpty()) {
                    hasMore = false;
                } else {
                    response.content().stream().map(this::toDomain).forEach(all::add);
                    hasMore = response.content().size() >= PAGE_SIZE;
                    page++;
                }
            }
            return all;

        } catch (Exception e) {
            log.error("Error consultando facturas estatus {} en fiscal-api: {}", status.getCode(), e.getMessage());
            throw new RuntimeException("Failed to fetch invoices from fiscal-api", e);
        }
    }

    @Override
    public List<FbcInvoice> fetchAllPendingInvoices() {
        return fetchInvoicesByStatus(InvoiceFlowStatus.PENDIENTE_REGISTRO_SAPITO);
    }

    @Override
    public boolean updateInvoiceStatus(String idProveedor, String documentNumber, InvoiceFlowStatus newStatus) {
        // Legacy (no usado en v1.0): la actualización va por updateInvoiceStatusByUuid.
        log.warn("updateInvoiceStatus(legacy) no soportado en v1.0; usar updateInvoiceStatusByUuid");
        return false;
    }

    @Override
    @Retry(name = "fbcPortal")
    public Optional<String> updateInvoiceStatusByUuid(String uuid, int numeroProveedor, int currentStatus, int newStatus) {
        log.info("Actualizando factura {} en fiscal-api: estatus {} -> {} (Proveedor: {})",
                uuid, currentStatus, newStatus, numeroProveedor);

        try {
            FbcStatusUpdateRequest request = FbcStatusUpdateRequest.forBatchUpdate(
                    numeroProveedor, currentStatus, newStatus);

            FbcStatusUpdateResponse response = restClient.put()
                    .uri("/invoices/{uuid}/status", uuid)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) ->
                            log.error("Client error actualizando factura {}: {}", uuid, res.getStatusCode()))
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) ->
                            log.error("Server error actualizando factura {}: {}", uuid, res.getStatusCode()))
                    .body(FbcStatusUpdateResponse.class);

            if (response != null && response.isSuccessful()) {
                log.info("Factura {} actualizada: {}", uuid, response.getResultMessage());
                return Optional.of(response.getResultMessage());
            } else if (response != null) {
                log.warn("fiscal-api rechazó factura {}: {}", uuid, response.getResultMessage());
                return Optional.empty();
            }
            log.warn("Sin respuesta de fiscal-api al actualizar factura {}", uuid);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error actualizando factura {} en fiscal-api: {}", uuid, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isServiceAvailable() {
        // fiscal-api se valida en cada consulta; no se bloquea el batch aquí.
        return true;
    }

    private FbcInvoice toDomain(InvoiceSearchResponseDto dto) {
        return FbcInvoice.builder()
                .idProveedor(dto.numeroProveedor() != null ? dto.numeroProveedor().toBigInteger().toString() : null)
                .serie(dto.series())
                .folio(dto.folio())
                .uuid(dto.fiscalUuid() != null ? dto.fiscalUuid().toString() : null)
                .currentStatus(InvoiceFlowStatus.fromCode(dto.status() != null ? dto.status() : 0)
                        .orElse(InvoiceFlowStatus.PENDIENTE_REGISTRO_SAPITO))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ===== DTOs del contrato fiscal-api =====

    private record InvoiceSearchRequest(
            String rfcEmisor,
            String fechaInicioRecepcion,
            String fechaFinalRecepcion,
            String tipoDocumento,
            Integer estatus,
            Integer page,
            Integer size
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PageResponse(List<InvoiceSearchResponseDto> content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record InvoiceSearchResponseDto(
            UUID fiscalUuid,
            String series,
            String folio,
            Integer status,
            BigDecimal numeroProveedor
    ) {}
}
