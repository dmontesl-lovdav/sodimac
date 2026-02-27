package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.AuthorizedReceiverCatalogDto;
import com.sodimac.fiscal.api.service.AuthorizedReceiverCatalogService;
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
 * CONTROLADOR BFF: Catálogo de Receptores Autorizados
 * ============================================================================
 * Este controlador implementa el patrón Backend-for-Frontend (BFF) para
 * la gestión de receptores autorizados para recibir facturas electrónicas.
 *
 * RESPONSABILIDADES DEL BFF:
 * - Exponer endpoints REST simplificados para el frontend
 * - Orquestar llamadas al servicio de negocio (AuthorizedReceiverCatalogService)
 * - Transformar entidades de dominio a DTOs optimizados para el cliente
 * - Documentar API con anotaciones OpenAPI (Swagger)
 *
 * RUTA BASE: /api/authorized-receivers
 * PUERTO: 8087 (configurado en application.properties)
 * SEGURIDAD: JWT con KeyCloak (si security.enabled=true)
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/authorized-receivers")
@RequiredArgsConstructor
@Tag(name = "Authorized Receivers", description = "Gestión de catálogo de receptores autorizados")
public class AuthorizedReceiverCatalogController {

    // INYECCIÓN DE DEPENDENCIAS (Patrón BFF)
    // El controlador NO contiene lógica de negocio, solo orquesta servicios
    private final AuthorizedReceiverCatalogService authorizedReceiverCatalogService;

    /**
     * GET /api/authorized-receivers
     *
     * ENDPOINT BFF: Obtener catálogo completo de receptores autorizados
     *
     * FLUJO BFF:
     * 1. Cliente frontend hace GET a este endpoint
     * 2. BFF llama al servicio de negocio (findAll)
     * 3. Servicio consulta repositorio JPA (base de datos)
     * 4. BFF transforma entidades a DTOs
     * 5. BFF retorna JSON al cliente
     *
     * NOTA: Este endpoint NO tiene paginación. Si la lista crece mucho,
     *       considerar agregar paginación como en InvoiceController.
     *
     * @return Lista completa de receptores autorizados como JSON
     */
    @Operation(summary = "Obtener todos los receptores autorizados", description = "Devuelve la lista completa de receptores autorizados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<AuthorizedReceiverCatalogDto>> getAllAuthorizedReceivers() {
        // Delegación al servicio (BFF no contiene lógica de negocio)
        List<AuthorizedReceiverCatalogDto> authorizedReceivers = authorizedReceiverCatalogService.findAll();

        // Retorno con código HTTP 200 y body JSON
        return ResponseEntity.ok(authorizedReceivers);
    }

}