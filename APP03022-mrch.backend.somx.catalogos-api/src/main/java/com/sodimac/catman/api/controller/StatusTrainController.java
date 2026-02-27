package com.sodimac.catman.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.catman.api.exception.ExceptionWrapper;
import com.sodimac.catman.api.model.dto.StatusTrainCreateDto;
import com.sodimac.catman.api.model.dto.StatusTrainDto;
import com.sodimac.catman.api.model.dto.StatusTrainListResponse;
import com.sodimac.catman.api.model.dto.StatusTrainUpdateDto;
import com.sodimac.catman.api.model.dto.StatusTrainValidationResponse;
import com.sodimac.catman.api.service.StatusTrainService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para gestión del tren de estatus.
 * Proporciona endpoints para validar y administrar transiciones de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@RestController
@RequestMapping("/status-train")
@Tag(name = "Tren de Estatus", description = "API para gestión de transiciones de estatus permitidas")
public class StatusTrainController {

    private final StatusTrainService service;

    public StatusTrainController(StatusTrainService service) {
        this.service = service;
    }

    @Operation(
        operationId = "validateTransition",
        summary = "Valida si una transición de estatus está permitida",
        description = "Verifica si la transición del estatus origen al destino es válida para la opción especificada",
        responses = {
            @ApiResponse(responseCode = "200", description = "Resultado de la validación",
                content = @Content(schema = @Schema(implementation = StatusTrainValidationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estatus origen o transición no válida",
                content = @Content(schema = @Schema(implementation = StatusTrainValidationResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/validate")
    public ResponseEntity<StatusTrainValidationResponse> validateTransition(
            @Parameter(description = "ID de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)", required = true)
            @RequestParam Integer optionId,
            @Parameter(description = "Estatus origen", required = true)
            @RequestParam Integer sourceStatus,
            @Parameter(description = "Estatus destino", required = true)
            @RequestParam Integer targetStatus) {

        StatusTrainValidationResponse response = service.validateTransition(optionId, sourceStatus, targetStatus);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(
        operationId = "getAllowedDestinations",
        summary = "Obtiene los estatus destino permitidos desde un estatus origen",
        description = "Retorna todos los estatus a los que se puede transicionar desde el estatus origen especificado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de destinos permitidos",
                content = @Content(schema = @Schema(implementation = StatusTrainListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estatus origen no catalogado",
                content = @Content(schema = @Schema(implementation = StatusTrainListResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/allowed-destinations")
    public ResponseEntity<StatusTrainListResponse> getAllowedDestinations(
            @Parameter(description = "ID de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)", required = true)
            @RequestParam Integer optionId,
            @Parameter(description = "Estatus origen", required = true)
            @RequestParam Integer sourceStatus) {

        StatusTrainListResponse response = service.getAllowedDestinations(optionId, sourceStatus);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(
        operationId = "createStatusTrain",
        summary = "Crea una nueva regla de transición de estatus",
        responses = {
            @ApiResponse(responseCode = "201", description = "Regla creada exitosamente",
                content = @Content(schema = @Schema(implementation = StatusTrainDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PostMapping
    public ResponseEntity<StatusTrainDto> create(
            @Valid @RequestBody StatusTrainCreateDto createDto) {

        StatusTrainDto created = service.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        operationId = "updateStatusTrain",
        summary = "Actualiza una regla de transición existente",
        responses = {
            @ApiResponse(responseCode = "200", description = "Regla actualizada exitosamente",
                content = @Content(schema = @Schema(implementation = StatusTrainDto.class))),
            @ApiResponse(responseCode = "404", description = "Regla no encontrada",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PutMapping("/{id}")
    public ResponseEntity<StatusTrainDto> update(
            @Parameter(description = "ID de la regla a actualizar", required = true)
            @PathVariable Integer id,
            @Parameter(description = "ID de la opción para validación", required = true)
            @RequestParam Integer optionId,
            @Valid @RequestBody StatusTrainUpdateDto updateDto) {

        return service.update(id, optionId, updateDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "deleteStatusTrain",
        summary = "Elimina una regla de transición",
        responses = {
            @ApiResponse(responseCode = "204", description = "Regla eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Regla no encontrada",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la regla a eliminar", required = true)
            @PathVariable Integer id) {

        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
        operationId = "getStatusTrainById",
        summary = "Obtiene una regla de transición por su ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Regla encontrada",
                content = @Content(schema = @Schema(implementation = StatusTrainDto.class))),
            @ApiResponse(responseCode = "404", description = "Regla no encontrada",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{id}")
    public ResponseEntity<StatusTrainDto> findById(
            @Parameter(description = "ID de la regla", required = true)
            @PathVariable Integer id) {

        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
