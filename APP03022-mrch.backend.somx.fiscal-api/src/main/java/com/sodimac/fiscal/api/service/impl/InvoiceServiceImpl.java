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
import com.sodimac.fiscal.api.pdf.PaymentPdfService;
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

    private static final String SEP_LINE = "========================================";
    private static final String K_SYSTEM = "system";
    private static final String LBL_CODIGO = "Codigo: ";
    private static final String LBL_FOLIO_SEP = ", Folio: ";
    private static final String LBL_SERIE = "Serie: ";
    private static final String LBL_RFC = "RFC: ";
    private static final String K_INVOICE_UUID = "invoiceUuid";
    private static final String LOG_UUID = "UUID: {}";
    private static final String LOG_DOC_NO_ENCONTRADO = "Documento no encontrado. UUID: {}";
    private static final String LBL_UUID = "UUID: ";
    private static final String K_REQUEST = "request";
    private static final String LBL_RECEPCION = ", recepcion=";
    private static final String LBL_ESTATUS = "Estatus: ";
    private static final String K_SERIE = "Serie";
    private static final String K_FOLIO = "Folio";
    private static final String K_FECHA_EMISION = "Fecha Emision";
    private static final String K_SUBTOTAL = "Subtotal";
    private static final String K_TOTAL = "Total";
    // Catálogo con los TipoRelacion permitidos para ligar una NC con su factura (hoy 01 y 03).
    // Administrado por negocio desde el portal; se lee directo de shared_catalogs. Regla Ivan 2026-07-20.
    private static final String CAT_TIPO_RELACION_NC = "CatTipoRelacionFacturaNC";
    // Tipo de NC "Descuento Comercial" (CatTipoNotaCredito value 2): puede registrarse SIN factura relacionada (f196).
    private static final String TIPO_NC_DESCUENTO_COMERCIAL = "2";
    /** CatTipoProveedor value "2" = TRANSPORTE (key TPR002). Gate de la cascada de guía al cancelar. */
    private static final String TIPO_PROVEEDOR_TRANSPORTE = "2";

    // Mappers
    private final InvoiceMapper invoiceMapper;

    // Services
    private final CfdiXmlProcessorService cfdiProcessor;
    private final XmlDocumentTypeDetector documentTypeDetector;
    private final IssuerService issuerService;
    private final ReceiverService receiverService;
    private final TaxExtractionService taxExtractionService;
    private final MessageCatalogService messageCatalog;
    private final SatCatalogService satCatalogService;
    private final PdfRenderService pdfRenderService;
    private final PaymentPdfService paymentPdfService;
    private final UtilsApiService utilsApiService;
    private final SupplierBlockApiService supplierBlockApiService;
    private final AuditoriaApiService auditoriaApiService;
    private final FinanzasApiService finanzasApiService;

    // Repositories
    private final InvoiceRepository invoiceRepository;
    private final PaymentsRepository paymentsRepository;
    private final AddendumRepository addendumRepository;
    private final ReceptionRepository receptionRepository;
    private final RebateRepository rebateRepository;
    private final StatusTrainRepository statusTrainRepository;
    private final VersionCatalogRepository versionCatalogRepository;
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
            String receptionId, String supplierNumber, String purchaseOrderNumber, MultipartFile pdfFile,
            String tipoNotaCredito, String rebateId, boolean confirmarCancelacionNc) {
        final String SERVICE_NAME = "InvoiceService.registerInvoice";
        long startTime = System.currentTimeMillis();

        log.info(SEP_LINE);
        log.info("INICIO REGISTRO FACTURA/NOTA DE CREDITO");
        log.info(SEP_LINE);
        log.info("Archivo: {}, idTransaccion: {}", xmlFile.getOriginalFilename(), idTransaccion);
        log.info("Tamano del archivo: {} bytes", xmlFile.getSize());

        // Registrar request en bitácora (STM-704)
        auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_REQUEST.getCode(), SERVICE_NAME,
                K_SYSTEM, false, "Inicio de registro de factura/NC",
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
                    K_SYSTEM, false, "Archivo XML leido correctamente",
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
                        K_SYSTEM, true, "Tipo de documento no permitido: " + tipoDocumento.getCodigo(),
                        "Solo se permiten tipos I (Factura) y E (Nota de Credito)", null, null);
                // QA junio-2026: mensaje claro cuando el XML no es una factura/NC válida (BUS060, ex-BUS057 de Ivan)
                messageCatalog.throwException(FiscalMessageCode.BUS060);
            }
            auditoriaApiService.logActivity(idTransaccion, AuditAction.DETECTAR_TIPO_DOCUMENTO.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Tipo de documento detectado: " + tipoDocumento.getDescripcion(),
                    LBL_CODIGO + tipoDocumento.getCodigo(), null, null);

            // === PASO 3: PROCESAR Y PARSEAR XML CFDI ===
            log.info("Paso 3: Procesando y validando estructura del XML CFDI");
            invoiceDto = cfdiProcessor.processCfdi(xmlContent, tipoDocumento);
            log.info("XML procesado exitosamente. Serie: {}, Folio: {}, Total: {}",
                    invoiceDto.getSerie(), invoiceDto.getFolio(), invoiceDto.getTotal());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PROCESAR_XML_CFDI.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "XML CFDI procesado exitosamente",
                    LBL_SERIE + invoiceDto.getSerie() + LBL_FOLIO_SEP + invoiceDto.getFolio() + ", Total: " + invoiceDto.getTotal(),
                    Map.of("serie", String.valueOf(invoiceDto.getSerie()),
                            "folio", String.valueOf(invoiceDto.getFolio()),
                            "rfcEmisor", String.valueOf(invoiceDto.getEmisorRfc()),
                            "rfcReceptor", String.valueOf(invoiceDto.getReceptorRfc())), null);

            // === PASO 3.1: VALIDAR SERIE Y FOLIO (STM-395/STM-397) ===
            log.info("Paso 3.1: Validando que el documento tenga serie y folio");
            validateSeriesAndFolio(invoiceDto, tipoDocumento);
            log.info("Serie y folio validados correctamente");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_SERIE_FOLIO.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Serie y folio validados correctamente",
                    LBL_SERIE + invoiceDto.getSerie() + LBL_FOLIO_SEP + invoiceDto.getFolio(), null, null);

            // === PASO 4: VALIDAR VERSION CFDI VIGENTE ===
            log.info("Paso 4: Validando version CFDI vigente");
            validateCfdiVersion(invoiceDto, tipoDocumento);
            log.info("Version CFDI {} validada correctamente", invoiceDto.getVersion());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_VERSION_CFDI.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Version CFDI validada correctamente",
                    "Version: " + invoiceDto.getVersion(), null, null);

            // === PASO 5: VALIDAR RFC RECEPTOR AUTORIZADO ===
            log.info("Paso 5: Validando RFC receptor autorizado");
            validateAuthorizedReceiver(invoiceDto.getReceptorRfc());
            log.info("RFC receptor {} autorizado y vigente", invoiceDto.getReceptorRfc());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_RFC_RECEPTOR.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "RFC receptor autorizado y vigente",
                    LBL_RFC + invoiceDto.getReceptorRfc(), null, null);

            // === PASO 6: OBTENER EMISOR Y VALIDAR DUPLICIDAD (STM-395/STM-397) ===
            log.info("Paso 6: Obteniendo emisor para validaciones de duplicidad");
            IssuerEntity issuer = issuerService.getOrCreate(
                    invoiceDto.getEmisorRfc(),
                    invoiceDto.getEmisorNombre(),
                    invoiceDto.getEmisorRegimenFiscal()
            );
            log.debug("Emisor obtenido. UUID: {}", issuer.getIssuerUuid());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.OBTENER_EMISOR.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Emisor obtenido correctamente",
                    LBL_RFC + invoiceDto.getEmisorRfc() + ", Issuer UUID: " + issuer.getIssuerUuid(), null, null);

            // === PASO 6.1: VALIDAR DUPLICADO POR SERIE+FOLIO (STM-395/STM-397) ===
            log.info("Paso 6.1: Validando duplicado por serie+folio del proveedor");
            validateNoDuplicateBySeriesAndFolio(invoiceDto.getSerie(), invoiceDto.getFolio(),
                    issuer.getIssuerUuid(), tipoDocumento);
            log.info("No existe documento duplicado por serie+folio");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_SERIE_FOLIO.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "No existe documento duplicado por serie+folio",
                    LBL_SERIE + invoiceDto.getSerie() + LBL_FOLIO_SEP + invoiceDto.getFolio(), null, null);

            // === PASO 6.2: EXTRAER UUID FISCAL Y VALIDAR DUPLICADO (STM-395/STM-397) ===
            log.info("Paso 6.2: Extrayendo UUID fiscal y validando duplicado por UUID");
            UUID fiscalUuid = extractFiscalUuid(invoiceDto);
            log.debug("UUID fiscal extraido: {}", fiscalUuid);

            validateNoDuplicateByUuid(fiscalUuid, issuer.getIssuerUuid(), tipoDocumento);
            log.info("Documento no duplicado. UUID unico: {}", fiscalUuid);
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_UUID.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Documento no duplicado, UUID unico",
                    "UUID fiscal: " + fiscalUuid, null, null);

            // === PASO 6.2.1: VALIDAR QUE EL FOLIO FISCAL NO ESTÉ YA CARGADO COMO ADDENDA MANUAL ===
            // finanzas (Josue) carga manualmente la addenda con el folio fiscal antes del registro.
            // Si ya existe, no se permite cargar el XML -> WRN7032. Fila 47 QA (2026-06-23).
            log.info("Paso 6.2.1: Validando que el folio fiscal no exista en addenda manual");
            if (addendumRepository.existsAddendaManualByFolioFiscal(fiscalUuid)) {
                log.warn("Folio fiscal {} ya registrado manualmente en addenda manual", fiscalUuid);
                auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_UUID.getCode(), SERVICE_NAME,
                        K_SYSTEM, true, "Factura ya registrada manualmente (addenda manual)",
                        "UUID fiscal: " + fiscalUuid, null, null);
                messageCatalog.throwException(FiscalMessageCode.WRN7032);
            }

            // === PASO 6.3 / 6.4: VALIDAR BLOQUEO DE PUBLICACIÓN (solo Facturas) ===
            // Orden definido por QA: primero por tipo de proveedor (BUS2028), luego por proveedor (BUS2029).
            if (tipoDocumento == TipoDocumentoFiscal.FACTURA) {
                // PASO 6.3: bloqueo por TIPO de proveedor (CatBloqueoTipoProveedor) -> BUS2028
                log.info("Paso 6.3: Validando bloqueo de publicación por tipo de proveedor");
                if (supplierBlockApiService.isSupplierTypeBlocked(supplierNumber)) {
                    log.warn("Publicación bloqueada por tipo de proveedor. supplierNumber={}", supplierNumber);
                    auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_UUID.getCode(), SERVICE_NAME,
                            K_SYSTEM, true, "Publicación bloqueada por tipo de proveedor (BUS2028)",
                            "supplierNumber: " + supplierNumber, null, null);
                    messageCatalog.throwException(FiscalMessageCode.BUS2028);
                }

                // PASO 6.4: bloqueo por PROVEEDOR individual (supplier_block) -> BUS2029
                log.info("Paso 6.4: Validando bloqueo de publicación por proveedor");
                if (supplierBlockApiService.isSupplierBlocked(supplierNumber)) {
                    log.warn("Publicación bloqueada por proveedor. supplierNumber={}", supplierNumber);
                    auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_UUID.getCode(), SERVICE_NAME,
                            K_SYSTEM, true, "Publicación bloqueada por proveedor (BUS2029)",
                            "supplierNumber: " + supplierNumber, null, null);
                    messageCatalog.throwException(FiscalMessageCode.BUS2029);
                }
            }

            // === PASO 6.5: VALIDACIONES NOTA DE CRÉDITO (forma de pago + uso CFDI) ===
            // Orden QA: primero forma de pago (CatFormaPagoValidoNc), luego uso CFDI (CatUsoCfdiValidoNc).
            if (tipoDocumento == TipoDocumentoFiscal.NOTA_CREDITO) {
                validateCreditNoteCatalogs(invoiceDto, idTransaccion, SERVICE_NAME);

                // === PASO 6.6: TOLERANCIA DESCUENTO COMERCIAL (solo NC tipo 2) ===
                // El Ajuste por Recepción (tipo 1) NO valida tolerancia: ya se validó en la carga
                // inicial de la factura (decisión Ivan ago-2026).
                if (TIPO_NC_DESCUENTO_COMERCIAL.equals(tipoNotaCredito)) {
                    validateDescuentoComercialTolerance(invoiceDto, rebateId, idTransaccion, SERVICE_NAME);
                }
            }

            // === PASO 7: VALIDAR TOLERANCIA IMPORTE (solo Facturas) ===
            // Fuera de tolerancia NO rechaza: registra como Recibido Parcial (status 2) y pide NC (decisión Ivan).
            log.info("Paso 7: Validando tolerancia entre subtotal factura e importe recepción");
            ToleranceResult toleranceResult = ToleranceResult.recibida();
            if (tipoDocumento == TipoDocumentoFiscal.FACTURA) {
                toleranceResult = validateImporteTolerance(invoiceDto, receptionId, idTransaccion, SERVICE_NAME);
            }
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Validación de tolerancia completada",
                    "receptionId: " + receptionId, null, null);

            // === PASO 8: VALIDAR CON SAT (OPCIONAL - COMENTADO POR AHORA) ===
            // TODO: Implementar validación SAT mediante PAC cuando esté disponible
            // log.info("Paso 8: Validando documento con SAT via PAC");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_SAT.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Validacion SAT omitida (pendiente de implementar via PAC)",
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
                    receptionId,
                    toleranceResult.statusFactura,
                    tipoNotaCredito
            );
            log.info("Documento persistido exitosamente. Invoice UUID: {}", savedInvoice.getInvoiceUuid());

            // === PASO 9.5: SUBIR PDF A GCS (opcional) ===
            // Rechazo Comercial (factura menor a la recepción fuera de tolerancia): NO se sube el
            // PDF al bucket (decisión Ivan 2026-06-22). El XML sí queda persistido en BD (xml_content).
            String pdfUploadWarning = null; // WRN7033 si el upload del PDF falla (no crítico)
            boolean esRechazoComercial = toleranceResult.statusFactura == InvoiceStatus.RECHAZO_COMERCIAL.getCodigo();
            if (pdfFile != null && !pdfFile.isEmpty() && !esRechazoComercial) {
                try {
                    String gcsObject = gcsStorageService.uploadPdf(pdfFile, savedInvoice.getInvoiceUuid().toString());
                    savedInvoice.setPdfGcsObject(gcsObject);
                    invoiceRepository.save(savedInvoice);
                    log.info("PDF subido a GCS. Object: {}", gcsObject);
                } catch (Exception e) {
                    // No crítico: la factura ya quedó registrada. Se informa al front vía warnings[]
                    // (WRN7033) para que sepa que el PDF no quedó disponible y pueda reintentar.
                    log.warn("PDF no pudo subirse a GCS (no crítico, factura ya registrada): {}", e.getMessage());
                    pdfUploadWarning = messageCatalog.getMessage(FiscalMessageCode.WRN7033);
                }
            } else if (esRechazoComercial) {
                log.info("Factura en Rechazo Comercial: se omite la subida del PDF al bucket (XML persiste en BD)");
            }

            // === PASO 9.6: ACTUALIZAR ESTATUS DE LA RECEPCIÓN A CONSUMIDA (solo Facturas) ===
            // Cuando la factura cuadra con la recepción (dentro de tolerancia -> Recibida, o mayor
            // -> Recibido Parcial) la recepción pasa a Consumida (1). Si es Rechazo Comercial
            // (factura menor) NO se toca. QA filas 54-57 / catálogo CatEstatusRecepcion.
            if (tipoDocumento == TipoDocumentoFiscal.FACTURA && !esRechazoComercial) {
                marcarRecepcionConsumida(receptionId, idTransaccion, SERVICE_NAME);
            }

            // === PASO 9.7: RE-EVALUAR TOLERANCIA DE LA FACTURA TRAS APLICAR LA NC (fila 104 QA) ===
            // Al subir una NC, el neto (subtotal factura - Σ subtotal NCs vinculadas) se re-evalúa vs
            // la recepción: dentro de tolerancia -> factura a 3 (En proceso de envío); por debajo y
            // fuera de tolerancia -> cascada de rechazo (factura a 1, NCs a 9 Cancelada, recepción a 0
            // Disponible), previa confirmación del usuario (WRN7034). Decisión Ivan 2026-06-29.
            if (tipoDocumento == TipoDocumentoFiscal.NOTA_CREDITO) {
                reevaluarFacturaTrasNc(savedInvoice, confirmarCancelacionNc, idTransaccion, SERVICE_NAME);
            }

            auditoriaApiService.logActivity(idTransaccion, AuditAction.PERSISTIR_DOCUMENTO.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Documento persistido exitosamente en base de datos",
                    "Invoice UUID: " + savedInvoice.getInvoiceUuid(),
                    Map.of(K_INVOICE_UUID, savedInvoice.getInvoiceUuid().toString(),
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

            // Fuera de tolerancia: registro exitoso, se adjunta advertencia (WRN7030 parcial / WRN7031 rechazo comercial).
            if (toleranceResult.warning != null) {
                response.getWarnings().add(toleranceResult.warning);
            }

            // PDF no se pudo subir al bucket: registro exitoso, se informa al front (WRN7033).
            if (pdfUploadWarning != null) {
                response.getWarnings().add(pdfUploadWarning);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info(SEP_LINE);
            log.info("REGISTRO COMPLETADO EXITOSAMENTE");
            log.info(SEP_LINE);
            log.info("Codigo de respuesta: {}", response.getCode());
            log.info("Invoice UUID: {}", response.getInvoiceUuid());
            log.info("Fiscal UUID: {}", response.getFiscalUuid());
            log.info("Duracion: {} ms", duration);

            // Registrar response exitoso en bitácora (STM-704)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_RESPONSE.getCode(), SERVICE_NAME,
                    K_SYSTEM, false, "Registro completado exitosamente",
                    LBL_CODIGO + response.getCode() + ", Invoice UUID: " + response.getInvoiceUuid(),
                    Map.of("code", response.getCode(),
                            K_INVOICE_UUID, String.valueOf(response.getInvoiceUuid()),
                            "fiscalUuid", String.valueOf(response.getFiscalUuid()),
                            "hasAddenda", response.isHasAddenda(),
                            "pendingAddenda", response.isPendingAddenda()), duration);

            return response;

        } catch (FiscalException e) {
            long duration = System.currentTimeMillis() - startTime;
            // El registro atrapa la excepción y retorna un response de error; sin esto, Spring
            // commitearía la transacción dejando datos parciales (ej. NC persistida con relación inválida).
            markRollbackOnly();
            log.error("Error de validacion de negocio: [{}] {}", e.getCode(), e.getMessage());
            log.error(SEP_LINE);
            log.error("REGISTRO FALLIDO - ERROR DE NEGOCIO");
            log.error(SEP_LINE);

            // Registrar error de negocio en bitácora (STM-704)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_ERROR_NEGOCIO.getCode(), SERVICE_NAME,
                    K_SYSTEM, true, "Error de validacion: " + e.getMessage(),
                    LBL_CODIGO + e.getCode() + ", Mensaje: " + e.getMessage(),
                    Map.of("errorCode", e.getCode(), "errorMessage", e.getMessage()), duration);

            return InvoiceRegistrationResponse.error(e.getCode(), e.getMessage());

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            markRollbackOnly();
            log.error("Error inesperado durante el registro", e);
            log.error(SEP_LINE);
            log.error("REGISTRO FALLIDO - ERROR TECNICO");
            log.error(SEP_LINE);

            // Registrar error técnico en bitácora (STM-704)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.REGISTRO_ERROR_TECNICO.getCode(), SERVICE_NAME,
                    K_SYSTEM, true, "Error inesperado durante el registro",
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
        log.info(SEP_LINE);
        log.info("INICIO ACTUALIZACION FACTURA/NOTA DE CREDITO");
        log.info(SEP_LINE);
        log.info(LOG_UUID, request.getUuid());
        log.info("Numero Proveedor: {}", request.getNumeroProveedor());
        log.info("Nuevo Estatus: {}", request.getEstatus());
        log.info("Usuario Actualizacion: {}", request.getIdUsuarioActualizacion());

        // Variables para bitácora
        final String SERVICE_NAME_UPDATE = "InvoiceService.updateInvoice";
        String traceId = UUID.randomUUID().toString();
        long startTimeMs = System.currentTimeMillis();
        String requestDataJson = buildRequestDataJson(request);
        UUID invoiceUuid = null;  // Se asigna cuando se encuentra la factura (para log con FK correcta)

        try {
            // === PASO 1: BUSCAR Y VALIDAR DOCUMENTO ===
            log.info("Paso 1: Buscando documento por UUID");
            InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(request.getUuid())
                    .orElseThrow(() -> {
                        log.error(LOG_DOC_NO_ENCONTRADO, request.getUuid());
                        return new FiscalException(FiscalMessageCode.BUS046, LBL_UUID + request.getUuid());
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
            invoice.setUpdatedBy(parseUserUuid(request.getIdUsuarioActualizacion()));
            // Nota: BaseEntity maneja updated_at automáticamente con @PreUpdate

            invoice = invoiceRepository.save(invoice);
            log.info("Estatus actualizado exitosamente (usuario actualización: {})",
                    request.getIdUsuarioActualizacion());

            // Cancelación de factura (estatus 20): libera la recepción (→0 Disponible) y, si es
            // transporte, la guía (→2 Pendiente de Facturar). Tabla de conversión de estatus (Ivan).
            if (TipoDocumentoFiscal.FACTURA.getCodigo().equals(documentType)
                    && Integer.valueOf(FACTURA_CANCELADA).equals(newStatusCode)) {
                liberarRecepcionPorCancelacionFactura(invoice,
                        "CANCEL-" + invoice.getInvoiceUuid(), "InvoiceService.updateInvoice");
            }

            // Cancelación de NC (estatus 20): si la factura relacionada queda sin NCs activas,
            // regresa a 2 (Recibido Parcial). Regla Ivan (2026-09-04).
            if (TipoDocumentoFiscal.NOTA_CREDITO.getCodigo().equals(documentType)
                    && Integer.valueOf(NC_CANCELADA).equals(newStatusCode)) {
                reevaluarFacturaTrasCancelacionNc(invoice,
                        "CANCEL-NC-" + invoice.getInvoiceUuid(), "InvoiceService.updateInvoice");
            }

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
                    LBL_CODIGO + response.getCode() + ", Estatus: " + currentStatusCode + " -> " + newStatusCode,
                    Map.of(K_INVOICE_UUID, invoiceUuid.toString(),
                            "uuid", request.getUuid(),
                            "estatusAnterior", currentStatusCode,
                            "estatusNuevo", newStatusCode,
                            K_REQUEST, requestDataJson,
                            "response", buildResponseDataJson(response)), durationMs);

            log.info(SEP_LINE);
            log.info("ACTUALIZACION COMPLETADA EXITOSAMENTE");
            log.info(SEP_LINE);
            log.info("Codigo de respuesta: {}", response.getCode());
            log.info("Estatus anterior: {}, Estatus nuevo: {}", currentStatusCode, newStatusCode);

            return response;

        } catch (FiscalException e) {
            log.error("Error de validacion de negocio: [{}] {}", e.getCode(), e.getMessage());
            log.error(SEP_LINE);
            log.error("ACTUALIZACION FALLIDA - ERROR DE NEGOCIO");
            log.error(SEP_LINE);

            // Registrar error en bitácora (auditoria-api)
            long durationMs = System.currentTimeMillis() - startTimeMs;
            auditoriaApiService.logActivity(traceId, AuditAction.UPDATE_ERROR_NEGOCIO.getCode(), SERVICE_NAME_UPDATE,
                    String.valueOf(request.getIdUsuarioActualizacion()), true,
                    "Error de validacion: " + e.getMessage(),
                    LBL_CODIGO + e.getCode(),
                    Map.of("errorCode", e.getCode(), "errorMessage", e.getMessage(),
                            "uuid", request.getUuid(), K_REQUEST, requestDataJson), durationMs);

            return InvoiceUpdateResponse.error(e.getCode(), e.getMessage());

        } catch (Exception e) {
            log.error("Error inesperado durante la actualizacion", e);
            log.error(SEP_LINE);
            log.error("ACTUALIZACION FALLIDA - ERROR TECNICO");
            log.error(SEP_LINE);

            // Registrar error en bitácora (auditoria-api)
            long durationMs = System.currentTimeMillis() - startTimeMs;
            auditoriaApiService.logActivity(traceId, AuditAction.UPDATE_ERROR_TECNICO.getCode(), SERVICE_NAME_UPDATE,
                    String.valueOf(request.getIdUsuarioActualizacion()), true,
                    "Error inesperado durante la actualizacion",
                    e.getClass().getName() + ": " + e.getMessage(),
                    Map.of("uuid", request.getUuid(), K_REQUEST, requestDataJson), durationMs);

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

        // Catálogo CatRfcReceptor (shared_catalogs). Reemplaza la tabla authorized_receiver_catalog
        // (decisión Ivan 2026-06-23). Si el RFC no está autorizado/activo -> BUS008.
        if (!addendumRepository.existsRfcReceptorAutorizado(rfcReceptor)) {
            log.error("RFC receptor {} no autorizado o inactivo", rfcReceptor);
            messageCatalog.throwException(FiscalMessageCode.BUS008, LBL_RFC + rfcReceptor);
        }

        log.debug("RFC receptor {} autorizado en CatRfcReceptor", rfcReceptor);
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
     * Valida los catálogos de Nota de Crédito (QA junio-2026):
     * - Forma de pago del comprobante debe existir en CatFormaPagoValidoNc (BUS058).
     * - Uso CFDI del receptor debe existir en CatUsoCfdiValidoNc (BUS059), posterior a forma de pago.
     *
     * Si el catálogo está vacío/inactivo o el valor no está configurado, se rechaza el registro.
     */
    /**
     * Tolerancia del descuento comercial (NC tipo 2). Regla Ivan ago-2026:
     * el importe (subtotal) de la NC NO puede quedar por debajo del valor del descuento comercial
     * (tenant_finance.rebate.amount, leído por rebate_uuid) más allá de la tolerancia del parámetro
     * "ToleranciaImporteRebate". Se toma la ÚLTIMA VERSIÓN ACTIVA del parámetro (Ivan 2026-09-02);
     * si no hay ninguna versión activa, la comparación es EXACTA (tolerancia 0). Si se pasa, se
     * RECHAZA con BUS2032.
     *
     * Es una validación de un solo sentido: solo rechaza cuando la NC es INFERIOR al descuento.
     * Si el rebateId no viene o el rebate no existe / no tiene monto, se omite (no bloquea).
     */
    private void validateDescuentoComercialTolerance(InvoiceXmlDto invoiceDto, String rebateId,
            String idTransaccion, String serviceName) {

        if (rebateId == null || rebateId.isBlank()) {
            log.warn("rebateId no proporcionado en NC de Descuento Comercial; se omite validación de tolerancia");
            return;
        }

        UUID rebateUuid;
        try {
            rebateUuid = UUID.fromString(rebateId.trim());
        } catch (IllegalArgumentException e) {
            log.warn("rebateId no es un UUID válido ({}); se omite validación de tolerancia", rebateId);
            return;
        }

        BigDecimal subtotalNc;
        try {
            subtotalNc = new BigDecimal(invoiceDto.getSubTotal());
        } catch (Exception e) {
            log.warn("SubTotal de la NC no es numérico ({}); se omite validación de tolerancia", invoiceDto.getSubTotal());
            return;
        }

        RebateEntity rebate = rebateRepository.findById(rebateUuid).orElse(null);
        if (rebate == null || rebate.getAmount() == null) {
            log.warn("Rebate {} no encontrado o sin monto; se omite validación de tolerancia", rebateUuid);
            return;
        }
        BigDecimal descuento = rebate.getAmount();

        // Última versión ACTIVA del parámetro (por nombre, no por id fijo): Ivan versiona el
        // parámetro y cada versión tiene distinto id_parameter. Sin versión activa -> exacto.
        BigDecimal tolerancia = readActiveParamValueByName(PARAM_TOLERANCIA_REBATE);
        if (tolerancia == null) {
            tolerancia = BigDecimal.ZERO; // sin versión activa -> comparación exacta (100%)
        }

        // Rechaza solo si la NC queda por DEBAJO del descuento más allá de la tolerancia:
        //   descuento - subtotalNc > tolerancia
        BigDecimal faltante = descuento.subtract(subtotalNc);
        log.info("Validación tolerancia descuento comercial: descuento={}, subtotalNC={}, faltante={}, tolerancia={}",
                descuento, subtotalNc, faltante, tolerancia);

        if (faltante.compareTo(tolerancia) > 0) {
            log.warn("NC ({}) inferior al descuento comercial ({}) fuera de tolerancia ({}). Rechazo BUS2032.",
                    subtotalNc, descuento, tolerancia);
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                    K_SYSTEM, true, "NC de descuento comercial fuera de tolerancia (BUS2032)",
                    "descuento=" + descuento.toPlainString() + ", subtotalNC=" + subtotalNc.toPlainString()
                            + ", tolerancia=" + tolerancia.toPlainString(), null, null);
            // Muestra la tolerancia REAL usada (última versión activa), no un valor fijo.
            messageCatalog.throwExceptionWithParams(FiscalMessageCode.BUS2032, maskMoney(tolerancia));
        }
    }

    /** Parsea el UUID de usuario que manda el front (sub del token). null si viene vacío o inválido. */
    private static UUID parseUserUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            log.warn("idUsuario no es un UUID válido ({}); se guarda null", value);
            return null;
        }
    }

    private void validateCreditNoteCatalogs(InvoiceXmlDto invoiceDto, String idTransaccion, String serviceName) {
        final String CAT_FORMA_PAGO_NC = "CatFormaPagoValidoNc";
        final String CAT_USO_CFDI_NC = "CatUsoCfdiValidoNc";

        // Los catálogos viven en shared_catalogs (misma BD). Se leen DIRECTO por JPA (mismo patrón
        // que CatTipoRelacionFacturaNC en findActiveCatalogValues) para NO depender de util-api:
        // si fiscal no alcanza util-api, getActiveCatalogValues devolvía set vacío -> BUS058/BUS059 falso.
        // PASO 6.5.1: forma de pago (Comprobante/@FormaPago)
        String formaPago = invoiceDto.getFormaPago();
        log.info("Paso 6.5.1: Validando forma de pago NC '{}' contra {}", formaPago, CAT_FORMA_PAGO_NC);
        java.util.Set<String> formasValidas = new java.util.HashSet<>(addendumRepository.findActiveCatalogValues(CAT_FORMA_PAGO_NC));
        if (formaPago == null || formaPago.isBlank() || !formasValidas.contains(formaPago.trim())) {
            log.warn("Forma de pago NC no válida. formaPago={} validas={}", formaPago, formasValidas);
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_UUID.getCode(), serviceName,
                    K_SYSTEM, true, "Forma de pago de NC no configurada como válida (BUS058)",
                    "formaPago: " + formaPago, null, null);
            messageCatalog.throwExceptionWithParams(FiscalMessageCode.BUS058, formaPago);
        }

        // PASO 6.5.2: uso CFDI (Receptor/@UsoCFDI)
        String usoCfdi = invoiceDto.getReceptorUsoCFDI();
        log.info("Paso 6.5.2: Validando uso CFDI NC '{}' contra {}", usoCfdi, CAT_USO_CFDI_NC);
        java.util.Set<String> usosValidos = new java.util.HashSet<>(addendumRepository.findActiveCatalogValues(CAT_USO_CFDI_NC));
        if (usoCfdi == null || usoCfdi.isBlank() || !usosValidos.contains(usoCfdi.trim())) {
            log.warn("Uso CFDI NC no válido. usoCfdi={} validos={}", usoCfdi, usosValidos);
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_DUPLICADO_UUID.getCode(), serviceName,
                    K_SYSTEM, true, "Uso CFDI de NC no configurado como válido (BUS059)",
                    "usoCFDI: " + usoCfdi, null, null);
            messageCatalog.throwExceptionWithParams(FiscalMessageCode.BUS059, usoCfdi);
        }

        log.info("Validaciones de catálogos NC completadas correctamente");
    }

    /**
     * Resultado de la evaluación de tolerancia: estatus con que debe quedar la factura y
     * advertencia opcional para el response.
     */
    private static final class ToleranceResult {
        final int statusFactura;   // 3 Recibida, 2 Recibido Parcial, 1 Rechazo Comercial
        final String warning;      // mensaje para warnings[], o null

        ToleranceResult(int statusFactura, String warning) {
            this.statusFactura = statusFactura;
            this.warning = warning;
        }

        static ToleranceResult recibida() {
            return new ToleranceResult(InvoiceStatus.RECIBIDA.getCodigo(), null);
        }
    }

    /**
     * Evalúa la tolerancia entre el subtotal de la factura y el importe de la recepción y
     * determina el estatus de registro (decisión Ivan, diagrama 2026-06-18):
     * <ul>
     *   <li>Dentro de tolerancia -> 3 Recibida.</li>
     *   <li>Fuera de tolerancia, factura &gt; recepción -> 2 Recibido Parcial (requiere NC) + WRN7030.</li>
     *   <li>Fuera de tolerancia, factura &lt; recepción -> 1 Rechazo Comercial + WRN7031.</li>
     * </ul>
     * Si no se puede evaluar (datos faltantes) se asume 3 Recibida.
     */
    private ToleranceResult validateImporteTolerance(InvoiceXmlDto invoiceDto, String receptionId,
            String idTransaccion, String serviceName) {

        if (receptionId == null || receptionId.isBlank()) {
            log.warn("receptionId no proporcionado; se omite validación de tolerancia");
            return ToleranceResult.recibida();
        }

        BigDecimal subtotal;
        try {
            subtotal = new BigDecimal(invoiceDto.getSubTotal());
        } catch (Exception e) {
            log.warn("SubTotal del XML no es numérico ({}); se omite validación de tolerancia", invoiceDto.getSubTotal());
            return ToleranceResult.recibida();
        }

        FinanzasReceptionResponse reception = finanzasApiService.getReception(receptionId);
        if (reception == null || reception.getAmount() == null) {
            log.warn("finanzas-api no retornó amount para receptionId {}; se omite validación", receptionId);
            return ToleranceResult.recibida();
        }

        BigDecimal receptionAmount;
        try {
            receptionAmount = new BigDecimal(reception.getAmount());
        } catch (Exception e) {
            log.warn("Amount de recepción no es numérico ({}); se omite validación", reception.getAmount());
            return ToleranceResult.recibida();
        }

        // Determinar tolerancia efectiva según QA junio-2026:
        //  1) Si "Tolerancia por importe" (id 3) está activa -> se compara por MONTO.
        //  2) Si no, y "Tolerancia por porcentaje" (id 4) está activa -> por PORCENTAJE
        //     (base = importe de la recepción). El valor se interpreta como fracción
        //     (ej: 0.01 = 1%). Confirmar interpretación con Ivan (ver doc C5).
        //  3) Si ambas están apagadas -> comparación EXACTA (tolerancia 0).
        BigDecimal toleranceMonto = readActiveParamValue(CatParameterKey.TOLERANCIA_IMPORTE.getId());
        BigDecimal tolerancePct = readActiveParamValue(CatParameterKey.TOLERANCIA_PORCENTAJE.getId());

        BigDecimal tolerance;
        String modo;
        if (toleranceMonto != null) {
            tolerance = toleranceMonto;
            modo = "monto";
        } else if (tolerancePct != null) {
            tolerance = receptionAmount.multiply(tolerancePct).abs();
            modo = "porcentaje(" + tolerancePct.toPlainString() + ")";
        } else {
            tolerance = BigDecimal.ZERO;
            modo = "exacto";
        }

        BigDecimal diff = subtotal.subtract(receptionAmount).abs();
        log.info("Validación tolerancia [{}]: subtotal={}, receptionAmount={}, diff={}, tolerancia={}",
                modo, subtotal, receptionAmount, diff, tolerance);

        if (diff.compareTo(tolerance) > 0) {
            // Fuera de tolerancia: NO rechaza, registra. El estatus depende de la dirección (decisión Ivan).
            if (subtotal.compareTo(receptionAmount) < 0) {
                // Factura MENOR a recepción -> 1 Rechazo Comercial.
                log.warn("Subtotal {} menor a recepción {} fuera de tolerancia {} ({}). Se registrará como Rechazo Comercial.",
                        subtotal, receptionAmount, tolerance, modo);
                auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                        K_SYSTEM, false, "Fuera de tolerancia (factura < recepción) -> Rechazo Comercial",
                        "subtotal=" + subtotal.toPlainString() + LBL_RECEPCION + receptionAmount.toPlainString()
                                + ", tolerancia=" + tolerance.toPlainString() + " (" + modo + ")", null, null);
                String warning = messageCatalog.getMessage(FiscalMessageCode.WRN7031,
                        maskMoney(subtotal), maskMoney(receptionAmount), maskMoney(tolerance));
                return new ToleranceResult(InvoiceStatus.RECHAZO_COMERCIAL.getCodigo(), warning);
            }
            // Factura MAYOR a recepción -> 2 Recibido Parcial (requiere NC).
            log.warn("Subtotal {} mayor a recepción {} fuera de tolerancia {} ({}). Se registrará como Recibido Parcial.",
                    subtotal, receptionAmount, tolerance, modo);
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                    K_SYSTEM, false, "Fuera de tolerancia (factura > recepción) -> Recibido Parcial (pendiente NC)",
                    "subtotal=" + subtotal.toPlainString() + LBL_RECEPCION + receptionAmount.toPlainString()
                            + ", tolerancia=" + tolerance.toPlainString() + " (" + modo + ")", null, null);
            String warning = messageCatalog.getMessage(FiscalMessageCode.WRN7030,
                    maskMoney(subtotal), maskMoney(receptionAmount), maskMoney(tolerance));
            return new ToleranceResult(InvoiceStatus.RECIBIDO_PARCIAL.getCodigo(), warning);
        }

        log.info("Tolerancia validada correctamente [{}]. Diferencia: {} pesos", modo, diff);
        return ToleranceResult.recibida();
    }

    /**
     * Formatea un monto con máscara de valor económico ($#,##0.00, ej. $41,098.18) para los
     * mensajes al usuario (tolerancia factura vs recepción). QA jul-2026 (Fer/Fernando).
     */
    private static String maskMoney(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "MX")).format(value);
    }

    /**
     * Lee el valor numérico de un parámetro de cat_parameter solo si está ACTIVO (status=1).
     * Devuelve null si no existe, está inactivo o el valor no es numérico.
     */
    private BigDecimal readActiveParamValue(int parameterId) {
        try {
            CatParameterEntity param = catParameterRepository.findById(parameterId).orElse(null);
            if (param == null) {
                log.debug("Parámetro id={} no encontrado", parameterId);
                return null;
            }
            if (param.getStatus() == null || param.getStatus() != 1) {
                log.debug("Parámetro id={} inactivo (status={})", parameterId, param.getStatus());
                return null;
            }
            if (param.getValue() == null || param.getValue().isBlank()) {
                return null;
            }
            return new BigDecimal(param.getValue().trim());
        } catch (Exception e) {
            log.warn("Error leyendo parámetro id={}: {}", parameterId, e.getMessage());
            return null;
        }
    }

    /** Nombre del parámetro de tolerancia del descuento comercial (versionado en cat_parameter). */
    private static final String PARAM_TOLERANCIA_REBATE = "ToleranciaImporteRebate";

    /**
     * Lee el valor numérico de la ÚLTIMA VERSIÓN ACTIVA (mayor version, status=1) de un parámetro
     * de cat_parameter por NOMBRE. Devuelve null si no hay versión activa, o el valor no es numérico
     * -> el llamador aplica comparación exacta. Ivan 2026-09-02: el parámetro se versiona (cada
     * versión con distinto id_parameter), así que NO se puede leer por id fijo.
     */
    private BigDecimal readActiveParamValueByName(String name) {
        try {
            CatParameterEntity param = catParameterRepository
                    .findTopByNameAndStatusOrderByVersionDesc(name, 1).orElse(null);
            if (param == null) {
                log.debug("Parámetro '{}' sin versión activa", name);
                return null;
            }
            if (param.getValue() == null || param.getValue().isBlank()) {
                return null;
            }
            return new BigDecimal(param.getValue().trim());
        } catch (Exception e) {
            log.warn("Error leyendo parámetro '{}': {}", name, e.getMessage());
            return null;
        }
    }

    private void validateSeriesAndFolio(InvoiceXmlDto invoiceDto, TipoDocumentoFiscal tipoDocumento) {
        String serie = invoiceDto.getSerie();
        String folio = invoiceDto.getFolio();

        // Regla 2026-06-23: el FOLIO es requerido (identifica el documento, dedup serie+folio); la
        // SERIE es OPCIONAL (en CFDI 4.0 SAT ambos son opcionales, pero el portal exige folio). Antes
        // se exigían ambos. Fila 98 QA.
        if (folio == null || folio.isBlank()) {
            FiscalMessageCode code = (tipoDocumento == TipoDocumentoFiscal.FACTURA)
                    ? FiscalMessageCode.WRN7012
                    : FiscalMessageCode.WRN7015;
            log.error("Documento sin folio. Tipo: {}, Serie: {}, Folio: {}",
                    tipoDocumento.getCodigo(), serie, folio);
            messageCatalog.throwException(code);
        }
    }

    /**
     * Valida que no exista documento duplicado por serie+folio del mismo proveedor (STM-395/STM-397 CA02).
     * Excepción: Rechazo Comercial y Rechazo Contable no bloquean un nuevo intento de publicación.
     *
     * @param serie Serie del documento
     * @param folio Folio del documento
     * @param issuerUuid UUID del emisor (proveedor)
     * @param tipoDocumento Tipo de documento para determinar el mensaje de error
     */
    private void validateNoDuplicateBySeriesAndFolio(String serie, String folio,
            UUID issuerUuid, TipoDocumentoFiscal tipoDocumento) {
        String docType = tipoDocumento.getCodigo();
        var ignorados = statusesIgnoredForDuplicate(tipoDocumento);

        if (invoiceRepository.existsBySeriesAndFolioAndIssuerUuidAndDocumentTypeExcludingStatuses(
                serie, folio, issuerUuid, docType, ignorados)) {
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
     * Excepción: si el registro con ese UUID fiscal está en Rechazo Comercial o Rechazo Contable,
     * se permite reintentar (se reutilizará el mismo invoice_uuid por uq_invoice_fiscal_uuid).
     *
     * @param fiscalUuid UUID fiscal del documento
     * @param issuerUuid UUID del emisor (proveedor)
     * @param tipoDocumento Tipo de documento para determinar el mensaje de error
     */
    private void validateNoDuplicateByUuid(UUID fiscalUuid, UUID issuerUuid,
            TipoDocumentoFiscal tipoDocumento) {
        String docType = tipoDocumento.getCodigo();
        var ignorados = statusesIgnoredForDuplicate(tipoDocumento);

        // El constraint único en BD es por fiscal_uuid SOLO (uq_invoice_fiscal_uuid).
        boolean duplicado = invoiceRepository.existsByFiscalUuidExcludingStatuses(
                        fiscalUuid, ignorados)
                || invoiceRepository.existsByFiscalUuidAndIssuerUuidAndDocumentTypeExcludingStatuses(
                        fiscalUuid, issuerUuid, docType, ignorados);

        if (duplicado) {
            FiscalMessageCode code = (tipoDocumento == TipoDocumentoFiscal.FACTURA)
                    ? FiscalMessageCode.WRN7014
                    : FiscalMessageCode.WRN7017;
            log.error("Documento duplicado por UUID. Tipo: {}, UUID: {}, Emisor: {}",
                    docType, fiscalUuid, issuerUuid);
            messageCatalog.throwException(code);
        }
    }

    /**
     * Estatus que no cuentan como duplicado al republicar.
     * Factura (CatEstatusFactura): Rechazo Comercial value=1 (EFA001), Rechazo Contable value=14 (EFA012).
     * NC: Rechazo Comercial=0, Rechazo Contable=11.
     */
    private static List<Integer> statusesIgnoredForDuplicate(TipoDocumentoFiscal tipoDocumento) {
        if (tipoDocumento == TipoDocumentoFiscal.NOTA_CREDITO) {
            return List.of(
                    CreditNoteStatus.RECHAZO_COMERCIAL.getCodigo(),
                    CreditNoteStatus.RECHAZO_CONTABLE.getCodigo());
        }
        return List.of(
                InvoiceStatus.RECHAZO_COMERCIAL.getCodigo(),
                InvoiceStatus.RECHAZO_CONTABLE.getCodigo());
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
            String receptionId,
            int statusFactura,
            String tipoNotaCredito) {

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

            // 3. Crear entidad Invoice (o reutilizar si ya existe en Rechazo Comercial
            //    o Rechazo Contable — uq_invoice_fiscal_uuid impide un INSERT nuevo).
            log.debug("Creando entidad Invoice");
            var ignorados = statusesIgnoredForDuplicate(tipoDocumento);
            InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(fiscalUuid)
                    .filter(existing -> existing.getStatus() != null
                            && ignorados.contains(existing.getStatus()))
                    .orElseGet(InvoiceEntity::new);
            if (invoice.getInvoiceUuid() != null) {
                log.info(
                        "Reutilizando invoice en rechazo comercial/contable para re-publicación. invoiceUuid={}, fiscalUuid={}, status={}",
                        invoice.getInvoiceUuid(), fiscalUuid, invoice.getStatus());
            }
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

            // Rechazo Comercial (factura menor a la recepción fuera de tolerancia): NO se persiste el
            // XML; el desglose en tablas (invoice + impuestos) es suficiente. Decisión Ivan 2026-06-22.
            boolean esRechazoComercial = tipoDocumento == TipoDocumentoFiscal.FACTURA
                    && statusFactura == InvoiceStatus.RECHAZO_COMERCIAL.getCodigo();
            invoice.setXmlContent(esRechazoComercial ? null : xmlContent);
            // Factura: estatus por tolerancia. NC: 3 En proceso de envío; si es Descuento Comercial
            // (tipoNotaCredito=2) nace en 8 Contabilizada.
            invoice.setStatus(resolveInitialDocumentStatus(tipoDocumento, statusFactura, tipoNotaCredito));
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
            saveAddenda(invoice, xmlContent, supplierNumber, purchaseOrderNumber, receptionId, tipoNotaCredito, esRechazoComercial);

            // 5. Guardar impuestos
            log.debug("Guardando impuestos de la factura");
            taxExtractionService.extractAndSaveTaxes(invoice.getInvoiceUuid(), invoiceDto);

            // 6. Guardar CFDIs relacionados (para notas de crédito) - STM-1168
            if (tipoDocumento == TipoDocumentoFiscal.NOTA_CREDITO) {
                log.debug("Guardando CFDIs relacionados para Nota de Crédito");
                saveRelatedCfdis(invoice, invoiceDto, tipoNotaCredito);
            }

            return invoice;

        } catch (FiscalException e) {
            // Los errores de negocio (BUS0xx: relación NC, monto NC, etc.) deben propagarse
            // con su código original, no enmascararse como ERR003 técnico.
            throw e;
        } catch (Exception e) {
            log.error("Error guardando en base de datos", e);
            messageCatalog.throwException(FiscalMessageCode.ERR003, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Marca la transacción actual como rollback-only. Necesario porque registerInvoice atrapa
     * las excepciones y retorna un response de error (la transacción no se revierte sola al no
     * propagarse la excepción fuera del método @Transactional).
     */
    private void markRollbackOnly() {
        try {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
        } catch (Exception ex) {
            log.warn("No se pudo marcar rollback-only (sin transacción activa?): {}", ex.getMessage());
        }
    }

    /**
     * Resuelve el número de recepción numérico (tenant_finance.reception.reception_number)
     * a partir del receptionId (UUID). Si no es un UUID válido o la recepción no existe,
     * devuelve el valor original como fallback. Issue Fer #1 (2026-06-19).
     */
    private String resolveReceptionNumber(String receptionId) {
        try {
            UUID uuid = UUID.fromString(receptionId.trim());
            String numero = receptionRepository.findById(uuid)
                    .map(ReceptionEntity::getReceptionNumber)
                    .orElse(null);
            return (numero != null && !numero.isBlank()) ? numero : receptionId;
        } catch (IllegalArgumentException e) {
            return receptionId; // no es UUID -> ya viene como número
        }
    }

    /**
     * Resuelve el NOMBRE del estatus leyéndolo de la BD (catálogo CatEstatusFactura / CatEstatusNotaCredito
     * en shared_catalogs), NO de un enum hardcodeado. Los enums solo sirven para los códigos; las
     * descripciones deben venir de la BD para no divergir del catálogo (retro Ivan 2026-06-22; el enum
     * decía "Recibida" pero el catálogo dice "En proceso de envió" para el código 3). Fallback al enum
     * solo si el catálogo no devuelve descripción.
     */
    private String resolveStatusName(String documentType, Integer statusCode) {
        if (statusCode == null) {
            return null;
        }
        String catalogCode = null;
        if (TipoDocumentoFiscal.FACTURA.getCodigo().equals(documentType)) {
            catalogCode = "CatEstatusFactura";
        } else if (TipoDocumentoFiscal.NOTA_CREDITO.getCodigo().equals(documentType)) {
            catalogCode = "CatEstatusNotaCredito";
        }
        if (catalogCode != null) {
            // lang_id 1 = ES (mismo criterio que findTipoProveedorDescripcion)
            String desc = addendumRepository.findCatalogDescription(catalogCode, statusCode.toString(), 1);
            if (desc != null && !desc.isBlank()) {
                return desc;
            }
        }
        // Fallback defensivo al enum si el catálogo no tiene el valor.
        return InvoiceSearchResponse.getStatusName(documentType, statusCode);
    }

    /**
     * Descripción del tipo de NC (catálogo CatTipoNotaCredito, lang ES): 1=Ajuste por Recepción,
     * 2=Descuento Comercial. Se toma de la addenda. Solo aplica a NC; null si no hay valor. Fer
     * jul-2026 (columna "Tipo NC" en consulta de NC + CSV).
     */
    private String resolveTipoNotaCreditoDescripcion(AddendumEntity addendum) {
        if (addendum == null || addendum.getTipoNotaCredito() == null
                || addendum.getTipoNotaCredito().isBlank()) {
            return null;
        }
        // lang_id 1 = ES (mismo criterio que resolveStatusName / tipoProveedorDescripcion).
        return addendumRepository.findCatalogDescription(
                "CatTipoNotaCredito", addendum.getTipoNotaCredito().trim(), 1);
    }

    /**
     * Marca la recepción asociada como Consumida (CatEstatusRecepcion = 1). Se invoca al publicar
     * una factura que cuadra con la recepción (dentro de tolerancia o mayor). No falla el registro
     * si la recepción no existe o el id no es válido (solo log). QA filas 54-57 (2026-06-22).
     * <p>
     * Si la recepción tiene {@code guide_number} (transporte), también marca las guías de embarque
     * ligadas a estatus 3 (Por Contabilizar), solo cuando están en 2.
     */
    private void marcarRecepcionConsumida(String receptionId, String idTransaccion, String serviceName) {
        final java.math.BigDecimal consumida = java.math.BigDecimal.valueOf(1);
        if (receptionId == null || receptionId.isBlank()) {
            log.debug("Sin receptionId: no se actualiza estatus de recepción");
            return;
        }
        try {
            UUID uuid = UUID.fromString(receptionId.trim());
            receptionRepository.findById(uuid).ifPresentOrElse(reception -> {
                reception.setStatus(consumida);
                receptionRepository.save(reception);
                log.info("Recepción {} marcada como Consumida (status=1)", uuid);
                auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                        K_SYSTEM, false, "Recepción marcada como Consumida",
                        "receptionId: " + uuid, null, null);
                marcarGuiasEmbarquePorContabilizar(reception.getGuideNumber(), uuid, idTransaccion, serviceName);
            }, () -> log.warn("Recepción {} no encontrada: no se actualiza estatus", uuid));
        } catch (IllegalArgumentException e) {
            log.warn("receptionId '{}' no es UUID válido: no se actualiza estatus de recepción", receptionId);
        }
    }

    /**
     * Best-effort: guía(s) con el mismo {@code guide_number} de la recepción → estatus 3.
     * No falla el registro de factura si no hay guía o el UPDATE no afecta filas.
     */
    private void marcarGuiasEmbarquePorContabilizar(
            String guideNumber, UUID receptionUuid, String idTransaccion, String serviceName) {
        if (guideNumber == null || guideNumber.isBlank()) {
            log.debug("Recepción {} sin guide_number: no se actualizan guías de embarque", receptionUuid);
            return;
        }
        try {
            int updated = receptionRepository.markShippingGuidesPorContabilizar(guideNumber.trim());
            if (updated > 0) {
                log.info("Guías de embarque guideNumber={} → status=3 ({} fila(s)); recepción {}",
                        guideNumber.trim(), updated, receptionUuid);
                auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                        K_SYSTEM, false, "Guías de embarque marcadas a estatus 3 (Por Contabilizar)",
                        "receptionId: " + receptionUuid + ", guideNumber: " + guideNumber.trim()
                                + ", updated: " + updated, null, null);
            } else {
                log.info("Sin guías en status=2 para guideNumber={} (recepción {})",
                        guideNumber.trim(), receptionUuid);
            }
        } catch (Exception e) {
            log.warn("No se pudieron actualizar guías guideNumber={} (recepción {}): {} — no crítico",
                    guideNumber.trim(), receptionUuid, e.getMessage());
        }
    }

    /**
     * Re-evalúa la tolerancia de la(s) factura(s) relacionadas a una NC recién registrada (fila 104 QA,
     * decisión Ivan 2026-06-29). Solo actúa sobre facturas en Recibido Parcial (2):
     * <ul>
     *   <li>neto = subtotal factura - Σ subtotal de TODAS las NCs vinculadas.</li>
     *   <li>|neto - recepción| ≤ tolerancia -> factura a 3 (En proceso de envío).</li>
     *   <li>neto &lt; recepción y fuera de tolerancia -> cascada de rechazo (requiere confirmación,
     *       WRN7034): factura a 1, NCs a 9 (Cancelada), recepción a 0 (Disponible).</li>
     *   <li>neto &gt; recepción y fuera de tolerancia -> sigue en 2 (faltan más NCs).</li>
     * </ul>
     */
    // Estatus de NC según catálogo CatEstatusNotaCredito. Tren v1.0(5) (Ivan 2026-08-24/25):
    // 2 = Recibida Parcial, 3 = En proceso de envío, 17 = Pendiente de complemento, 20 = Cancelada.
    private static final int NC_RECIBIDO_PARCIAL = 2;
    private static final int NC_EN_PROCESO_ENVIO = 3;
    // NC de descuento comercial (rebate) nace directo en "Pendiente de complemento" (Ivan v1.0(5)):
    // no pasa por desglose/contabilización, solo espera el complemento de pago.
    private static final int NC_DESCUENTO_PENDIENTE_COMPLEMENTO = 17;
    // Tren v1.0(5) (Ivan 2026-08-24): NC "Cancelada" pasa de 11 a 20.
    private static final int NC_CANCELADA = 20;

    /**
     * Estatus inicial al persistir: factura por tolerancia; NC descuento comercial (tipo 2) → 17
     * (Pendiente de complemento, Ivan v1.0(5)); resto de NC → 3.
     */
    private static int resolveInitialDocumentStatus(
            TipoDocumentoFiscal tipoDocumento, int statusFactura, String tipoNotaCredito) {
        if (tipoDocumento == TipoDocumentoFiscal.FACTURA) {
            return statusFactura;
        }
        if (TIPO_NC_DESCUENTO_COMERCIAL.equals(tipoNotaCredito)) {
            return NC_DESCUENTO_PENDIENTE_COMPLEMENTO;
        }
        return NC_EN_PROCESO_ENVIO;
    }

    /**
     * Setea el estatus de TODAS las NCs vinculadas a una factura (la NC acompaña el estado de la
     * factura, fila 122).
     */
    private void setEstatusNcsDeFactura(UUID facturaUuid, int nuevoStatus) {
        for (RelatedCfdiEntity rel : relatedCfdiRepository.findByRelatedInvoiceUuid(facturaUuid)) {
            invoiceRepository.findById(rel.getInvoiceUuid()).ifPresent(ncRel -> {
                ncRel.setStatus(nuevoStatus);
                invoiceRepository.save(ncRel);
            });
        }
    }

    private void reevaluarFacturaTrasNc(InvoiceEntity nc, boolean confirmarCancelacionNc,
            String idTransaccion, String serviceName) {
        List<RelatedCfdiEntity> relacionesNc = relatedCfdiRepository.findByInvoiceUuid(nc.getInvoiceUuid());
        for (RelatedCfdiEntity relNc : relacionesNc) {
            Optional<InvoiceEntity> facturaOpt = invoiceRepository.findById(relNc.getRelatedInvoiceUuid());
            if (facturaOpt.isEmpty()) {
                continue;
            }
            InvoiceEntity factura = facturaOpt.get();

            // Solo aplica desde Recibido Parcial (2) (regla Ivan: solo cambia el estatus si está en 2).
            if (!InvoiceStatus.RECIBIDO_PARCIAL.getCodigo().equals(factura.getStatus())
                    || factura.getSubtotal() == null) {
                log.debug("Factura {} no aplica re-eval NC (status={})", factura.getInvoiceUuid(), factura.getStatus());
                continue;
            }

            // Sumar subtotales de TODAS las NCs vinculadas a la factura (incluye la recién registrada).
            BigDecimal sumaNc = BigDecimal.ZERO;
            for (RelatedCfdiEntity rel : relatedCfdiRepository.findByRelatedInvoiceUuid(factura.getInvoiceUuid())) {
                Optional<InvoiceEntity> ncRelOpt = invoiceRepository.findById(rel.getInvoiceUuid());
                if (ncRelOpt.isPresent() && ncRelOpt.get().getSubtotal() != null) {
                    sumaNc = sumaNc.add(ncRelOpt.get().getSubtotal());
                }
            }
            BigDecimal neto = factura.getSubtotal().subtract(sumaNc);

            ReceptionEntity reception = resolveReceptionDeFactura(factura.getInvoiceUuid());
            if (reception == null || reception.getAmount() == null) {
                log.warn("No se resolvió la recepción de la factura {}; no se re-evalúa tolerancia tras NC",
                        factura.getInvoiceUuid());
                continue;
            }
            BigDecimal receptionAmount = reception.getAmount();
            BigDecimal tolerance = resolveTolerance(receptionAmount);
            BigDecimal diff = neto.subtract(receptionAmount).abs();
            log.info("Re-eval NC->factura {}: subtotal={}, sumaNC={}, neto={}, recepcion={}, diff={}, tol={}",
                    factura.getInvoiceUuid(), factura.getSubtotal(), sumaNc, neto, receptionAmount, diff, tolerance);

            if (diff.compareTo(tolerance) <= 0) {
                // Neto en tolerancia -> factura Y NCs a 3 (En proceso de envío). Fila 122.
                factura.setStatus(InvoiceStatus.RECIBIDA.getCodigo());
                invoiceRepository.save(factura);
                setEstatusNcsDeFactura(factura.getInvoiceUuid(), NC_EN_PROCESO_ENVIO);
                log.info("Factura {} y sus NCs -> estatus 3 (neto en tolerancia tras NC)", factura.getInvoiceUuid());
                auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                        K_SYSTEM, false, "Factura y NCs a 3 (En proceso de envío): neto (factura - NCs) en tolerancia",
                        "factura=" + factura.getInvoiceUuid() + ", neto=" + neto.toPlainString()
                                + LBL_RECEPCION + receptionAmount.toPlainString(), null, null);
            } else if (neto.compareTo(receptionAmount) < 0) {
                // Neto por debajo de la recepción y fuera de tolerancia -> cascada de rechazo.
                // Requiere confirmación (front muestra WRN7034). Sin confirmar -> rollback de la NC.
                if (!confirmarCancelacionNc) {
                    log.warn("NC dejaría la factura {} en rechazo (neto {} < recepción {}); falta confirmación -> WRN7034",
                            factura.getInvoiceUuid(), neto, receptionAmount);
                    messageCatalog.throwExceptionWithParams(FiscalMessageCode.WRN7034,
                            maskMoney(neto), maskMoney(receptionAmount));
                }
                ejecutarCascadaRechazoNc(factura, reception, neto, receptionAmount, tolerance, idTransaccion, serviceName);
            } else {
                // neto > recepción fuera de tolerancia -> aún falta NC: factura Y NCs quedan en
                // 2 (Recibido Parcial). Fila 122 (la NC acompaña a la factura).
                setEstatusNcsDeFactura(factura.getInvoiceUuid(), NC_RECIBIDO_PARCIAL);
                log.info("Factura {} y sus NCs siguen en Recibido Parcial (neto {} aún mayor a recepción {})",
                        factura.getInvoiceUuid(), neto, receptionAmount);
            }
        }
    }

    /**
     * Cascada de rechazo cuando el neto (factura - NCs) queda por debajo de la recepción fuera de
     * tolerancia (fila 104): factura -> 1 (Rechazo Comercial) con motivo en bitácora, NCs vinculadas
     * -> 11 (Cancelada, catálogo CatEstatusNotaCredito nuevo E/F), recepción -> 0 (Disponible) para
     * que el proveedor pueda volver a subir su factura.
     */
    private void ejecutarCascadaRechazoNc(InvoiceEntity factura, ReceptionEntity reception,
            BigDecimal neto, BigDecimal receptionAmount, BigDecimal tolerance,
            String idTransaccion, String serviceName) {
        final BigDecimal recepcionDisponible = BigDecimal.ZERO; // CatEstatusRecepcion 0 = Disponible

        String motivo = "Neto (factura - NCs)=" + neto.toPlainString() + " menor a recepción="
                + receptionAmount.toPlainString() + " fuera de tolerancia=" + tolerance.toPlainString();

        // 1. Factura -> 1 Rechazo Comercial (motivo en log/bitácora).
        factura.setStatus(InvoiceStatus.RECHAZO_COMERCIAL.getCodigo());
        invoiceRepository.save(factura);
        log.warn("Cascada rechazo factura {}: {}", factura.getInvoiceUuid(), motivo);
        auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                K_SYSTEM, true, "Factura a Rechazo Comercial: neto (factura - NCs) menor a la recepción fuera de tolerancia",
                motivo, null, null);

        // 2. NCs vinculadas -> 11 Cancelada.
        setEstatusNcsDeFactura(factura.getInvoiceUuid(), NC_CANCELADA);

        // 3. Recepción -> 0 Disponible (el proveedor podrá volver a subir su factura).
        reception.setStatus(recepcionDisponible);
        receptionRepository.save(reception);
        log.info("Cascada rechazo completa. Factura {} -> 1, NCs -> 9, recepción {} -> 0 (Disponible)",
                factura.getInvoiceUuid(), reception.getReceptionId());
    }

    /** Factura Cancelada (tabla de conversión de estatus, Ivan). */
    private static final int FACTURA_CANCELADA = 20;

    /**
     * Cascada al CANCELAR una factura (estatus 20). Conforme a la tabla de conversión de estatus
     * (Ivan 2026-09-03): la recepción ligada regresa a 0 (Disponible) para que el proveedor pueda
     * volver a facturar, y si es de transporte, la(s) guía(s) de embarque regresan a 2 (Pendiente
     * de Facturar). Best-effort: NO rompe la cancelación si algo falla (solo log).
     */
    private void liberarRecepcionPorCancelacionFactura(InvoiceEntity factura, String idTransaccion,
            String serviceName) {
        try {
            ReceptionEntity reception = resolveReceptionDeFactura(factura.getInvoiceUuid());
            if (reception == null) {
                log.info("Cancelación factura {}: sin recepción ligada, no hay nada que liberar",
                        factura.getInvoiceUuid());
                return;
            }
            // Recepción -> 0 Disponible (Factura 20 -> Recepción 0).
            reception.setStatus(BigDecimal.ZERO);
            receptionRepository.save(reception);
            log.info("Cancelación factura {}: recepción {} -> 0 (Disponible)",
                    factura.getInvoiceUuid(), reception.getReceptionId());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                    K_SYSTEM, false, "Recepción liberada a Disponible por cancelación de factura",
                    "facturaUuid: " + factura.getInvoiceUuid() + ", receptionId: " + reception.getReceptionId(),
                    null, null);

            // Guía(s) -> 2 Pendiente de Facturar (Factura 20 -> Carta Porte 2), SOLO si el proveedor
            // es de tipo transporte (CatTipoProveedor value "2"). Directiva Ivan (2026-09): el disparo
            // se valida por tipo de proveedor — transporte mueve AMBOS estatus (recepción 0 + guía 2);
            // cualquier otro tipo solo libera la recepción (ya hecho arriba).
            String tipoProveedorId = resolveTipoProveedorDeFactura(factura.getInvoiceUuid());
            if (!TIPO_PROVEEDOR_TRANSPORTE.equals(tipoProveedorId)) {
                log.info("Cancelación factura {}: proveedor tipo={} (no transporte), solo se liberó la recepción",
                        factura.getInvoiceUuid(), tipoProveedorId);
                return;
            }
            String guide = reception.getGuideNumber();
            if (guide != null && !guide.isBlank()) {
                int updated = receptionRepository.markShippingGuidesPendienteFacturar(guide.trim());
                log.info("Cancelación factura {}: guías guideNumber={} -> 2 Pendiente de Facturar ({} fila(s))",
                        factura.getInvoiceUuid(), guide.trim(), updated);
                if (updated > 0) {
                    auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                            K_SYSTEM, false, "Guías de embarque a 2 (Pendiente de Facturar) por cancelación de factura",
                            "facturaUuid: " + factura.getInvoiceUuid() + ", guideNumber: " + guide.trim()
                                    + ", updated: " + updated, null, null);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo liberar recepción/guía por cancelación de factura {}: {} — no crítico",
                    factura.getInvoiceUuid(), e.getMessage());
        }
    }

    /**
     * Cascada al CANCELAR una NC (estatus 20). Regla Ivan (2026-09-04, Excel): por cada factura
     * relacionada a la NC, si el total de NCs ACTIVAS (no canceladas) de esa factura es 0, la
     * factura regresa a 2 (Recibido Parcial) — sin NC el neto vuelve a superar la recepción y la
     * factura no debe viajar. Si aún tiene NCs activas (>0), se mantiene su estatus actual.
     * La NC recién cancelada ya está en 20 al llegar aquí, por lo que NO cuenta como activa.
     * Best-effort: no rompe la cancelación si algo falla.
     */
    private void reevaluarFacturaTrasCancelacionNc(InvoiceEntity nc, String idTransaccion, String serviceName) {
        try {
            for (RelatedCfdiEntity relNc : relatedCfdiRepository.findByInvoiceUuid(nc.getInvoiceUuid())) {
                UUID facturaUuid = relNc.getRelatedInvoiceUuid();
                InvoiceEntity factura = invoiceRepository.findById(facturaUuid).orElse(null);
                if (factura == null) {
                    continue;
                }
                long ncsActivas = 0;
                for (RelatedCfdiEntity rel : relatedCfdiRepository.findByRelatedInvoiceUuid(facturaUuid)) {
                    InvoiceEntity ncRel = invoiceRepository.findById(rel.getInvoiceUuid()).orElse(null);
                    if (ncRel != null && !Integer.valueOf(NC_CANCELADA).equals(ncRel.getStatus())) {
                        ncsActivas++;
                    }
                }
                if (ncsActivas == 0) {
                    factura.setStatus(InvoiceStatus.RECIBIDO_PARCIAL.getCodigo());
                    invoiceRepository.save(factura);
                    log.info("Cancelación NC {}: factura {} sin NCs activas -> 2 (Recibido Parcial)",
                            nc.getInvoiceUuid(), facturaUuid);
                    auditoriaApiService.logActivity(idTransaccion, AuditAction.VALIDAR_ADDENDA.getCode(), serviceName,
                            K_SYSTEM, false, "Factura a 2 (Recibido Parcial): su única NC activa fue cancelada",
                            "facturaUuid: " + facturaUuid + ", ncUuid: " + nc.getInvoiceUuid(), null, null);
                } else {
                    log.info("Cancelación NC {}: factura {} conserva {} NC(s) activa(s) -> estatus sin cambio",
                            nc.getInvoiceUuid(), facturaUuid, ncsActivas);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo reevaluar factura tras cancelación de NC {}: {} — no crítico",
                    nc.getInvoiceUuid(), e.getMessage());
        }
    }

    /**
     * Tipo de proveedor (CatTipoProveedor value, ej. "2" = transporte) de la factura, resuelto EN
     * VIVO desde el supplier_number de su addenda. null si no se puede resolver.
     */
    private String resolveTipoProveedorDeFactura(UUID facturaUuid) {
        return addendumRepository.findByInvoiceUuid(facturaUuid)
                .map(AddendumEntity::getSupplierNumber)
                .filter(java.util.Objects::nonNull)
                .map(java.math.BigDecimal::toPlainString)
                .map(addendumRepository::findTipoProveedorId)
                .orElse(null);
    }

    /**
     * Resuelve la recepción de una factura a partir de addendum.reception_number (la factura no
     * guarda el UUID de la recepción). Devuelve null si no se puede resolver.
     */
    private ReceptionEntity resolveReceptionDeFactura(UUID facturaUuid) {
        Optional<AddendumEntity> addOpt = addendumRepository.findByInvoiceUuid(facturaUuid);
        if (addOpt.isEmpty() || addOpt.get().getReceptionNumber() == null
                || addOpt.get().getReceptionNumber().isBlank()) {
            return null;
        }
        AddendumEntity addendum = addOpt.get();
        String receptionNumber = addendum.getReceptionNumber().trim();
        String orderNumber = addendum.getPurchaseOrderNumber();

        // reception_number no es único; se desambigua por la OC (order_number del addendum). Si el
        // addendum trae la OC, se resuelve por número + OC; si no, se cae al número (más reciente).
        if (orderNumber != null && !orderNumber.isBlank()) {
            List<ReceptionEntity> porNumeroYoc =
                    receptionRepository.findByReceptionNumberAndOrderNumber(receptionNumber, orderNumber.trim());
            if (!porNumeroYoc.isEmpty()) {
                return porNumeroYoc.get(0);
            }
            log.warn("No se encontró recepción por número {} + OC {}; se intenta solo por número",
                    receptionNumber, orderNumber);
        }

        List<ReceptionEntity> porNumero = receptionRepository.findByReceptionNumberOrdered(receptionNumber);
        return porNumero.isEmpty() ? null : porNumero.get(0);
    }

    /**
     * Tolerancia efectiva (misma regla que {@link #validateImporteTolerance}): monto (id 3) tiene
     * prioridad; si no, porcentaje (id 4) sobre el importe de la recepción; si ambos off -> 0 (exacto).
     */
    private BigDecimal resolveTolerance(BigDecimal receptionAmount) {
        BigDecimal toleranceMonto = readActiveParamValue(CatParameterKey.TOLERANCIA_IMPORTE.getId());
        BigDecimal tolerancePct = readActiveParamValue(CatParameterKey.TOLERANCIA_PORCENTAJE.getId());
        if (toleranceMonto != null) {
            return toleranceMonto;
        }
        if (tolerancePct != null) {
            return receptionAmount.multiply(tolerancePct).abs();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Guarda la addenda asociada a la factura/NC.
     */
    private void saveAddenda(InvoiceEntity invoice, String xmlContent,
            String supplierNumber, String purchaseOrderNumber, String receptionId, String tipoNotaCredito,
            boolean esRechazoComercial) {
        log.debug("Creando registro de addenda para invoice UUID: {}", invoice.getInvoiceUuid());

        try {
            AddendumEntity addendum = new AddendumEntity();
            addendum.setInvoiceUuid(invoice.getInvoiceUuid());
            addendum.setAddendaType(5);
            // Rechazo Comercial: no se persiste el XML (ni en addendum_content); el desglose en tablas
            // basta. Decisión Ivan 2026-06-22.
            addendum.setAddendumContent(esRechazoComercial ? null : xmlContent);

            // Tipo de NC (catálogo CatTipoNotaCredito): 1=Ajuste por Recepción, 2=Descuento Comercial.
            // Lo manda el front al publicar la NC. En facturas (sin parámetro) queda 0 por defecto
            // (decisión Ivan 2026-06-22). QA filas 16/43.
            addendum.setTipoNotaCredito(
                    (tipoNotaCredito != null && !tipoNotaCredito.isBlank()) ? tipoNotaCredito.trim() : "0");

            if (supplierNumber != null) {
                addendum.setSupplierNumber(new BigDecimal(supplierNumber));
                // Issue Fer #3: poblar el tipo de proveedor (id 1-4 de CatTipoProveedor) leyendo
                // directo de shared_catalogs (sin ir a util-api).
                String tipoProveedorId = addendumRepository.findTipoProveedorId(supplierNumber);
                if (tipoProveedorId != null) {
                    addendum.setSupplierType(tipoProveedorId);
                }
            }
            if (purchaseOrderNumber != null) {
                addendum.setPurchaseOrderNumber(purchaseOrderNumber);
            }
            if (receptionId != null) {
                // Issue Fer #1: guardar el NÚMERO de recepción (numérico, de finanzas), no el UUID.
                // receptionId llega como UUID; se resuelve reception.reception_number. Si no se puede
                // (no es UUID o no existe), se guarda el valor recibido tal cual como fallback.
                addendum.setReceptionNumber(resolveReceptionNumber(receptionId));
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
    private void saveRelatedCfdis(InvoiceEntity ncInvoice, InvoiceXmlDto invoiceDto, String tipoNotaCredito) {
        log.info("=== INICIO GUARDADO CFDIS RELACIONADOS (STM-1168) ===");
        log.debug("NC UUID: {}, Fiscal UUID: {}", ncInvoice.getInvoiceUuid(), ncInvoice.getFiscalUuid());

        // 1. Solo se consideran los bloques cuyo TipoRelacion esté en el catálogo CatTipoRelacionFacturaNC
        //    (tipos permitidos para ligar una NC con su factura; hoy 01 y 03). Se lee DIRECTO de
        //    shared_catalogs, solo estatus activo. Regla Ivan 2026-07-20.
        java.util.Set<String> tiposPermitidos =
                new java.util.HashSet<>(addendumRepository.findActiveCatalogValues(CAT_TIPO_RELACION_NC));
        List<CfdiRelacionadosDto> bloques = invoiceDto.getCfdiRelacionados();
        List<CfdiRelacionadosDto> bloquesValidos = (bloques == null) ? java.util.List.of() :
                bloques.stream()
                        .filter(b -> b.getTipoRelacion() != null
                                && tiposPermitidos.contains(b.getTipoRelacion().trim())
                                && b.getCfdiRelacionado() != null && !b.getCfdiRelacionado().isEmpty())
                        .collect(java.util.stream.Collectors.toList());
        if (bloquesValidos.isEmpty()) {
            // f196: una NC de Descuento Comercial (tipo 2 de CatTipoNotaCredito) puede ir SIN factura
            // relacionada. Para los demás tipos (ej. 1 Ajuste por Recepción) la factura sigue siendo obligatoria.
            if (TIPO_NC_DESCUENTO_COMERCIAL.equals(tipoNotaCredito)) {
                log.info("NC de Descuento Comercial sin factura relacionada: permitido (f196), no se guardan relaciones");
                return;
            }
            // Mensaje claro según el caso (retro Ivan 2026-07-24):
            // - La NC trae CfdiRelacionados pero con TipoRelacion NO permitido -> BUS045 (tipo no permitido).
            // - La NC no trae ningún CfdiRelacionados -> BUS042 (debe incluir un relacionado).
            boolean traeRelacionados = bloques != null && bloques.stream()
                    .anyMatch(b -> b.getCfdiRelacionado() != null && !b.getCfdiRelacionado().isEmpty());
            if (traeRelacionados) {
                log.error("La NC trae CfdiRelacionados pero ninguno con TipoRelacion permitido {}", tiposPermitidos);
                messageCatalog.throwException(FiscalMessageCode.BUS045);
            }
            log.error("La NC no contiene CfdiRelacionados en el XML");
            messageCatalog.throwException(FiscalMessageCode.BUS042);
        }

        int totalRelaciones = 0;

        // 2. Procesar cada bloque permitido y sus CFDIs relacionados
        for (CfdiRelacionadosDto bloque : bloquesValidos) {
            String tipoRelacion = bloque.getTipoRelacion();
            List<CfdiRelacionadoDto> relacionados = bloque.getCfdiRelacionado();
            log.info("Bloque TipoRelacion={} con {} CFDIs relacionados", tipoRelacion, relacionados.size());

            for (CfdiRelacionadoDto relacionado : relacionados) {
                String uuidRelacionadoStr = relacionado.getUuid();
                log.debug("Procesando CFDI relacionado: {}", uuidRelacionadoStr);

                // 2.1 Parsear UUID
                UUID uuidRelacionado;
                try {
                    uuidRelacionado = UUID.fromString(uuidRelacionadoStr);
                } catch (IllegalArgumentException e) {
                    log.error("UUID de CFDI relacionado no válido: {}", uuidRelacionadoStr);
                    messageCatalog.throwException(FiscalMessageCode.BUS043, LBL_UUID + uuidRelacionadoStr);
                    return; // Nunca alcanza aquí
                }

                // 2.2 Buscar la Factura relacionada por fiscal_uuid
                Optional<InvoiceEntity> facturaOpt = invoiceRepository.findByFiscalUuid(uuidRelacionado);

                if (facturaOpt.isEmpty()) {
                    // Regla Ivan 2026-07-31: una NC de Descuento Comercial (tipo 2) PUEDE o no tener
                    // factura relacionada. Si el XML declara una factura que no existe en el sistema,
                    // se deja pasar sin ligar (no rechaza). Para los demás tipos (ej. 1 Ajuste por
                    // Recepción) la factura relacionada sigue siendo obligatoria -> BUS043.
                    if (TIPO_NC_DESCUENTO_COMERCIAL.equals(tipoNotaCredito)) {
                        log.info("NC de Descuento Comercial: factura relacionada {} no existe en el sistema; "
                                + "se permite pasar sin ligar la relación (regla Ivan 2026-07-31)", uuidRelacionado);
                        continue;
                    }
                    log.error("Factura relacionada no encontrada en BD. UUID: {}", uuidRelacionado);
                    messageCatalog.throwException(FiscalMessageCode.BUS043, LBL_UUID + uuidRelacionado);
                }

                InvoiceEntity facturaRelacionada = facturaOpt.get();
                log.debug("Factura encontrada. Invoice UUID: {}, Tipo: {}",
                        facturaRelacionada.getInvoiceUuid(), facturaRelacionada.getDocumentType());

                // 2.3 Validar que sea una Factura (tipo I)
                if (!"I".equals(facturaRelacionada.getDocumentType())) {
                    log.error("El CFDI relacionado no es una Factura. Tipo: {}",
                            facturaRelacionada.getDocumentType());
                    messageCatalog.throwException(FiscalMessageCode.BUS044,
                            LBL_UUID + uuidRelacionado + ", Tipo: " + facturaRelacionada.getDocumentType());
                }

                // 2.3.1 Validar que el monto de la NC no sea mayor al de la factura relacionada (QA junio-2026, BUS061)
                BigDecimal ncTotal = ncInvoice.getTotal();
                BigDecimal facturaTotal = facturaRelacionada.getTotal();
                if (ncTotal != null && facturaTotal != null && ncTotal.compareTo(facturaTotal) > 0) {
                    log.error("Monto NC {} mayor a factura relacionada {} (UUID {})", ncTotal, facturaTotal, uuidRelacionado);
                    messageCatalog.throwExceptionWithParams(FiscalMessageCode.BUS061,
                            ncTotal.toPlainString(), facturaTotal.toPlainString());
                }

                // 2.4 Crear y guardar la relación
                RelatedCfdiEntity relacion = new RelatedCfdiEntity();
                relacion.setInvoiceUuid(ncInvoice.getInvoiceUuid());           // UUID de la NC
                relacion.setRelatedInvoiceUuid(facturaRelacionada.getInvoiceUuid()); // UUID de la Factura
                relacion.setRelationType(tipoRelacion);

                relatedCfdiRepository.save(relacion);
                totalRelaciones++;
                log.info("Relación guardada exitosamente. NC: {} -> Factura: {}",
                        ncInvoice.getFiscalUuid(), facturaRelacionada.getFiscalUuid());
            }
        }

        log.info("=== FIN GUARDADO CFDIS RELACIONADOS - {} relaciones guardadas ===", totalRelaciones);
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
                    LBL_UUID + invoice.getFiscalUuid() + ", Proveedor solicitado: " + numeroProveedorRequest);
        }

        log.info("Validacion de proveedor exitosa. Supplier Number: {}", addendum.getSupplierNumber());
    }

    /**
     * Valida la transición de estatus según el tipo de documento.
     */
    private void validateStatusTransition(Integer currentStatusCode, Integer newStatusCode, String documentType) {
        log.debug("Validando transicion de estatus: {} -> {} para tipo: {}", currentStatusCode, newStatusCode, documentType);

        // Validación DIRECTO contra shared_catalogs.status_train (misma BD, sin util-api ni enum):
        // los cambios que hace Ivan en el tren aplican sin redeploy. Los nombres salen del catálogo.
        if ("I".equals(documentType)) {
            if (!statusTrainRepository.existsByOptionIdAndSourceStatusAndTargetStatus(
                    OPTION_FACTURA, currentStatusCode, newStatusCode)) {
                messageCatalog.throwException(FiscalMessageCode.BUS051,
                        String.format("De: %d (%s) a: %d (%s)",
                                currentStatusCode, resolveStatusName(documentType, currentStatusCode),
                                newStatusCode, resolveStatusName(documentType, newStatusCode)));
            }

        } else if ("E".equals(documentType)) {
            if (!statusTrainRepository.existsByOptionIdAndSourceStatusAndTargetStatus(
                    OPTION_NOTA_CREDITO, currentStatusCode, newStatusCode)) {
                // Cancelación de NC con afectación contable. Código correcto = NC_CANCELADA (11),
                // del catálogo; NO el enum CreditNoteStatus (que tenía 10, desactualizado).
                if (Integer.valueOf(NC_CANCELADA).equals(newStatusCode)) {
                    messageCatalog.throwException(FiscalMessageCode.WRN7023);
                } else {
                    messageCatalog.throwException(FiscalMessageCode.BUS051,
                            String.format("De: %d (%s) a: %d (%s)",
                                    currentStatusCode, resolveStatusName(documentType, currentStatusCode),
                                    newStatusCode, resolveStatusName(documentType, newStatusCode)));
                }
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
                    LBL_ESTATUS + estatus + ", Tipo: " + ("I".equals(documentType) ? "Factura (I)" : "Nota de Crédito (E)"));
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
            // tipoNotaCredito -> tipo_nota_credito (1 Ajuste Recepción / 2 Descuento Comercial). QA 16/43.
            if (addendaDto.getTipoNotaCredito() != null && !addendaDto.getTipoNotaCredito().isBlank()) {
                log.debug("Actualizando tipoNotaCredito: {} -> {}", addendum.getTipoNotaCredito(), addendaDto.getTipoNotaCredito());
                addendum.setTipoNotaCredito(addendaDto.getTipoNotaCredito().trim());
                hasChanges = true;
            }

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
        } else { // "E"
            successCode = addendaActualizada ? FiscalSuccessCode.RES012 : FiscalSuccessCode.RES011;
        }
        // Nombre del estatus desde el catálogo de BD (resolveStatusName), no del enum.
        estatusNuevoNombre = resolveStatusName(documentType, estatusNuevo);

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

        // Fechas: obligatorias SOLO si NO se busca por UUID. Se omiten con el UUID propio (fiscalUuid)
        // o al filtrar las NCs de una factura por su UUID relacionado (Fer, QA jul-2026): ambos ya
        // acotan la búsqueda, no hace falta el rango de fechas.
        if (searchRequest.getUuid() != null || searchRequest.getRelatedInvoiceUuid() != null) {
            log.info("Búsqueda por UUID (propio o factura relacionada) -> se omiten la validación y el filtro de fechas");
        } else if (searchRequest.getFechaInicioRecepcion() == null || searchRequest.getFechaFinalRecepcion() == null) {
            log.error("Fechas de recepción faltantes en búsqueda sin UUID");
            messageCatalog.throwException(FiscalMessageCode.BUS3103);
        }

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
        log.info(SEP_LINE);
        log.info("INICIO BUSQUEDA DE FACTURAS/NOTAS DE CREDITO");
        log.info(SEP_LINE);
        log.info("RFC Emisor: {}", searchRequest.getRfcEmisor());
        log.info("Fecha Inicio: {}", searchRequest.getFechaInicioRecepcion());
        log.info("Fecha Final: {}", searchRequest.getFechaFinalRecepcion());
        log.info("Tipo Documento: {}", searchRequest.getTipoDocumento());
        log.info("RFC Receptor: {}", searchRequest.getRfcReceptor());
        log.info("Serie: {}", searchRequest.getSerie());
        log.info("Folio: {}", searchRequest.getFolio());
        log.info(LOG_UUID, searchRequest.getUuid());
        log.info("Estatus: {}", searchRequest.getEstatus());
        log.info("No. Orden Compra: {}", searchRequest.getNoOrdenCompra());
        log.info("No. Recepcion: {}", searchRequest.getNoRecepcion());

        // === PASO 0: VALIDAR RANGO DE FECHAS (STM-393) ===
        // Cuando se busca por UUID (fiscalUuid, único) las fechas se ignoran (Fer/Ivan QA jul-2026).
        if (searchRequest.getUuid() != null) {
            log.info("Paso 0: Búsqueda por UUID -> se omite la validación de fechas");
        } else {
            log.info("Paso 0: Validando rango de fechas");
            validateDateRange(searchRequest.getFechaInicioRecepcion(), searchRequest.getFechaFinalRecepcion());
        }

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

        log.info(SEP_LINE);
        log.info("BUSQUEDA COMPLETADA EXITOSAMENTE");
        log.info(SEP_LINE);
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

        // Issue Fer #4: devolver id + descripción del tipo de proveedor en campos separados.
        // Retro Ivan 2026-06-22: el tipo se resuelve EN VIVO desde el supplier_number (no del valor
        // guardado en la addenda al registrar), para reflejar cambios posteriores en CatTipoProveedor.
        // Fallback al valor guardado si el proveedor ya no está en el catálogo.
        String tipoProveedorId = null;
        if (addendum != null) {
            if (addendum.getSupplierNumber() != null) {
                tipoProveedorId = addendumRepository.findTipoProveedorId(addendum.getSupplierNumber().toPlainString());
            }
            if (tipoProveedorId == null) {
                tipoProveedorId = addendum.getSupplierType();
            }
        }
        String tipoProveedorDescripcion = (tipoProveedorId != null && !tipoProveedorId.isBlank())
                ? addendumRepository.findTipoProveedorDescripcion(tipoProveedorId)
                : null;

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
                .statusName(resolveStatusName(invoice.getDocumentType(), invoice.getStatus()))
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
                .tipoProveedor(tipoProveedorId)
                .tipoProveedorDescripcion(tipoProveedorDescripcion)
                .guiaEntrega(addendum != null ? addendum.getShippingGuideNumber() : null)
                .tipoNotaCredito(addendum != null ? addendum.getTipoNotaCredito() : null)
                // Descripción del tipo de NC desde el catálogo CatTipoNotaCredito (lang ES). Fer jul-2026.
                .tipoNotaCreditoDescripcion(resolveTipoNotaCreditoDescripcion(addendum))
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
                // Fecha de registro de la factura en el portal. Es la MISMA columna por la que
                // filtra el search (created_at). El front debe listar esta fecha para que coincida
                // con el rango buscado (no accountingSentDate, que es updated_at). Issue Fer 2026-06-19.
                .createdAt(invoice.getCreatedAt())
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
                        .statusNombre(resolveStatusName(TipoDocumentoFiscal.NOTA_CREDITO.getCodigo(), nc.getStatus()))
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
        return buildDocumentFileName(invoice, ".xml");
    }

    /**
     * Construye el nombre del archivo PDF para el ZIP.
     */
    private String buildPdfFileName(InvoiceEntity invoice) {
        return buildDocumentFileName(invoice, ".pdf");
    }

    /**
     * Construye el nombre de archivo de un documento: {@code Serie-Folio_UUID.ext}. El separador
     * {@code "-"} solo se agrega si hay serie Y folio; si falta alguno no se deja separador colgante
     * (evita nombres como "CV-uuid" o "-uuid" cuando no hay folio). Cae al UUID si no hay serie/folio.
     */
    private String buildDocumentFileName(InvoiceEntity invoice, String extension) {
        String series = invoice.getSeries();
        String folio = invoice.getFolio();
        boolean hasSeries = series != null && !series.isEmpty();
        boolean hasFolio = folio != null && !folio.isEmpty();

        StringBuilder fileName = new StringBuilder();
        if (hasSeries) {
            fileName.append(series);
        }
        if (hasSeries && hasFolio) {
            fileName.append("-");
        }
        if (hasFolio) {
            fileName.append(folio);
        }
        if (fileName.length() > 0) {
            fileName.append("_");
        }
        fileName.append(invoice.getFiscalUuid() != null ? invoice.getFiscalUuid() : invoice.getInvoiceUuid());
        fileName.append(extension);

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
                K_SERIE,
                K_FOLIO,
                K_FECHA_EMISION,
                "RFC Emisor",
                "Nombre Emisor",
                "RFC Receptor",
                "Nombre Receptor",
                K_SUBTOTAL,
                K_TOTAL,
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
            K_SERIE, K_FOLIO, K_SUBTOTAL, K_TOTAL, "Orden de Compra", "Recepcion",
            "UUID", "# NC Relacionadas", "ID Proveedor", "Nombre Proveedor",
            K_FECHA_EMISION, "Fecha Recepcion", "Fecha Envio Contabilizar"
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
            K_SERIE, K_FOLIO, K_SUBTOTAL, K_TOTAL, "Motivo", "UUID",
            K_FECHA_EMISION, "Fecha Recepcion", "Fecha Envio Contabilizar",
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

        // Buscar en complementos de pago (folio fiscal o payments_uuid)
        Optional<PaymentsEntity> payment = paymentsRepository.findByFiscalUuid(uuid)
                .or(() -> paymentsRepository.findById(uuid));
        if (payment.isPresent()) {
            String xmlContent = payment.get().getXmlContent();
            if (xmlContent != null && !xmlContent.isEmpty()) {
                log.info("XML encontrado en complementos de pago. UUID: {}", fiscalUuid);
                return xmlContent;
            }
            log.warn("Complemento de pago encontrado pero sin contenido XML. UUID: {}", fiscalUuid);
        }

        log.error(LOG_DOC_NO_ENCONTRADO, fiscalUuid);
        throw new FiscalException(FiscalMessageCode.ERR001, "Documento no encontrado con UUID: " + fiscalUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getPdfByInvoiceUuid(String invoiceUuid) {
        log.info("Buscando PDF por UUID: {}", invoiceUuid);

        UUID uuid;
        try {
            uuid = UUID.fromString(invoiceUuid);
        } catch (IllegalArgumentException e) {
            throw new FiscalException(FiscalMessageCode.ERR001, "UUID inválido: " + invoiceUuid);
        }

        Optional<InvoiceEntity> invoiceOpt = invoiceRepository.findById(uuid)
                .or(() -> invoiceRepository.findByFiscalUuid(uuid));
        if (invoiceOpt.isPresent()) {
            return renderInvoicePdf(invoiceOpt.get());
        }

        Optional<PaymentsEntity> paymentOpt = paymentsRepository.findByFiscalUuid(uuid)
                .or(() -> paymentsRepository.findById(uuid));
        if (paymentOpt.isPresent()) {
            return renderPaymentPdf(paymentOpt.get());
        }

        throw new FiscalException(FiscalMessageCode.ERR001, "Documento no encontrado: " + invoiceUuid);
    }

    private byte[] renderInvoicePdf(InvoiceEntity invoice) {
        UUID invoiceUuid = invoice.getInvoiceUuid();
        if (invoice.getPdfGcsObject() != null && !invoice.getPdfGcsObject().isBlank()) {
            try {
                return gcsStorageService.downloadPdf(invoice.getPdfGcsObject());
            } catch (Exception e) {
                log.warn("PDF en bucket no disponible (object={}); se generará desde el XML. {}",
                        invoice.getPdfGcsObject(), e.getMessage());
            }
        }

        String xmlContent = invoice.getXmlContent();
        if (xmlContent == null || xmlContent.isBlank()) {
            throw new FiscalException(FiscalMessageCode.ERR001,
                    "No hay PDF ni XML disponible para la factura: " + invoiceUuid);
        }
        try {
            log.info("Generando PDF desde XML (fallback) para invoice {}", invoiceUuid);
            return pdfRenderService.renderFromXml(xmlContent);
        } catch (Exception e) {
            log.error("Error generando PDF desde XML. invoice={} error={}", invoiceUuid, e.getMessage());
            throw new FiscalException(FiscalMessageCode.ERR001,
                    "Error al generar el PDF desde el XML: " + e.getMessage());
        }
    }

    private byte[] renderPaymentPdf(PaymentsEntity payment) {
        String xmlContent = payment.getXmlContent();
        if (xmlContent == null || xmlContent.isBlank()) {
            throw new FiscalException(FiscalMessageCode.ERR001,
                    "No hay XML disponible para el complemento: " + payment.getPaymentsUuid());
        }
        try {
            log.info("Generando PDF de complemento desde XML. paymentsUuid={}", payment.getPaymentsUuid());
            return paymentPdfService.renderFromXml(xmlContent);
        } catch (FiscalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generando PDF de complemento. paymentsUuid={} error={}",
                    payment.getPaymentsUuid(), e.getMessage());
            throw new FiscalException(FiscalMessageCode.ERR001,
                    "Error al generar el PDF del complemento: " + e.getMessage());
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
        log.info(SEP_LINE);
        log.info("INICIO ACTUALIZACIÓN ESTATUS (STM-410)");
        log.info(SEP_LINE);
        log.info(LOG_UUID, uuid);
        log.info("Transición: {} -> {}", request.getEstatusOrigen(), request.getEstatusDestino());

        UUID fiscalUuid;
        try {
            fiscalUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            log.error("UUID inválido: {}", uuid);
            return InvoiceStatusUpdateResponse.error("BUS3100", "UUID inválido: " + uuid);
        }

        try {
            // === PASO 1: BUSCAR DOCUMENTO ===
            Optional<InvoiceEntity> invoiceOpt = invoiceRepository.findByFiscalUuid(fiscalUuid);
            if (invoiceOpt.isEmpty()) {
                log.error(LOG_DOC_NO_ENCONTRADO, uuid);
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

            // === PASO 4: VALIDAR TRANSICIÓN CONTRA status_train (BD directo, sin util-api) ===
            log.info("Validando transición contra status_train (BD): opt={}, {} -> {}",
                    optionId, request.getEstatusOrigen(), request.getEstatusDestino());
            boolean transicionPermitida = statusTrainRepository.existsByOptionIdAndSourceStatusAndTargetStatus(
                    optionId, request.getEstatusOrigen(), request.getEstatusDestino());

            if (!transicionPermitida) {
                // ¿El estatus origen está catalogado en el tren? Si no, WRN7010.
                if (!statusTrainRepository.existsByOptionIdAndSourceStatus(optionId, request.getEstatusOrigen())) {
                    return InvoiceStatusUpdateResponse.sourceNotCataloged(request.getEstatusOrigen());
                }
                // STM-335: mensaje específico para cancelación de NC con afectación contable.
                if ("E".equals(invoice.getDocumentType())
                        && Integer.valueOf(NC_CANCELADA).equals(request.getEstatusDestino())) {
                    return InvoiceStatusUpdateResponse.error("WRN7023",
                            "La nota de crédito no puede cancelarse porque ya cuenta con una afectación contable.");
                }
                return InvoiceStatusUpdateResponse.transitionNotAllowed(
                        request.getEstatusOrigen(), request.getEstatusDestino());
            }

            log.info("Transición validada correctamente (status_train BD)");

            // === PASO 5: ACTUALIZAR ESTATUS ===
            Integer estatusAnterior = invoice.getStatus();
            invoice.setStatus(request.getEstatusDestino());
            invoice.setUpdatedBy(parseUserUuid(request.getIdUsuarioActualizacion()));
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

            // Cancelación de factura (estatus 20): libera la recepción (→0 Disponible) y, si es
            // transporte, la guía (→2 Pendiente de Facturar). Tabla de conversión de estatus (Ivan).
            if (TipoDocumentoFiscal.FACTURA.getCodigo().equals(invoice.getDocumentType())
                    && Integer.valueOf(FACTURA_CANCELADA).equals(request.getEstatusDestino())) {
                liberarRecepcionPorCancelacionFactura(invoice,
                        "CANCEL-" + invoice.getInvoiceUuid(), "InvoiceService.updateInvoiceStatus");
            }

            // Cancelación de NC (estatus 20): si la factura relacionada queda sin NCs activas,
            // regresa a 2 (Recibido Parcial). Regla Ivan (2026-09-04).
            if (TipoDocumentoFiscal.NOTA_CREDITO.getCodigo().equals(invoice.getDocumentType())
                    && Integer.valueOf(NC_CANCELADA).equals(request.getEstatusDestino())) {
                reevaluarFacturaTrasCancelacionNc(invoice,
                        "CANCEL-NC-" + invoice.getInvoiceUuid(), "InvoiceService.updateInvoiceStatus");
            }

            // === PASO 7: GUARDAR HISTORIAL DE CAMBIO DE ESTATUS ===
            try {
                InvoiceStatusHistoryEntity historyEntry = InvoiceStatusHistoryEntity.builder()
                        .invoiceUuid(invoice.getInvoiceUuid())
                        .fiscalUuid(invoice.getFiscalUuid())
                        .statusFrom(estatusAnterior)
                        .statusTo(request.getEstatusDestino())
                        .changedBy(parseUserUuid(request.getIdUsuarioActualizacion()))
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

            log.info(SEP_LINE);
            log.info("ACTUALIZACIÓN ESTATUS COMPLETADA");
            log.info(LOG_UUID, uuid);
            log.info("Estatus: {} -> {} ({})", estatusAnterior, request.getEstatusDestino(), estatusNuevoNombre);
            log.info(SEP_LINE);

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
     * Obtiene el nombre del estatus según tipo de documento. Lee de la BD (catálogo) vía
     * resolveStatusName; ya no del enum. Retro Ivan 2026-06-22.
     */
    private String getStatusName(String documentType, Integer statusCode) {
        String nombre = resolveStatusName(documentType, statusCode);
        return nombre != null ? nombre : "Estatus " + statusCode;
    }
}