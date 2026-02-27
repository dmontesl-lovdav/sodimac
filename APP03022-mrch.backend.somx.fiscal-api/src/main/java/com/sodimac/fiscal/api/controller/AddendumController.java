package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.AddendumDto;
import com.sodimac.fiscal.api.service.AddendumService;
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
 * CONTROLADOR BFF: Gestión de Addendas
 * ============================================================================
 * Maneja consultas de addendas (información adicional) de comprobantes fiscales.
 *
 * RUTA BASE: /api/addendums
 * MODELO: Addendum (Información adicional no fiscal en CFDI)
 * PAGINACIÓN: Habilitada
 *
 * CONTEXTO FISCAL:
 * Las addendas son secciones opcionales del CFDI donde las empresas
 * pueden incluir información adicional específica de sus procesos
 * de negocio (números de orden, referencias internas, etc.).
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/addendums")
@RequiredArgsConstructor
@Tag(name = "Addendums", description = "Gestión de addendas de comprobantes")
public class AddendumController {

    private final AddendumService addendumService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/addendums?page=0
     * Obtener addendas con paginación
     */
    @Operation(summary = "Obtener todas las addendas", description = "Devuelve las addendas paginadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<AddendumDto>> getAllAddendums(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<AddendumDto> addendums = addendumService.findAll(pageable);
        return ResponseEntity.ok(addendums);
    }

}