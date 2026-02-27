package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.service.PacCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * CONTROLADOR BFF: Catálogo de PACs (Proveedores Autorizados de Certificación)
 * ============================================================================
 * Gestiona el catálogo de PACs que timbran los comprobantes fiscales.
 *
 * RUTA BASE: /api/pac-catalog
 * MODELO: PacCatalog (DETECNO, CARBAJAL, etc.)
 *
 * CONTEXTO: Los PACs son empresas autorizadas por el SAT (México) para
 *           certificar y timbrar facturas electrónicas (CFDI).
 *           Este proyecto soporta múltiples PACs con implementaciones específicas.
 *
 * PATRÓN MULTI-PAC BFF:
 * - Este endpoint lista los PACs disponibles
 * - Cada PAC tiene su propia implementación en service/impl/
 *   * PacServiceDetecnoImpl
 *   * PacServiceCabajalImpl
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/pac-catalog")
@RequiredArgsConstructor
@Tag(name = "PAC Catalog", description = "Gestión de catálogo de Proveedores Autorizados de Certificación")
public class PacCatalogController {

    private final PacCatalogService pacCatalogService;

    /**
     * GET /api/pac-catalog
     * BFF: Obtener catálogo de PACs disponibles para timbrado
     */
    @Operation(summary = "Obtener todos los PACs", description = "Devuelve la lista completa de proveedores autorizados de certificación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<PacCatalogDto>> getAllPacs() {
        List<PacCatalogDto> pacs = pacCatalogService.findAll();
        return ResponseEntity.ok(pacs);
    }

}