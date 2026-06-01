package com.rebatesync.infrastructure.adapter.web;

import com.rebatesync.domain.model.Agreement;
import com.rebatesync.domain.model.Contract;
import com.rebatesync.domain.port.output.ExternalRebateClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final ExternalRebateClient externalRebateClient;

    @GetMapping("/test-connections")
    public ResponseEntity<Map<String, Object>> testExternalConnections() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Test Contracts Service
            log.info("Testing Contracts Service...");
            List<Contract> contracts = externalRebateClient.fetchContracts(1, 10);
            result.put("contractsService", Map.of(
                "status", "SUCCESS",
                "contractsCount", contracts.size(),
                "message", "Contracts service is responding correctly"
            ));
            log.info("Contracts Service: OK - Retrieved {} contracts", contracts.size());

            // Test Agreements Service (use first contract if available)
            if (!contracts.isEmpty()) {
                String contractId = contracts.get(0).getContractId();
                log.info("Testing Agreements Service with contract ID: {}", contractId);
                List<Agreement> agreements = externalRebateClient.fetchAgreements(contractId, 1, 10);
                result.put("agreementsService", Map.of(
                    "status", "SUCCESS",
                    "agreementsCount", agreements.size(),
                    "contractId", contractId,
                    "message", "Agreements service is responding correctly"
                ));
                log.info("Agreements Service: OK - Retrieved {} agreements", agreements.size());
            } else {
                result.put("agreementsService", Map.of(
                    "status", "SKIPPED",
                    "message", "No contracts available to test agreements service"
                ));
            }

            result.put("overallStatus", "SUCCESS");
            result.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error testing external services", e);
            result.put("overallStatus", "ERROR");
            result.put("error", e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
}
