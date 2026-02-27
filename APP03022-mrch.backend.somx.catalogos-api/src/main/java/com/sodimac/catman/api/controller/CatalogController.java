package com.sodimac.catman.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.catman.api.exception.ExceptionWrapper;
import com.sodimac.catman.api.model.dto.CatalogDetailDto;
import com.sodimac.catman.api.model.dto.CatalogHeaderDto;
import com.sodimac.catman.api.service.CatalogHeaderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping
@Tag(name = "Catalogos", description = "API de catalogos compartidos del portal FBC")
public class CatalogController {

    private final CatalogHeaderService service;

    public CatalogController(CatalogHeaderService service) {
        this.service = service;
    }

    @Operation(
        operationId = "getAllCatalogs",
        summary = "Obtiene todos los catálogos disponibles",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de catálogos",
                content = @Content(schema = @Schema(implementation = CatalogHeaderDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping
    public ResponseEntity<List<CatalogHeaderDto>> getAllCatalogs(
            @Parameter(description = "ID del idioma (1=ES, 2=EN). Por defecto: 1")
            @RequestParam(required = false, defaultValue = "1") Integer lang) {
        return ResponseEntity.ok(service.findAllCatalogs(lang));
    }

    @Operation(
        operationId = "getCatalogsByModule",
        summary = "Obtiene catálogos por módulo",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de catálogos del módulo",
                content = @Content(schema = @Schema(implementation = CatalogHeaderDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/module/{module}")
    public ResponseEntity<List<CatalogHeaderDto>> getCatalogsByModule(
            @Parameter(description = "Código del módulo", required = true)
            @PathVariable String module,
            @Parameter(description = "ID del idioma (1=ES, 2=EN). Por defecto: 1")
            @RequestParam(required = false, defaultValue = "1") Integer lang) {
        return ResponseEntity.ok(service.findCatalogsByModule(module, lang));
    }

    @Operation(
        operationId = "getCatalogByCode",
        summary = "Obtiene un catálogo por su código, incluyendo detalles",
        responses = {
            @ApiResponse(responseCode = "200", description = "Catálogo con detalles",
                content = @Content(schema = @Schema(implementation = CatalogHeaderDto.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{code}")
    public ResponseEntity<CatalogHeaderDto> getCatalogByCode(
            @Parameter(description = "Código del catálogo", required = true)
            @PathVariable String code,
            @Parameter(description = "ID del idioma (1=ES, 2=EN). Por defecto: 1")
            @RequestParam(required = false, defaultValue = "1") Integer lang) {
        return service.findCatalogByCode(code, lang)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getCatalogDetails",
        summary = "Obtiene solo los detalles de un catálogo",
        description = "Permite filtrar por dependencia de otro catálogo usando el parámetro dependsOn. " +
                      "Formato: dependsOn=CATALOG_CODE:EXTERNAL_KEY (ej: dependsOn=REGIMEN_FISCAL:605). " +
                      "Si no se especifica dependsOn o si no hay relaciones configuradas, retorna todos los detalles.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de detalles del catálogo",
                content = @Content(schema = @Schema(implementation = CatalogDetailDto[].class))),
            @ApiResponse(responseCode = "400", description = "Formato de dependsOn inválido",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{code}/details")
    public ResponseEntity<List<CatalogDetailDto>> getCatalogDetails(
            @Parameter(description = "Código del catálogo", required = true)
            @PathVariable String code,
            @Parameter(description = "ID del idioma (1=ES, 2=EN). Por defecto: 1")
            @RequestParam(required = false, defaultValue = "1") Integer lang,
            @Parameter(description = "Filtro de dependencia. Formato: CATALOG_CODE:EXTERNAL_KEY (ej: REGIMEN_FISCAL:605)")
            @RequestParam(required = false) String dependsOn) {

        // Si no hay filtro de dependencia, retornar todos los detalles
        if (dependsOn == null || dependsOn.isBlank()) {
            return ResponseEntity.ok(service.findDetailsByCode(code, lang));
        }

        // Parsear el filtro dependsOn
        String[] parts = dependsOn.split(":");
        if (parts.length != 2) {
            return ResponseEntity.badRequest().build();
        }

        String targetCatalogCode = parts[0].trim();
        String targetExternalKey = parts[1].trim();

        return ResponseEntity.ok(service.findDetailsByCodeWithDependency(code, targetCatalogCode, targetExternalKey, lang));
    }

    @Operation(
        operationId = "getCatalogDetailByKey",
        summary = "Obtiene un detalle específico por código de catálogo y clave",
        responses = {
            @ApiResponse(responseCode = "200", description = "Detalle del catálogo",
                content = @Content(schema = @Schema(implementation = CatalogDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/{code}/details/{key}")
    public ResponseEntity<CatalogDetailDto> getCatalogDetailByKey(
            @Parameter(description = "Código del catálogo", required = true)
            @PathVariable String code,
            @Parameter(description = "Clave del detalle", required = true)
            @PathVariable String key,
            @Parameter(description = "ID del idioma (1=ES, 2=EN). Por defecto: 1")
            @RequestParam(required = false, defaultValue = "1") Integer lang) {
        return service.findDetailByCodeAndKey(code, key, lang)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getMessageByKey",
        summary = "Obtiene un mensaje/detalle directamente por su clave única (ej: BUS001, WRN002)",
        description = "Busca directamente por la clave estandarizada sin necesidad de especificar el catálogo. Idioma por defecto: Español (1)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Mensaje encontrado",
                content = @Content(schema = @Schema(implementation = CatalogDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/message/{key}")
    public ResponseEntity<CatalogDetailDto> getMessageByKey(
            @Parameter(description = "Clave única del mensaje (ej: BUS001, WRN002, EFA001)", required = true)
            @PathVariable String key,
            @Parameter(description = "ID del idioma (1=ES, 2=EN, 3=PT). Por defecto: 1 (Español)")
            @RequestParam(required = false, defaultValue = "1") Integer lang) {
        return service.findDetailByKey(key, lang)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getMessageByKeyFormatted",
        summary = "Obtiene un mensaje con placeholders sustituidos por parámetros",
        description = "Busca el mensaje por clave y sustituye los placeholders {0}, {1}, etc. con los parámetros proporcionados. " +
                      "Ejemplo: GET /message/BUS002/format?params=RFC123456789 donde el mensaje es 'El RFC del receptor {0} no acepta...'",
        responses = {
            @ApiResponse(responseCode = "200", description = "Mensaje formateado",
                content = @Content(schema = @Schema(implementation = CatalogDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad de parámetros incorrecta",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/message/{key}/format")
    public ResponseEntity<CatalogDetailDto> getMessageByKeyFormatted(
            @Parameter(description = "Clave única del mensaje (ej: BUS002, RES001)", required = true)
            @PathVariable String key,
            @Parameter(description = "ID del idioma (1=ES, 2=EN, 3=PT). Por defecto: 1 (Español)")
            @RequestParam(required = false, defaultValue = "1") Integer lang,
            @Parameter(description = "Parámetros para sustituir placeholders {0}, {1}, etc. Separados por coma")
            @RequestParam(required = false) List<String> params) {
        return service.findDetailByKeyFormatted(key, lang, params)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getCatalogById",
        summary = "Obtiene un catálogo por su ID, incluyendo detalles",
        responses = {
            @ApiResponse(responseCode = "200", description = "Catálogo con detalles",
                content = @Content(schema = @Schema(implementation = CatalogHeaderDto.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/id/{id}")
    public ResponseEntity<CatalogHeaderDto> getCatalogById(
            @Parameter(description = "ID del catálogo", required = true)
            @PathVariable Integer id,
            @Parameter(description = "ID del idioma (1=ES, 2=EN, 3=PT). Por defecto: 1")
            @RequestParam(required = false, defaultValue = "1") Integer lang) {
        return service.findCatalogById(id, lang)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        operationId = "getCatalogByPrefix",
        summary = "Obtiene un catálogo por su prefijo, incluyendo detalles",
        description = "El prefijo identifica el tipo de catálogo (ej: EFA=Estatus Factura, BUS=Mensajes Negocio)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Catálogo con detalles",
                content = @Content(schema = @Schema(implementation = CatalogHeaderDto.class))),
            @ApiResponse(responseCode = "404", description = "Catálogo no encontrado",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
    @GetMapping("/prefix/{prefix}")
    public ResponseEntity<CatalogHeaderDto> getCatalogByPrefix(
            @Parameter(description = "Prefijo del catálogo (ej: EFA, BUS, ERR, WRN)", required = true)
            @PathVariable String prefix,
            @Parameter(description = "ID del idioma (1=ES, 2=EN, 3=PT). Por defecto: 1")
            @RequestParam(required = false, defaultValue = "1") Integer lang) {
        return service.findCatalogByPrefix(prefix, lang)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
