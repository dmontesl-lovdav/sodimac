package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.IssuerDto;
import com.sodimac.fiscal.api.service.IssuerService;
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
 * CONTROLADOR BFF: Catálogo de Emisores
 * ============================================================================
 * Gestiona los emisores de comprobantes fiscales (empresas que emiten facturas).
 *
 * RUTA BASE: /api/issuers
 * MODELO: Issuer (Emisor - quien emite la factura)
 * PATRÓN: BFF simple - Solo consulta sin lógica compleja
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/issuers")
@RequiredArgsConstructor
@Tag(name = "Issuers", description = "Gestión de emisores de comprobantes fiscales")
public class IssuerController {

    private final IssuerService issuerService;

    /**
     * GET /api/issuers
     * BFF: Obtener catálogo de emisores
     */
    @Operation(summary = "Obtener todos los emisores", description = "Devuelve la lista completa de emisores registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<IssuerDto>> getAllIssuers() {
        List<IssuerDto> issuers = issuerService.findAll();
        return ResponseEntity.ok(issuers);
    }

}