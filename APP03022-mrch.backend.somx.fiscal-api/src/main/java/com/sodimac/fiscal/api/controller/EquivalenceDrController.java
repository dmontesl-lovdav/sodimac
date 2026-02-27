package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.EquivalenceDrDto;
import com.sodimac.fiscal.api.service.EquivalenceDrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * CONTROLADOR BFF: Equivalencias DR
 * ============================================================================
 * Gestiona equivalencias de DR (Documento Relacionado) en CFDI.
 *
 * RUTA BASE: /api/equivalence-dr
 * MODELO: EquivalenceDr
 * PAGINACIÓN: Habilitada
 *
 * CONTEXTO FISCAL:
 * Las equivalencias DR permiten relacionar documentos fiscales cuando
 * existen diferentes formatos o versiones del mismo comprobante.
 * Mantiene la trazabilidad entre documentos equivalentes.
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/equivalence-dr")
@RequiredArgsConstructor
@Tag(name = "Equivalence DR", description = "Gestión de equivalencias de DR")
public class EquivalenceDrController {

    private final EquivalenceDrService equivalenceDrService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/equivalence-dr?page=0
     * Obtener equivalencias DR con paginación
     */
    @Operation(summary = "Obtener todas las equivalencias DR", description = "Devuelve las equivalencias DR paginadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<EquivalenceDrDto>> getAllEquivalenceDrs(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<EquivalenceDrDto> equivalenceDrs = equivalenceDrService.findAll(pageable);
        return ResponseEntity.ok(equivalenceDrs);
    }

}