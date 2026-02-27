package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.PaymentsDto;
import com.sodimac.fiscal.api.service.PaymentsService;
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
 * CONTROLADOR BFF: Documentos de Pago (Complemento de Pagos 2.0)
 * ============================================================================
 * Gestiona documentos de complemento de pago del SAT.
 *
 * RUTA BASE: /api/payment-documents
 * MODELO: PaymentsDto (Complemento de Pagos 2.0)
 * PAGINACIÓN: Habilitada
 *
 * DIFERENCIA CON /api/payments:
 * - /api/payments: Gestiona entidades individuales de pago
 * - /api/payment-documents: Gestiona documentos CFDI de complemento de pago completos
 *
 * CONTEXTO FISCAL:
 * El complemento de pago es un documento XML que relaciona pagos
 * realizados con las facturas originales cuando el pago se efectúa
 * en una fecha posterior a la emisión del CFDI.
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/payment-documents")
@RequiredArgsConstructor
@Tag(name = "Payment Documents", description = "Gestión de documentos de pago")
public class PaymentsDocumentsController {

    private final PaymentsService paymentsService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/payment-documents?page=0
     * Obtener documentos de complemento de pago con paginación
     */
    @Operation(summary = "Obtener todos los documentos de pago", description = "Devuelve los documentos de pago paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<PaymentsDto>> getAllPayments(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<PaymentsDto> payments = paymentsService.findAll(pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payment-documents/{uuid}
     * Obtener un documento de complemento de pago específico por UUID
     */
    @Operation(
            summary = "Obtener documento de pago por UUID",
            description = "Devuelve un documento de complemento de pago específico identificado por su UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento de pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Documento de pago no encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID inválido")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<PaymentsDto> getPaymentDocumentByUuid(
            @Parameter(description = "UUID del documento de complemento de pago", required = true)
            @PathVariable UUID uuid) {

        return paymentsService.findByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}