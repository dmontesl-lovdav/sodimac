package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.PaymentDto;
import com.sodimac.fiscal.api.service.PaymentService;
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
 * CONTROLADOR BFF: Gestión de Pagos
 * ============================================================================
 * Maneja consultas de pagos y complementos de pago del sistema fiscal.
 *
 * RUTA BASE: /api/payments
 * MODELO: Payment (Complemento de Pagos 2.0 del SAT)
 * PAGINACIÓN: Habilitada con PaginationConfig
 *
 * CONTEXTO FISCAL:
 * Los complementos de pago se utilizan cuando el pago de una factura
 * se realiza en una fecha diferente a la emisión del comprobante.
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Gestión de pagos")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaginationConfig paginationConfig;

    /**
     * GET /api/payments?page=0
     * Obtener pagos con paginación
     */
    @Operation(summary = "Obtener todos los pagos", description = "Devuelve los pagos paginados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<PaymentDto>> getAllPayments(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());
        Page<PaymentDto> payments = paymentService.findAll(pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payments/{uuid}
     * Obtener un complemento de pago específico por UUID
     */
    @Operation(
            summary = "Obtener complemento de pago por UUID",
            description = "Devuelve un complemento de pago específico identificado por su UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Complemento de pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Complemento de pago no encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID inválido")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<PaymentDto> getPaymentByUuid(
            @Parameter(description = "UUID del complemento de pago", required = true)
            @PathVariable UUID uuid) {

        return paymentService.findByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}