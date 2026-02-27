package com.sodimac.catman.api.controller;

import com.sodimac.catman.api.model.dto.*;
import com.sodimac.catman.api.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/conversions")
@Tag(name = "Conversiones", description = "API para gestión de conversiones entre elementos")
public class ConversionController {

    private final ConversionService svc;
    public ConversionController(ConversionService svc) { this.svc = svc; }

    @Operation(summary = "Buscar conversiones con filtros y paginación")
    @GetMapping
    public ResponseEntity<ConversionPageResponse> search(
            @RequestParam(required = false) Integer idElementoOrigen,
            @RequestParam(required = false) Integer idElemento,
            @RequestParam(required = false) String elemento,
            @RequestParam(required = false) String valorElemento,
            @RequestParam(required = false) String catalogoOrigen,
            @RequestParam(required = false) Integer estatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, sort);
        return ResponseEntity.ok(svc.search(idElementoOrigen, idElemento, elemento, valorElemento, catalogoOrigen, estatus, pageable));
    }

    @Operation(summary = "Consultar conversión por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ConversionDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(svc.getById(id));
    }

    @Operation(summary = "Crear conversión")
    @PostMapping
    public ResponseEntity<ConversionDto> create(
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @Valid @RequestBody ConversionCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(dto, userId));
    }

    @Operation(summary = "Editar conversión")
    @PutMapping("/{id}")
    public ResponseEntity<ConversionDto> update(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @Valid @RequestBody ConversionUpdateDto dto) {
        return ResponseEntity.ok(svc.update(id, dto, userId));
    }

    @Operation(summary = "Cambiar conversión principal")
    @PatchMapping("/{id}/principal")
    public ResponseEntity<ConversionDto> setPrincipal(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @RequestBody Map<String, Boolean> body) {
        Boolean isPrincipal = body.getOrDefault("esPrincipal", body.getOrDefault("conversionPrincipal", true));
        return ResponseEntity.ok(svc.setPrincipal(id, isPrincipal, userId));
    }

    @Operation(summary = "Eliminar conversión individual")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar múltiples conversiones")
    @DeleteMapping
    public ResponseEntity<Void> deleteMultiple(
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @RequestBody Map<String, List<Integer>> body) {
        List<Integer> ids = body.get("ids");
        if (ids != null && !ids.isEmpty()) svc.deleteMultiple(ids);
        return ResponseEntity.noContent().build();
    }
}

