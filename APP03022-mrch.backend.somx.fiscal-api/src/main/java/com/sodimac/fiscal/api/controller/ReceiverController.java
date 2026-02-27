package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.ReceiverDto;
import com.sodimac.fiscal.api.service.ReceiverService;
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
 * CONTROLADOR BFF: Catálogo de Receptores
 * ============================================================================
 * Gestiona los receptores de comprobantes fiscales (clientes que reciben facturas).
 *
 * RUTA BASE: /api/receivers
 * MODELO: Receiver (Receptor - quien recibe la factura)
 * DIFERENCIA con /api/receiver-rfc: Este endpoint gestiona receptores generales,
 *                                    mientras que receiver-rfc maneja RFCs autorizados
 *
 * @author Sodimac Tech Team
 */
@RestController
@RequestMapping("/receivers")
@RequiredArgsConstructor
@Tag(name = "Receivers", description = "Gestión de receptores de comprobantes fiscales")
public class ReceiverController {

    private final ReceiverService receiverService;

    /**
     * GET /api/receivers
     * BFF: Obtener catálogo de receptores
     */
    @Operation(summary = "Obtener todos los receptores", description = "Devuelve la lista completa de receptores registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<ReceiverDto>> getAllReceivers() {
        List<ReceiverDto> receivers = receiverService.findAll();
        return ResponseEntity.ok(receivers);
    }

}