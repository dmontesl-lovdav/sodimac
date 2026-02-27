package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.TotalsDto;
import com.sodimac.fiscal.api.service.TotalsService;
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
 * CONTROLADOR BFF: Totales de CFDI
 * ============================================================================
 * Gestiona totales calculados de comprobantes fiscales digitales.
 *
 * RUTA BASE: /api/totals
 * MODELO: Totals (Totales: subtotal, impuestos, descuentos, total)
 * PAGINACIÓN: Habilitada
 *
 * CONTEXTO FISCAL:
 * Los totales de un CFDI incluyen:
 * - SubTotal: Suma de importes antes de impuestos
 * - TotalImpuestosTraslados: IVA, IEPS trasladados
 * - TotalImpuestosRetenidos: ISR, IVA retenidos
 * - Descuento: Descuentos aplicados
 * - Total: Monto total del comprobante
 *
 * CÁLCULO SEGÚN SAT:
 * Total = SubTotal - Descuento + TotalImpuestosTraslados - TotalImpuestosRetenidos
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/totals")
@RequiredArgsConstructor
@Tag(name = "Totals", description = "Gestión de totales")
public class TotalsController {

    private final TotalsService totalsService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/totals?page=0
     * Obtener totales de CFDIs con paginación
     */
    @Operation(summary = "Obtener todos los totales", description = "Devuelve los totales paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<TotalsDto>> getAllTotals(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<TotalsDto> totals = totalsService.findAll(pageable);
        return ResponseEntity.ok(totals);
    }

}