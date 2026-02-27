package com.sodimac.catman.api.controller;

import java.util.List;

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
import com.sodimac.catman.api.model.dto.PaymentConditionDto;
import com.sodimac.catman.api.model.dto.SupplierCreateDto;
import com.sodimac.catman.api.model.dto.SupplierDto;
import com.sodimac.catman.api.model.dto.SupplierFilterDto;
import com.sodimac.catman.api.model.dto.SupplierTypeDto;
import com.sodimac.catman.api.model.dto.SupplierUpdateDto;
import com.sodimac.catman.api.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/suppliers")
@Tag(name = "Proveedores", description = "API para gestion de proveedores")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Operation(
        operationId = "getAllSuppliers",
        summary = "Obtiene todos los proveedores",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de proveedores",
                content = @Content(schema = @Schema(implementation = SupplierDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping
    public ResponseEntity<List<SupplierDto>> getAllSuppliers(
            @Parameter(description = "Estado: 1=Activo, 0=Inactivo. Por defecto: solo activos")
            @RequestParam(required = false) Integer status) {
        List<SupplierDto> suppliers = status != null
                ? supplierService.findByStatus(status)
                : supplierService.findAll();
        return ResponseEntity.ok(suppliers);
    }

    @Operation(
        operationId = "getSupplierById",
        summary = "Obtiene un proveedor por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> getSupplierById(
            @Parameter(description = "ID del proveedor", required = true)
            @PathVariable Integer id) {
        return supplierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getSupplierByNumber",
        summary = "Obtiene un proveedor por numero",
        responses = {
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/number/{supplierNumber}")
    public ResponseEntity<SupplierDto> getSupplierByNumber(
            @Parameter(description = "Numero de proveedor", required = true)
            @PathVariable String supplierNumber) {
        return supplierService.findBySupplierNumber(supplierNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getSupplierByRfc",
        summary = "Obtiene un proveedor por RFC",
        responses = {
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/rfc/{rfc}")
    public ResponseEntity<SupplierDto> getSupplierByRfc(
            @Parameter(description = "RFC del proveedor", required = true)
            @PathVariable String rfc) {
        return supplierService.findByRfc(rfc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "createSupplier",
        summary = "Crea un nuevo proveedor",
        responses = {
            @ApiResponse(responseCode = "201", description = "Proveedor creado",
                content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PostMapping
    public ResponseEntity<SupplierDto> createSupplier(
            @Valid @RequestBody SupplierCreateDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        SupplierDto created = supplierService.create(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        operationId = "updateSupplier",
        summary = "Actualiza un proveedor existente",
        responses = {
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado",
                content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PutMapping("/{id}")
    public ResponseEntity<SupplierDto> updateSupplier(
            @Parameter(description = "ID del proveedor", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody SupplierUpdateDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return supplierService.update(id, dto, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "deleteSupplier",
        summary = "Elimina (desactiva) un proveedor",
        responses = {
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "ID del proveedor", required = true)
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        boolean deleted = supplierService.delete(id, userId);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(
        operationId = "getAllSupplierTypes",
        summary = "Obtiene todos los tipos de proveedor",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de proveedor",
                content = @Content(schema = @Schema(implementation = SupplierTypeDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/types")
    public ResponseEntity<List<SupplierTypeDto>> getAllSupplierTypes() {
        return ResponseEntity.ok(supplierService.findAllSupplierTypes());
    }

    @Operation(
        operationId = "getAllPaymentConditions",
        summary = "Obtiene todas las condiciones de pago",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de condiciones de pago",
                content = @Content(schema = @Schema(implementation = PaymentConditionDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/payment-conditions")
    public ResponseEntity<List<PaymentConditionDto>> getAllPaymentConditions() {
        return ResponseEntity.ok(supplierService.findAllPaymentConditions());
    }

    @Operation(
        operationId = "filterSuppliers",
        summary = "STM-831: Filtra proveedores por tipo y estado de bloqueo",
        description = "Permite filtrar proveedores por tipo (0=todos, 1=mercancia, 2=transporte, 3=indirectos, 4=servicios) " +
                     "y por estado de bloqueo (0=solo bloqueados, 1=solo activos, 2=todos)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de proveedores filtrados",
                content = @Content(schema = @Schema(implementation = SupplierFilterDto[].class))),
            @ApiResponse(responseCode = "400", description = "Tipo de proveedor invalido",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/filter")
    public ResponseEntity<List<SupplierFilterDto>> filterSuppliers(
            @Parameter(description = "Tipo de proveedor: 0=Todos, 1=Mercancia, 2=Transporte, 3=Indirectos, 4=Servicios", required = true)
            @RequestParam Integer tipoProveedor,
            @Parameter(description = "Estado de bloqueo: 0=Solo bloqueados, 1=Solo activos, 2=Todos", required = true)
            @RequestParam Integer estatusBloqueo) {
        List<SupplierFilterDto> suppliers = supplierService.findByTypeAndBlockStatus(tipoProveedor, estatusBloqueo);
        return ResponseEntity.ok(suppliers);
    }
}
