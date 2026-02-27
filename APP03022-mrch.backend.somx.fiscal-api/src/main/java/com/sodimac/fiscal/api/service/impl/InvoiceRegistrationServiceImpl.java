package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.model.dto.InvoiceRegistrationResponse;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.entity.*;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.model.enums.FiscalSuccessCode;
import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import com.sodimac.fiscal.api.repository.AddendumRepository;
import com.sodimac.fiscal.api.repository.AuthorizedReceiverCatalogRepository;
import com.sodimac.fiscal.api.repository.InvoiceRepository;
import com.sodimac.fiscal.api.repository.VersionCatalogRepository;
import com.sodimac.fiscal.api.service.*;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Implementación del servicio de registro de facturas y notas de crédito (STM-337).
 *
 * Flujo de validaciones:
 * 1. Lectura del archivo XML
 * 2. Detección del tipo de documento (I=Factura, E=Nota de Crédito)
 * 3. Parseo y validación de estructura XML
 * 4. Validación de versión CFDI vigente
 * 5. Validación de RFC receptor autorizado
 * 6. Validación de duplicidad por UUID fiscal
 * 7. Validación de estructura de addenda
 * 8. Validación con SAT mediante PAC (opcional según configuración)
 * 9. Persistencia en base de datos (invoice, issuer, receiver, addenda, related_cfdi)
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceRegistrationServiceImpl implements InvoiceRegistrationService {

    // Services
    private final CfdiXmlProcessorService cfdiProcessor;
    private final AddendaValidationService addendaValidator;
    private final XmlDocumentTypeDetector documentTypeDetector;
    private final IssuerService issuerService;
    private final ReceiverService receiverService;
    private final TaxExtractionService taxExtractionService;
    private final MessageCatalogService messageCatalog;

    // Repositories
    private final InvoiceRepository invoiceRepository;
    private final AddendumRepository addendumRepository;
    private final AuthorizedReceiverCatalogRepository receiverCatalogRepository;
    private final VersionCatalogRepository versionCatalogRepository;

    @Override
    @Transactional
    public InvoiceRegistrationResponse registerInvoice(MultipartFile xmlFile) {
        log.info("========================================");
        log.info("INICIO REGISTRO FACTURA/NOTA DE CREDITO");
        log.info("========================================");
        log.info("Archivo: {}", xmlFile.getOriginalFilename());
        log.info("Tamano del archivo: {} bytes", xmlFile.getSize());

        String xmlContent = null;
        InvoiceXmlDto invoiceDto = null;
        TipoDocumentoFiscal tipoDocumento = null;

        try {
            // === PASO 1: LEER CONTENIDO DEL ARCHIVO XML ===
            log.info("Paso 1: Leyendo contenido del archivo XML");
            xmlContent = readXmlFile(xmlFile);
            log.debug("Archivo XML leido correctamente. Longitud: {} caracteres", xmlContent.length());

            // === PASO 2: DETECTAR TIPO DE DOCUMENTO ===
            log.info("Paso 2: Detectando tipo de documento fiscal (I=Factura, E=Nota de Credito)");
            tipoDocumento = documentTypeDetector.detectDocumentType(xmlContent);
            log.info("Tipo de documento detectado: {} ({})",
                    tipoDocumento.getCodigo(), tipoDocumento.getDescripcion());

            // Validar que sea solo I o E (no P, N) - FACTURA_CARTA_PORTE se trata como FACTURA
            if (tipoDocumento != TipoDocumentoFiscal.FACTURA &&
                tipoDocumento != TipoDocumentoFiscal.NOTA_CREDITO &&
                tipoDocumento != TipoDocumentoFiscal.FACTURA_CARTA_PORTE) {
                log.error("Tipo de documento no permitido: {}", tipoDocumento.getCodigo());
                messageCatalog.throwException(FiscalMessageCode.BUS023);
            }

            // === PASO 3: PROCESAR Y PARSEAR XML CFDI ===
            log.info("Paso 3: Procesando y validando estructura del XML CFDI");
            invoiceDto = cfdiProcessor.processCfdi(xmlContent, tipoDocumento);
            log.info("XML procesado exitosamente. Serie: {}, Folio: {}, Total: {}",
                    invoiceDto.getSerie(), invoiceDto.getFolio(), invoiceDto.getTotal());
            log.debug("RFC Emisor: {}, RFC Receptor: {}",
                    invoiceDto.getEmisorRfc(), invoiceDto.getReceptorRfc());

            // === PASO 4: VALIDAR VERSION CFDI VIGENTE ===
            log.info("Paso 4: Validando version CFDI vigente");
            validateCfdiVersion(invoiceDto, tipoDocumento);
            log.info("Version CFDI {} validada correctamente", invoiceDto.getVersion());

            // === PASO 5: VALIDAR RFC RECEPTOR AUTORIZADO ===
            log.info("Paso 5: Validando RFC receptor autorizado");
            validateAuthorizedReceiver(invoiceDto.getReceptorRfc());
            log.info("RFC receptor {} autorizado y vigente", invoiceDto.getReceptorRfc());

            // === PASO 6: EXTRAER UUID FISCAL Y VALIDAR DUPLICIDAD ===
            log.info("Paso 6: Extrayendo UUID fiscal y validando duplicidad");
            UUID fiscalUuid = extractFiscalUuid(invoiceDto);
            log.debug("UUID fiscal extraido: {}", fiscalUuid);

            validateNoDuplicate(fiscalUuid, invoiceDto);
            log.info("Documento no duplicado. UUID unico: {}", fiscalUuid);

            // === PASO 7: VALIDAR ADDENDA ===
            log.info("Paso 7: Validando estructura y contenido de addenda");
            boolean hasValidAddenda = addendaValidator.validateAddenda(xmlContent, invoiceDto);

            if (hasValidAddenda) {
                log.info("Addenda validada exitosamente (Addenda_Sodimac o Addenda_Sodimac_CartaPorte)");
            } else {
                log.info("Documento sin addenda. Sera marcado como PENDIENTE DE ADDENDA");
            }

            // === PASO 8: VALIDAR CON SAT (OPCIONAL - COMENTADO POR AHORA) ===
            // TODO: Implementar validación SAT mediante PAC cuando esté disponible
            // log.info("Paso 8: Validando documento con SAT via PAC");
            // validateWithSat(xmlContent, invoiceDto);
            // log.info("Validacion SAT completada exitosamente");

            // === PASO 9: PERSISTIR EN BASE DE DATOS ===
            log.info("Paso 8: Persistiendo documento en base de datos");
            InvoiceEntity savedInvoice = saveInvoiceToDatabase(
                    invoiceDto,
                    xmlContent,
                    fiscalUuid,
                    tipoDocumento,
                    hasValidAddenda
            );
            log.info("Documento persistido exitosamente. Invoice UUID: {}", savedInvoice.getInvoiceUuid());

            // === PASO 10: CONSTRUIR RESPUESTA ===
            log.info("Paso 9: Construyendo respuesta de registro exitoso");
            InvoiceRegistrationResponse response = buildSuccessResponse(
                    savedInvoice,
                    fiscalUuid,
                    tipoDocumento,
                    hasValidAddenda,
                    invoiceDto
            );

            log.info("========================================");
            log.info("REGISTRO COMPLETADO EXITOSAMENTE");
            log.info("========================================");
            log.info("Codigo de respuesta: {}", response.getCode());
            log.info("Invoice UUID: {}", response.getInvoiceUuid());
            log.info("Fiscal UUID: {}", response.getFiscalUuid());
            log.info("Tiene addenda: {}", response.isHasAddenda());
            log.info("Pendiente de addenda: {}", response.isPendingAddenda());

            return response;

        } catch (FiscalException e) {
            log.error("Error de validacion de negocio: [{}] {}", e.getCode(), e.getMessage());
            log.error("========================================");
            log.error("REGISTRO FALLIDO - ERROR DE NEGOCIO");
            log.error("========================================");
            return InvoiceRegistrationResponse.error(e.getCode(), e.getMessage());

        } catch (Exception e) {
            log.error("Error inesperado durante el registro", e);
            log.error("========================================");
            log.error("REGISTRO FALLIDO - ERROR TECNICO");
            log.error("========================================");
            return InvoiceRegistrationResponse.error(
                    FiscalMessageCode.ERR003.getCode(),
                    "Error inesperado: " + e.getMessage()
            );
        }
    }

    // ========== METODOS PRIVADOS ==========

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

            log.debug("Archivo XML leido correctamente");
            return xmlContent;

        } catch (Exception e) {
            log.error("Error leyendo archivo XML", e);
            throw messageCatalog.createException(FiscalMessageCode.ERR005, e.getMessage(), e);
        }
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
     */
    private UUID extractFiscalUuid(InvoiceXmlDto invoiceDto) {
        log.debug("Extrayendo UUID fiscal del TimbreFiscalDigital");

        String uuidStr = null;

        if (invoiceDto.getTimbreFiscalDigital() != null) {
            uuidStr = invoiceDto.getTimbreFiscalDigital().getUuid();
        }

        if (uuidStr == null || uuidStr.trim().isEmpty()) {
            log.error("No se encontro UUID fiscal en el TimbreFiscalDigital");
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
     * Valida que el documento no esté duplicado.
     */
    private void validateNoDuplicate(UUID fiscalUuid, InvoiceXmlDto invoiceDto) {
        log.debug("Validando que el UUID fiscal no este duplicado: {}", fiscalUuid);

        if (invoiceRepository.findByFiscalUuid(fiscalUuid).isPresent()) {
            log.error("Documento duplicado encontrado. UUID fiscal: {}", fiscalUuid);
            messageCatalog.throwException(FiscalMessageCode.BUS034, "UUID: " + fiscalUuid);
        }

        log.debug("UUID fiscal no duplicado");
    }

    /**
     * Guarda la factura/NC en la base de datos.
     */
    private InvoiceEntity saveInvoiceToDatabase(
            InvoiceXmlDto invoiceDto,
            String xmlContent,
            UUID fiscalUuid,
            TipoDocumentoFiscal tipoDocumento,
            boolean hasValidAddenda) {

        log.info("Iniciando persistencia en base de datos");

        try {
            // 1. Guardar/obtener Emisor
            log.debug("Obteniendo o creando emisor: {}", invoiceDto.getEmisorRfc());
            IssuerEntity issuer = issuerService.getOrCreate(
                    invoiceDto.getEmisorRfc(),
                    invoiceDto.getEmisorNombre(),
                    invoiceDto.getEmisorRegimenFiscal()
            );
            log.debug("Emisor obtenido. UUID: {}", issuer.getIssuerUuid());

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
            invoice.setStatus(1); // Activo
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

            // 4. Guardar addenda (si existe)
            if (hasValidAddenda) {
                log.debug("Guardando addenda asociada al invoice");
                saveAddenda(invoice, invoiceDto, xmlContent);
            } else {
                log.debug("No se guarda addenda (pendiente de addenda)");
            }

            // 5. Guardar impuestos (traslados y retenciones)
            log.debug("Extrayendo y guardando impuestos del comprobante");
            taxExtractionService.extractAndSaveTaxes(invoice.getInvoiceUuid(), invoiceDto);
            log.debug("Impuestos guardados correctamente");

            // NOTA: El guardado de CFDIs relacionados (NC) se implementa en InvoiceServiceImpl.saveRelatedCfdis() (STM-1168)
            // Este servicio (InvoiceRegistrationServiceImpl) no se usa actualmente - el controlador usa InvoiceServiceImpl

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
    private void saveAddenda(InvoiceEntity invoice, InvoiceXmlDto invoiceDto, String xmlContent) {
        log.debug("Creando registro de addenda para invoice UUID: {}", invoice.getInvoiceUuid());

        try {
            AddendumEntity addendum = new AddendumEntity();
            addendum.setInvoiceUuid(invoice.getInvoiceUuid());
            addendum.setAddendaType(5); // Tipo estándar para Sodimac (se puede ajustar según negocio)

            // Guardar contenido de la addenda
            addendum.setAddendumContent(xmlContent);

            addendumRepository.save(addendum);
            log.debug("Addenda guardada exitosamente");

        } catch (Exception e) {
            log.error("Error guardando addenda", e);
            // No lanzar excepción para no bloquear el registro principal
            log.warn("El invoice fue guardado pero la addenda fallo");
        }
    }

    /**
     * Construye la respuesta exitosa según el tipo y estado de addenda.
     */
    private InvoiceRegistrationResponse buildSuccessResponse(
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
}
