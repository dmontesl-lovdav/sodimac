package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.response.FiscalXmlResponse;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import com.sodimac.fiscal.api.service.FiscalXmlTransformerService;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.service.XmlDocumentTypeDetector;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * CONTROLADOR BFF: Procesador de XML Fiscales Mexicanos
 * ============================================================================
 * Controlador REST para procesamiento de documentos XML fiscales mexicanos.
 *
 * ARQUITECTURA BFF:
 * Implementa un BFF que realiza las siguientes operaciones:
 * 1. Recibe XML raw desde el frontend (String o File)
 * 2. Detecta automáticamente el tipo de documento (CFDI 4.0, CartaPorte 3.1, Pagos 2.0)
 * 3. Orquesta múltiples servicios especializados de transformación
 * 4. Devuelve JSON estructurado para el frontend
 *
 * PATRÓN DE DISEÑO: Strategy Pattern + BFF
 * - Cada tipo de documento tiene su procesador específico
 * - El BFF actúa como facade entre cliente y servicios internos
 *
 * ENDPOINTS BFF:
 * - POST /api/fiscal/xml/process         -> Procesar XML como String
 * - POST /api/fiscal/xml/process/file    -> Procesar archivo XML (multipart)
 * - POST /api/fiscal/xml/detect          -> Detectar tipo de documento
 * - POST /api/fiscal/xml/validate        -> Validar documento fiscal
 * - GET  /api/fiscal/xml/health          -> Health check del servicio
 *
 * FORMATOS SOPORTADOS:
 * - CFDI v4.0 (Comprobante Fiscal Digital por Internet)
 * - CartaPorte v3.1 (Complemento de transporte)
 * - Pagos v2.0 (Complemento de pagos)
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/fiscal/xml")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Procesador XML Fiscal", description = "APIs para procesar documentos fiscales XML del SAT mexicano")
public class FiscalXmlProcessorController {

    // SERVICIOS BFF: Cada uno con responsabilidad única
    private final FiscalXmlTransformerService xmlTransformerService;    // Transformación XML -> JSON
    private final XmlDocumentTypeDetector xmlDocumentTypeDetector;      // Detección de tipo de documento
    private final MessageCatalogService messageCatalog;                 // Catálogo de mensajes

    /**
     * Procesa un documento XML fiscal retornando respuesta JSON estructurada.
     *
     * @param xmlContent Contenido XML como String
     * @return Respuesta JSON con nodos: Comprobante, Emisor, Receptor, TimbreFiscalDigital
     */
    @PostMapping(value = "/process", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Procesar XML fiscal",
               description = "Detecta automáticamente el tipo de documento fiscal usando cat_version y retorna JSON estructurado con nodos específicos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "XML procesado exitosamente"),
        @ApiResponse(responseCode = "400", description = "XML inválido o tipo no soportado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<FiscalXmlResponse> processXml(
            @Parameter(description = "Contenido XML del documento fiscal", required = true)
            @RequestBody String xmlContent) {

        log.info("Recibida solicitud de procesamiento XML fiscal");

        try {
            FiscalXmlResponse response = xmlTransformerService.transformToStructuredResponse(xmlContent);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error procesando XML fiscal", e);
            messageCatalog.throwError(FiscalMessageCode.ERR003, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Procesa un archivo XML fiscal subido.
     *
     * @param file Archivo XML
     * @return DTO específico según el tipo de documento detectado
     */
    @PostMapping(value = "/process/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Procesar archivo XML fiscal",
               description = "Sube y procesa un archivo XML fiscal retornando JSON estructurado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Archivo procesado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o tipo no soportado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<FiscalXmlResponse> processXmlFile(
            @Parameter(description = "Archivo XML del documento fiscal", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Recibida solicitud de procesamiento archivo XML: {}", file.getOriginalFilename());

        try {
            // Validar archivo
            if (file.isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR001);
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
                messageCatalog.throwError(FiscalMessageCode.ERR002);
            }

            // Leer contenido y procesar
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            FiscalXmlResponse response = xmlTransformerService.transformToStructuredResponse(xmlContent);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error procesando archivo XML fiscal", e);
            messageCatalog.throwError(FiscalMessageCode.ERR003, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Detecta el tipo de documento XML fiscal usando cat_version.
     *
     * @param xmlContent Contenido XML
     * @return Información del tipo de documento detectado desde base de datos
     */
    @PostMapping(value = "/detect", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Detectar tipo de documento fiscal",
               description = "Analiza el XML y detecta el tipo de documento fiscal usando la tabla cat_version")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipo de documento detectado exitosamente"),
        @ApiResponse(responseCode = "400", description = "XML inválido o tipo no reconocido")
    })
    public ResponseEntity<TipoDocumentoFiscal> detectDocumentType(
            @Parameter(description = "Contenido XML del documento fiscal", required = true)
            @RequestBody String xmlContent) {

        log.info("Recibida solicitud de detección de tipo XML fiscal");

        try {
            TipoDocumentoFiscal tipoDocumento = xmlDocumentTypeDetector.detectDocumentType(xmlContent);
            return ResponseEntity.ok(tipoDocumento);

        } catch (Exception e) {
            log.error("Error detectando tipo de documento XML", e);
            messageCatalog.throwError(FiscalMessageCode.ERR014, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Valida si un XML es un documento fiscal válido.
     *
     * @param xmlContent Contenido XML
     * @return Estado de validez del documento
     */
    @PostMapping(value = "/validate", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validar documento fiscal XML",
               description = "Valida si el XML corresponde a un documento fiscal mexicano válido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validación completada")
    })
    public ResponseEntity<?> validateFiscalDocument(
            @Parameter(description = "Contenido XML del documento fiscal", required = true)
            @RequestBody String xmlContent) {

        log.info("Recibida solicitud de validación XML fiscal");

        try {
            TipoDocumentoFiscal tipoDocumento = xmlDocumentTypeDetector.detectDocumentType(xmlContent);
            boolean isValid = tipoDocumento != null;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isValid", isValid);

            if (isValid) {
                response.put("documentType", tipoDocumento);
                response.put("message", "Documento fiscal válido");
            } else {
                response.put("message", "Documento fiscal inválido o no soportado");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validando documento XML fiscal", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", true);
            errorResponse.put("isValid", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Error en la validación del documento");

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Endpoint de salud para verificar el estado del servicio.
     *
     * @return Estado del servicio
     */
    @GetMapping("/health")
    @Operation(summary = "Estado del servicio", description = "Verifica que el servicio de procesamiento XML fiscal esté funcionando")
    @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Fiscal XML Processor");
        response.put("timestamp", System.currentTimeMillis());
        response.put("supportedTypes", TipoDocumentoFiscal.values());

        return ResponseEntity.ok(response);
    }
}