package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.VersionCatalogDto;
import com.sodimac.fiscal.api.service.VersionCatalogService;
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
 * CONTROLADOR BFF: Catálogo de Versiones CFDI
 * ============================================================================
 * Gestiona catálogo de versiones de CFDI y complementos soportados por el sistema.
 *
 * RUTA BASE: /api/version-catalog
 * MODELO: VersionCatalog
 * PAGINACIÓN: No (catálogo estático)
 *
 * CONTEXTO FISCAL:
 * El SAT ha publicado diferentes versiones de CFDI y complementos:
 *
 * CFDI (Comprobante):
 * - CFDI 3.3 (2017-2023)
 * - CFDI 4.0 (2022-actual)
 *
 * Complementos:
 * - CartaPorte 2.0, 3.0, 3.1
 * - Pagos 1.0, 2.0
 * - Comercio Exterior 1.1, 2.0
 *
 * Este catálogo permite al sistema identificar y procesar
 * correctamente cada versión de documento fiscal.
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/version-catalog")
@RequiredArgsConstructor
@Tag(name = "Version Catalog", description = "Gestión de catálogo de versiones de CFDI")
public class VersionCatalogController {

    private final VersionCatalogService versionCatalogService;

    /**
     * GET /api/version-catalog
     * Obtener catálogo completo de versiones CFDI y complementos
     */
    @Operation(summary = "Obtener todas las versiones", description = "Devuelve la lista completa de versiones de CFDI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<VersionCatalogDto>> getAllVersions() {
        List<VersionCatalogDto> versions = versionCatalogService.findAll();
        return ResponseEntity.ok(versions);
    }

}