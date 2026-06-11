package com.invoicesync.infrastructure.adapter.external;

import com.invoicesync.domain.enums.InvoiceFlowStatus;
import com.invoicesync.domain.model.FbcInvoice;
import com.invoicesync.domain.port.output.FbcPortalClient;
import com.invoicesync.infrastructure.adapter.external.dto.FbcStatusUpdateRequest;
import com.invoicesync.infrastructure.adapter.external.dto.FbcStatusUpdateResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class FbcPortalClientAdapter implements FbcPortalClient {

    private static final Logger log = LoggerFactory.getLogger(FbcPortalClientAdapter.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final boolean mockEnabled;

    public FbcPortalClientAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${fbc.portal.url:http://localhost:8081}") String baseUrl,
            @Value("${fbc.portal.mock-enabled:true}") boolean mockEnabled) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
        this.mockEnabled = mockEnabled;
    }

    @Override
    @Retry(name = "fbcPortal")
    public List<FbcInvoice> fetchInvoicesByStatus(InvoiceFlowStatus status) {
        log.debug("Fetching invoices with status {} from FBC Portal", status.getCode());

        if (mockEnabled) {
            return mockFetchInvoicesByStatus(status);
        }

        try {
            List<FbcInvoiceDto> dtos = restClient.get()
                    .uri("/api/facturas?estatus={status}", status.getCode())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FbcInvoiceDto>>() {});

            if (dtos == null) return List.of();

            return dtos.stream()
                    .map(this::toDomain)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching invoices from FBC Portal: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch invoices from FBC Portal", e);
        }
    }

    @Override
    @Retry(name = "fbcPortal")
    public List<FbcInvoice> fetchAllPendingInvoices() {
        log.debug("Fetching all pending invoices from FBC Portal");

        if (mockEnabled) {
            return mockFetchAllPending();
        }

        try {
            List<FbcInvoiceDto> dtos = restClient.get()
                    .uri("/api/facturas/pendientes")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FbcInvoiceDto>>() {});

            if (dtos == null) return List.of();

            return dtos.stream()
                    .map(this::toDomain)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching pending invoices: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch pending invoices", e);
        }
    }

    @Override
    @Retry(name = "fbcPortal")
    public boolean updateInvoiceStatus(String idProveedor, String documentNumber, InvoiceFlowStatus newStatus) {
        log.debug("Updating invoice {} to status {} in FBC Portal", documentNumber, newStatus.getCode());

        if (mockEnabled) {
            return mockUpdateStatus(idProveedor, documentNumber, newStatus);
        }

        try {
            restClient.patch()
                    .uri("/api/facturas/{idProveedor}/{documentNumber}/estatus", idProveedor, documentNumber)
                    .body(new StatusUpdateDto(newStatus.getCode()))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Invoice {} status updated to {} in FBC Portal", documentNumber, newStatus.getCode());
            return true;

        } catch (Exception e) {
            log.error("Error updating invoice status in FBC Portal: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Retry(name = "fbcPortal")
    public Optional<String> updateInvoiceStatusByUuid(String uuid, int numeroProveedor, int currentFbcStatus, int newFbcStatus) {
        log.info("Updating invoice {} status in FBC: {} -> {} (Provider: {})",
                uuid, currentFbcStatus, newFbcStatus, numeroProveedor);

        if (mockEnabled) {
            return mockUpdateStatusByUuid(uuid, numeroProveedor, currentFbcStatus, newFbcStatus);
        }

        try {
            FbcStatusUpdateRequest request = FbcStatusUpdateRequest.forBatchUpdate(
                    numeroProveedor,
                    currentFbcStatus,
                    newFbcStatus
            );

            log.debug("Sending PUT request to /invoices/{}/status with payload: {}", uuid, request);

            FbcStatusUpdateResponse response = restClient.put()
                    .uri("/invoices/{uuid}/status", uuid)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.error("Client error updating invoice {} in FBC: {} - {}",
                                uuid, res.getStatusCode(), res.getStatusText());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Server error updating invoice {} in FBC: {} - {}",
                                uuid, res.getStatusCode(), res.getStatusText());
                    })
                    .body(FbcStatusUpdateResponse.class);

            if (response != null && response.isSuccessful()) {
                String message = response.getResultMessage();
                log.info("Invoice {} status updated successfully in FBC: {}", uuid, message);
                return Optional.of(message);
            } else if (response != null) {
                log.warn("Failed to update invoice {} in FBC: {}", uuid, response.getResultMessage());
                return Optional.empty();
            } else {
                log.warn("No response from FBC when updating invoice {}", uuid);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Error updating invoice {} status in FBC Portal: {}", uuid, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isServiceAvailable() {
        if (mockEnabled) {
            log.debug("Mock mode enabled, FBC Portal is always available");
            return true;
        }

        try {
            restClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("FBC Portal is not available: {}", e.getMessage());
            return false;
        }
    }

    private FbcInvoice toDomain(FbcInvoiceDto dto) {
        return FbcInvoice.builder()
                .idProveedor(dto.idProveedor())
                .serie(dto.serie())
                .folio(dto.folio())
                .uuid(dto.uuid())
                .currentStatus(InvoiceFlowStatus.fromCode(dto.estatus()).orElse(InvoiceFlowStatus.PENDIENTE_REGISTRO_SAPITO))
                .lastUpdated(dto.fechaActualizacion())
                .build();
    }

    // Mock methods for development
    private List<FbcInvoice> mockFetchInvoicesByStatus(InvoiceFlowStatus status) {
        log.info("[MOCK] Fetching invoices with status {}", status.getCode());
        return List.of(
                FbcInvoice.builder()
                        .idProveedor("PROV001")
                        .serie("A")
                        .folio("12345")
                        .uuid("UUID-001-MOCK")
                        .currentStatus(status)
                        .lastUpdated(LocalDateTime.now())
                        .build(),
                FbcInvoice.builder()
                        .idProveedor("PROV002")
                        .serie("B")
                        .folio("67890")
                        .uuid("UUID-002-MOCK")
                        .currentStatus(status)
                        .lastUpdated(LocalDateTime.now())
                        .build()
        );
    }

    private List<FbcInvoice> mockFetchAllPending() {
        log.info("[MOCK] Fetching all pending invoices");
        return mockFetchInvoicesByStatus(InvoiceFlowStatus.PENDIENTE_REGISTRO_SAPITO);
    }

    private boolean mockUpdateStatus(String idProveedor, String documentNumber, InvoiceFlowStatus newStatus) {
        log.info("[MOCK] Updating invoice {}/{} to status {}", idProveedor, documentNumber, newStatus.getCode());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    private Optional<String> mockUpdateStatusByUuid(String uuid, int numeroProveedor, int currentFbcStatus, int newFbcStatus) {
        log.info("[MOCK] Updating invoice {} status: {} -> {} (Provider: {})",
                uuid, currentFbcStatus, newFbcStatus, numeroProveedor);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String fbcStatusName = FbcStatusMapper.getFbcStatusName(newFbcStatus);
        return Optional.of(String.format("Estado actualizado de %d a %d (%s)",
                currentFbcStatus, newFbcStatus, fbcStatusName));
    }

    // DTOs for API communication
    private record FbcInvoiceDto(
            String idProveedor,
            String serie,
            String folio,
            String uuid,
            int estatus,
            LocalDateTime fechaActualizacion
    ) {}

    private record StatusUpdateDto(int estatus) {}
}
