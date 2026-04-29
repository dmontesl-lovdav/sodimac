package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.request.PaymentRegistrationRequest;
import com.sodimac.fiscal.api.model.dto.request.PaymentSearchRequest;
import com.sodimac.fiscal.api.model.dto.response.PaymentRegistrationResponse;
import com.sodimac.fiscal.api.model.dto.response.PaymentSearchResponse;
import com.sodimac.fiscal.api.service.PaymentQueryService;
import com.sodimac.fiscal.api.service.PaymentRegistrationService;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el registro de complementos de pago.
 *
 * Endpoint principal para recibir y procesar complementos de pago CFDI tipo P
 * con todas las validaciones de negocio y SAT requeridas.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/fiscal/complementos-pago")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Complementos de Pago", description = "Registro y gestión de complementos de pago CFDI")
public class PaymentRegistrationController {

    private final PaymentRegistrationService paymentRegistrationService;
    private final PaymentQueryService paymentQueryService;

    /**
     * POST /api/fiscal/complementos-pago/registrar
     *
     * Registra un complemento de pago en el sistema.
     *
     * Recibe: idProveedor, tipoAddenda, tipoProveedor, idUsuario, xmlFile
     *
     * Proceso:
     * 1. Validar estructura XML (XSD Pagos 2.0)
     * 2. Validar con SAT vía multipac
     * 3. Validar reglas de negocio
     * 4. Registrar en base de datos
     * 5. Guardar log y archivo
     *
     * @param request DTO con los parámetros de entrada
     * @return ResponseEntity con el resultado del registro
     */
    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Registrar complemento de pago",
            description = "Registra un complemento de pago CFDI tipo P validando estructura, SAT y reglas de negocio"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Complemento de pago registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentRegistrationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la validación del XML o datos de entrada"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Complemento de pago rechazado por el SAT o reglas de negocio"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<PaymentRegistrationResponse> registrarComplementoPago(
            @Valid @ModelAttribute PaymentRegistrationRequest request,
            @Parameter(description = "UUID de transacción para trazabilidad en auditoría (STM-272)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(value = "idTransaccion", required = false) String idTransaccion) {

        log.info("Recibida solicitud de registro de complemento de pago - " +
                        "Proveedor: {}, Usuario: {}, Archivo: {}, idTransaccion: {}",
                request.getIdProveedor(),
                request.getIdUsuario(),
                request.getXmlFile().getOriginalFilename(),
                idTransaccion);

        // Validar idTransaccion obligatorio (STM-272)
        if (idTransaccion == null || idTransaccion.isBlank()) {
            log.error("idTransaccion no proporcionado");
            return ResponseEntity
                    .badRequest()
                    .body(PaymentRegistrationResponse.builder()
                            .processingStatus("FAILED")
                            .responseCode("FISCAL-ERR-102")
                            .message("El idTransaccion es obligatorio para el registro")
                            .fileName(request.getXmlFile().getOriginalFilename())
                            .build());
        }

        try {
            PaymentRegistrationResponse response = paymentRegistrationService.registerPayment(request, idTransaccion);

            log.info("Complemento de pago registrado exitosamente - UUID: {}, Archivo: {}",
                    response.getPaymentsUuid(),
                    response.getFileName());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error registrando complemento de pago: {}", e.getMessage(), e);

            PaymentRegistrationResponse errorResponse = PaymentRegistrationResponse.builder()
                    .processingStatus("FAILED")
                    .fileName(request.getXmlFile().getOriginalFilename())
                    .message(e.getMessage())
                    .build();

            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(errorResponse);
        }
    }

    /**
     * GET /api/fiscal/complementos-pago/buscar
     *
     * Busca complementos de pago aplicando filtros opcionales.
     *
     * Permite al portal de proveedores consultar complementos de pago
     * con múltiples criterios de búsqueda como RFC, fechas, folio, etc.
     *
     * @param searchRequest Filtros de búsqueda (query parameters)
     * @return Página de complementos de pago que cumplen los criterios
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar complementos de pago",
            description = "Consulta complementos de pago con filtros dinámicos. " +
                         "Todos los filtros son opcionales. Soporta paginación y ordenamiento."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda exitosa (puede retornar lista vacía si no hay resultados)",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<?> buscarComplementosPago(
            @ModelAttribute PaymentSearchRequest searchRequest,
            @RequestHeader(value = "x-user-vendors", required = false) String xUserVendors) {

        List<String> allowedVendors = parseVendorHeader(xUserVendors);
        if (allowedVendors != null && allowedVendors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "WRN7029",
                    "message", "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador"
            ));
        }

        log.info("Búsqueda complementos de pago - Filtros: {}, vendors: {}", searchRequest,
                allowedVendors == null ? "sin restricción" : allowedVendors);

        Page<PaymentSearchResponse> results = paymentQueryService.searchPayments(searchRequest, allowedVendors);

        log.info("Búsqueda completada - {}/{} resultados, página {}/{}",
                results.getNumberOfElements(), results.getTotalElements(),
                results.getNumber() + 1, results.getTotalPages());

        return ResponseEntity.ok(results);
    }

    private List<String> parseVendorHeader(String header) {
        if (header == null) return null;
        String trimmed = header.trim();
        if (trimmed.isEmpty()) return Collections.emptyList();
        if ("-1".equals(trimmed)) return null;
        return Arrays.asList(trimmed.split(","));
    }
}
