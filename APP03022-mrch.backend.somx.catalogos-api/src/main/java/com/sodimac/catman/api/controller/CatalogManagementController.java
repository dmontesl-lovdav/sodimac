package com.sodimac.catman.api.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.sodimac.catman.api.model.dto.CatalogCreateDto;
import com.sodimac.catman.api.model.dto.CatalogPageResponse;
import com.sodimac.catman.api.model.dto.CatalogResponseDto;
import com.sodimac.catman.api.model.dto.CatalogUpdateDto;
import com.sodimac.catman.api.service.CatalogManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/catalogos")
@Tag(name = "Gestión de Catálogos", description = "API para administración de catálogos")
public class CatalogManagementController {

    private final CatalogManagementService catalogService;

    public CatalogManagementController(CatalogManagementService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(
        operationId = "getCatalogs",
        summary = "Listar catálogos con filtros y paginación",
        description = "Retorna catálogos con filtros opcionales. Por defecto 10 registros por página, ordenados por fecha de registro descendente.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de catálogos",
                content = @Content(schema = @Schema(implementation = CatalogPageResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping
    public ResponseEntity<CatalogPageResponse> getCatalogs(
            @Parameter(description = "ID del catálogo (filtro exacto)")
            @RequestParam(required = false) Integer id,
            @Parameter(description = "Nombre del catálogo (filtro parcial)")
            @RequestParam(required = false) String nombre,
            @Parameter(description = "Descripción del catálogo (filtro parcial)")
            @RequestParam(required = false) String descripcion,
            @Parameter(description = "Tipo de catálogo: PRIMARIO, SECUNDARIO")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Código del catálogo (filtro parcial)")
            @RequestParam(required = false) String code,
            @Parameter(description = "Prefijo del catálogo (filtro parcial)")
            @RequestParam(required = false) String prefix,
            @Parameter(description = "Estatus: 1=Activo, 0=Inactivo")
            @RequestParam(required = false) Integer estatus,
            @Parameter(description = "Número de página (1-indexed)")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "Campo para ordenar")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de orden: asc, desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        int pageIndex = Math.max(0, page - 1);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

        CatalogPageResponse response = catalogService.findCatalogs(
                id, nombre, descripcion, tipo, estatus, code, prefix, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(
        operationId = "getCatalogById",
        summary = "Obtener catálogo por ID",
        description = "Retorna el detalle completo de un catálogo para edición",
        responses = {
            @ApiResponse(responseCode = "200", description = "Catálogo encontrado",
                content = @Content(schema = @Schema(implementation = CatalogResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{id}")
    public ResponseEntity<CatalogResponseDto> getCatalogById(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer id) {

        return catalogService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "createCatalog",
        summary = "Crear un nuevo catálogo",
        description = "Crea un catálogo con los datos generales. El catálogo inicia con estatus Inactivo.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Catálogo creado",
                content = @Content(schema = @Schema(implementation = CatalogResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PostMapping
    public ResponseEntity<CatalogResponseDto> createCatalog(
            @Parameter(description = "ID del usuario que crea")
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @Valid @RequestBody CatalogCreateDto createDto) {

        CatalogResponseDto created = catalogService.createCatalog(createDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        operationId = "updateCatalog",
        summary = "Actualizar un catálogo",
        description = "Actualiza los datos de un catálogo existente (nombre, descripción, tipo)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Catálogo actualizado",
                content = @Content(schema = @Schema(implementation = CatalogResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PutMapping("/{id}")
    public ResponseEntity<CatalogResponseDto> updateCatalog(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer id,
            @Parameter(description = "ID del usuario que actualiza")
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @Valid @RequestBody CatalogUpdateDto updateDto) {

        CatalogResponseDto updated = catalogService.updateCatalog(id, updateDto, userId);
        return ResponseEntity.ok(updated);
    }
}







