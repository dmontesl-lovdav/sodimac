package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.RelatedCfdiDto;
import com.sodimac.fiscal.api.service.RelatedCfdiService;
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
 * CONTROLADOR BFF: CFDIs Relacionados
 * ============================================================================
 * Gestiona relaciones entre comprobantes fiscales digitales (CFDI).
 *
 * RUTA BASE: /api/related-cfdi
 * MODELO: RelatedCfdi
 * PAGINACIÓN: Habilitada
 *
 * CONTEXTO FISCAL:
 * Los CFDIs relacionados permiten vincular comprobantes fiscales entre sí,
 * por ejemplo:
 * - Nota de crédito relacionada con factura original
 * - CFDI de sustitución
 * - CFDI de cancelación
 * - Traslado relacionado con compra
 *
 * Tipos de relación definidos por el SAT (clave TipoRelacion):
 * 01 - Nota de crédito de los documentos relacionados
 * 02 - Nota de débito de los documentos relacionados
 * 03 - Devolución de mercancía sobre facturas o traslados previos
 * 04 - Sustitución de los CFDI previos
 * 07 - CFDI por aplicación de anticipo
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/related-cfdi")
@RequiredArgsConstructor
@Tag(name = "Related CFDI", description = "Gestión de CFDIs relacionados")
public class RelatedCfdiController {

    private final RelatedCfdiService relatedCfdiService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/related-cfdi?page=0
     * Obtener CFDIs relacionados con paginación
     */
    @Operation(summary = "Obtener todos los CFDIs relacionados", description = "Devuelve los CFDIs relacionados paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<RelatedCfdiDto>> getAllRelatedCfdis(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<RelatedCfdiDto> relatedCfdis = relatedCfdiService.findAll(pageable);
        return ResponseEntity.ok(relatedCfdis);
    }

}