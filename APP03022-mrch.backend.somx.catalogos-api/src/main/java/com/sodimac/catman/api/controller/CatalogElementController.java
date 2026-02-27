package com.sodimac.catman.api.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.catman.api.exception.ExceptionWrapper;
import com.sodimac.catman.api.model.dto.CatalogElementCreateDto;
import com.sodimac.catman.api.model.dto.CatalogElementDto;
import com.sodimac.catman.api.model.dto.CatalogElementPageResponse;
import com.sodimac.catman.api.model.dto.CatalogElementStatusDto;
import com.sodimac.catman.api.model.dto.CatalogElementUpdateDto;
import com.sodimac.catman.api.model.dto.CatalogSimpleDto;
import com.sodimac.catman.api.service.CatalogElementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/catalogos")
@Tag(name = "Elementos de Catálogo", description = "API para gestión de elementos de catálogo")
public class CatalogElementController {

    private final CatalogElementService elementService;

    public CatalogElementController(CatalogElementService elementService) {
        this.elementService = elementService;
    }

    @Operation(
        operationId = "getElements",
        summary = "Consultar elementos de un catálogo",
        description = "Retorna elementos con filtros opcionales y paginación. Por defecto 10 registros por página.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de elementos",
                content = @Content(schema = @Schema(implementation = CatalogElementPageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{catalogId}/elementos")
    public ResponseEntity<CatalogElementPageResponse> getElements(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer catalogId,
            @Parameter(description = "ID del elemento (filtro exacto)")
            @RequestParam(required = false) Integer idElemento,
            @Parameter(description = "Nombre del elemento (filtro parcial)")
            @RequestParam(required = false) String elemento,
            @Parameter(description = "Valor del elemento (filtro parcial)")
            @RequestParam(required = false) String valor,
            @Parameter(description = "ID del catálogo padre")
            @RequestParam(required = false) Integer idCatalogoPadre,
            @Parameter(description = "ID del elemento padre")
            @RequestParam(required = false) Integer idElementoPadre,
            @Parameter(description = "Clave del elemento (filtro parcial)")
            @RequestParam(required = false) String clave,
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

        CatalogElementPageResponse response = elementService.findElements(
                catalogId, idElemento, elemento, valor, idCatalogoPadre, idElementoPadre, estatus, clave, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(
        operationId = "getElementById",
        summary = "Obtener detalle de un elemento",
        description = "Retorna el detalle completo de un elemento para edición",
        responses = {
            @ApiResponse(responseCode = "200", description = "Elemento encontrado",
                content = @Content(schema = @Schema(implementation = CatalogElementDto.class))),
            @ApiResponse(responseCode = "404", description = "Elemento no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{catalogId}/elementos/{elementId}")
    public ResponseEntity<CatalogElementDto> getElementById(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer catalogId,
            @Parameter(description = "ID del elemento", required = true)
            @PathVariable Integer elementId) {

        return elementService.findElementById(catalogId, elementId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "createElement",
        summary = "Crear un elemento",
        description = "Crea un nuevo elemento en el catálogo especificado",
        responses = {
            @ApiResponse(responseCode = "201", description = "Elemento creado",
                content = @Content(schema = @Schema(implementation = CatalogElementDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PostMapping("/{catalogId}/elementos")
    public ResponseEntity<CatalogElementDto> createElement(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer catalogId,
            @Parameter(description = "ID del usuario que crea")
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @Valid @RequestBody CatalogElementCreateDto createDto) {

        CatalogElementDto created = elementService.createElement(catalogId, createDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        operationId = "updateElement",
        summary = "Actualizar un elemento",
        description = "Actualiza un elemento existente en el catálogo",
        responses = {
            @ApiResponse(responseCode = "200", description = "Elemento actualizado",
                content = @Content(schema = @Schema(implementation = CatalogElementDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Elemento no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PutMapping("/{catalogId}/elementos/{elementId}")
    public ResponseEntity<CatalogElementDto> updateElement(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer catalogId,
            @Parameter(description = "ID del elemento", required = true)
            @PathVariable Integer elementId,
            @Parameter(description = "ID del usuario que actualiza")
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @Valid @RequestBody CatalogElementUpdateDto updateDto) {

        CatalogElementDto updated = elementService.updateElement(catalogId, elementId, updateDto, userId);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        operationId = "changeElementStatus",
        summary = "Cambiar estatus de un elemento",
        description = "Activa o desactiva un elemento. Al desactivar, el elemento deja de ser visible.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Estatus actualizado",
                content = @Content(schema = @Schema(implementation = CatalogElementDto.class))),
            @ApiResponse(responseCode = "400", description = "Estatus inválido",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Elemento no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @PatchMapping("/elementos/{elementId}/estatus")
    public ResponseEntity<CatalogElementDto> changeStatus(
            @Parameter(description = "ID del elemento", required = true)
            @PathVariable Integer elementId,
            @Parameter(description = "ID del usuario que realiza el cambio")
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @Valid @RequestBody CatalogElementStatusDto statusDto) {

        CatalogElementDto updated = elementService.changeStatus(elementId, statusDto.getStatus(), userId);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        operationId = "getPrimaryCatalogs",
        summary = "Listar catálogos primarios activos",
        description = "Retorna catálogos primarios activos para el dropdown de 'Seleccionar catálogo padre'",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de catálogos primarios",
                content = @Content(schema = @Schema(implementation = CatalogSimpleDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/primarios")
    public ResponseEntity<List<CatalogSimpleDto>> getPrimaryCatalogs() {
        List<CatalogSimpleDto> catalogs = elementService.findPrimaryCatalogs();
        return ResponseEntity.ok(catalogs);
    }

    @Operation(
        operationId = "getActiveElements",
        summary = "Listar elementos activos de un catálogo",
        description = "Retorna elementos activos para el dropdown de 'Seleccionar elemento padre'",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de elementos activos",
                content = @Content(schema = @Schema(implementation = CatalogElementDto[].class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{catalogId}/elementos/activos")
    public ResponseEntity<List<CatalogElementDto>> getActiveElements(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer catalogId) {

        List<CatalogElementDto> elements = elementService.findActiveElements(catalogId);
        return ResponseEntity.ok(elements);
    }

    @Operation(
        operationId = "getCatalogDetail",
        summary = "Obtener detalle de un catálogo",
        description = "Retorna información del catálogo para mostrar en la tarjeta de detalle",
        responses = {
            @ApiResponse(responseCode = "200", description = "Catálogo encontrado",
                content = @Content(schema = @Schema(implementation = CatalogSimpleDto.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{catalogId}/detalle")
    public ResponseEntity<CatalogSimpleDto> getCatalogDetail(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer catalogId) {

        return elementService.findCatalogById(catalogId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}







