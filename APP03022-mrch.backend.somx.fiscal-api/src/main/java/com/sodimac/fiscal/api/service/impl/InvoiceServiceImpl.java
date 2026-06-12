package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.mapper.InvoiceMapper;
import com.sodimac.fiscal.api.model.dto.*;
import com.sodimac.fiscal.api.model.dto.invoicexml.CfdiRelacionadoDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.CfdiRelacionadosDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.entity.*;
import com.sodimac.fiscal.api.model.enums.CreditNoteStatus;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.model.enums.FiscalSuccessCode;
import com.sodimac.fiscal.api.model.enums.InvoiceStatus;
import com.sodimac.fiscal.api.model.enums.AuditAction;
import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import com.sodimac.fiscal.api.pdf.PdfRenderService;
import com.sodimac.fiscal.api.repository.*;
import com.sodimac.fiscal.api.repository.InvoiceStatusHistoryRepository;
import com.sodimac.fiscal.api.repository.specification.InvoiceSpecification;
import com.sodimac.fiscal.api.model.dto.FinanzasReceptionResponse;
import com.sodimac.fiscal.api.model.entity.CatParameterEntity;
import com.sodimac.fiscal.api.model.enums.CatParameterKey;
import com.sodimac.fiscal.api.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implementación del servicio de gestión de facturas y notas de crédito.
 *
 * Implementa:
 * - STM-337: Registro de facturas y notas de crédito
 * - STM-339: Actualización de facturas y notas de crédito
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    // Mappers
    private final InvoiceMapper invoiceMapper;

    // Services
    private final CfdiXmlProcessorService cfdiProcessor;
    private final AddendaValidationService addendaValidator;
    private final XmlDocumentTypeDetector documentTypeDetector;
    private final IssuerService issuerService;
    private final ReceiverService receiverService;
    private final TaxExtractionService taxExtractionService;
    private final MessageCatalogService messageCatalog;
    private final SatCatalogService satCatalogService;
    private final PdfRenderService pdfRenderService;
    private final UtilsApiService utilsApiService;
    private final StatusTrainApiService statusTrainApiService;
    private final ActivityLogService activityLogService;
    private final AuditoriaApiService auditoriaApiService;
    private final FinanzasApiService finanzasApiService;

    // Repositories
    private final InvoiceRepository invoiceRepository;
    private final PaymentsRepository paymentsRepository;
    private final AddendumRepository addendumRepository;
    private final AuthorizedReceiverCatalogRepository receiverCatalogRepository;
    private final VersionCatalogRepository versionCatalogRepository;
    private final LogRepository logRepository;
    private final RelatedCfdiRepository relatedCfdiRepository;
    private final InvoiceStatusHistoryRepository invoiceStatusHistoryRepository;
    private final CatParameterRepository catParameterRepository;
    private final GcsStorageService gcsStorageService;

    // ========== CONSULTA ==========

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> findAll(Pageable pageable) {
        log.debug("Finding all invoices with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<InvoiceEntity> entities = invoiceRepository.findAll(pageable);
        return entities.map(invoiceMapper::toDto);
    }

    // ========== REGISTRO (STM-337) ==========

    @Override
    @Transactional
    public InvoiceRegistrationResponse registerInvoice(MultipartFile xmlFile, String idTransaccion,
            String receptionId, String supplierNumber, String purchaseOrderNumber, MultipartFile pdfFile) {
        final String SERVICE_NAME = "InvoiceService.registerInvoice";
        long startTime = System.currentTimeMillis();

        log.info("========================================");
        log.info("INICIO REGISTRO FACTURA/NOTA DE CREDITO");
        log.info("========================================");
        log.info("Archivo: {}, idTransaccion: {}", xmlFile.getOriginalFilename(), idTransaccion);
        log.info("Tamano del archivo: {} bytes", xmlFile.getSize());

        // Registrar request en bitácora (STM-704)
        auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_REQUEST.getCode(), SERVICE_NAME,
                "system", false, "Inicio de registro de factura/NC",
                "Archivo: " + xmlFile.getOriginalFilename() + ", Tamano: " + xmlFile.getSize() + " bytes",
                Map.of("fileName", xmlFile.getOriginalFilename(), "fileSize", xmlFile.getSize()), null);

        String xmlContent = null;
        InvoiceXmlDto invoiceDto = null;
        TipoDocumentoFiscal tipoDocumento = null;

        try {
            // === PASO 1: LEER CONTENIDO DEL ARCHIVO XML ===
            log.info("Paso 1: Leyendo contenido del archivo XML");
            xmlContent = readXmlFile(xmlFile);
            log.debug("Archivo XML leido correctamente. Longitud: {} caracteres", xmlContent.length());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.LEER_ARCHIVO_XML.getCode(), SERVICE_NAME,
                    "system", false, "Archivo XML leido correctamente",
                    "Longitud: " + xmlContent.length() + " caracteres", null, null);

            // === PASO 2: DETECTAR TIPO DE DOCUMENTO ===
            log.info("Paso 2: Detectando tipo de documento fiscal (I=Factura, E=Nota de Credito)");
            tipoDocumento = documentTypeDetector.detectDocumentType(xmlContent);
            log.info("Tipo de documento detectado: {} ({})",
                    tipoDocumento.getCodigo(), tipoDocumento.getDescripcion());

            // Una factura (TipoDeComprobante I) con complemento CartaPorte se detecta como
            // FACTURA_CARTA_PORTE (transporte foraneo). Para el registro es una FACTURA: se procesa
            // igual y la addenda CartaPorte se valida por contenido del nodo (Addenda_Sodimac_CartaPorte).
            // Un Traslado real (T) tambien cae aqui pero se rechaza despues en validateCfdiByType
            // porque su TipoDeComprobante no es "I".
            if (tipoDocumento == TipoDocumentoFiscal.FACTURA_CARTA_PORTE) {
                log.info("Documento con complemento CartaPorte detectado; se procesa como FACTURA (I)");
                tipoDocumento = TipoDocumentoFiscal.FACTURA;
            }

            // Validar que sea solo I o E (no T, P, N)
            if (tipoDocumento != TipoDocumentoFiscal.FACTURA && tipoDocumento != TipoDocumentoFiscal.NOTA_CREDITO) {
                log.error("Tipo de documento no permitido: {}", tipoDocumento.getCodigo());
                auditoriaApiService.logActivity(idTransaccion, AuditAction.DETECTAR_TIPO_DOCUMENTO.getCode(), SERVICE_NAME,
                        "system", true, "Tipo de documento no permitido: " + tipoDocumento.getCodigo(),
                        "Solo se permiten tipos I (Factura) y E (Nota de Credito)", null, null);
                messageCatalog.throwException(FiscalMessageCode.BUS023);
            }
            auditoriaApiService.logActivity(idTransaccion, AuditAction.DETECTAR_TIPO_DOCUMENTO.getCode(), SERVICE_NAME,
                    "system", false, "Tipo de documento detectado: " + tipoDocumento.getDescripcion(),
                    "Codigo: " + tipoDocumento.getCodigo(), null, null);

            // === PASO 3: PROCESAR Y PARSEAR XML CFDI ===
            log.info("Paso 3: Procesando y validando estructura del XML CFDI");
            invoiceDto = cfdiProcessor.processCfdi(xmlContent, tipoDocumento);
            log.info("XML procesado exitosamente. Serie: {}, Folio: {}, Total: {}",
                    invoiceDto.getSerie(), invoiceDto.getFolio(), invoiceDto.getTotal());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PROCESAR_XML_CFDI.getCode(), SERVICE_NAME,
                    "system", false, "XML CFDI procesado exitosamente",
                    "Serie: " + invoiceDto.getSerie() + ", Folio: " + invoiceDto.getFolio() + ", Total: " + invoiceDto.getTotal(),
                    Map.of("serie", String.valueOf(invoiceDto.getSerie()),
                            "folio", String.valueOf(invoiceDto.getFolio()),
                            "rfcEmisor", String.valueOf(invoiceDto.getEmisorRfc()),
                            "rfcReceptor", String.valueOf(invoiceDto.getReceptorRfc())), null);

            // === PASO 3.1: VALIDAR SERIE Y FOLIO (STM-395/STM-397) ===
            log.info("Paso 3.1: Validando que el documento tenga serie y folio");
            validateSeriesAndFolio(invoiceDto, tipoDocumento);
            log.info("Serie y folio validados correctamente");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_SERIE_FOLIO.getCode(), SERVICE_NAME,
                    "system", false, "Serie y folio validados correctamente",
                    "Serie: " + invoiceDto.getSerie() + ", Folio: " + invoiceDto.getFolio(), null, null);

            // === PASO 4: VALIDAR VERSION CFDI VIGENTE ===
            log.info("Paso 4: Validando version CFDI vigente");
            validateCfdiVersion(invoiceDto, tipoDocumento);
            log.info("Version CFDI {} validada correctamente", invoiceDto.getVersion());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_VERSION_CFDI.getCode(), SERVICE_NAME,
                    "system", false, "Version CFDI validada correctamente",
                    "Version: " + invoiceDto.getVersion(), null, null);

            // === PASO 5: VALIDAR RFC RECEPTOR AUTORIZADO ===
            log.info("Paso 5: Validando RFC receptor autorizado");
            validateAuthorizedReceiver(invoiceDto.getReceptorRfc());
            log.info("RFC receptor {} autorizado y vigente", invoiceDto.getReceptorRfc());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_RFC_RECEPTOR.getCode(), SERVICE_NAME,
                    "system", false, "RFC receptor autorizado y vigente",
                    "RFC: " + invoiceDto.getReceptorRfc(), null, null);

            // === PASO 6: OBTENER EMISOR Y VALIDAR DUPLICIDAD (STM-395/STM-397) ===
            log.info("Paso 6: Obteniendo emisor para validaciones de duplicidad");
            IssuerEntity issuer = issuerService.getOrCreate(
                    invoiceDto.getEmisorRfc(),
                    invoiceDto.getEmisorNombre(),
                    invoiceDto.getEmisorRegimenFiscal()
            );
            log.debug("Emisor obtenido. UUID: {}", issuer.getIssuerUuid());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.OBTENER_EMISOR.getCode(), SERVICE_NAME,
                    "system", false, "Emisor obtenido correctamente",
                    "RFC: " + invoiceDto.getEmisorRfc() + ", Issuer UUID: " + issuer.getIssuerUuid(), null, null);

            // === PASO 6.1: VALIDAR DUPLICADO POR SERIE+FOLIO (STM-395/STM-397) ===
            log.info("Paso 6.1: Validando duplicado por serie+folio del proveedor");
            validateNoDuplicateBySeriesAndFolio(invoiceDto.getSerie(), invoiceDto.getFolio(),
                    issuer.getIssuerUuid(), tipoDocumento);
            log.info("No existe documento duplicado por serie+folio");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_SERIE_FOLIO.getCode(), SERVICE_NAME,
                    "system", false, "No existe documento duplicado por serie+folio",
                    "Serie: " + invoiceDto.getSerie() + ", Folio: " + invoiceDto.getFolio(), null, null);

            // === PASO 6.2: EXTRAER UUID FISCAL Y VALIDAR DUPLICADO (STM-395/STM-397) ===
            log.info("Paso 6.2: Extrayendo UUID fiscal y validando duplicado por UUID");
            UUID fiscalUuid = extractFiscalUuid(invoiceDto);
            log.debug("UUID fiscal extraido: {}", fiscalUuid);

            validateNoDuplicateByUuid(fiscalUuid, issuer.getIssuerUuid(), tipoDocumento);
            log.info("Documento no duplicado. UUID unico: {}", fiscalUuid);
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_UUID.getCode(), SERVICE_NAME,
                    "system", false, "Documento no duplicado, UUID unico",
                    "UUID fiscal: " + fiscalUuid, null, null);

            // === PASO 7: VALIDAR TOLERANCIA IMPORTE (solo Facturas) ===
            log.info("Paso 7: Validando tolerancia entre subtotal factura e importe recepción");
            if (tipoDocumento == TipoDocumentoFiscal.FACTURA) {
                validateImporteTolerance(invoiceDto, receptionId, idTransaccion, SERVICE_NAME);
            }
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), SERVICE_NAME,
                    "system", false, "Validación de tolerancia completada",
                    "receptionId: " + receptionId, null, null);

            // === PASO 8: VALIDAR CON SAT (OPCIONAL - COMENTADO POR AHORA) ===
            // TODO: Implementar validación SAT mediante PAC cuando esté disponible
            // log.info("Paso 8: Validando documento con SAT via PAC");
            // validateWithSat(xmlContent, invoiceDto);
            // log.info("Validacion SAT completada exitosamente");
            log.info("Paso 8: Validacion SAT omitida (pendiente de implementar via PAC)");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_SAT.getCode(), SERVICE_NAME,
                    "system", false, "Validacion SAT omitida (pendiente de implementar via PAC)",
                    "Este paso se habilitara cuando se integre el servicio PAC", null, null);

            // === PASO 9: PERSISTIR EN BASE DE DATOS ===
            log.info("Paso 9: Persistiendo documento en base de datos");
            InvoiceEntity savedInvoice = saveInvoiceToDatabase(
                    invoiceDto,
                    xmlContent,
                    fiscalUuid,
                    tipoDocumento,
                    issuer,
                    supplierNumber,
                    purchaseOrderNumber,
                    receptionId
            );
            log.info("Documento persistido exitosamente. Invoice UUID: {}", savedInvoice.getInvoiceUuid());

            // === PASO 9.5: SUBIR PDF A GCS (opcional) ===
            if (pdfFile != null && !pdfFile.isEmpty()) {
                try {
                    String gcsObject = gcsStorageService.uploadPdf(pdfFile, savedInvoice.getInvoiceUuid().toString());
                    savedInvoice.setPdfGcsObject(gcsObject);
                    invoiceRepository.save(savedInvoice);
                    log.info("PDF subido a GCS. Object: {}", gcsObject);
                } catch (Exception e) {
                    log.warn("PDF no pudo subirse a GCS (no crítico, factura ya registrada): {}", e.getMessage());
                }
            }

            auditoriaApiService.logActivity(idTransaccion, AuditAction.PERSISTIR_DOCUMENTO.getCode(), SERVICE_NAME,
                    "system", false, "Documento persistido exitosamente en base de datos",
                    "Invoice UUID: " + savedInvoice.getInvoiceUuid(),
                    Map.of("invoiceUuid", savedInvoice.getInvoiceUuid().toString(),
                            "fiscalUuid", fiscalUuid.toString()), null);

            // === PASO 10: CONSTRUIR RESPUESTA ===
            log.info("Paso 10: Construyendo respuesta de registro exitoso");
            InvoiceRegistrationResponse response = buildRegistrationSuccessResponse(
                    savedInvoice,
                    fiscalUuid,
                    tipoDocumento,
                    true,
                    invoiceDto
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("========================================");
            log.info("REGISTRO COMPLETADO EXITOSAMENTE");
            log.info("========================================");
            log.info("Codigo de respuesta: {}", response.getCode());
            log.info("Invoice UUID: {}", response.getInvoiceUuid());
            log.info("Fiscal UUID: {}", response.getFiscalUuid());
            log.info("Duracion: {} ms", duration);

            // Registrar response exitoso en bitácora (STM-704)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_RESPONSE.getCode(), SERVICE_NAME,
                    "system", false, "Registro completado exitosamente",
                    "Codigo: " + response.getCode() + ", Invoice UUID: " + response.getInvoiceUuid(),
                    Map.of("code", response.getCode(),
                            "invoiceUuid", String.valueOf(response.getInvoiceUuid()),
                            "fiscalUuid", String.valueOf(response.getFiscalUuid()),
                            "hasAddenda", response.isHasAddenda(),
                            "pendingAddenda", response.isPendingAddenda()), duration);

            return response;

        } catch (FiscalException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error de validacion de negocio: [{}] {}", e.getCode(), e.getMessage());
            log.error("========================================");
            log.error("REGISTRO FALLIDO - ERROR DE NEGOCIO");
            log.error("========================================");

            // Registrar error de negocio en bitácora (STM-704)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_ERROR_NEGOCIO.getCode(), SERVICE_NAME,
                    "system", true, "Error de validacion: " + e.getMessage(),
                    "Codigo: " + e.getCode() + ", Mensaje: " + e.getMessage(),
                    Map.of("errorCode", e.getCode(), "errorMessage", e.getMessage()), duration);

            return InvoiceRegistrationResponse.error(e.getCode(), e.getMessage());

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error inesperado durante el registro", e);
            log.error("========================================");
            log.error("REGISTRO FALLIDO - ERROR TECNICO");
            log.error("========================================");

            // Registrar error técnico en bitácora (STM-704)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_ERROR_TECNICO.getCode(), SERVICE_NAME,
                    "system", true, "Error inesperado durante el registro",
                    e.getClass().getName() + ": " + e.getMessage(), null, duration);

            return InvoiceRegistrationResponse.error(
                    FiscalMessageCode.ERR003.getCode(),
                    "Error inesperado: " + e.getMessage()
            );
        }
    }

    // ========== ACTUALIZACIÓN (STM-339) ==========

    @Override
    @Transactional
    public InvoiceUpdateResponse updateInvoice(InvoiceUpdateRequest request) {
        log.info("========================================");
        log.info("INICIO ACTUALIZACION FACTURA/NOTA DE CREDITO");
        log.info("========================================");
        log.info("UUID: {}", request.getUuid());
        log.info("Numero Proveedor: {}", request.getNumeroProveedor());
        log.info("Nuevo Estatus: {}", request.getEstatus());
        log.info("Usuario Actualizacion: {}", request.getIdUsuarioActualizacion());

        // Variables para bitácora
        final String SERVICE_NAME_UPDATE = "InvoiceService.updateInvoice";
        String traceId = UUID.randomUUID().toString();
        long startTimeMs = System.currentTimeMillis();
        LocalDateTime startTime = LocalDateTime.now();
        String requestDataJson = buildRequestDataJson(request);
        UUID invoiceUuid = null;  // Se asigna cuando se encuentra la factura (para log con FK correcta)

        try {
            // === PASO 1: BUSCAR Y VALIDAR DOCUMENTO ===
            log.info("Paso 1: Buscando documento por UUID");
            InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(request.getUuid())
                    .orElseThrow(() -> {
                        log.error("Documento no encontrado. UUID: {}", request.getUuid());
                        return new FiscalException(FiscalMessageCode.BUS046, "UUID: " + request.getUuid());
                    });
            invoiceUuid = invoice.getInvoiceUuid();  // Capturar invoiceUuid para la bitácora
            log.info("Documento encontrado. Invoice UUID: {}, Tipo: {}",
                    invoice.getInvoiceUuid(), invoice.getDocumentType());

            // === PASO 2: VALIDAR PROVEEDOR (STM-339) ===
            log.info("Paso 2: Validando que el documento pertenezca al proveedor");
            validateSupplierOwnership(invoice, request.getNumeroProveedor());

            // === PASO 3: VALIDAR TRANSICIÓN DE ESTATUS ===
            log.info("Paso 3: Validando transicion de estatus");
            Integer currentStatusCode = invoice.getStatus();
            Integer newStatusCode = request.getEstatus();
            String documentType = invoice.getDocumentType();

            validateStatusTransition(currentStatusCode, newStatusCode, documentType);
            log.info("Transicion de estatus validada: {} -> {}", currentStatusCode, newStatusCode);

            // === PASO 4: ACTUALIZAR ESTATUS ===
            log.info("Paso 4: Actualizando estatus");
            invoice.setStatus(newStatusCode);
            invoice.setUpdatedBy(request.getIdUsuarioActualizacion());
            // Nota: BaseEntity maneja updated_at automáticamente con @PreUpdate

            invoice = invoiceRepository.save(invoice);
            log.info("Estatus actualizado exitosamente (usuario actualización: {})",
                    request.getIdUsuarioActualizacion());

            // === PASO 5: ACTUALIZAR ADDENDA (SI SE PROPORCIONA) ===
            boolean addendaActualizada = false;
            if (request.getAddenda() != null) {
                log.info("Paso 5: Actualizando addenda");
                addendaActualizada = updateAddenda(invoice, request.getAddenda());
                if (addendaActualizada) {
                    log.info("Addenda actualizada exitosamente");
                } else {
                    log.warn("No se pudo actualizar la addenda");
                }
            }

            // === PASO 6: CONSTRUIR RESPUESTA ===
            log.info("Paso 6: Construyendo respuesta de actualizacion");
            InvoiceUpdateResponse response = buildUpdateSuccessResponse(
                    invoice,
                    currentStatusCode,
                    newStatusCode,
                    documentType,
                    addendaActualizada
            );

            // === PASO 7: REGISTRAR EN BITÁCORA (estandarizado via auditoria-api) ===
            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.info("Paso 7: Registrando actividad en bitacora (auditoria-api)");
            auditoriaApiService.logActivity(traceId, AuditAction.UPDATE_RESPONSE.getCode(), SERVICE_NAME_UPDATE,
                    String.valueOf(request.getIdUsuarioActualizacion()), false,
                    "Actualizacion completada exitosamente",
                    "Codigo: " + response.getCode() + ", Estatus: " + currentStatusCode + " -> " + newStatusCode,
                    Map.of("invoiceUuid", invoiceUuid.toString(),
                            "uuid", request.getUuid(),
                            "estatusAnterior", currentStatusCode,
                            "estatusNuevo", newStatusCode,
                            "request", requestDataJson,
                            "response", buildResponseDataJson(response)), durationMs);

            log.info("========================================");
            log.info("ACTUALIZACION COMPLETADA EXITOSAMENTE");
            log.info("========================================");
            log.info("Codigo de respuesta: {}", response.getCode());
            log.info("Estatus anterior: {}, Estatus nuevo: {}", currentStatusCode, newStatusCode);

            return response;

        } catch (FiscalException e) {
            log.error("Error de validacion de negocio: [{}] {}", e.getCode(), e.getMessage());
            log.error("========================================");
            log.error("ACTUALIZACION FALLIDA - ERROR DE NEGOCIO");
            log.error("========================================");

            // Registrar error en bitácora (auditoria-api)
            long durationMs = System.currentTimeMillis() - startTimeMs;
            auditoriaApiService.logActivity(traceId, AuditAction.UPDATE_ERROR_NEGOCIO.getCode(), SERVICE_NAME_UPDATE,
                    String.valueOf(request.getIdUsuarioActualizacion()), true,
                    "Error de validacion: " + e.getMessage(),
                    "Codigo: " + e.getCode(),
                    Map.of("errorCode", e.getCode(), "errorMessage", e.getMessage(),
                            "uuid", request.getUuid(), "request", requestDataJson), durationMs);

            return InvoiceUpdateResponse.error(e.getCode(), e.getMessage());

        } catch (Exception e) {
            log.error("Error inesperado durante la actualizacion", e);
            log.error("========================================");
            log.error("ACTUALIZACION FALLIDA - ERROR TECNICO");
            log.error("========================================");

            // Registrar error en bitácora (auditoria-api)
            long durationMs = System.currentTimeMillis() - startTimeMs;
            auditoriaApiService.logActivity(traceId, AuditAction.UPDATE_ERROR_TECNICO.getCode(), SERVICE_NAME_UPDATE,
                    String.valueOf(request.getIdUsuarioActualizacion()), true,
                    "Error inesperado durante la actualizacion",
                    e.getClass().getName() + ": " + e.getMessage(),
                    Map.of("uuid", request.getUuid(), "request", requestDataJson), durationMs);

            return InvoiceUpdateResponse.error(
                    FiscalMessageCode.ERR003.getCode(),
                    "Error inesperado: " + e.getMessage()
            );
        }
    }

    // ========== MÉTODOS PRIVADOS - REGISTRO ==========

    /**
     * Lee el contenido del archivo XML.
     */
    private String readXmlFile(MultipartFile file) {
        log.debug("Iniciando lectura de archivo XML");
        try {
            StringBuilder xmlBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    xmlBuilder.append(line);
                }
            }

            String xmlContent = xmlBuilder.toString();
            if (xmlContent.isEmpty()) {
                log.error("El archivo XML esta vacio");
                messageCatalog.throwException(FiscalMessageCode.ERR001);
            }

            // Algunos CFDI (ej. facturas con complemento CartaPorte) traen BOM UTF-8 (U+FEFF) o
            // caracteres antes del primer '<'. Se descartan aqui, en el origen, para que todos los
            // consumidores (parser CFDI, validador de addenda, detector) reciban XML limpio y no
            // fallen con "Content is not allowed in prolog".
            int firstTag = xmlContent.indexOf('<');
            if (firstTag > 0) {
                log.debug("Descartando {} caracter(es) previos al primer elemento XML (BOM/prolog)", firstTag);
                xmlContent = xmlContent.substring(firstTag);
            }

            log.debug("Archivo XML leido correctamente");
            return xmlContent;

        } catch (Exception e) {
            log.error("Error leyendo archivo XML", e);
            messageCatalog.throwException(FiscalMessageCode.ERR005, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Valida que la versión del CFDI sea vigente en el sistema.
     */
    private void validateCfdiVersion(InvoiceXmlDto invoiceDto, TipoDocumentoFiscal tipoDocumento) {
        log.debug("Validando version CFDI: {}", invoiceDto.getVersion());

        BigDecimal version = new BigDecimal(invoiceDto.getVersion());
        String documentType = tipoDocumento.getCodigo();

        // Buscar versión vigente en catálogo
        if (versionCatalogRepository
                .findByVersionAndDocumentTypeAndStatus(version, documentType, 1)
                .isEmpty()) {
            log.error("Version {} no vigente para tipo de documento {}", version, documentType);
            String additionalInfo = String.format("Versión recibida: %s, Tipo documento: %s", version, documentType);
            messageCatalog.throwException(FiscalMessageCode.BUS021, additionalInfo);
        }

        log.debug("Version {} encontrada en catalogo y vigente", version);
    }

    /**
     * Valida que el RFC receptor esté autorizado y vigente.
     */
    private void validateAuthorizedReceiver(String rfcReceptor) {
        log.debug("Validando RFC receptor autorizado: {}", rfcReceptor);

        // Buscar RFC en catálogo de receptores autorizados
        AuthorizedReceiverCatalogEntity receiver = receiverCatalogRepository
                .findByRfcAndStatus(rfcReceptor, 1)
                .orElse(null);

        if (receiver == null) {
            log.error("RFC receptor {} no autorizado o inactivo", rfcReceptor);
            messageCatalog.throwException(FiscalMessageCode.BUS008, "RFC: " + rfcReceptor);
        }

        log.debug("RFC receptor encontrado en catalogo de autorizados. ID: {}", receiver.getAuthorizedReceiverId());
    }

    /**
     * Extrae el UUID fiscal del TimbreFiscalDigital.
     *
     * El UUID fiscal SIEMPRE debe obtenerse del TimbreFiscalDigital que está dentro del Complemento.
     * Este es el identificador único e irrepetible asignado por el SAT al timbrar el CFDI.
     */
    private UUID extractFiscalUuid(InvoiceXmlDto invoiceDto) {
        log.debug("Extrayendo UUID fiscal del TimbreFiscalDigital");

        String uuidStr = null;

        // El UUID SOLO existe en el TimbreFiscalDigital (dentro del Complemento)
        if (invoiceDto.getTimbreFiscalDigital() != null) {
            uuidStr = invoiceDto.getTimbreFiscalDigital().getUuid();
        }

        if (uuidStr == null || uuidStr.trim().isEmpty()) {
            log.error("No se encontro UUID fiscal en el TimbreFiscalDigital del documento");
            messageCatalog.throwException(FiscalMessageCode.ERR003, "UUID fiscal no encontrado en TimbreFiscalDigital");
        }

        try {
            UUID uuid = UUID.fromString(uuidStr);
            log.debug("UUID fiscal extraido exitosamente: {}", uuid);
            return uuid;
        } catch (IllegalArgumentException e) {
            log.error("Formato de UUID invalido: {}", uuidStr);
            messageCatalog.throwException(FiscalMessageCode.ERR003, "Formato de UUID invalido: " + uuidStr);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Valida que el documento tenga serie y folio (STM-395/STM-397 CA01).
     *
     * @param invoiceDto DTO con los datos del documento
     * @param tipoDocumento Tipo de documento para determinar el mensaje de error
     */
    /**
     * Valida que la diferencia entre subtotal del XML y el importe de la recepción en finanzas-api
     * no supere la tolerancia configurada en cat_parameter (id=3, valor por defecto 40 pesos).
     * Lanza BUS057 si la diferencia supera la tolerancia.
     */
    private void validateImporteTolerance(InvoiceXmlDto invoiceDto, String receptionId,
            String idTransaccion, String serviceName) {

        if (receptionId == null || receptionId.isBlank()) {
            log.warn("receptionId no proporcionado; se omite validación de tolerancia");
            return;
        }

        BigDecimal subtotal;
        try {
            subtotal = new BigDecimal(invoiceDto.getSubTotal());
        } catch (Exception e) {
            log.warn("SubTotal del XML no es numérico ({}); se omite validación de tolerancia", invoiceDto.getSubTotal());
            return;
        }

        FinanzasReceptionResponse reception = finanzasApiService.getReception(receptionId);
        if (reception == null || reception.getAmount() == null) {
            log.warn("finanzas-api no retornó amount para receptionId {}; se omite validación", receptionId);
            return;
        }

        BigDecimal receptionAmount;
        try {
            receptionAmount = new BigDecimal(reception.getAmount());
        } catch (Exception e) {
            log.warn("Amount de recepción no es numérico ({}); se omite validación", reception.getAmount());
            return;
        }

        // Leer tolerancia de core_utils.cat_parameter (name='Tolerancia por importe', status=1)
        BigDecimal tolerance = BigDecimal.valueOf(40);
        try {
            CatParameterEntity param = catParameterRepository
                    .findById(CatParameterKey.TOLERANCIA_IMPORTE.getId())
                    .orElse(null);
            if (param != null && param.getValue() != null) {
                tolerance = new BigDecimal(param.getValue());
                log.debug("Tolerancia leída de BD: {}", tolerance);
            } else {
                log.warn("Parámetro 'Tolerancia por importe' no encontrado en BD; usando valor por defecto {}", tolerance);
            }
        } catch (Exception e) {
            log.warn("Error leyendo tolerancia de BD; usando valor por defecto {}: {}", tolerance, e.getMessage());
        }

        BigDecimal diff = subtotal.subtract(receptionAmount).abs();
        log.info("Validación tolerancia: subtotal={}, receptionAmount={}, diff={}, tolerancia={}",
                subtotal, receptionAmount, diff, tolerance);

        if (diff.compareTo(tolerance) > 0) {
            log.error("Diferencia {} supera tolerancia {} para receptionId {}", diff, tolerance, receptionId);
            messageCatalog.throwExceptionWithParams(FiscalMessageCode.BUS057,
                    subtotal.toPlainString(),
                    receptionAmount.toPlainString(),
                    tolerance.toPlainString());
        }

        log.info("Tolerancia validada correctamente. Diferencia: {} pesos", diff);
    }

    private void validateSeriesAndFolio(InvoiceXmlDto invoiceDto, TipoDocumentoFiscal tipoDocumento) {
        String serie = invoiceDto.getSerie();
        String folio = invoiceDto.getFolio();

        if ((serie == null || serie.isBlank()) || (folio == null || folio.isBlank())) {
            FiscalMessageCode code = (tipoDocumento == TipoDocumentoFiscal.FACTURA)
                    ? FiscalMessageCode.WRN7012
                    : FiscalMessageCode.WRN7015;
            log.error("Documento sin serie o folio. Tipo: {}, Serie: {}, Folio: {}",
                    tipoDocumento.getCodigo(), serie, folio);
            messageCatalog.throwException(code);
        }
    }

    /**
     * Valida que no exista documento duplicado por serie+folio del mismo proveedor (STM-395/STM-397 CA02).
     *
     * @param serie Serie del documento
     * @param folio Folio del documento
     * @param issuerUuid UUID del emisor (proveedor)
     * @param tipoDocumento Tipo de documento para determinar el mensaje de error
     */
    private void validateNoDuplicateBySeriesAndFolio(String serie, String folio,
            UUID issuerUuid, TipoDocumentoFiscal tipoDocumento) {
        String docType = tipoDocumento.getCodigo();

        if (invoiceRepository.existsBySeriesAndFolioAndIssuerUuidAndDocumentType(
                serie, folio, issuerUuid, docType)) {
            FiscalMessageCode code = (tipoDocumento == TipoDocumentoFiscal.FACTURA)
                    ? FiscalMessageCode.WRN7013
                    : FiscalMessageCode.WRN7016;
            log.error("Documento duplicado por serie+folio. Tipo: {}, Serie: {}, Folio: {}, Emisor: {}",
                    docType, serie, folio, issuerUuid);
            messageCatalog.throwException(code);
        }
    }

    /**
     * Valida que no exista documento duplicado por UUID del mismo proveedor (STM-395/STM-397 CA03).
     *
     * @param fiscalUuid UUID fiscal del documento
     * @param issuerUuid UUID del emisor (proveedor)
     * @param tipoDocumento Tipo de documento para determinar el mensaje de error
     */
    private void validateNoDuplicateByUuid(UUID fiscalUuid, UUID issuerUuid,
            TipoDocumentoFiscal tipoDocumento) {
        String docType = tipoDocumento.getCodigo();

        if (invoiceRepository.existsByFiscalUuidAndIssuerUuidAndDocumentType(
                fiscalUuid, issuerUuid, docType)) {
            FiscalMessageCode code = (tipoDocumento == TipoDocumentoFiscal.FACTURA)
                    ? FiscalMessageCode.WRN7014
                    : FiscalMessageCode.WRN7017;
            log.error("Documento duplicado por UUID. Tipo: {}, UUID: {}, Emisor: {}",
                    docType, fiscalUuid, issuerUuid);
            messageCatalog.throwException(code);
        }
    }

    /**
     * Guarda la factura/NC en la base de datos.
     *
     * @param issuer Emisor ya obtenido en las validaciones de duplicidad (STM-395/STM-397)
     */
    private InvoiceEntity saveInvoiceToDatabase(
            InvoiceXmlDto invoiceDto,
            String xmlContent,
            UUID fiscalUuid,
            TipoDocumentoFiscal tipoDocumento,
            IssuerEntity issuer,
            String supplierNumber,
            String purchaseOrderNumber,
            String receptionId) {

        log.info("Iniciando persistencia en base de datos");

        try {
            // 1. Emisor ya obtenido previamente en las validaciones de duplicidad
            log.debug("Usando emisor previamente obtenido. UUID: {}", issuer.getIssuerUuid());

            // 2. Guardar/obtener Receptor
            log.debug("Obteniendo o creando receptor: {}", invoiceDto.getReceptorRfc());
            ReceiverEntity receiver = receiverService.getOrCreate(
                    invoiceDto.getReceptorRfc(),
                    invoiceDto.getReceptorNombre(),
                    invoiceDto.getReceptorRegimenFiscalReceptor()
            );
            log.debug("Receptor obtenido. UUID: {}", receiver.getReceiverUuid());

            // 3. Crear entidad Invoice
            log.debug("Creando entidad Invoice");
            InvoiceEntity invoice = new InvoiceEntity();
            invoice.setFiscalUuid(fiscalUuid);
            invoice.setDocumentType(tipoDocumento.getCodigo());
            invoice.setSeries(invoiceDto.getSerie());
            invoice.setFolio(invoiceDto.getFolio());
            invoice.setVersion(new BigDecimal(invoiceDto.getVersion()));
            invoice.setTotal(new BigDecimal(invoiceDto.getTotal()));
            invoice.setSubtotal(new BigDecimal(invoiceDto.getSubTotal()));
            invoice.setCurrency(invoiceDto.getMoneda() != null ? invoiceDto.getMoneda() : "MXN");
            invoice.setPaymentMethod(invoiceDto.getMetodoPago());
            invoice.setPaymentForm(invoiceDto.getFormaPago());
            invoice.setPaymentConditions(invoiceDto.getCondicionesDePago());
            invoice.setPlaceOfIssue(invoiceDto.getLugarExpedicion());

            // Parsear fecha de emisión
            if (invoiceDto.getFecha() != null) {
                LocalDate issueDate = LocalDate.parse(invoiceDto.getFecha().substring(0, 10));
                invoice.setIssueDate(issueDate);
            }

            // Parsear fecha de certificación del timbre (si existe)
            if (invoiceDto.getTimbreFiscalDigital() != null &&
                invoiceDto.getTimbreFiscalDigital().getFechaTimbrado() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime certificationDate = LocalDateTime.parse(
                        invoiceDto.getTimbreFiscalDigital().getFechaTimbrado(),
                        formatter
                );
                invoice.setCertificationDate(certificationDate);
            }

            invoice.setXmlContent(xmlContent);
            // v1.0: Factura entra en estatus 3 (Recibida); NC mantiene 1 hasta alinear CreditNoteStatus
            invoice.setStatus(tipoDocumento == TipoDocumentoFiscal.FACTURA
                    ? InvoiceStatus.RECIBIDA.getCodigo()
                    : 1);
            invoice.setIssuerUuid(issuer.getIssuerUuid());
            invoice.setReceiverUuid(receiver.getReceiverUuid());

            // Descuento (si existe)
            if (invoiceDto.getDescuento() != null && !invoiceDto.getDescuento().isEmpty()) {
                invoice.setDiscount(new BigDecimal(invoiceDto.getDescuento()));
            }

            // Tipo de cambio (si existe)
            if (invoiceDto.getTipoCambio() != null && !invoiceDto.getTipoCambio().isEmpty()) {
                invoice.setExchangeRate(new BigDecimal(invoiceDto.getTipoCambio()));
            }

            log.debug("Guardando Invoice en base de datos");
            invoice = invoiceRepository.save(invoice);
            log.info("Invoice guardado exitosamente. UUID: {}", invoice.getInvoiceUuid());

            // 4. Guardar addenda con datos estructurados del FE
            log.debug("Guardando addenda con datos de proveedor/OC/recepción");
            saveAddenda(invoice, xmlContent, supplierNumber, purchaseOrderNumber, receptionId);

            // 5. Guardar impuestos
            log.debug("Guardando impuestos de la factura");
            taxExtractionService.extractAndSaveTaxes(invoice.getInvoiceUuid(), invoiceDto);

            // 6. Guardar CFDIs relacionados (para notas de crédito) - STM-1168
            if (tipoDocumento == TipoDocumentoFiscal.NOTA_CREDITO) {
                log.debug("Guardando CFDIs relacionados para Nota de Crédito");
                saveRelatedCfdis(invoice, invoiceDto);
            }

            return invoice;

        } catch (Exception e) {
            log.error("Error guardando en base de datos", e);
            messageCatalog.throwException(FiscalMessageCode.ERR003, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Guarda la addenda asociada a la factura/NC.
     */
    private void saveAddenda(InvoiceEntity invoice, String xmlContent,
            String supplierNumber, String purchaseOrderNumber, String receptionId) {
        log.debug("Creando registro de addenda para invoice UUID: {}", invoice.getInvoiceUuid());

        try {
            AddendumEntity addendum = new AddendumEntity();
            addendum.setInvoiceUuid(invoice.getInvoiceUuid());
            addendum.setAddendaType(5);
            addendum.setAddendumContent(xmlContent);

            if (supplierNumber != null) {
                addendum.setSupplierNumber(new BigDecimal(supplierNumber));
            }
            if (purchaseOrderNumber != null) {
                addendum.setPurchaseOrderNumber(purchaseOrderNumber);
            }
            if (receptionId != null) {
                addendum.setReceptionNumber(receptionId);
            }

            addendumRepository.save(addendum);
            log.debug("Addenda guardada. supplierNumber: {}, purchaseOrderNumber: {}, receptionId: {}",
                    supplierNumber, purchaseOrderNumber, receptionId);

        } catch (Exception e) {
            log.error("Error guardando addenda", e);
            log.warn("El invoice fue guardado pero la addenda falló");
        }
    }

    /**
     * Guarda los CFDIs relacionados de una Nota de Crédito (STM-1168).
     *
     * Valida que:
     * 1. La NC tenga al menos un CFDI relacionado
     * 2. Cada Factura relacionada exista en el sistema
     * 3. El documento relacionado sea una Factura (tipo I)
     *
     * Si alguna validación falla, lanza excepción y rechaza el registro de la NC.
     *
     * @param ncInvoice Entidad de la Nota de Crédito recién guardada
     * @param invoiceDto DTO con los datos del XML parseado
     * @throws FiscalException si no se cumplen las validaciones
     */
    private void saveRelatedCfdis(InvoiceEntity ncInvoice, InvoiceXmlDto invoiceDto) {
        log.info("=== INICIO GUARDADO CFDIS RELACIONADOS (STM-1168) ===");
        log.debug("NC UUID: {}, Fiscal UUID: {}", ncInvoice.getInvoiceUuid(), ncInvoice.getFiscalUuid());

        // 1. Validar que exista nodo CfdiRelacionados en el XML
        CfdiRelacionadosDto cfdiRelacionados = invoiceDto.getCfdiRelacionados();
        if (cfdiRelacionados == null || cfdiRelacionados.getCfdiRelacionado() == null
                || cfdiRelacionados.getCfdiRelacionado().isEmpty()) {
            log.error("La NC no contiene CFDIs relacionados en el XML");
            messageCatalog.throwException(FiscalMessageCode.BUS042);
        }

        String tipoRelacion = cfdiRelacionados.getTipoRelacion();
        List<CfdiRelacionadoDto> relacionados = cfdiRelacionados.getCfdiRelacionado();

        log.info("Tipo de relación: {}", tipoRelacion);
        log.info("Cantidad de CFDIs relacionados: {}", relacionados.size());

        // 2. Procesar cada CFDI relacionado
        for (CfdiRelacionadoDto relacionado : relacionados) {
            String uuidRelacionadoStr = relacionado.getUuid();
            log.debug("Procesando CFDI relacionado: {}", uuidRelacionadoStr);

            // 2.1 Parsear UUID
            UUID uuidRelacionado;
            try {
                uuidRelacionado = UUID.fromString(uuidRelacionadoStr);
            } catch (IllegalArgumentException e) {
                log.error("UUID de CFDI relacionado no válido: {}", uuidRelacionadoStr);
                messageCatalog.throwException(FiscalMessageCode.BUS043, "UUID: " + uuidRelacionadoStr);
                return; // Nunca alcanza aquí
            }

            // 2.2 Buscar la Factura relacionada por fiscal_uuid
            Optional<InvoiceEntity> facturaOpt = invoiceRepository.findByFiscalUuid(uuidRelacionado);

            if (facturaOpt.isEmpty()) {
                log.error("Factura relacionada no encontrada en BD. UUID: {}", uuidRelacionado);
                messageCatalog.throwException(FiscalMessageCode.BUS043, "UUID: " + uuidRelacionado);
            }

            InvoiceEntity facturaRelacionada = facturaOpt.get();
            log.debug("Factura encontrada. Invoice UUID: {}, Tipo: {}",
                    facturaRelacionada.getInvoiceUuid(), facturaRelacionada.getDocumentType());

            // 2.3 Validar que sea una Factura (tipo I)
            if (!"I".equals(facturaRelacionada.getDocumentType())) {
                log.error("El CFDI relacionado no es una Factura. Tipo: {}",
                        facturaRelacionada.getDocumentType());
                messageCatalog.throwException(FiscalMessageCode.BUS044,
                        "UUID: " + uuidRelacionado + ", Tipo: " + facturaRelacionada.getDocumentType());
            }

            // 2.4 Crear y guardar la relación
            RelatedCfdiEntity relacion = new RelatedCfdiEntity();
            relacion.setInvoiceUuid(ncInvoice.getInvoiceUuid());           // UUID de la NC
            relacion.setRelatedInvoiceUuid(facturaRelacionada.getInvoiceUuid()); // UUID de la Factura
            relacion.setRelationType(tipoRelacion);

            relatedCfdiRepository.save(relacion);
            log.info("Relación guardada exitosamente. NC: {} -> Factura: {}",
                    ncInvoice.getFiscalUuid(), facturaRelacionada.getFiscalUuid());
        }

        log.info("=== FIN GUARDADO CFDIS RELACIONADOS - {} relaciones guardadas ===", relacionados.size());
    }

    /**
     * Construye la respuesta exitosa de registro según el tipo y estado de addenda.
     */
    private InvoiceRegistrationResponse buildRegistrationSuccessResponse(
            InvoiceEntity invoice,
            UUID fiscalUuid,
            TipoDocumentoFiscal tipoDocumento,
            boolean hasValidAddenda,
            InvoiceXmlDto invoiceDto) {

        log.debug("Construyendo respuesta de registro exitoso");

        FiscalSuccessCode successCode;

        // Determinar código de éxito según tipo de documento y estado de addenda
        if (tipoDocumento == TipoDocumentoFiscal.FACTURA) {
            successCode = hasValidAddenda ? FiscalSuccessCode.RES004 : FiscalSuccessCode.RES005;
        } else { // NOTA_CREDITO
            successCode = hasValidAddenda ? FiscalSuccessCode.RES006 : FiscalSuccessCode.RES007;
        }

        // Obtener datos del emisor y receptor desde el DTO del XML
        String issuerRfc = invoiceDto.getEmisor() != null ? invoiceDto.getEmisor().getRfc() : null;
        String receiverRfc = invoiceDto.getReceptor() != null ? invoiceDto.getReceptor().getRfc() : null;

        // Convertir LocalDate a LocalDateTime (al inicio del día)
        LocalDateTime issueDateTime = invoice.getIssueDate() != null
                ? invoice.getIssueDate().atStartOfDay()
                : null;

        if (hasValidAddenda) {
            return InvoiceRegistrationResponse.success(
                    invoice.getInvoiceUuid(),
                    fiscalUuid,
                    invoice.getSeries(),
                    invoice.getFolio(),
                    invoice.getDocumentType(),
                    issuerRfc,
                    receiverRfc,
                    invoice.getTotal() != null ? invoice.getTotal().toString() : null,
                    issueDateTime,
                    successCode.getCode(),
                    successCode.getMessage()
            );
        } else {
            return InvoiceRegistrationResponse.successPendingAddenda(
                    invoice.getInvoiceUuid(),
                    fiscalUuid,
                    invoice.getSeries(),
                    invoice.getFolio(),
                    invoice.getDocumentType(),
                    issuerRfc,
                    receiverRfc,
                    invoice.getTotal() != null ? invoice.getTotal().toString() : null,
                    issueDateTime,
                    successCode.getCode(),
                    successCode.getMessage()
            );
        }
    }

    // ========== MÉTODOS PRIVADOS - ACTUALIZACIÓN ==========

    /**
     * Valida que el documento pertenezca al proveedor especificado (STM-339).
     *
     * La validación se realiza usando la relación: Invoice → Addendum → supplierNumber
     *
     * Regla de negocio (confirmada por Ivan):
     * - Si el documento NO tiene addenda, es un ERROR (no debería ocurrir)
     * - Si el supplierNumber de la addenda NO coincide con el request, es un ERROR
     *
     * @param invoice Entidad del documento fiscal
     * @param numeroProveedorRequest Número de proveedor enviado en el request
     * @throws FiscalException si la validación falla
     */
    private void validateSupplierOwnership(InvoiceEntity invoice, BigDecimal numeroProveedorRequest) {
        log.debug("Validando propiedad del proveedor. Invoice UUID: {}, Proveedor Request: {}",
                invoice.getInvoiceUuid(), numeroProveedorRequest);

        // Buscar addenda asociada al documento
        AddendumEntity addendum = addendumRepository.findByInvoiceUuid(invoice.getInvoiceUuid())
                .orElse(null);

        // Sin addenda o sin supplier_number: no se puede validar propiedad → no bloquear
        if (addendum == null || addendum.getSupplierNumber() == null) {
            log.warn("Documento sin addenda o sin supplier_number. UUID: {}. Se omite validación de propiedad.",
                    invoice.getFiscalUuid());
            return;
        }

        log.debug("Addenda encontrada. Addendum UUID: {}, Supplier Number en addenda: {}",
                addendum.getAddendumUuid(), addendum.getSupplierNumber());

        // Comparar el supplierNumber de la addenda con el del request
        if (addendum.getSupplierNumber().compareTo(numeroProveedorRequest) != 0) {
            log.error("El documento no pertenece al proveedor. Supplier en addenda: {}, Supplier en request: {}",
                    addendum.getSupplierNumber(), numeroProveedorRequest);
            messageCatalog.throwException(FiscalMessageCode.BUS047,
                    "UUID: " + invoice.getFiscalUuid() + ", Proveedor solicitado: " + numeroProveedorRequest);
        }

        log.info("Validacion de proveedor exitosa. Supplier Number: {}", addendum.getSupplierNumber());
    }

    /**
     * Valida la transición de estatus según el tipo de documento.
     */
    private void validateStatusTransition(Integer currentStatusCode, Integer newStatusCode, String documentType) {
        log.debug("Validando transicion de estatus: {} -> {} para tipo: {}", currentStatusCode, newStatusCode, documentType);

        // Validar según tipo de documento
        if ("I".equals(documentType)) {
            // Factura
            try {
                InvoiceStatus currentStatus = InvoiceStatus.fromCodigo(currentStatusCode);
                InvoiceStatus newStatus = InvoiceStatus.fromCodigo(newStatusCode);

                if (!currentStatus.puedeTransicionarA(newStatusCode)) {
                    messageCatalog.throwException(FiscalMessageCode.BUS051,
                            String.format("De: %d (%s) a: %d (%s)",
                                    currentStatusCode, currentStatus.getNombre(),
                                    newStatusCode, newStatus.getNombre()));
                }
            } catch (IllegalArgumentException e) {
                messageCatalog.throwException(FiscalMessageCode.BUS049,
                        "Estatus: " + newStatusCode + ", Tipo: Factura (I)");
            }

        } else if ("E".equals(documentType)) {
            // Nota de Crédito
            try {
                CreditNoteStatus currentStatus = CreditNoteStatus.fromCodigo(currentStatusCode);
                CreditNoteStatus newStatus = CreditNoteStatus.fromCodigo(newStatusCode);

                // STM-335: Validar transición usando enum (incluye cancelación)
                if (!currentStatus.puedeTransicionarA(newStatusCode)) {
                    if (newStatus == CreditNoteStatus.CANCELADA) {
                        messageCatalog.throwException(FiscalMessageCode.WRN7023);
                    } else {
                        messageCatalog.throwException(FiscalMessageCode.BUS051,
                                String.format("De: %d (%s) a: %d (%s)",
                                        currentStatusCode, currentStatus.getNombre(),
                                        newStatusCode, newStatus.getNombre()));
                    }
                }
            } catch (IllegalArgumentException e) {
                messageCatalog.throwException(FiscalMessageCode.BUS049,
                        "Estatus: " + newStatusCode + ", Tipo: Nota de Crédito (E)");
            }

        } else {
            // Tipo de documento no reconocido
            messageCatalog.throwException(FiscalMessageCode.BUS023);
        }

        log.debug("Transicion de estatus validada correctamente");
    }

    /**
     * Valida que un código de estatus exista en el enum correspondiente al tipo de documento.
     * Se usa para validar filtros de búsqueda y evitar retornar resultados vacíos silenciosamente.
     */
    private void validateStatusExists(Integer estatus, String documentType) {
        try {
            if ("I".equals(documentType)) {
                InvoiceStatus.fromCodigo(estatus);
            } else if ("E".equals(documentType)) {
                CreditNoteStatus.fromCodigo(estatus);
            }
        } catch (IllegalArgumentException e) {
            messageCatalog.throwException(FiscalMessageCode.BUS049,
                    "Estatus: " + estatus + ", Tipo: " + ("I".equals(documentType) ? "Factura (I)" : "Nota de Crédito (E)"));
        }
    }

    /**
     * Actualiza la addenda asociada al invoice (STM-339).
     *
     * Mapeo de campos según tipo de documento:
     *
     * Factura (I):
     * - idProveedor -> supplierNumber
     * - noRecepcion -> receptionNumber
     * - noOc -> purchaseOrderNumber
     * - idGuiaEntrega -> shippingGuideNumber
     * - tipoProveedor -> supplierType
     * - tipoAddenda -> addendaType
     *
     * Nota de Crédito (E):
     * - idProveedor -> supplierNumber
     * - tipoProveedor -> supplierType
     */
    private boolean updateAddenda(InvoiceEntity invoice, AddendaUpdateDto addendaDto) {
        log.debug("Actualizando addenda para invoice UUID: {}", invoice.getInvoiceUuid());

        try {
            // Buscar addenda existente
            AddendumEntity addendum = addendumRepository.findByInvoiceUuid(invoice.getInvoiceUuid())
                    .orElse(null);

            if (addendum == null) {
                log.error("Addenda no encontrada para invoice UUID: {}", invoice.getInvoiceUuid());
                messageCatalog.throwException(FiscalMessageCode.BUS048);
            }

            boolean hasChanges = false;

            // === CAMPOS COMUNES (Factura y NC) ===

            // idProveedor -> supplierNumber
            if (addendaDto.getIdProveedor() != null) {
                log.debug("Actualizando supplierNumber: {} -> {}", addendum.getSupplierNumber(), addendaDto.getIdProveedor());
                addendum.setSupplierNumber(addendaDto.getIdProveedor());
                hasChanges = true;
            }

            // tipoProveedor -> supplierType
            if (addendaDto.getTipoProveedor() != null) {
                log.debug("Actualizando supplierType: {} -> {}", addendum.getSupplierType(), addendaDto.getTipoProveedor());
                addendum.setSupplierType(addendaDto.getTipoProveedor());
                hasChanges = true;
            }

            // === CAMPOS ESPECÍFICOS FACTURA (I) ===
            if ("I".equals(invoice.getDocumentType())) {

                // noRecepcion -> receptionNumber
                if (addendaDto.getNoRecepcion() != null) {
                    log.debug("Actualizando receptionNumber: {} -> {}", addendum.getReceptionNumber(), addendaDto.getNoRecepcion());
                    addendum.setReceptionNumber(addendaDto.getNoRecepcion());
                    hasChanges = true;
                }

                // noOc -> purchaseOrderNumber
                if (addendaDto.getNoOc() != null) {
                    log.debug("Actualizando purchaseOrderNumber: {} -> {}", addendum.getPurchaseOrderNumber(), addendaDto.getNoOc());
                    addendum.setPurchaseOrderNumber(addendaDto.getNoOc());
                    hasChanges = true;
                }

                // idGuiaEntrega -> shippingGuideNumber
                if (addendaDto.getIdGuiaEntrega() != null) {
                    log.debug("Actualizando shippingGuideNumber: {} -> {}", addendum.getShippingGuideNumber(), addendaDto.getIdGuiaEntrega());
                    addendum.setShippingGuideNumber(addendaDto.getIdGuiaEntrega());
                    hasChanges = true;
                }

                // tipoAddenda -> addendaType
                if (addendaDto.getTipoAddenda() != null) {
                    log.debug("Actualizando addendaType: {} -> {}", addendum.getAddendaType(), addendaDto.getTipoAddenda());
                    addendum.setAddendaType(addendaDto.getTipoAddenda());
                    hasChanges = true;
                }
            }

            // === CAMPOS ESPECÍFICOS NC (E) ===
            // tipoNotaCredito - No existe campo equivalente en AddendumEntity
            // Se puede agregar en el addendumContent como JSON si es necesario

            if (hasChanges) {
                // Actualizar fecha y usuario de modificación
                addendum.setUpdateDate(LocalDateTime.now());
                addendum.setUserId(invoice.getUpdatedBy());

                addendumRepository.save(addendum);
                log.info("Addenda actualizada exitosamente. Campos modificados para invoice UUID: {}", invoice.getInvoiceUuid());
            } else {
                log.debug("No se detectaron cambios en la addenda");
            }

            return hasChanges;

        } catch (FiscalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error actualizando addenda", e);
            return false;
        }
    }

    /**
     * Construye la respuesta exitosa de actualización.
     */
    private InvoiceUpdateResponse buildUpdateSuccessResponse(
            InvoiceEntity invoice,
            Integer estatusAnterior,
            Integer estatusNuevo,
            String documentType,
            boolean addendaActualizada) {

        log.debug("Construyendo respuesta de actualizacion exitosa");

        FiscalSuccessCode successCode;
        String estatusNuevoNombre;

        // Determinar código de éxito según tipo de documento y si hubo actualización de addenda
        if ("I".equals(documentType)) {
            successCode = addendaActualizada ? FiscalSuccessCode.RES010 : FiscalSuccessCode.RES009;
            estatusNuevoNombre = InvoiceStatus.fromCodigo(estatusNuevo).getNombre();
        } else { // "E"
            successCode = addendaActualizada ? FiscalSuccessCode.RES012 : FiscalSuccessCode.RES011;
            estatusNuevoNombre = CreditNoteStatus.fromCodigo(estatusNuevo).getNombre();
        }

        return InvoiceUpdateResponse.success(
                successCode.getCode(),
                successCode.getMessage(),
                invoice.getInvoiceUuid(),
                invoice.getFiscalUuid(),
                documentType,
                estatusAnterior,
                estatusNuevo,
                estatusNuevoNombre,
                addendaActualizada
        );
    }

    // ========== BÚSQUEDA (STM-338) ==========

    @Override
    public Page<InvoiceSearchResponse> searchInvoices(InvoiceSearchRequest searchRequest, java.util.List<String> allowedVendors) {
        log.info("BUSQUEDA FACTURAS con filtro seguridad vendors={}", allowedVendors);
        Specification<InvoiceEntity> spec = InvoiceSpecification.buildSpecification(searchRequest, allowedVendors);
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(searchRequest.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "createdAt"
        );
        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getSize() != null ? searchRequest.getSize() : 20,
                sort
        );
        Page<InvoiceEntity> page = invoiceRepository.findAll(spec, pageable);
        return page.map(this::mapToSearchResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSearchResponse> searchInvoices(InvoiceSearchRequest searchRequest) {
        log.info("========================================");
        log.info("INICIO BUSQUEDA DE FACTURAS/NOTAS DE CREDITO");
        log.info("========================================");
        log.info("RFC Emisor: {}", searchRequest.getRfcEmisor());
        log.info("Fecha Inicio: {}", searchRequest.getFechaInicioRecepcion());
        log.info("Fecha Final: {}", searchRequest.getFechaFinalRecepcion());
        log.info("Tipo Documento: {}", searchRequest.getTipoDocumento());
        log.info("RFC Receptor: {}", searchRequest.getRfcReceptor());
        log.info("Serie: {}", searchRequest.getSerie());
        log.info("Folio: {}", searchRequest.getFolio());
        log.info("UUID: {}", searchRequest.getUuid());
        log.info("Estatus: {}", searchRequest.getEstatus());
        log.info("No. Orden Compra: {}", searchRequest.getNoOrdenCompra());
        log.info("No. Recepcion: {}", searchRequest.getNoRecepcion());

        // === PASO 0: VALIDAR RANGO DE FECHAS (STM-393) ===
        log.info("Paso 0: Validando rango de fechas");
        validateDateRange(searchRequest.getFechaInicioRecepcion(), searchRequest.getFechaFinalRecepcion());

        // === PASO 0.5: VALIDAR ESTATUS CONTRA ENUM LOCAL ===
        if (searchRequest.getEstatus() != null) {
            validateStatusExists(searchRequest.getEstatus(), searchRequest.getTipoDocumento());
        }

        // === PASO 1: CONSTRUIR SPECIFICATION CON FILTROS (JPA CRITERIA) ===
        log.info("Paso 1: Construyendo Specification con filtros usando JPA Criteria");
        Specification<InvoiceEntity> spec = InvoiceSpecification.buildSpecification(searchRequest);
        log.debug("Specification construida exitosamente");

        // === PASO 2: CONFIGURAR PAGINACION Y ORDENAMIENTO ===
        log.info("Paso 2: Configurando paginacion y ordenamiento");
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(searchRequest.getSortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "createdAt"
        );

        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getSize() != null ? searchRequest.getSize() : 20,
                sort
        );
        log.debug("Paginacion configurada: page={}, size={}, sort={} {}",
                pageable.getPageNumber(), pageable.getPageSize(),
                searchRequest.getSortBy(), searchRequest.getSortDirection());

        // === PASO 3: EJECUTAR BUSQUEDA CON SPECIFICATION ===
        log.info("Paso 3: Ejecutando busqueda con JPA Criteria API");
        Page<InvoiceEntity> invoicePage = invoiceRepository.findAll(spec, pageable);
        log.info("Busqueda completada. Resultados encontrados: {}, Total paginas: {}",
                invoicePage.getNumberOfElements(), invoicePage.getTotalPages());

        // === PASO 4: MAPEAR RESULTADOS A DTO (CON ADDENDA) ===
        log.info("Paso 4: Mapeando resultados a InvoiceSearchResponse (incluyendo Addenda)");
        Page<InvoiceSearchResponse> responsePage = invoicePage.map(this::mapToSearchResponse);

        log.info("========================================");
        log.info("BUSQUEDA COMPLETADA EXITOSAMENTE");
        log.info("========================================");
        log.info("Total elementos: {}", responsePage.getTotalElements());
        log.info("Elementos en pagina: {}", responsePage.getNumberOfElements());
        log.info("Total paginas: {}", responsePage.getTotalPages());
        log.info("Pagina actual: {}", responsePage.getNumber());

        return responsePage;
    }

    /**
     * Mapea InvoiceEntity a InvoiceSearchResponse incluyendo datos de Addenda y NC relacionadas.
     *
     * NOTA: Los joins con Issuer y Receiver ya se realizan en el Specification (InvoiceSpecification),
     * por lo que las entidades relacionadas ya están cargadas (no hay problema N+1).
     *
     * STM-1168: Si el documento es una Factura (tipo I), incluye las NC relacionadas.
     */
    private InvoiceSearchResponse mapToSearchResponse(InvoiceEntity invoice) {
        log.debug("Mapeando invoice UUID: {} a InvoiceSearchResponse", invoice.getInvoiceUuid());

        // Las relaciones Issuer y Receiver ya fueron cargadas por el JOIN en Specification
        IssuerEntity issuer = invoice.getIssuer();
        ReceiverEntity receiver = invoice.getReceiver();

        // Buscar addenda asociada (consulta adicional necesaria)
        AddendumEntity addendum = addendumRepository.findByInvoiceUuid(invoice.getInvoiceUuid())
                .orElse(null);

        // STM-1168: Buscar NC relacionadas (solo para Facturas tipo I)
        List<NotaCreditoRelacionadaDto> notasCreditoRelacionadas = null;
        if (TipoDocumentoFiscal.FACTURA.getCodigo().equals(invoice.getDocumentType())) {
            notasCreditoRelacionadas = findNotasCreditoRelacionadas(invoice.getInvoiceUuid());
        }

        // STM-396: Buscar factura relacionada (solo para NC tipo E)
        String relationType = null;
        String relationTypeName = null;
        String relatedInvoiceSeries = null;
        String relatedInvoiceFolio = null;
        UUID relatedInvoiceUuid = null;
        BigDecimal relatedInvoiceSubtotal = null;
        BigDecimal relatedInvoiceTotal = null;

        if (TipoDocumentoFiscal.NOTA_CREDITO.getCodigo().equals(invoice.getDocumentType())) {
            // Buscar la relacion de la NC con la factura
            List<RelatedCfdiEntity> ncRelations = relatedCfdiRepository.findByInvoiceUuid(invoice.getInvoiceUuid());
            if (!ncRelations.isEmpty()) {
                RelatedCfdiEntity ncRelation = ncRelations.get(0); // Tomamos la primera relacion
                relationType = ncRelation.getRelationType();
                relationTypeName = satCatalogService.getTipoRelacionDescription(relationType);

                // Obtener datos de la factura relacionada
                Optional<InvoiceEntity> relatedInvoiceOpt = invoiceRepository.findById(ncRelation.getRelatedInvoiceUuid());
                if (relatedInvoiceOpt.isPresent()) {
                    InvoiceEntity relatedInvoice = relatedInvoiceOpt.get();
                    relatedInvoiceSeries = relatedInvoice.getSeries();
                    relatedInvoiceFolio = relatedInvoice.getFolio();
                    relatedInvoiceUuid = relatedInvoice.getFiscalUuid();
                    relatedInvoiceSubtotal = relatedInvoice.getSubtotal();
                    relatedInvoiceTotal = relatedInvoice.getTotal();
                }
            }
        }

        // Construir response con Comprobante, Emisor, Receptor, Addenda, NC Relacionadas
        return InvoiceSearchResponse.builder()
                // ========== COMPROBANTE ==========
                .invoiceUuid(invoice.getInvoiceUuid())
                .fiscalUuid(invoice.getFiscalUuid())
                .documentType(invoice.getDocumentType())
                .series(invoice.getSeries())
                .folio(invoice.getFolio())
                .version(invoice.getVersion())
                .issueDate(invoice.getIssueDate())
                .certificationDate(invoice.getCertificationDate())
                .total(invoice.getTotal())
                .subtotal(invoice.getSubtotal())
                .currency(invoice.getCurrency())
                .paymentMethod(invoice.getPaymentMethod())
                .status(invoice.getStatus())
                .statusName(InvoiceSearchResponse.getStatusName(invoice.getDocumentType(), invoice.getStatus()))
                // ========== EMISOR ==========
                .emisorRfc(issuer != null ? issuer.getRfc() : null)
                .emisorName(issuer != null ? issuer.getName() : null)
                .emisorTaxRegime(issuer != null ? issuer.getTaxRegime() : null)
                // ========== RECEPTOR ==========
                .receptorRfc(receiver != null ? receiver.getRfc() : null)
                .receptorName(receiver != null ? receiver.getName() : null)
                .receptorTaxRegime(receiver != null ? receiver.getTaxRegime() : null)
                // ========== ADDENDA ==========
                .hasAddenda(addendum != null)
                .addendaUuid(addendum != null ? addendum.getAddendumUuid() : null)
                .addendaType(addendum != null ? addendum.getAddendaType() : null)
                .addendaTypeName(addendum != null ? satCatalogService.getTipoAddendaDescription(addendum.getAddendaType()) : null)
                // ========== DATOS DE NEGOCIO ADDENDA (STM-1169) ==========
                .noOrdenCompra(addendum != null ? addendum.getPurchaseOrderNumber() : null)
                .noRecepcion(addendum != null ? addendum.getReceptionNumber() : null)
                .numeroProveedor(addendum != null ? addendum.getSupplierNumber() : null)
                .tipoProveedor(addendum != null ? addendum.getSupplierType() : null)
                .guiaEntrega(addendum != null ? addendum.getShippingGuideNumber() : null)
                // ========== XML CONTENT (STM-771) ==========
                .xmlContent(invoice.getXmlContent())
                // ========== NOTAS DE CRÉDITO RELACIONADAS (STM-1168) ==========
                .notasCreditoRelacionadas(notasCreditoRelacionadas)
                .creditNotesCount(notasCreditoRelacionadas != null ? notasCreditoRelacionadas.size() : 0)
                // ========== DATOS DE FACTURA RELACIONADA (STM-396) - Solo para NC ==========
                .relationType(relationType)
                .relationTypeName(relationTypeName)
                .relatedInvoiceSeries(relatedInvoiceSeries)
                .relatedInvoiceFolio(relatedInvoiceFolio)
                .relatedInvoiceUuid(relatedInvoiceUuid)
                .relatedInvoiceSubtotal(relatedInvoiceSubtotal)
                .relatedInvoiceTotal(relatedInvoiceTotal)
                // ========== DATOS ADICIONALES PROVEEDOR (STM-396) ==========
                .supplierName(issuer != null ? issuer.getName() : null)
                .accountingSentDate(invoice.getUpdatedAt()) // TODO: Cambiar por invoice.getAccountingSentDate() cuando se implemente el flujo de envío a contabilizar
                .build();
    }

    /**
     * Busca las Notas de Crédito relacionadas a una Factura (STM-1168).
     *
     * @param facturaInvoiceUuid UUID interno de la Factura
     * @return Lista de NC relacionadas, o lista vacía si no hay ninguna
     */
    private List<NotaCreditoRelacionadaDto> findNotasCreditoRelacionadas(UUID facturaInvoiceUuid) {
        log.debug("Buscando NC relacionadas para Factura UUID: {}", facturaInvoiceUuid);

        // Buscar relaciones donde esta factura es el documento relacionado
        List<RelatedCfdiEntity> relaciones = relatedCfdiRepository.findByRelatedInvoiceUuid(facturaInvoiceUuid);

        if (relaciones.isEmpty()) {
            log.debug("No se encontraron NC relacionadas");
            return new ArrayList<>();
        }

        log.debug("Se encontraron {} NC relacionadas", relaciones.size());

        List<NotaCreditoRelacionadaDto> resultado = new ArrayList<>();

        for (RelatedCfdiEntity relacion : relaciones) {
            // Buscar los datos de la NC
            Optional<InvoiceEntity> ncOpt = invoiceRepository.findById(relacion.getInvoiceUuid());

            if (ncOpt.isPresent()) {
                InvoiceEntity nc = ncOpt.get();

                NotaCreditoRelacionadaDto ncDto = NotaCreditoRelacionadaDto.builder()
                        .invoiceUuid(nc.getInvoiceUuid())
                        .fiscalUuid(nc.getFiscalUuid())
                        .serie(nc.getSeries())
                        .folio(nc.getFolio())
                        .subtotal(nc.getSubtotal())
                        .total(nc.getTotal())
                        .tipoRelacion(relacion.getRelationType())
                        .tipoRelacionNombre(satCatalogService.getTipoRelacionDescription(relacion.getRelationType()))
                        .status(nc.getStatus())
                        .statusNombre(InvoiceSearchResponse.getStatusName(TipoDocumentoFiscal.NOTA_CREDITO.getCodigo(), nc.getStatus()))
                        .fechaEmision(nc.getIssueDate())
                        .fechaRecepcion(nc.getCreatedAt())
                        .accountingSentDate(nc.getUpdatedAt()) // TODO: Cambiar por nc.getAccountingSentDate() cuando se implemente el flujo
                        .build();

                resultado.add(ncDto);
                log.debug("NC agregada: {} - Serie: {} Folio: {}", nc.getFiscalUuid(), nc.getSeries(), nc.getFolio());
            }
        }

        return resultado;
    }

    // ========== MÉTODOS PRIVADOS - VALIDACIÓN DE FECHAS (STM-393) ==========

    /**
     * Valida el rango de fechas para la búsqueda de documentos.
     *
     * Validaciones:
     * 1. Fecha inicio no puede ser mayor que fecha fin (WRN7005)
     * 2. Rango máximo de meses permitido según parámetro MAX_SEARCH_MONTHS (WRN7000)
     *
     * @param fechaInicio Fecha de inicio de búsqueda
     * @param fechaFin Fecha final de búsqueda
     * @throws FiscalException si las validaciones fallan
     */
    private void validateDateRange(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Validando rango de fechas: {} - {}", fechaInicio, fechaFin);

        // Validación 1: Fecha inicio no puede ser mayor que fecha fin
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            log.error("Fecha inicio ({}) es mayor que fecha fin ({})", fechaInicio, fechaFin);
            messageCatalog.throwException(FiscalMessageCode.WRN7005);
        }

        // Validación 2: Rango máximo de meses
        if (fechaInicio != null && fechaFin != null) {
            // Obtener parámetro MAX_SEARCH_MONTHS desde utils-api (default: 6 meses)
            int maxMonths = utilsApiService.getParameterValueAsInt("MAX_SEARCH_MONTHS", 6);
            log.debug("Parámetro MAX_SEARCH_MONTHS: {} meses", maxMonths);

            // Calcular diferencia en meses
            long monthsBetween = ChronoUnit.MONTHS.between(fechaInicio, fechaFin);
            log.debug("Diferencia en meses entre fechas: {}", monthsBetween);

            if (monthsBetween > maxMonths) {
                log.error("Rango de fechas ({} meses) excede el máximo permitido ({} meses)",
                        monthsBetween, maxMonths);
                messageCatalog.throwExceptionWithParams(FiscalMessageCode.WRN7000, maxMonths);
            }
        }

        log.info("Validación de rango de fechas completada exitosamente");
    }

    // ========== MÉTODOS PRIVADOS - BITÁCORA (STM-339) ==========

    /**
     * Registra una actividad en la bitácora (tabla log).
     *
     * @param operationType   Tipo de operación (UPDATE, CREATE, DELETE, etc.)
     * @param cfdiUuid        UUID del CFDI afectado
     * @param startTime       Hora de inicio de la operación
     * @param statusCode      Código de resultado
     * @param statusMessage   Mensaje de resultado
     * @param requestData     Datos del request en formato JSON
     * @param responseData    Datos del response en formato JSON
     * @param userId          ID del usuario que realizó la operación
     */
    private void saveActivityLog(
            String operationType,
            UUID cfdiUuid,
            LocalDateTime startTime,
            String statusCode,
            String statusMessage,
            String requestData,
            String responseData,
            Long userId) {

        try {
            LogEntity logEntry = new LogEntity();
            logEntry.setOperationType(operationType);
            logEntry.setCfdiUuid(cfdiUuid);
            logEntry.setTransactionDate(LocalDateTime.now());
            logEntry.setRecordStartDate(startTime);
            logEntry.setRecordEndDate(LocalDateTime.now());
            logEntry.setStatusCode(statusCode);
            logEntry.setStatusMessage(truncateMessage(statusMessage, 500));
            logEntry.setRequestData(requestData);
            logEntry.setResponseData(responseData);
            logEntry.setCreatedBy(userId);

            logRepository.save(logEntry);
            log.debug("Actividad registrada en bitacora. Operation: {}, CFDI UUID: {}, Status: {}",
                    operationType, cfdiUuid, statusCode);

        } catch (Exception e) {
            // No lanzar excepción si falla el registro de bitácora
            // La operación principal ya fue exitosa
            log.error("Error registrando actividad en bitacora: {}", e.getMessage());
        }
    }

    /**
     * Construye el JSON del request para almacenar en la bitácora.
     * No incluye información sensible.
     */
    private String buildRequestDataJson(InvoiceUpdateRequest request) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"uuid\":\"").append(request.getUuid()).append("\",");
            json.append("\"numeroProveedor\":").append(request.getNumeroProveedor()).append(",");
            json.append("\"estatus\":").append(request.getEstatus()).append(",");
            json.append("\"idUsuarioActualizacion\":").append(request.getIdUsuarioActualizacion());

            if (request.getAddenda() != null) {
                json.append(",\"addenda\":{");
                AddendaUpdateDto addenda = request.getAddenda();
                boolean first = true;

                if (addenda.getIdProveedor() != null) {
                    json.append("\"idProveedor\":").append(addenda.getIdProveedor());
                    first = false;
                }
                if (addenda.getNoRecepcion() != null) {
                    if (!first) json.append(",");
                    json.append("\"noRecepcion\":\"").append(addenda.getNoRecepcion()).append("\"");
                    first = false;
                }
                if (addenda.getNoOc() != null) {
                    if (!first) json.append(",");
                    json.append("\"noOc\":\"").append(addenda.getNoOc()).append("\"");
                    first = false;
                }
                if (addenda.getTipoProveedor() != null) {
                    if (!first) json.append(",");
                    json.append("\"tipoProveedor\":\"").append(addenda.getTipoProveedor()).append("\"");
                    first = false;
                }
                if (addenda.getTipoAddenda() != null) {
                    if (!first) json.append(",");
                    json.append("\"tipoAddenda\":").append(addenda.getTipoAddenda());
                }
                json.append("}");
            }

            json.append("}");
            return json.toString();

        } catch (Exception e) {
            log.warn("Error construyendo JSON de request para bitacora: {}", e.getMessage());
            return "{\"error\":\"No se pudo serializar el request\"}";
        }
    }

    /**
     * Construye el JSON del response para almacenar en la bitácora.
     */
    private String buildResponseDataJson(InvoiceUpdateResponse response) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"code\":\"").append(response.getCode()).append("\",");
            json.append("\"message\":\"").append(escapeJson(response.getMessage())).append("\",");
            json.append("\"success\":").append(response.isSuccess());

            if (response.getInvoiceUuid() != null) {
                json.append(",\"invoiceUuid\":\"").append(response.getInvoiceUuid()).append("\"");
            }
            if (response.getFiscalUuid() != null) {
                json.append(",\"fiscalUuid\":\"").append(response.getFiscalUuid()).append("\"");
            }
            if (response.getDocumentType() != null) {
                json.append(",\"documentType\":\"").append(response.getDocumentType()).append("\"");
            }
            if (response.getEstatusAnterior() != null) {
                json.append(",\"estatusAnterior\":").append(response.getEstatusAnterior());
            }
            if (response.getEstatusNuevo() != null) {
                json.append(",\"estatusNuevo\":").append(response.getEstatusNuevo());
            }
            json.append(",\"addendaActualizada\":").append(response.isAddendaActualizada());

            json.append("}");
            return json.toString();

        } catch (Exception e) {
            log.warn("Error construyendo JSON de response para bitacora: {}", e.getMessage());
            return "{\"error\":\"No se pudo serializar el response\"}";
        }
    }

    /**
     * Escapa caracteres especiales para JSON.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Trunca un mensaje a la longitud máxima especificada.
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) return null;
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength - 3) + "...";
    }

    // ========== DESCARGA MASIVA (STM-396) ==========

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadXmlZip(BulkDownloadRequest request) {
        log.info("Iniciando descarga masiva XML. Documentos solicitados: {}", request.getInvoiceUuids().size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            int filesAdded = 0;
            for (UUID invoiceUuid : request.getInvoiceUuids()) {
                Optional<InvoiceEntity> invoiceOpt = invoiceRepository.findById(invoiceUuid);

                if (invoiceOpt.isPresent()) {
                    InvoiceEntity invoice = invoiceOpt.get();
                    String xmlContent = invoice.getXmlContent();

                    if (xmlContent != null && !xmlContent.isEmpty()) {
                        // Nombre del archivo: Serie-Folio_UUID.xml o UUID.xml si no hay serie/folio
                        String fileName = buildXmlFileName(invoice);

                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(xmlContent.getBytes(StandardCharsets.UTF_8));
                        zipOut.closeEntry();
                        filesAdded++;

                        log.debug("XML agregado al ZIP: {}", fileName);
                    } else {
                        log.warn("Documento sin contenido XML: {}", invoiceUuid);
                    }
                } else {
                    log.warn("Documento no encontrado: {}", invoiceUuid);
                }
            }

            zipOut.finish();
            log.info("Descarga masiva XML completada. Archivos en ZIP: {}", filesAdded);

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando ZIP de XMLs: {}", e.getMessage(), e);
            throw new FiscalException(FiscalMessageCode.ERR036, "Error generando archivo ZIP de XMLs");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPdfZip(BulkDownloadRequest request) {
        log.info("Iniciando descarga masiva PDF. Documentos solicitados: {}", request.getInvoiceUuids().size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            int filesAdded = 0;
            int filesSkipped = 0;

            for (UUID invoiceUuid : request.getInvoiceUuids()) {
                Optional<InvoiceEntity> invoiceOpt = invoiceRepository.findById(invoiceUuid);

                if (invoiceOpt.isPresent()) {
                    InvoiceEntity invoice = invoiceOpt.get();
                    String xmlContent = invoice.getXmlContent();

                    if (xmlContent != null && !xmlContent.isEmpty()) {
                        try {
                            // Generar PDF real usando PdfRenderService
                            byte[] pdfBytes = pdfRenderService.renderFromXml(xmlContent);
                            String fileName = buildPdfFileName(invoice);

                            ZipEntry zipEntry = new ZipEntry(fileName);
                            zipOut.putNextEntry(zipEntry);
                            zipOut.write(pdfBytes);
                            zipOut.closeEntry();
                            filesAdded++;

                            log.debug("PDF generado y agregado al ZIP: {}", fileName);
                        } catch (Exception e) {
                            log.warn("Error generando PDF para documento {}: {}", invoiceUuid, e.getMessage());
                            filesSkipped++;
                        }
                    } else {
                        log.warn("Documento sin contenido XML, no se puede generar PDF: {}", invoiceUuid);
                        filesSkipped++;
                    }
                } else {
                    log.warn("Documento no encontrado: {}", invoiceUuid);
                    filesSkipped++;
                }
            }

            zipOut.finish();
            log.info("Descarga masiva PDF completada. Archivos en ZIP: {}, Omitidos: {}", filesAdded, filesSkipped);

            return baos.toByteArray();

        } catch (FiscalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generando ZIP de PDFs: {}", e.getMessage(), e);
            throw new FiscalException(FiscalMessageCode.ERR036, "Error generando archivo ZIP de PDFs");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(InvoiceSearchRequest searchRequest) {
        log.info("Iniciando exportacion CSV. Filtros - RFC Emisor: {}, Tipo: {}",
                searchRequest.getRfcEmisor(), searchRequest.getTipoDocumento());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // Escribir BOM para Excel
            baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // Escribir encabezados CSV
            writer.println(buildCsvHeader());

            // Obtener todos los resultados (sin paginacion para exportacion)
            searchRequest.setPage(0);
            searchRequest.setSize(10000); // Limite maximo para exportacion

            Page<InvoiceSearchResponse> results = searchInvoices(searchRequest);

            // Escribir filas de datos
            for (InvoiceSearchResponse invoice : results.getContent()) {
                writer.println(buildCsvRow(invoice));
            }

            writer.flush();
            log.info("Exportacion CSV completada. Registros exportados: {}", results.getTotalElements());

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando CSV: {}", e.getMessage(), e);
            throw new FiscalException(FiscalMessageCode.ERR036, "Error generando archivo CSV");
        }
    }

    // ========== METODOS AUXILIARES DESCARGA MASIVA ==========

    /**
     * Construye el nombre del archivo XML para el ZIP.
     */
    private String buildXmlFileName(InvoiceEntity invoice) {
        StringBuilder fileName = new StringBuilder();

        if (invoice.getSeries() != null && !invoice.getSeries().isEmpty()) {
            fileName.append(invoice.getSeries()).append("-");
        }
        if (invoice.getFolio() != null && !invoice.getFolio().isEmpty()) {
            fileName.append(invoice.getFolio()).append("_");
        }
        fileName.append(invoice.getFiscalUuid() != null ? invoice.getFiscalUuid() : invoice.getInvoiceUuid());
        fileName.append(".xml");

        return sanitizeFileName(fileName.toString());
    }

    /**
     * Construye el nombre del archivo PDF para el ZIP.
     */
    private String buildPdfFileName(InvoiceEntity invoice) {
        StringBuilder fileName = new StringBuilder();

        if (invoice.getSeries() != null && !invoice.getSeries().isEmpty()) {
            fileName.append(invoice.getSeries()).append("-");
        }
        if (invoice.getFolio() != null && !invoice.getFolio().isEmpty()) {
            fileName.append(invoice.getFolio()).append("_");
        }
        fileName.append(invoice.getFiscalUuid() != null ? invoice.getFiscalUuid() : invoice.getInvoiceUuid());
        fileName.append(".pdf");

        return sanitizeFileName(fileName.toString());
    }

    /**
     * Sanitiza el nombre de archivo removiendo caracteres no validos.
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Construye el encabezado del CSV.
     */
    private String buildCsvHeader() {
        return String.join(",",
                "UUID Fiscal",
                "Tipo Documento",
                "Serie",
                "Folio",
                "Fecha Emision",
                "RFC Emisor",
                "Nombre Emisor",
                "RFC Receptor",
                "Nombre Receptor",
                "Subtotal",
                "Total",
                "Moneda",
                "Metodo Pago",
                "Estatus",
                "Nombre Estatus",
                "No Orden Compra",
                "No Recepcion",
                "Numero Proveedor"
        );
    }

    /**
     * Construye una fila de datos CSV.
     */
    private String buildCsvRow(InvoiceSearchResponse invoice) {
        return String.join(",",
                escapeCsvField(invoice.getFiscalUuid() != null ? invoice.getFiscalUuid().toString() : ""),
                escapeCsvField(invoice.getDocumentType()),
                escapeCsvField(invoice.getSeries()),
                escapeCsvField(invoice.getFolio()),
                escapeCsvField(invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : ""),
                escapeCsvField(invoice.getEmisorRfc()),
                escapeCsvField(invoice.getEmisorName()),
                escapeCsvField(invoice.getReceptorRfc()),
                escapeCsvField(invoice.getReceptorName()),
                escapeCsvField(invoice.getSubtotal() != null ? invoice.getSubtotal().toString() : ""),
                escapeCsvField(invoice.getTotal() != null ? invoice.getTotal().toString() : ""),
                escapeCsvField(invoice.getCurrency()),
                escapeCsvField(invoice.getPaymentMethod()),
                escapeCsvField(invoice.getStatus() != null ? invoice.getStatus().toString() : ""),
                escapeCsvField(invoice.getStatusName()),
                escapeCsvField(invoice.getNoOrdenCompra()),
                escapeCsvField(invoice.getNoRecepcion()),
                escapeCsvField(invoice.getNumeroProveedor() != null ? invoice.getNumeroProveedor().toString() : "")
        );
    }

    /**
     * Escapa un campo para formato CSV (maneja comas, comillas y saltos de linea).
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Si contiene coma, comilla o salto de linea, envolver en comillas y escapar comillas internas
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // ========== EXPORTACION XLSX ==========

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToXlsx(InvoiceSearchRequest request, String lang) {
        log.info("Iniciando exportacion XLSX. Filtros - RFC Emisor: {}, Tipo: {}",
                request.getRfcEmisor(), request.getTipoDocumento());

        // Obtener todos los resultados sin paginacion
        request.setPage(0);
        request.setSize(10000); // Limite maximo para exportacion

        Page<InvoiceSearchResponse> invoices = searchInvoices(request);

        if (invoices.isEmpty()) {
            log.warn("Sin resultados para exportar a XLSX");
            throw new FiscalException(FiscalMessageCode.ERR036, "Sin resultados para exportar");
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Crear estilos
            CellStyle headerStyle = createXlsxHeaderStyle(workbook);
            CellStyle dateStyle = createXlsxDateStyle(workbook);
            CellStyle dateTimeStyle = createXlsxDateTimeStyle(workbook);

            // Hoja 1: Facturas
            Sheet facturasSheet = workbook.createSheet("Facturas");
            createFacturasSheet(facturasSheet, invoices.getContent(), headerStyle, dateStyle, dateTimeStyle);

            // Hoja 2: Notas de Credito
            Sheet ncSheet = workbook.createSheet("Notas de Credito");
            List<NotaCreditoXlsxDto> notasCredito = extractNotasCreditoForXlsx(invoices.getContent());
            createNotasCreditoSheet(ncSheet, notasCredito, headerStyle, dateStyle, dateTimeStyle);

            // Auto-size columnas
            autoSizeColumns(facturasSheet, 13);
            autoSizeColumns(ncSheet, 12);

            workbook.write(outputStream);
            log.info("Exportacion XLSX completada. Facturas: {}, NC: {}",
                    invoices.getTotalElements(), notasCredito.size());

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generando XLSX: {}", e.getMessage(), e);
            throw new FiscalException(FiscalMessageCode.ERR036, "Error generando archivo XLSX");
        }
    }

    private void createFacturasSheet(Sheet sheet, List<InvoiceSearchResponse> facturas,
            CellStyle headerStyle, CellStyle dateStyle, CellStyle dateTimeStyle) {

        // Headers
        String[] headers = {
            "Serie", "Folio", "Subtotal", "Total", "Orden de Compra", "Recepcion",
            "UUID", "# NC Relacionadas", "ID Proveedor", "Nombre Proveedor",
            "Fecha Emision", "Fecha Recepcion", "Fecha Envio Contabilizar"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (InvoiceSearchResponse factura : facturas) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(nullSafeString(factura.getSeries()));
            row.createCell(1).setCellValue(nullSafeString(factura.getFolio()));
            row.createCell(2).setCellValue(factura.getSubtotal() != null ?
                factura.getSubtotal().doubleValue() : 0);
            row.createCell(3).setCellValue(factura.getTotal() != null ?
                factura.getTotal().doubleValue() : 0);
            row.createCell(4).setCellValue(nullSafeString(factura.getNoOrdenCompra()));
            row.createCell(5).setCellValue(nullSafeString(factura.getNoRecepcion()));
            row.createCell(6).setCellValue(factura.getFiscalUuid() != null ?
                factura.getFiscalUuid().toString() : "");
            row.createCell(7).setCellValue(factura.getNotasCreditoRelacionadas() != null ?
                factura.getNotasCreditoRelacionadas().size() : 0);
            row.createCell(8).setCellValue(factura.getNumeroProveedor() != null ?
                factura.getNumeroProveedor().toString() : "");
            row.createCell(9).setCellValue(nullSafeString(factura.getEmisorName()));

            // Fechas
            Cell cellFechaEmision = row.createCell(10);
            if (factura.getIssueDate() != null) {
                cellFechaEmision.setCellValue(Date.from(
                    factura.getIssueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                cellFechaEmision.setCellStyle(dateStyle);
            }

            Cell cellFechaRecepcion = row.createCell(11);
            if (factura.getCertificationDate() != null) {
                cellFechaRecepcion.setCellValue(Date.from(
                    factura.getCertificationDate().atZone(ZoneId.systemDefault()).toInstant()));
                cellFechaRecepcion.setCellStyle(dateTimeStyle);
            }

            Cell cellFechaEnvio = row.createCell(12);
            if (factura.getAccountingSentDate() != null) {
                cellFechaEnvio.setCellValue(Date.from(
                    factura.getAccountingSentDate().atZone(ZoneId.systemDefault()).toInstant()));
                cellFechaEnvio.setCellStyle(dateTimeStyle);
            }
        }
    }

    private void createNotasCreditoSheet(Sheet sheet, List<NotaCreditoXlsxDto> notasCredito,
            CellStyle headerStyle, CellStyle dateStyle, CellStyle dateTimeStyle) {

        // Headers
        String[] headers = {
            "Serie", "Folio", "Subtotal", "Total", "Motivo", "UUID",
            "Fecha Emision", "Fecha Recepcion", "Fecha Envio Contabilizar",
            "Serie Factura", "Folio Factura", "UUID Factura"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (NotaCreditoXlsxDto nc : notasCredito) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(nullSafeString(nc.getSerie()));
            row.createCell(1).setCellValue(nullSafeString(nc.getFolio()));
            row.createCell(2).setCellValue(nc.getSubtotal() != null ?
                nc.getSubtotal().doubleValue() : 0);
            row.createCell(3).setCellValue(nc.getTotal() != null ?
                nc.getTotal().doubleValue() : 0);
            row.createCell(4).setCellValue(nullSafeString(nc.getTipoRelacionNombre()));
            row.createCell(5).setCellValue(nullSafeString(nc.getFiscalUuid()));

            // Fechas NC
            Cell cellFechaEmision = row.createCell(6);
            if (nc.getFechaEmision() != null) {
                cellFechaEmision.setCellValue(Date.from(
                    nc.getFechaEmision().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                cellFechaEmision.setCellStyle(dateStyle);
            }

            Cell cellFechaRecepcion = row.createCell(7);
            if (nc.getFechaRecepcion() != null) {
                cellFechaRecepcion.setCellValue(Date.from(
                    nc.getFechaRecepcion().atZone(ZoneId.systemDefault()).toInstant()));
                cellFechaRecepcion.setCellStyle(dateTimeStyle);
            }

            Cell cellFechaEnvio = row.createCell(8);
            if (nc.getAccountingSentDate() != null) {
                cellFechaEnvio.setCellValue(Date.from(
                    nc.getAccountingSentDate().atZone(ZoneId.systemDefault()).toInstant()));
                cellFechaEnvio.setCellStyle(dateTimeStyle);
            }

            // Datos Factura relacionada
            row.createCell(9).setCellValue(nullSafeString(nc.getFacturaSerie()));
            row.createCell(10).setCellValue(nullSafeString(nc.getFacturaFolio()));
            row.createCell(11).setCellValue(nullSafeString(nc.getFacturaUuid()));
        }
    }

    private List<NotaCreditoXlsxDto> extractNotasCreditoForXlsx(List<InvoiceSearchResponse> facturas) {
        List<NotaCreditoXlsxDto> result = new ArrayList<>();

        for (InvoiceSearchResponse factura : facturas) {
            if (factura.getNotasCreditoRelacionadas() != null) {
                for (NotaCreditoRelacionadaDto nc : factura.getNotasCreditoRelacionadas()) {
                    NotaCreditoXlsxDto dto = NotaCreditoXlsxDto.builder()
                        .serie(nc.getSerie())
                        .folio(nc.getFolio())
                        .subtotal(nc.getSubtotal())
                        .total(nc.getTotal())
                        .tipoRelacionNombre(nc.getTipoRelacionNombre())
                        .fiscalUuid(nc.getFiscalUuid() != null ? nc.getFiscalUuid().toString() : null)
                        .fechaEmision(nc.getFechaEmision())
                        .fechaRecepcion(nc.getFechaRecepcion())
                        .accountingSentDate(nc.getAccountingSentDate())
                        .facturaSerie(factura.getSeries())
                        .facturaFolio(factura.getFolio())
                        .facturaUuid(factura.getFiscalUuid() != null ? factura.getFiscalUuid().toString() : null)
                        .build();
                    result.add(dto);
                }
            }
        }

        return result;
    }

    private CellStyle createXlsxHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createXlsxDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createXlsxDateTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int numColumns) {
        for (int i = 0; i < numColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String nullSafeString(String value) {
        return value != null ? value : "";
    }

    // ========== DESCARGA INDIVIDUAL XML ==========

    @Override
    @Transactional(readOnly = true)
    public String getXmlByFiscalUuid(String fiscalUuid) {
        log.info("Buscando XML por UUID fiscal: {}", fiscalUuid);

        UUID uuid;
        try {
            uuid = UUID.fromString(fiscalUuid);
        } catch (IllegalArgumentException e) {
            log.error("UUID invalido: {}", fiscalUuid);
            throw new FiscalException(FiscalMessageCode.ERR001, "UUID invalido: " + fiscalUuid);
        }

        // Buscar primero en facturas/NC
        Optional<InvoiceEntity> invoice = invoiceRepository.findByFiscalUuid(uuid);
        if (invoice.isPresent()) {
            String xmlContent = invoice.get().getXmlContent();
            if (xmlContent != null && !xmlContent.isEmpty()) {
                log.info("XML encontrado en facturas. UUID: {}, Tipo: {}",
                        fiscalUuid, invoice.get().getDocumentType());
                return xmlContent;
            }
            log.warn("Factura encontrada pero sin contenido XML. UUID: {}", fiscalUuid);
        }

        // Buscar en complementos de pago
        Optional<PaymentsEntity> payment = paymentsRepository.findByFiscalUuid(uuid);
        if (payment.isPresent()) {
            String xmlContent = payment.get().getXmlContent();
            if (xmlContent != null && !xmlContent.isEmpty()) {
                log.info("XML encontrado en complementos de pago. UUID: {}", fiscalUuid);
                return xmlContent;
            }
            log.warn("Complemento de pago encontrado pero sin contenido XML. UUID: {}", fiscalUuid);
        }

        log.error("Documento no encontrado. UUID: {}", fiscalUuid);
        throw new FiscalException(FiscalMessageCode.ERR001, "Documento no encontrado con UUID: " + fiscalUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getPdfByInvoiceUuid(String invoiceUuid) {
        log.info("Buscando PDF por invoice UUID: {}", invoiceUuid);

        UUID uuid;
        try {
            uuid = UUID.fromString(invoiceUuid);
        } catch (IllegalArgumentException e) {
            throw new FiscalException(FiscalMessageCode.ERR001, "UUID inválido: " + invoiceUuid);
        }

        InvoiceEntity invoice = invoiceRepository.findById(uuid)
                .orElseThrow(() -> new FiscalException(FiscalMessageCode.ERR001, "Factura no encontrada: " + invoiceUuid));

        if (invoice.getPdfGcsObject() == null || invoice.getPdfGcsObject().isBlank()) {
            throw new FiscalException(FiscalMessageCode.ERR001, "No hay PDF disponible para la factura: " + invoiceUuid);
        }

        try {
            return gcsStorageService.downloadPdf(invoice.getPdfGcsObject());
        } catch (Exception e) {
            log.error("Error al descargar PDF de GCS. object={} error={}", invoice.getPdfGcsObject(), e.getMessage());
            throw new FiscalException(FiscalMessageCode.ERR001, "Error al obtener el PDF: " + e.getMessage());
        }
    }

    // ========== STM-410: BÚSQUEDA Y ACTUALIZACIÓN POR ESTATUS ==========

    /**
     * Constantes para option_id del tren de estatus.
     */
    private static final int OPTION_FACTURA = 1;
    private static final int OPTION_NOTA_CREDITO = 2;
    private static final int OPTION_PAGOS = 3;
    private static final int OPTION_CARTA_PORTE = 4;

    /**
     * Estatus que requiere fecha de contabilización.
     */
    private static final int STATUS_PENDIENTE_PAGO = 7;

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSearchResponse> searchInvoicesByStatus(InvoiceStatusSearchRequest searchRequest) {
        log.info("Iniciando búsqueda por estatus. Estatus: {}, RFC Emisor: {}, Tipo: {}",
                searchRequest.getEstatus(),
                searchRequest.getRfcEmisor(),
                searchRequest.getTipoDocumento());

        // Convertir a InvoiceSearchRequest para reutilizar lógica existente
        InvoiceSearchRequest request = new InvoiceSearchRequest();
        request.setRfcEmisor(searchRequest.getRfcEmisor());
        request.setFechaInicioRecepcion(searchRequest.getFechaInicioRecepcion());
        request.setFechaFinalRecepcion(searchRequest.getFechaFinalRecepcion());
        request.setTipoDocumento(searchRequest.getTipoDocumento());
        request.setEstatus(searchRequest.getEstatus()); // Estatus ya es obligatorio aquí
        request.setRfcReceptor(searchRequest.getRfcReceptor());
        request.setIdProveedor(searchRequest.getIdProveedor());
        request.setSerie(searchRequest.getSerie());
        request.setFolio(searchRequest.getFolio());
        request.setUuid(searchRequest.getUuid());
        request.setNoOrdenCompra(searchRequest.getNoOrdenCompra());
        request.setNoRecepcion(searchRequest.getNoRecepcion());
        request.setPage(searchRequest.getPage());
        request.setSize(searchRequest.getSize());
        request.setSortBy(searchRequest.getSortBy());
        request.setSortDirection(searchRequest.getSortDirection());

        // Reutilizar el método existente
        return searchInvoices(request);
    }

    @Override
    @Transactional
    public InvoiceStatusUpdateResponse updateInvoiceStatus(String uuid, InvoiceStatusUpdateRequest request) {
        log.info("========================================");
        log.info("INICIO ACTUALIZACIÓN ESTATUS (STM-410)");
        log.info("========================================");
        log.info("UUID: {}", uuid);
        log.info("Transición: {} -> {}", request.getEstatusOrigen(), request.getEstatusDestino());

        try {
            // === PASO 1: BUSCAR DOCUMENTO ===
            UUID fiscalUuid;
            try {
                fiscalUuid = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                log.error("UUID inválido: {}", uuid);
                return InvoiceStatusUpdateResponse.error("BUS3100", "UUID inválido: " + uuid);
            }

            Optional<InvoiceEntity> invoiceOpt = invoiceRepository.findByFiscalUuid(fiscalUuid);
            if (invoiceOpt.isEmpty()) {
                log.error("Documento no encontrado. UUID: {}", uuid);
                return InvoiceStatusUpdateResponse.error("BUS3101", "Documento no encontrado: " + uuid);
            }

            InvoiceEntity invoice = invoiceOpt.get();

            // === PASO 2: VALIDAR ESTATUS ACTUAL ===
            if (!invoice.getStatus().equals(request.getEstatusOrigen())) {
                log.error("Estatus actual no coincide. Esperado: {}, Actual: {}",
                        request.getEstatusOrigen(), invoice.getStatus());
                return InvoiceStatusUpdateResponse.error("BUS3102",
                        String.format("El estatus actual del documento (%d) no coincide con el estatus origen indicado (%d)",
                                invoice.getStatus(), request.getEstatusOrigen()));
            }

            // === PASO 3: DETERMINAR OPTION_ID ===
            int optionId = determineOptionId(invoice.getDocumentType());

            // === PASO 4: VALIDAR TRANSICIÓN CON TREN DE ESTATUS ===
            log.info("Validando transición con servicio de tren de estatus...");
            StatusTrainValidationResult validationResult = statusTrainApiService.validateTransition(
                    optionId,
                    request.getEstatusOrigen(),
                    request.getEstatusDestino()
            );

            if (!validationResult.isValid()) {
                log.warn("Transición no válida. Código: {}, Mensaje: {}",
                        validationResult.getErrorCode(), validationResult.getErrorMessage());

                if ("WRN7010".equals(validationResult.getErrorCode())) {
                    return InvoiceStatusUpdateResponse.sourceNotCataloged(request.getEstatusOrigen());
                } else if ("WRN7011".equals(validationResult.getErrorCode())) {
                    // STM-335: Mensaje específico para cancelación de NC
                    if ("E".equals(invoice.getDocumentType())
                            && CreditNoteStatus.CANCELADA.getCodigo().equals(request.getEstatusDestino())) {
                        return InvoiceStatusUpdateResponse.error("WRN7023",
                                "La nota de crédito no puede cancelarse porque ya cuenta con una afectación contable.");
                    }
                    return InvoiceStatusUpdateResponse.transitionNotAllowed(
                            request.getEstatusOrigen(), request.getEstatusDestino());
                } else if ("ERR5001".equals(validationResult.getErrorCode())) {
                    return InvoiceStatusUpdateResponse.error("SVC5001",
                            "Servicio de tren de estatus no disponible: " + validationResult.getErrorMessage());
                }

                return InvoiceStatusUpdateResponse.error(
                        validationResult.getErrorCode(),
                        validationResult.getErrorMessage());
            }

            log.info("Transición validada correctamente");

            // === PASO 5: ACTUALIZAR ESTATUS ===
            Integer estatusAnterior = invoice.getStatus();
            invoice.setStatus(request.getEstatusDestino());
            invoice.setUpdatedBy(request.getIdUsuarioActualizacion());
            invoice.setUpdatedAt(LocalDateTime.now());

            // Si el estatus destino es 7 (Pendiente de Pago), guardar fecha de contabilización
            LocalDate fechaContabilizacion = null;
            if (STATUS_PENDIENTE_PAGO == request.getEstatusDestino()) {
                if (request.getFechaContabilizacion() != null) {
                    fechaContabilizacion = request.getFechaContabilizacion();
                    invoice.setAccountingDate(fechaContabilizacion);
                    log.info("Fecha de contabilización guardada: {}", fechaContabilizacion);
                } else {
                    // Si no se proporciona, usar fecha actual
                    fechaContabilizacion = LocalDate.now();
                    invoice.setAccountingDate(fechaContabilizacion);
                    log.info("Fecha de contabilización asignada (fecha actual): {}", fechaContabilizacion);
                }
            }

            // === PASO 6: PERSISTIR CAMBIOS ===
            invoiceRepository.save(invoice);

            // === PASO 7: GUARDAR HISTORIAL DE CAMBIO DE ESTATUS ===
            try {
                InvoiceStatusHistoryEntity historyEntry = InvoiceStatusHistoryEntity.builder()
                        .invoiceUuid(invoice.getInvoiceUuid())
                        .fiscalUuid(invoice.getFiscalUuid())
                        .statusFrom(estatusAnterior)
                        .statusTo(request.getEstatusDestino())
                        .changedBy(request.getIdUsuarioActualizacion() != null ? request.getIdUsuarioActualizacion().intValue() : null)
                        .changedAt(LocalDateTime.now())
                        .comment(request.getComentario())
                        .build();
                invoiceStatusHistoryRepository.save(historyEntry);
                log.info("Historial de cambio de estatus guardado. {} -> {}", estatusAnterior, request.getEstatusDestino());
            } catch (Exception e) {
                log.warn("No se pudo guardar historial de cambio de estatus: {}", e.getMessage());
            }

            // Obtener nombre del estatus
            String estatusNuevoNombre = getStatusName(invoice.getDocumentType(), request.getEstatusDestino());

            log.info("========================================");
            log.info("ACTUALIZACIÓN ESTATUS COMPLETADA");
            log.info("UUID: {}", uuid);
            log.info("Estatus: {} -> {} ({})", estatusAnterior, request.getEstatusDestino(), estatusNuevoNombre);
            log.info("========================================");

            return InvoiceStatusUpdateResponse.success(
                    "BUS3010",
                    "Estatus actualizado exitosamente",
                    invoice.getInvoiceUuid(),
                    invoice.getFiscalUuid(),
                    invoice.getDocumentType(),
                    estatusAnterior,
                    request.getEstatusDestino(),
                    estatusNuevoNombre,
                    fechaContabilizacion
            );

        } catch (Exception e) {
            log.error("Error actualizando estatus: {}", e.getMessage(), e);
            return InvoiceStatusUpdateResponse.error("ERR5000",
                    "Error interno al actualizar estatus: " + e.getMessage());
        }
    }

    /**
     * Determina el option_id para el tren de estatus según el tipo de documento.
     */
    private int determineOptionId(String documentType) {
        return switch (documentType) {
            case "I" -> OPTION_FACTURA;
            case "E" -> OPTION_NOTA_CREDITO;
            case "P" -> OPTION_PAGOS;
            case "T" -> OPTION_CARTA_PORTE;
            default -> {
                log.warn("Tipo de documento no reconocido: {}, usando FACTURA por defecto", documentType);
                yield OPTION_FACTURA;
            }
        };
    }

    /**
     * Obtiene el nombre del estatus según tipo de documento.
     */
    private String getStatusName(String documentType, Integer statusCode) {
        try {
            if ("E".equals(documentType)) {
                return CreditNoteStatus.fromCodigo(statusCode).getNombre();
            } else {
                return InvoiceStatus.fromCodigo(statusCode).getNombre();
            }
        } catch (IllegalArgumentException e) {
            return "Estatus " + statusCode;
        }
    }
}