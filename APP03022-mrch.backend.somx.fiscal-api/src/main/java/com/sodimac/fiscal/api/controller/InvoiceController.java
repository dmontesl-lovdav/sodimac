package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.config.PaginationConfig;
import com.sodimac.fiscal.api.model.dto.BulkDownloadRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceDto;
import com.sodimac.fiscal.api.model.dto.InvoiceRegistrationResponse;
import com.sodimac.fiscal.api.model.dto.InvoiceSearchRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceSearchResponse;
import com.sodimac.fiscal.api.model.dto.InvoiceUpdateRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceUpdateResponse;
import com.sodimac.fiscal.api.model.dto.InvoiceStatusSearchRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceStatusUpdateRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceStatusUpdateResponse;
import com.sodimac.fiscal.api.model.dto.NCValidationRequest;
import com.sodimac.fiscal.api.model.dto.NCValidationResponse;
import com.sodimac.fiscal.api.service.InvoiceService;
import com.sodimac.fiscal.api.service.NCValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * CONTROLADOR BFF: Gestión de Facturas (CFDI - Comprobante Fiscal Digital)
 * ============================================================================
 * Este controlador BFF maneja la consulta de facturas electrónicas (CFDI 4.0)
 * del sistema de facturación mexicano.
 *
 * CARACTERÍSTICAS BFF:
 * - Paginación automática para listas grandes (config: PaginationConfig)
 * - Transformación de entidades Invoice a InvoiceDto
 * - Respuesta optimizada para frontend (Spring Data Page con metadata)
 *
 * RUTA BASE: /api/invoices
 * MODELO DE DATOS: CFDI (Comprobante Fiscal Digital por Internet)
 * PAGINACIÓN: Default 20 items, Max 100 (configurado en PaginationConfig)
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoices", description = "Gestión de facturas y comprobantes fiscales digitales")
public class InvoiceController {

    // INYECCIÓN DE SERVICIOS BFF
    private final InvoiceService invoiceService;
    private final NCValidationService ncValidationService;
    private final PaginationConfig paginationConfig; // Configuración centralizada de paginación

    /**
     * GET /api/invoices?page=0
     *
     * ENDPOINT BFF: Obtener facturas con paginación automática
     *
     * FUNCIONAMIENTO: El frontend envía número de página,
     *                 el BFF calcula automáticamente offset y limit
     *
     * RESPUESTA: Spring Data Page con metadata:
     * {
     *   "content": [...],        // Array de facturas
     *   "totalElements": 1500,   // Total de registros
     *   "totalPages": 75,        // Total de páginas
     *   "size": 20,              // Tamaño de página
     *   "number": 0              // Página actual
     * }
     *
     * @param page Número de página (0-indexed, default 0)
     * @return Página de facturas con metadata de paginación
     */
    @Operation(summary = "Obtener todas las facturas", description = "Devuelve las facturas paginadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page) {

        // BFF construye el objeto Pageable con configuración centralizada
        Pageable pageable = PageRequest.of(page, paginationConfig.getDefaultPageSize());

        // Delegación al servicio de negocio
        Page<InvoiceDto> invoices = invoiceService.findAll(pageable);

        // Retorno con metadata de paginación incluida automáticamente
        return ResponseEntity.ok(invoices);
    }

