package com.sodimac.batch.fiscal.download.client;

import com.sodimac.batch.fiscal.download.mapper.InvoiceSearchRequestMapper;
import com.sodimac.batch.fiscal.download.mapper.StatusUpdateRequestMapper;
import com.sodimac.batch.fiscal.download.mapper.StatusUpdateResponseMapper;
import com.sodimac.batch.fiscal.download.model.dto.InvoiceSearchRequestDto;
import com.sodimac.batch.fiscal.download.model.dto.InvoiceSearchResponseDto;
import com.sodimac.batch.fiscal.download.model.dto.PageResponseDto;
import com.sodimac.batch.fiscal.download.model.dto.StatusUpdateRequestDto;
import com.sodimac.batch.fiscal.download.model.dto.StatusUpdateResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FiscalApiClient {

    private static final Logger log = LoggerFactory.getLogger(FiscalApiClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FiscalApiClient(RestTemplate restTemplate,
                           @Value("${fiscal-api.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<InvoiceSearchResponseDto> searchAllByStatusAndType(
            Integer status, String documentType, LocalDate dateFrom, LocalDate dateTo) {

        List<InvoiceSearchResponseDto> allResults = new ArrayList<>();
        int page = 0;
        int pageSize = 100;
        boolean hasMore = true;

        while (hasMore) {
            InvoiceSearchRequestDto request = InvoiceSearchRequestMapper.toDto(
                    status, documentType, dateFrom, dateTo, page, pageSize);

            try {
                ResponseEntity<PageResponseDto<InvoiceSearchResponseDto>> response = restTemplate.exchange(
                        baseUrl + "/invoices/search",
                        HttpMethod.POST,
                        new HttpEntity<>(request, jsonHeaders()),
                        new ParameterizedTypeReference<PageResponseDto<InvoiceSearchResponseDto>>() {}
                );

                PageResponseDto<InvoiceSearchResponseDto> pageData = response.getBody();
                if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
                    allResults.addAll(pageData.getContent());
                    hasMore = !pageData.isLast();
                    page++;
                } else {
                    hasMore = false;
                }
            } catch (Exception e) {
                log.error("Error buscando documentos tipo={} estatus={} pagina={}: {}",
                        documentType, status, page, e.getMessage());
                // Un fallo de conexión NO significa "0 documentos": se propaga para que
                // la ejecución termine en FAILED y no en un SUCCESS engañoso con origen=0.
                throw new RuntimeException("Error buscando documentos tipo=" + documentType +
                        " estatus=" + status + " pagina=" + page + ": " + e.getMessage(), e);
            }
        }

        log.info("Encontrados {} documentos tipo={} estatus={}", allResults.size(), documentType, status);
        return allResults;
    }

    public StatusUpdateResponseDto updateStatus(String fiscalUuid, BigDecimal numeroProveedor,
                                                 Integer estatusOrigen, Integer estatusDestino,
                                                 String comentario) {
        StatusUpdateRequestDto request = StatusUpdateRequestMapper.toDto(
                numeroProveedor, estatusOrigen, estatusDestino, comentario);

        try {
            ResponseEntity<StatusUpdateResponseDto> response = restTemplate.exchange(
                    baseUrl + "/invoices/" + fiscalUuid + "/status",
                    HttpMethod.PUT,
                    new HttpEntity<>(request, jsonHeaders()),
                    StatusUpdateResponseDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Error actualizando estatus uuid={} {}->{}:  {} {}",
                    fiscalUuid, estatusOrigen, estatusDestino, e.getStatusCode(), e.getResponseBodyAsString());
            return StatusUpdateResponseMapper.toError(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error actualizando estatus uuid={}: {}", fiscalUuid, e.getMessage());
            return StatusUpdateResponseMapper.toError(e.getMessage());
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
