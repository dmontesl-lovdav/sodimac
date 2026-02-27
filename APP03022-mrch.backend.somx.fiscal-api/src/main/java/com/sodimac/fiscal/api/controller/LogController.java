package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.LogDto;
import com.sodimac.fiscal.api.service.LogService;
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
 * CONTROLADOR BFF: Logs del Sistema
 * ============================================================================
 * Gestiona consulta de logs de auditoría y operaciones del sistema fiscal.
 *
 * RUTA BASE: /api/logs
 * MODELO: Log (Registros de auditoría)
 * PAGINACIÓN: Habilitada
 *
 * FUNCIONALIDAD:
 * Proporciona acceso a logs de:
 * - Procesamiento de XML fiscales
 * - Timbrado con PAC
 * - Validaciones fiscales
 * - Errores y excepciones
 * - Auditoría de operaciones
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Tag(name = "Logs", description = "Gestión de logs del sistema")
public class LogController {

    private final LogService logService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/logs?page=0
     * Obtener logs del sistema con paginación
     */
    @Operation(summary = "Obtener todos los logs", description = "Devuelve los logs paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<LogDto>> getAllLogs(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<LogDto> logs = logService.findAll(pageable);
        return ResponseEntity.ok(logs);
    }

}