package com.sodimac.fiscal.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check endpoint
 * Compatible with BFF proxy health check
 */
@RestController
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        LOGGER.info("Health check requested for fiscal-api");

        Map<String, String> response = new HashMap<>();
        response.put("status", "UP - fiscal health check updated");
        response.put("service", "fiscal-api");
        return ResponseEntity.ok(response);
    }
}