package com.rebatesync.infrastructure.adapter.external;

import com.rebatesync.domain.enums.AgreementType;
import com.rebatesync.domain.enums.ContractStatus;
import com.rebatesync.domain.enums.ContributionType;
import com.rebatesync.domain.exception.ExternalServiceException;
import com.rebatesync.domain.model.Agreement;
import com.rebatesync.domain.model.Contract;
import com.rebatesync.domain.port.output.ExternalRebateClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalRebateClientAdapter implements ExternalRebateClient {

    private final RestTemplate restTemplate;

    @Value("${rebate.external.api.contracts-url}")
    private String contractsUrl;

    @Value("${rebate.external.api.agreements-url}")
    private String agreementsUrl;

    @Override
    public List<Contract> fetchContracts(int page, int pageSize) {
        String url = String.format("%s?page=%d&page_size=%d", contractsUrl, page, pageSize);
        log.info("Fetching contracts from: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseContracts(response.getBody());
            }

            log.warn("Unexpected response status: {}", response.getStatusCode());
            return new ArrayList<>();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error fetching contracts: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("Failed to fetch contracts: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error fetching contracts", e);
            throw new ExternalServiceException("Failed to fetch contracts: " + e.getMessage(), e);
        }
    }

    @Override
    public Contract fetchContractById(String contractId) {
        String url = String.format("%s?contract_id=%s", contractsUrl, contractId);
        log.info("Fetching contract by ID: {} from: {}", contractId, url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Contract> contracts = parseContracts(response.getBody());
                if (!contracts.isEmpty()) {
                    return contracts.get(0);
                }
            }

            throw new ExternalServiceException("Contract not found: " + contractId);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error fetching contract: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("Failed to fetch contract: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error fetching contract", e);
            throw new ExternalServiceException("Failed to fetch contract: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Agreement> fetchAgreements(String contractId, int page, int pageSize) {
        String url = String.format("%s?page=%d&page_size=%d&contract_id=%s",
                agreementsUrl, page, pageSize, contractId);
        log.info("Fetching agreements for contract {} from: {}", contractId, url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseAgreements(response.getBody());
            }

            log.warn("Unexpected response status: {}", response.getStatusCode());
            return new ArrayList<>();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error fetching agreements: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("Failed to fetch agreements: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error fetching agreements", e);
            throw new ExternalServiceException("Failed to fetch agreements: " + e.getMessage(), e);
        }
    }

    @Override
    public Agreement fetchAgreementById(String contractId, String agreementId) {
        String url = String.format("%s?contract_id=%s", agreementsUrl, contractId);
        log.info("Fetching agreement {} for contract {} from: {}", agreementId, contractId, url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Agreement> agreements = parseAgreements(response.getBody());
                return agreements.stream()
                        .filter(a -> agreementId.equals(a.getAgreementId()))
                        .findFirst()
                        .orElseThrow(() -> new ExternalServiceException("Agreement not found: " + agreementId));
            }

            throw new ExternalServiceException("Agreement not found: " + agreementId);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error fetching agreement: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("Failed to fetch agreement: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error fetching agreement", e);
            throw new ExternalServiceException("Failed to fetch agreement: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasMorePages(int page, int pageSize, int totalFetched) {
        return totalFetched >= (page * pageSize);
    }

    private List<Contract> parseContracts(Map<String, Object> responseBody) {
        List<Contract> contracts = new ArrayList<>();

        try {
            Object dataObj = responseBody.get("data");
            if (dataObj instanceof List) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj;

                for (Map<String, Object> contractData : dataList) {
                    Contract contract = parseContract(contractData);
                    if (contract != null) {
                        contracts.add(contract);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing contracts response", e);
            throw new ExternalServiceException("Failed to parse contracts response", e);
        }

        return contracts;
    }

    private Contract parseContract(Map<String, Object> contractData) {
        try {
            String contractId = getString(contractData, "id");

            Map<String, Object> vendor = (Map<String, Object>) contractData.get("vendor");
            String vendorTaxId = vendor != null ? getString(vendor, "tax_id") : null;
            String vendorName = vendor != null ? getString(vendor, "name") : null;

            Map<String, Object> status = (Map<String, Object>) contractData.get("status");
            String statusName = status != null ? getString(status, "name") : null;
            ContractStatus contractStatus = parseContractStatus(statusName);

            Map<String, Object> businessManager = (Map<String, Object>) contractData.get("business_manager");
            Map<String, Object> prodCategory = businessManager != null ?
                    (Map<String, Object>) businessManager.get("prod_category") : null;
            String productCategoryId = prodCategory != null ? getString(prodCategory, "id") : null;

            List<String> agreementIds = (List<String>) contractData.get("agreements_ids");

            return Contract.builder()
                    .contractId(contractId)
                    .vendorTaxId(vendorTaxId)
                    .vendorName(vendorName)
                    .status(contractStatus)
                    .productCategoryId(productCategoryId)
                    .agreementIds(agreementIds != null ? agreementIds : new ArrayList<>())
                    .build();

        } catch (Exception e) {
            log.error("Error parsing individual contract", e);
            return null;
        }
    }

    private List<Agreement> parseAgreements(Map<String, Object> responseBody) {
        List<Agreement> agreements = new ArrayList<>();

        try {
            Object dataObj = responseBody.get("data");
            if (dataObj instanceof List) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj;

                for (Map<String, Object> agreementData : dataList) {
                    Agreement agreement = parseAgreement(agreementData);
                    if (agreement != null) {
                        agreements.add(agreement);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing agreements response", e);
            throw new ExternalServiceException("Failed to parse agreements response", e);
        }

        return agreements;
    }

    private Agreement parseAgreement(Map<String, Object> agreementData) {
        try {
            String agreementId = getString(agreementData, "id");

            Map<String, Object> productTypeSelector = (Map<String, Object>) agreementData.get("product_type_selector");
            String productTypeSelectorLabel = productTypeSelector != null ?
                    getString(productTypeSelector, "label") : null;

            Map<String, Object> rebateType = (Map<String, Object>) agreementData.get("rebate_type");
            String rebateTypeName = rebateType != null ? getString(rebateType, "name") : null;
            AgreementType agreementType = parseAgreementType(rebateTypeName);

            Map<String, Object> currency = (Map<String, Object>) agreementData.get("currency");
            String currencyCode = currency != null ? getString(currency, "code") : null;

            BigDecimal contributionAmount = getBigDecimal(agreementData, "contribution_amount");

            Map<String, Object> contributionType = (Map<String, Object>) agreementData.get("contribution_type");
            String contributionTypeName = contributionType != null ? getString(contributionType, "name") : null;
            ContributionType contrType = parseContributionType(contributionTypeName);

            BigDecimal agreedFillRate = getBigDecimal(agreementData, "agreed_fill_rate");

            Map<String, Object> paymentPeriod = (Map<String, Object>) agreementData.get("payment_period");
            String paymentPeriodLabel = paymentPeriod != null ? getString(paymentPeriod, "label") : null;

            String brand = getString(agreementData, "brand");

            return Agreement.builder()
                    .agreementId(agreementId)
                    .productTypeSelector(productTypeSelectorLabel)
                    .rebateType(agreementType)
                    .currencyCode(currencyCode)
                    .contributionAmount(contributionAmount)
                    .contributionType(contrType)
                    .agreedFillRate(agreedFillRate)
                    .paymentPeriodLabel(paymentPeriodLabel)
                    .brand(brand)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing individual agreement", e);
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal from value: {}", value);
            return null;
        }
    }

    private ContractStatus parseContractStatus(String status) {
        if (status == null) return null;
        try {
            return ContractStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown contract status: {}", status);
            return null;
        }
    }

    private AgreementType parseAgreementType(String type) {
        if (type == null) return null;
        try {
            return AgreementType.valueOf(type.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown agreement type: {}", type);
            return null;
        }
    }

    private ContributionType parseContributionType(String type) {
        if (type == null) return null;
        try {
            return ContributionType.valueOf(type.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown contribution type: {}", type);
            return null;
        }
    }
}
