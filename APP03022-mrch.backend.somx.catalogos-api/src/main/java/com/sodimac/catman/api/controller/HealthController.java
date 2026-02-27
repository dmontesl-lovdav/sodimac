package com.sodimac.catman.api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Simple health check endpoint
 * Compatible with BFF proxy health check
 */
@RestController
@Tag(name = "Health", description = "Endpoint de verificación de salud del servicio")
public class HealthController {

    @Operation(
        operationId = "healthCheck",
        summary = "Verifica el estado del servicio",
        responses = {
            @ApiResponse(responseCode = "200", description = "Servicio activo",
                content = @Content(schema = @Schema(implementation = Map.class)))
        })
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "catalogos-api");
        return ResponseEntity.ok(response);
    }
}
