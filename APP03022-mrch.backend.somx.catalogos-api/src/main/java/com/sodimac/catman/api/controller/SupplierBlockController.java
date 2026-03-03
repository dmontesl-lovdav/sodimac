package com.sodimac.catman.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.catman.api.exception.ExceptionWrapper;
import com.sodimac.catman.api.model.dto.SupplierBlockCreateDto;
import com.sodimac.catman.api.model.dto.SupplierBlockDto;
import com.sodimac.catman.api.model.dto.SupplierBlockResponseDto;
import com.sodimac.catman.api.model.dto.SupplierBlockUpdateDto;
import com.sodimac.catman.api.service.SupplierBlockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller para gestion de bloqueos de proveedores.
 * STM-1224: Bloqueo de proveedores por rango de fechas.
 */
@RestController
@RequestMapping("/supplier-blocks")
@Tag(name = "Bloqueos de Proveedores", description = "API para gestion de bloqueos temporales de proveedores")
public class SupplierBlockController {

    private final SupplierBlockService supplierBlockService;

    public SupplierBlockController(SupplierBlockService supplierBlockService) {
        this.supplierBlockService = supplierBlockService;
    }

    @Operation(
        operationId = "getAllSupplierBlocks",
        summary = "Obtiene todos los bloqueos",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de bloqueos",
                content = @Content(schema = @Schema(implementation = SupplierBlockDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping
    public ResponseEntity<List<SupplierBlockDto>> getAllSupplierBlocks(
            @Parameter(description = "Estado: 1=Activo, 0=Inactivo. Por defecto: solo activos")
            @RequestParam(required = false) Integer status) {
        List<SupplierBlockDto> blocks = status != null
                ? supplierBlockService.findByStatus(status)
                : supplierBlockService.findAll();
        return ResponseEntity.ok(blocks);
    }

    @Operation(
        operationId = "getSupplierBlockById",
        summary = "Obtiene un bloqueo por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Bloqueo encontrado",
                content = @Content(schema = @Schema(implementation = SupplierBlockDto.class))),
            @ApiResponse(responseCode = "404", description = "Bloqueo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{id}")
    public ResponseEntity<SupplierBlockDto> getSupplierBlockById(
            @Parameter(description = "ID del bloqueo", required = true)
            @PathVariable Integer id) {
        return supplierBlockService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getBlocksBySupplierNumber",
        summary = "Obtiene los bloqueos de un proveedor",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de bloqueos del proveedor",
                content = @Content(schema = @Schema(implementation = SupplierBlockDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/supplier/{supplierNumber}")
    public ResponseEntity<List<SupplierBlockDto>> getBlocksBySupplierNumber(
            @Parameter(description = "Numero de proveedor", required = true)
            @PathVariable String supplierNumber) {
        return ResponseEntity.ok(supplierBlockService.findBySupplierNumber(supplierNumber));
    }

    @Operation(
        operationId = "getCurrentActiveBlocks",
        summary = "Obtiene los bloqueos activos vigentes de un proveedor",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de bloqueos vigentes",
                content = @Content(schema = @Schema(implementation = SupplierBlockDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/supplier/{supplierNumber}/active")
    public ResponseEntity<List<SupplierBlockDto>> getCurrentActiveBlocks(
            @Parameter(description = "Numero de proveedor", required = true)
            @PathVariable String supplierNumber) {
        return ResponseEntity.ok(supplierBlockService.findCurrentActiveBlocks(supplierNumber));
    }

    @Operation(
        operationId = "isSupplierBlocked",
        summary = "Verifica si un proveedor esta actualmente bloqueado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Estado de bloqueo del proveedor"),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/supplier/{supplierNumber}/is-blocked")
    public ResponseEntity<Map<String, Object>> isSupplierBlocked(
            @Parameter(description = "Numero de proveedor", required = true)
            @PathVariable String supplierNumber) {
        boolean blocked = supplierBlockService.isSupplierCurrentlyBlocked(supplierNumber);
        return ResponseEntity.ok(Map.of(
                "supplierNumber", supplierNumber,
                "blocked", blocked
        ));
    }

    @Operation(
        operationId = "getBlocksAtDate",
        summary = "Obtiene los bloqueos activos a una fecha especifica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de bloqueos a la fecha",
                content = @Content(schema = @Schema(implementation = SupplierBlockDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/supplier/{supplierNumber}/at-date")
    public ResponseEntity<List<SupplierBlockDto>> getBlocksAtDate(
            @Parameter(description = "Numero de proveedor", required = true)
            @PathVariable String supplierNumber,
            @Parameter(description = "Fecha a consultar (formato: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(supplierBlockService.findActiveBlocksAtDate(supplierNumber, date));
    }

    @Operation(
        operationId = "createSupplierBlock",
        summary = "Crea un nuevo bloqueo de proveedor",
        responses = {
            @ApiResponse(responseCode = "201", description = "Bloqueo creado",
                content = @Content(schema = @Schema(implementation = SupplierBlockResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, proveedor no existe o bloqueo solapado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PostMapping
    public ResponseEntity<SupplierBlockResponseDto> createSupplierBlock(
            @Valid @RequestBody SupplierBlockCreateDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        SupplierBlockResponseDto created = supplierBlockService.create(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        operationId = "updateSupplierBlock",
        summary = "Actualiza un bloqueo existente",
        responses = {
            @ApiResponse(responseCode = "200", description = "Bloqueo actualizado",
                content = @Content(schema = @Schema(implementation = SupplierBlockResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Bloqueo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o bloqueo solapado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PutMapping("/{id}")
    public ResponseEntity<SupplierBlockResponseDto> updateSupplierBlock(
            @Parameter(description = "ID del bloqueo", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody SupplierBlockUpdateDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return supplierBlockService.update(id, dto, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "deleteSupplierBlock",
        summary = "Elimina (desactiva) un bloqueo",
        responses = {
            @ApiResponse(responseCode = "204", description = "Bloqueo eliminado"),
            @ApiResponse(responseCode = "404", description = "Bloqueo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplierBlock(
            @Parameter(description = "ID del bloqueo", required = true)
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        boolean deleted = supplierBlockService.delete(id, userId);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
