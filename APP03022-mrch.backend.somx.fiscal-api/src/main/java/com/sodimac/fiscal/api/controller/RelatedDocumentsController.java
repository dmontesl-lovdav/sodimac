package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.RelatedDocumentsDto;
import com.sodimac.fiscal.api.service.RelatedDocumentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ============================================================================
 * CONTROLADOR BFF: Documentos Relacionados
 * ============================================================================
 * Gestiona documentos relacionados en complementos fiscales (CartaPorte, Pagos).
 *
 * RUTA BASE: /api/related-documents
 * MODELO: RelatedDocuments
 * PAGINACIÓN: Habilitada
 *
 * DIFERENCIA CON /api/related-cfdi:
 * - /api/related-cfdi: Relaciona CFDIs completos entre sí (nivel documento)
 * - /api/related-documents: Relaciona documentos dentro de complementos (nivel detalle)
 *
 * CONTEXTO FISCAL:
 * Los documentos relacionados se utilizan en complementos como:
 * - CartaPorte: Documentos relacionados al transporte de mercancías
 * - Pagos: Documentos relacionados al pago (facturas que se están pagando)
 * - Comercio Exterior: Documentos aduaneros relacionados
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/related-documents")
@RequiredArgsConstructor
@Tag(name = "Related Documents", description = "Gestión de documentos relacionados")
public class RelatedDocumentsController {

    private final RelatedDocumentsService relatedDocumentsService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/related-documents?page=0
     * Obtener documentos relacionados con paginación
     */
    @Operation(summary = "Obtener todos los documentos relacionados", description = "Devuelve los documentos relacionados paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<RelatedDocumentsDto>> getAllRelatedDocuments(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<RelatedDocumentsDto> relatedDocuments = relatedDocumentsService.findAll(pageable);
        return ResponseEntity.ok(relatedDocuments);
    }

    /**
     * GET /api/related-documents/by-payment/{paymentsUuid}?page=0
     * Obtener facturas relacionadas de un complemento de pago específico
     *
     * Este endpoint permite consultar todas las facturas relacionadas (documentos que se están pagando)
     * de un complemento de pago específico utilizando el UUID del complemento de pago.
     *
     * @param paymentsUuid UUID del complemento de pago (payments_uuid de la tabla payments)
     * @param page Número de página (default 0)
     * @return Página de documentos relacionados con información de cada factura pagada
     */
    @Operation(
            summary = "Obtener facturas relacionadas de un complemento de pago",
            description = "Devuelve las facturas relacionadas (documentos pagados) de un complemento de pago específico. " +
                         "Incluye información de monto pagado, saldo anterior, saldo restante, serie y folio de cada factura."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facturas relacionadas obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "UUID inválido"),
            @ApiResponse(responseCode = "404", description = "Complemento de pago no encontrado")
    })
    @GetMapping("/by-payment/{paymentsUuid}")
    public ResponseEntity<Page<RelatedDocumentsDto>> getRelatedDocumentsByPayment(
            @Parameter(description = "UUID del complemento de pago", required = true)
            @PathVariable UUID paymentsUuid,
            @Parameter(description = "Número de página (comienza en 0)")
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<RelatedDocumentsDto> relatedDocuments = relatedDocumentsService.findByPaymentsUuid(paymentsUuid, pageable);
        return ResponseEntity.ok(relatedDocuments);
    }

}