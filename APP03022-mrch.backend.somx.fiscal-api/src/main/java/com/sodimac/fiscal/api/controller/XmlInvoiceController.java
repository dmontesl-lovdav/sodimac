package com.sodimac.fiscal.api.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import com.sodimac.fiscal.api.service.XmlDocumentTypeDetector;
import com.sodimac.fiscal.api.util.XmlToJsonConverter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * ============================================================================
 * CONTROLADOR BFF: Conversor de XML Facturas a JSON
 * ============================================================================
 * Proporciona endpoints para convertir archivos XML de facturas
 * electrónicas mexicanas (CFDI) a formato JSON.
 *
 * RUTA BASE: /api/xml-invoices
 * FUNCIONALIDAD: Conversión XML → JSON y detección de tipo de documento
 *
 * ENDPOINTS DISPONIBLES:
 * - GET /convert-all              -> Convierte todas las facturas de resources
 * - POST /upload-convert          -> Carga y convierte archivos XML dinámicamente
 * - POST /detect-document-type    -> Detecta el tipo de documento fiscal XML
 *
 * CARACTERÍSTICAS:
 * - Conversión XML a JSON preservando estructura
 * - Detección automática de tipo de documento (CFDI, CartaPorte, Pagos)
 * - Validación de formato y extensión .xml
 * - Manejo de errores estructurado
 *
 * DIFERENCIA CON /api/fiscal/xml:
 * - /api/fiscal/xml: Procesamiento con transformación a DTOs específicos
 * - /api/xml-invoices: Conversión directa XML → JSON sin transformación
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/xml-invoices")
@Tag(name = "XML Invoice Converter", description = "Endpoints para convertir facturas XML a JSON")
public class XmlInvoiceController {

    @Autowired
    private XmlToJsonConverter xmlToJsonConverter;

    @Autowired
    private XmlDocumentTypeDetector xmlDocumentTypeDetector;

    @GetMapping("/convert-all")
    @Operation(summary = "Convierte todas las facturas XML a JSON",
               description = "Lee todos los archivos XML de facturas desde resources/invoice y los convierte a JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversión exitosa"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<String>> convertAllInvoicesToJson() {
        try {
            List<String> jsonResults = xmlToJsonConverter.convertAllInvoiceXmlsToJson();
            return ResponseEntity.ok(jsonResults);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Endpoint para cargar y convertir archivos XML de facturas dinámicamente.
     *
     * Este método permite a los usuarios subir archivos XML de facturas CFDI
     * y obtener inmediatamente su conversión a formato JSON.
     *
     * Validaciones realizadas:
     * - Verificación de que el archivo no esté vacío
     * - Validación de extensión .xml
     * - Procesamiento seguro del contenido XML
     *
     * @param file Archivo XML de factura a procesar (MultipartFile)
     * @return ResponseEntity con el JSON resultante o mensaje de error
     * @author g_dco018
     */
    @PostMapping("/upload-convert")
    @Operation(summary = "Carga un archivo XML de factura y lo convierte a JSON",
               description = "Permite cargar un archivo XML de factura y regresa su conversión a JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversión exitosa"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o vacío"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<String> uploadAndConvertXmlToJson(
            @RequestParam("file") MultipartFile file) {
        try {
            // Validar que el archivo sea XML
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
                return ResponseEntity.badRequest().body("El archivo debe ser un XML");
            }

            // Convertir a JSON
            String jsonResult = xmlToJsonConverter.convertUploadedXmlToJson(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResult);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error al procesar el archivo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }

    /**
     * Endpoint para detectar el tipo de documento fiscal XML.
     *
     * Este método permite a los usuarios subir archivos XML de documentos fiscales
     * y obtener inmediatamente la identificación del tipo de documento.
     *
     * Validaciones realizadas:
     * - Verificación de que el archivo no esté vacío
     * - Validación de extensión .xml
     * - Procesamiento seguro del contenido XML
     *
     * @param file Archivo XML de documento fiscal a analizar (MultipartFile)
     * @return ResponseEntity con el tipo de documento detectado o mensaje de error
     * @author g_dco018
     */
    @PostMapping("/detect-document-type")
    @Operation(summary = "Detecta el tipo de documento fiscal XML",
               description = "Analiza un archivo XML fiscal y determina automáticamente su tipo de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento detectado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o vacío"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> detectDocumentType(
            @RequestParam("file") MultipartFile file) {
        try {
            // Validar que el archivo sea XML
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("El archivo está vacío"));
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
                return ResponseEntity.badRequest().body(createErrorResponse("El archivo debe ser un XML"));
            }

            // Leer contenido del archivo
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);

            // Detectar tipo de documento
            TipoDocumentoFiscal tipoDocumento = xmlDocumentTypeDetector.detectDocumentType(xmlContent);

            // Crear respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileName", fileName);
            response.put("documentType", tipoDocumento);
            response.put("message", "Tipo de documento detectado exitosamente");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error al procesar el archivo: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error al detectar tipo de documento: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Error interno: " + e.getMessage()));
        }
    }

    /**
     * Crea una respuesta de error estructurada.
     *
     * @param errorMessage Mensaje de error
     * @return Map con la estructura de error
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }
}