    /**
     * POST /api/invoices/register
     *
     * ENDPOINT: Registrar una factura o nota de crédito (STM-337)
     *
     * FUNCIONALIDAD:
     * - Recibe archivo XML de factura (I) o nota de crédito (E)
     * - Valida RFC receptor autorizado
     * - Valida versión CFDI vigente (4.0)
     * - Valida estructura de addenda (Addenda_Sodimac o Addenda_Sodimac_CartaPorte)
     * - Valida con SAT mediante PAC (opcional)
     * - Persiste en base de datos (invoice, issuer, receiver, addenda)
     *
     * RESPUESTA: InvoiceRegistrationResponse con códigos BUS:
     * - BUS1001: Factura registrada exitosamente
     * - BUS1002: Factura registrada - Pendiente de Addenda
     * - BUS1003: Nota de Crédito registrada exitosamente
     * - BUS1004: Nota de Crédito registrada - Pendiente de Addenda
     * - BUS2xxx: Errores de validación de negocio
     *
     * @param file Archivo XML del CFDI (factura o nota de crédito)
     * @return Respuesta del registro con código de negocio
     */
    @Operation(
            summary = "Registrar factura o nota de crédito",
            description = "Registra una factura (I) o nota de crédito (E) validando RFC receptor, versión CFDI, addenda y SAT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceRegistrationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación de negocio (códigos BUS2xxx)",
                    content = @Content(schema = @Schema(implementation = InvoiceRegistrationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = InvoiceRegistrationResponse.class))
            )
    })
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceRegistrationResponse> registerInvoice(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "UUID de transacción para trazabilidad en auditoría (STM-704)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(value = "idTransaccion", required = false) String idTransaccion) {

        log.info("Solicitud de registro de factura/NC recibida. Archivo: {}, idTransaccion: {}",
                file.getOriginalFilename(), idTransaccion);

        // Validar idTransaccion obligatorio (STM-704)
        if (idTransaccion == null || idTransaccion.isBlank()) {
            log.error("idTransaccion no proporcionado");
            return ResponseEntity
                    .badRequest()
                    .body(InvoiceRegistrationResponse.error(
                            "FISCAL-ERR-102",
                            "El idTransaccion es obligatorio para el registro"
                    ));
        }

        // Validaciones básicas del archivo
        if (file.isEmpty()) {
            log.error("Archivo vacio recibido");
            return ResponseEntity
                    .badRequest()
                    .body(InvoiceRegistrationResponse.error(
                            "FISCAL-ERR-100",
                            "El archivo esta vacio"
                    ));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
            log.error("Extension de archivo invalida: {}", file.getOriginalFilename());
            return ResponseEntity
                    .badRequest()
                    .body(InvoiceRegistrationResponse.error(
                            "FISCAL-ERR-101",
                            "El archivo debe tener extension .xml"
                    ));
        }

        // Procesar registro
        InvoiceRegistrationResponse response = invoiceService.registerInvoice(file, idTransaccion);

        // Determinar código HTTP según resultado
        if (response.isSuccess()) {
            log.info("Registro exitoso. Codigo: {}, Invoice UUID: {}",
                    response.getCode(), response.getInvoiceUuid());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Registro fallido. Codigo: {}, Mensaje: {}",
                    response.getCode(), response.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * PUT /api/invoices
     *
     * ENDPOINT: Actualizar una factura o nota de crédito existente (STM-339)
     *
     * FUNCIONALIDAD:
     * - Actualiza el estatus del documento (obligatorio)
     * - Registra el usuario que realizó la actualización (obligatorio)
     * - Permite actualizar información de addenda (opcional)
     *
     * VALIDACIONES:
     * - Valida que el documento exista por UUID fiscal
     * - Valida que el documento pertenezca al proveedor especificado
     * - Valida transiciones de estatus permitidas según flujo de negocio
     * - Valida estructura de addenda si se proporciona
     *
     * RESPUESTA: InvoiceUpdateResponse con códigos BUS3xxx:
     * - BUS3001: Factura actualizada exitosamente
     * - BUS3002: Factura actualizada exitosamente - Addenda actualizada
     * - BUS3003: Nota de Crédito actualizada exitosamente
     * - BUS3004: Nota de Crédito actualizada exitosamente - Addenda actualizada
     * - BUS31xx/32xx/33xx: Errores de validación
     *
     * @param request Datos de actualización (UUID, numeroProveedor, estatus, idUsuarioActualizacion, addenda)
     * @return Respuesta de actualización con código de negocio
     */
    @Operation(
            summary = "Actualizar factura o nota de crédito",
            description = "Actualiza el estatus y/o addenda de una factura (I) o nota de crédito (E) existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación de negocio (códigos BUS3xxx)",
                    content = @Content(schema = @Schema(implementation = InvoiceUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Documento no encontrado (código BUS3101)",
                    content = @Content(schema = @Schema(implementation = InvoiceUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = InvoiceUpdateResponse.class))
            )
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvoiceUpdateResponse> updateInvoice(
            @Valid @RequestBody InvoiceUpdateRequest request) {

        log.info("Solicitud de actualizacion de factura/NC recibida. UUID: {}, Nuevo Estatus: {}",
                request.getUuid(), request.getEstatus());

        // Procesar actualización
        InvoiceUpdateResponse response = invoiceService.updateInvoice(request);

        // Determinar código HTTP según resultado
        if (response.isSuccess()) {
            log.info("Actualizacion exitosa. Codigo: {}, Invoice UUID: {}",
                    response.getCode(), response.getInvoiceUuid());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Actualizacion fallida. Codigo: {}, Mensaje: {}",
                    response.getCode(), response.getMessage());

            // Determinar código HTTP según tipo de error
            if (response.getCode().equals("BUS3101")) {
                // Documento no encontrado
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            } else {
                // Otros errores de validación
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }
        }
    }

    /**
     * POST /api/invoices/search
     *
     * ENDPOINT: Buscar facturas y notas de crédito con filtros (STM-338 / STM-771)
     *
     * FUNCIONALIDAD:
     * - Busca facturas (I) y notas de crédito (E) usando filtros dinámicos
     * - Utiliza JPA Criteria API (Specifications) para consultas dinámicas
     * - Soporta paginación y ordenamiento configurable
     * - Incluye contenido XML completo del CFDI en la respuesta (STM-771)
     *
     * FILTROS OBLIGATORIOS:
     * - RFC Emisor (rfcEmisor)
     * - Fecha Inicio de Recepción (fechaInicioRecepcion)
     * - Fecha Final de Recepción (fechaFinalRecepcion)
     * - Tipo de Documento (tipoDocumento): I=Factura, E=Nota de Crédito
     *
     * FILTROS OPCIONALES:
     * - RFC Receptor (rfcReceptor)
     * - ID Proveedor (idProveedor)
     * - Serie (serie)
     * - Folio (folio)
     * - UUID Fiscal (uuid)
     * - Estatus (estatus) - STM-771
     *
     * RESPUESTA: Page<InvoiceSearchResponse> que incluye:
     * - Comprobante: Información completa de la factura/NC
     * - Emisor: RFC, nombre, régimen fiscal
     * - Receptor: RFC, nombre, régimen fiscal
     * - Addenda: Indicador de addenda y tipo (si existe)
     * - XML Content: Contenido XML completo del documento fiscal (STM-771)
     *
     * EJEMPLO REQUEST:
     * {
     *   "rfcEmisor": "AAA010101AAA",
     *   "fechaInicioRecepcion": "2025-01-01",
     *   "fechaFinalRecepcion": "2025-01-31",
     *   "tipoDocumento": "I",
     *   "rfcReceptor": "CSD161207R2A",
     *   "estatus": 1,
     *   "page": 0,
     *   "size": 20,
     *   "sortBy": "createdAt",
     *   "sortDirection": "DESC"
     * }
     *
     * @param searchRequest Filtros de búsqueda con validaciones Bean Validation
     * @return Página de resultados con información completa de cada documento
     */
    @Operation(
            summary = "Buscar facturas y notas de crédito",
            description = "Busca documentos fiscales usando filtros obligatorios (RFC Emisor, Fechas, Tipo) y opcionales (RFC Receptor, Serie, Folio, UUID). Retorna información de Comprobante, Emisor, Receptor y Addenda."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda exitosa (puede retornar lista vacía si no hay resultados)",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación - Filtros obligatorios faltantes o formato inválido"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<InvoiceSearchResponse>> searchInvoices(
            @Valid @RequestBody InvoiceSearchRequest searchRequest) {

        log.info("Solicitud de busqueda de facturas/NC recibida. RFC Emisor: {}, Tipo: {}, Fechas: {} - {}",
                searchRequest.getRfcEmisor(),
                searchRequest.getTipoDocumento(),
                searchRequest.getFechaInicioRecepcion(),
                searchRequest.getFechaFinalRecepcion());

        // Ejecutar búsqueda con JPA Criteria
        Page<InvoiceSearchResponse> results = invoiceService.searchInvoices(searchRequest);

        log.info("Busqueda completada. Resultados: {} de {} totales",
                results.getNumberOfElements(), results.getTotalElements());

        return ResponseEntity.ok(results);
    }

    /**
     * POST /api/invoices/validate-nc-relation
     *
     * ENDPOINT: Validar relación NC-Factura (PARA finanzas-api)
     *
     * FUNCIONALIDAD:
     * - Valida que un XML de Nota de Crédito está relacionado con una Factura
     * - Verifica el nodo CfdiRelacionados en el XML
     * - Extrae UUID fiscal de la NC y tipo de relación
     * - NO REGISTRA NADA (solo validación)
     *
     * USO:
     * - finanzas-api llama a este endpoint antes de guardar rebates
     * - fiscal-api solo valida XML (su expertise)
     * - finanzas-api guarda los rebates en userapp.tenant_finance
     *
     * @param request Contiene invoiceFiscalUuid
     * @param xmlFile Archivo XML de la Nota de Crédito
     * @return NCValidationResponse con resultado de validación
     */
    @Operation(
            summary = "Validar relación NC-Factura (para finanzas-api)",
            description = "Valida que un XML de NC está relacionado con una factura. SOLO VALIDACIÓN, no guarda datos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validación completada (isValid puede ser true o false)",
                    content = @Content(schema = @Schema(implementation = NCValidationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación de request"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping(value = "/validate-nc-relation",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NCValidationResponse> validateNCRelation(
            @Valid @ModelAttribute NCValidationRequest request,
            @RequestParam("xmlFile") MultipartFile xmlFile) {

        log.info("Solicitud de validacion NC-Factura recibida. Invoice UUID: {}",
                request.getInvoiceFiscalUuid());

        // Validar archivo - lanzar excepción para que GlobalExceptionHandler lo maneje
        if (xmlFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo XML de NC es obligatorio");
        }

        // Ejecutar validación
        NCValidationResponse response = ncValidationService.validateNCRelation(request, xmlFile);

        log.info("Validacion NC completada. Is Valid: {}, Code: {}",
                response.isValid(), response.getBusinessCode());

        return ResponseEntity.ok(response);
    }

    // ========== DESCARGA INDIVIDUAL XML ==========

    /**
     * GET /api/invoices/{uuid}/xml
     *
     * ENDPOINT: Obtener XML de un documento fiscal por UUID
     *
     * FUNCIONALIDAD:
     * - Busca en facturas/NC y complementos de pago
     * - Retorna el contenido XML del documento
     * - Soporta descarga como archivo o visualizacion
     *
     * @param uuid UUID fiscal del documento (TimbreFiscalDigital)
     * @return Contenido XML del documento
     */
    @Operation(
            summary = "Obtener XML por UUID",
            description = "Obtiene el contenido XML de una factura, nota de credito o complemento de pago por su UUID fiscal"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "XML obtenido exitosamente",
                    content = @Content(mediaType = "application/xml")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Documento no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @GetMapping(value = "/{uuid}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getXmlByUuid(
            @PathVariable("uuid") String uuid,
            @RequestParam(value = "download", defaultValue = "false") boolean download) {

        log.info("Solicitud de XML recibida. UUID: {}, Download: {}", uuid, download);

        String xmlContent = invoiceService.getXmlByFiscalUuid(uuid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        if (download) {
            String filename = uuid + ".xml";
            headers.setContentDispositionFormData("attachment", filename);
        }

        log.info("XML obtenido exitosamente. UUID: {}, Tamano: {} bytes", uuid, xmlContent.length());

        return new ResponseEntity<>(xmlContent, headers, HttpStatus.OK);
    }

    // ========== DESCARGA MASIVA (STM-396) ==========

    /**
     * POST /api/invoices/download/xml
     *
     * ENDPOINT: Descarga masiva de XMLs en archivo ZIP (STM-396)
     *
     * FUNCIONALIDAD:
     * - Recibe lista de invoice_uuid a descargar
     * - Genera archivo ZIP con los XMLs de los documentos
     * - Nombre de archivos: Serie-Folio_UUID.xml
     *
     * @param request Lista de invoice_uuid a descargar
     * @return Archivo ZIP con los XMLs
     */
    @Operation(
            summary = "Descargar XMLs en ZIP",
            description = "Genera un archivo ZIP con los XMLs de los documentos seleccionados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo ZIP generado exitosamente",
                    content = @Content(mediaType = "application/zip")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validacion - Lista de UUIDs vacia"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping(value = "/download/xml", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/zip")
    public ResponseEntity<byte[]> downloadXmlZip(
            @Valid @RequestBody BulkDownloadRequest request) {

        log.info("Solicitud de descarga masiva XML recibida. Documentos: {}", request.getInvoiceUuids().size());

        byte[] zipContent = invoiceService.downloadXmlZip(request);

        String filename = buildZipFileName("xml", request.getDocumentType());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(zipContent.length);

        log.info("Descarga masiva XML completada. Archivo: {}, Tamano: {} bytes", filename, zipContent.length);

        return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);
    }

    /**
     * POST /api/invoices/download/pdf
     *
     * ENDPOINT: Descarga masiva de PDFs en archivo ZIP (STM-396)
     *
     * FUNCIONALIDAD:
     * - Recibe lista de invoice_uuid a descargar
     * - Genera archivo ZIP con los PDFs de los documentos
     * - NOTA: Por ahora genera archivos placeholder, PDF real pendiente de implementar
     *
     * @param request Lista de invoice_uuid a descargar
     * @return Archivo ZIP con los PDFs
     */
    @Operation(
            summary = "Descargar PDFs en ZIP",
            description = "Genera un archivo ZIP con los PDFs de los documentos seleccionados (placeholder por ahora)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo ZIP generado exitosamente",
                    content = @Content(mediaType = "application/zip")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validacion - Lista de UUIDs vacia"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping(value = "/download/pdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/zip")
    public ResponseEntity<byte[]> downloadPdfZip(
            @Valid @RequestBody BulkDownloadRequest request) {

        log.info("Solicitud de descarga masiva PDF recibida. Documentos: {}", request.getInvoiceUuids().size());

        byte[] zipContent = invoiceService.downloadPdfZip(request);

        String filename = buildZipFileName("pdf", request.getDocumentType());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(zipContent.length);

        log.info("Descarga masiva PDF completada. Archivo: {}, Tamano: {} bytes", filename, zipContent.length);

        return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);
    }

    /**
     * POST /api/invoices/export/csv
     *
     * ENDPOINT: Exportar datos a CSV (STM-396)
     *
     * FUNCIONALIDAD:
     * - Usa los mismos filtros que /search
     * - Exporta hasta 10,000 registros
     * - Genera archivo CSV con los campos principales
     *
     * @param searchRequest Filtros de busqueda
     * @return Archivo CSV con los datos
     */
    @Operation(
            summary = "Exportar a CSV",
            description = "Genera un archivo CSV con los datos de los documentos segun los filtros de busqueda"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo CSV generado exitosamente",
                    content = @Content(mediaType = "text/csv")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validacion - Filtros obligatorios faltantes"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping(value = "/export/csv", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @Valid @RequestBody InvoiceSearchRequest searchRequest) {

        log.info("Solicitud de exportacion CSV recibida. RFC Emisor: {}, Tipo: {}",
                searchRequest.getRfcEmisor(), searchRequest.getTipoDocumento());

        byte[] csvContent = invoiceService.exportCsv(searchRequest);

        String filename = buildCsvFileName(searchRequest.getTipoDocumento());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(csvContent.length);

        log.info("Exportacion CSV completada. Archivo: {}, Tamano: {} bytes", filename, csvContent.length);

        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }

    /**
     * POST /api/invoices/export/xlsx
     *
     * ENDPOINT: Exportar datos a XLSX con 2 hojas
     *
     * FUNCIONALIDAD:
     * - Usa los mismos filtros que /search
     * - Genera archivo XLSX con 2 hojas:
     *   - Hoja 1 "Facturas": Lista de facturas encontradas
     *   - Hoja 2 "Notas de Credito": NC relacionadas a las facturas
     * - Exporta hasta 10,000 registros
     *
     * @param searchRequest Filtros de busqueda
     * @param lang Idioma para headers (default: es)
     * @return Archivo XLSX con los datos
     */
    @Operation(
            summary = "Exportar a XLSX",
            description = "Genera un archivo XLSX con 2 hojas: Facturas y Notas de Credito relacionadas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo XLSX generado exitosamente",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validacion - Filtros obligatorios faltantes"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sin resultados para exportar"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping(value = "/export/xlsx",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportToXlsx(
            @Valid @RequestBody InvoiceSearchRequest searchRequest,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        log.info("Solicitud de exportacion XLSX recibida. RFC Emisor: {}, Tipo: {}",
                searchRequest.getRfcEmisor(), searchRequest.getTipoDocumento());

        byte[] xlsxContent = invoiceService.exportToXlsx(searchRequest, lang);

        String filename = buildXlsxFileName(searchRequest.getTipoDocumento());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(xlsxContent.length);

        log.info("Exportacion XLSX completada. Archivo: {}, Tamano: {} bytes", filename, xlsxContent.length);

        return new ResponseEntity<>(xlsxContent, headers, HttpStatus.OK);
    }

    // ========== STM-410: BÚSQUEDA Y ACTUALIZACIÓN POR ESTATUS ==========

    /**
     * POST /api/invoices/search/by-status
     *
     * ENDPOINT: Buscar facturas con estatus obligatorio (STM-410)
     *
     * FUNCIONALIDAD:
     * - Similar a /search pero con estatus como campo obligatorio
     * - Usado para filtrar documentos en estados específicos del flujo
     *
     * @param searchRequest Filtros de búsqueda con estatus obligatorio
     * @return Página de resultados
     */
    @Operation(
            summary = "Buscar facturas por estatus (STM-410)",
            description = "Busca documentos fiscales con estatus obligatorio. Útil para filtrar por estados específicos del flujo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda exitosa",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación - Estatus es obligatorio"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping(value = "/search/by-status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<InvoiceSearchResponse>> searchInvoicesByStatus(
            @Valid @RequestBody InvoiceStatusSearchRequest searchRequest) {

        log.info("Solicitud de búsqueda por estatus recibida. RFC Emisor: {}, Estatus: {}, Tipo: {}",
                searchRequest.getRfcEmisor(),
                searchRequest.getEstatus(),
                searchRequest.getTipoDocumento());

        Page<InvoiceSearchResponse> results = invoiceService.searchInvoicesByStatus(searchRequest);

        log.info("Búsqueda por estatus completada. Resultados: {} de {} totales",
                results.getNumberOfElements(), results.getTotalElements());

        return ResponseEntity.ok(results);
    }

    /**
     * PUT /api/invoices/{uuid}/status
     *
     * ENDPOINT: Actualizar estatus con validación de transición (STM-410)
     *
     * FUNCIONALIDAD:
     * - Valida la transición contra el servicio de tren de estatus (catalogos-api)
     * - Si el estatus destino es 7 (Pendiente de Pago), guarda fechaContabilizacion
     * - No hay fallback: si catalogos-api no está disponible, falla la petición
     *
     * CÓDIGOS DE ERROR:
     * - WRN7010: Estatus origen no catalogado en el tren de estatus
     * - WRN7011: Transición de estatus no permitida
     *
     * @param uuid UUID fiscal del documento
     * @param request Datos de actualización
     * @return Respuesta con resultado de la operación
     */
    @Operation(
            summary = "Actualizar estatus con validación (STM-410)",
            description = "Actualiza el estatus validando la transición contra el servicio de tren de estatus. " +
                    "Si estatusDestino=7, guarda fechaContabilizacion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estatus actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = InvoiceStatusUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación o transición no permitida (WRN7010, WRN7011)",
                    content = @Content(schema = @Schema(implementation = InvoiceStatusUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Documento no encontrado",
                    content = @Content(schema = @Schema(implementation = InvoiceStatusUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio de tren de estatus no disponible",
                    content = @Content(schema = @Schema(implementation = InvoiceStatusUpdateResponse.class))
            )
    })
    @PutMapping(value = "/{uuid}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvoiceStatusUpdateResponse> updateInvoiceStatus(
            @PathVariable("uuid") String uuid,
            @Valid @RequestBody InvoiceStatusUpdateRequest request) {

        log.info("Solicitud de actualización de estatus recibida. UUID: {}, Transición: {} -> {}",
                uuid, request.getEstatusOrigen(), request.getEstatusDestino());

        InvoiceStatusUpdateResponse response = invoiceService.updateInvoiceStatus(uuid, request);

        if (response.isSuccess()) {
            log.info("Actualización de estatus exitosa. UUID: {}, Nuevo estatus: {}",
                    uuid, response.getEstatusNuevo());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Actualización de estatus fallida. UUID: {}, Código: {}, Mensaje: {}",
                    uuid, response.getCode(), response.getMessage());

            // Determinar código HTTP según tipo de error
            if ("BUS3101".equals(response.getCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if ("SVC5001".equals(response.getCode())) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    // ========== METODOS AUXILIARES ==========

    /**
     * Construye el nombre del archivo ZIP para descarga.
     */
    private String buildZipFileName(String type, String documentType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String docTypeName = "E".equals(documentType) ? "NC" : "Facturas";
        return String.format("%s_%s_%s.zip", docTypeName, type.toUpperCase(), timestamp);
    }

    /**
     * Construye el nombre del archivo CSV para exportacion.
     */
    private String buildCsvFileName(String documentType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String docTypeName = "E".equals(documentType) ? "NotasCredito" : "Facturas";
        return String.format("Exportacion_%s_%s.csv", docTypeName, timestamp);
    }

    /**
     * Construye el nombre del archivo XLSX para exportacion.
     */
    private String buildXlsxFileName(String documentType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String docTypeName = "E".equals(documentType) ? "NotasCredito" : "Facturas";
        return String.format("Exportacion_%s_%s.xlsx", docTypeName, timestamp);
    }

}