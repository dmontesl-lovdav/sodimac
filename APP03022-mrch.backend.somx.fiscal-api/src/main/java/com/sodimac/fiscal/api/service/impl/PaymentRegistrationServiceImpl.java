package com.sodimac.fiscal.api.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.model.dto.ParsedPaymentXmlDto;
import com.sodimac.fiscal.api.model.dto.ResponseValidationDetecnoDto;
import com.sodimac.fiscal.api.model.dto.request.PaymentRegistrationRequest;
import com.sodimac.fiscal.api.model.dto.response.PaymentRegistrationResponse;
import com.sodimac.fiscal.api.model.dto.invoicexml.DoctoRelacionadoDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.PagoDto;
import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import com.sodimac.fiscal.api.model.entity.IssuerEntity;
import com.sodimac.fiscal.api.model.entity.PaymentEntity;
import com.sodimac.fiscal.api.model.entity.PaymentFileRegistryEntity;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.model.entity.ReceiverEntity;
import com.sodimac.fiscal.api.model.entity.RelatedDocumentsEntity;
import com.sodimac.fiscal.api.model.enums.AuditAction;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.repository.AddendumRepository;
import com.sodimac.fiscal.api.repository.PaymentFileRegistryRepository;
import com.sodimac.fiscal.api.repository.PaymentRepository;
import com.sodimac.fiscal.api.repository.PaymentsRepository;
import com.sodimac.fiscal.api.repository.RelatedDocumentsRepository;
import com.sodimac.fiscal.api.service.AuditoriaApiService;
import com.sodimac.fiscal.api.service.IssuerService;
import com.sodimac.fiscal.api.service.LogService;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.service.PacCatalogService;
import com.sodimac.fiscal.api.service.PacService;
import com.sodimac.fiscal.api.service.PaymentRegistrationService;
import com.sodimac.fiscal.api.service.PaymentValidationService;
import com.sodimac.fiscal.api.service.PaymentXmlParserService;
import com.sodimac.fiscal.api.service.ReceiverService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de registro de complementos de pago.
 *
 * Orquesta todo el proceso de validación, parseo y registro.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class PaymentRegistrationServiceImpl implements PaymentRegistrationService {

    private final PaymentXmlParserService xmlParserService;
    private final PaymentValidationService validationService;
    private final PacService pacService;
    private final PacCatalogService pacCatalogService;
    private final LogService logService;
    private final MessageCatalogService messageCatalog;
    private final AuditoriaApiService auditoriaApiService;

    // Repositories
    private final PaymentFileRegistryRepository fileRegistryRepository;
    private final IssuerService issuerService;
    private final ReceiverService receiverService;
    private final PaymentsRepository paymentsRepository;
    private final PaymentRepository paymentRepository;
    private final RelatedDocumentsRepository relatedDocumentsRepository;
    private final AddendumRepository addendumRepository;

    /**
     * Constructor con inyección de dependencias.
     * Usa @Qualifier para especificar la implementación de PacService (Detecno).
     */
    public PaymentRegistrationServiceImpl(
            PaymentXmlParserService xmlParserService,
            PaymentValidationService validationService,
            @Qualifier("pacServiceDetecnoImpl") PacService pacService,
            PacCatalogService pacCatalogService,
            LogService logService,
            MessageCatalogService messageCatalog,
            AuditoriaApiService auditoriaApiService,
            PaymentFileRegistryRepository fileRegistryRepository,
            IssuerService issuerService,
            ReceiverService receiverService,
            PaymentsRepository paymentsRepository,
            PaymentRepository paymentRepository,
            RelatedDocumentsRepository relatedDocumentsRepository,
            AddendumRepository addendumRepository) {
        this.xmlParserService = xmlParserService;
        this.validationService = validationService;
        this.pacService = pacService;
        this.pacCatalogService = pacCatalogService;
        this.logService = logService;
        this.messageCatalog = messageCatalog;
        this.auditoriaApiService = auditoriaApiService;
        this.fileRegistryRepository = fileRegistryRepository;
        this.issuerService = issuerService;
        this.receiverService = receiverService;
        this.paymentsRepository = paymentsRepository;
        this.paymentRepository = paymentRepository;
        this.relatedDocumentsRepository = relatedDocumentsRepository;
        this.addendumRepository = addendumRepository;
    }

    @Override
    @Transactional
    public PaymentRegistrationResponse registerPayment(PaymentRegistrationRequest request, String idTransaccion) {
        final String SERVICE_NAME = "PaymentRegistrationService.registerPayment";
        long startTime = System.currentTimeMillis();

        log.info("========================================");
        log.info("INICIO REGISTRO COMPLEMENTO DE PAGO");
        log.info("========================================");
        log.info("Archivo: {}, idTransaccion: {}", request.getXmlFile().getOriginalFilename(), idTransaccion);
        log.info("Proveedor: {}, Usuario: {}", request.getIdProveedor(), request.getIdUsuario());

        // Registrar request en bitácora (STM-272)
        auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_REGISTRO_REQUEST.getCode(), SERVICE_NAME,
                String.valueOf(request.getIdUsuario()), false, "Inicio de registro de complemento de pago",
                "Archivo: " + request.getXmlFile().getOriginalFilename() + ", Proveedor: " + request.getIdProveedor(),
                Map.of("fileName", request.getXmlFile().getOriginalFilename(),
                        "fileSize", request.getXmlFile().getSize(),
                        "idProveedor", request.getIdProveedor(),
                        "tipoAddenda", request.getTipoAddenda()), null);

        String fileName = request.getXmlFile().getOriginalFilename();
        String xmlContent = null;
        ParsedPaymentXmlDto parsedXml = null;

        try {
            // === PASO 1: LEER ARCHIVO XML ===
            log.info("Paso 1: Leyendo contenido del archivo XML");
            xmlContent = readXmlFile(request);
            log.debug("XML leido exitosamente. Longitud: {} caracteres", xmlContent.length());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_LEER_ARCHIVO_XML.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Archivo XML leido correctamente",
                    "Longitud: " + xmlContent.length() + " caracteres", null, null);

            // === PASO 2: VALIDAR TIPO DE ADDENDA ===
            log.info("Paso 2: Validando tipo de addenda");
            validationService.validateAddendaType(request.getTipoAddenda());
            log.info("Tipo de addenda validado: {}", request.getTipoAddenda());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_TIPO_ADDENDA.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Tipo de addenda validado correctamente",
                    "TipoAddenda: " + request.getTipoAddenda(), null, null);

            // === PASO 3: VALIDAR ESTRUCTURA XML contra XSD ===
            log.info("Paso 3: Validando estructura XML contra XSD");
            if (!xmlParserService.validateXmlStructure(xmlContent)) {
                auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_ESTRUCTURA_XSD.getCode(), SERVICE_NAME,
                        String.valueOf(request.getIdUsuario()), true, "Estructura XML no valida contra XSD",
                        "El XML no cumple con el esquema XSD de Pagos 2.0", null, null);
                messageCatalog.throwError(FiscalMessageCode.ERR007);
            }
            log.info("Estructura XML validada contra XSD");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_ESTRUCTURA_XSD.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Estructura XML validada contra XSD",
                    "Esquema XSD Pagos 2.0 cumplido", null, null);

            // === PASO 4: PARSEAR XML ===
            log.info("Paso 4: Parseando XML de complemento de pago");
            parsedXml = xmlParserService.parsePaymentXml(xmlContent);
            log.info("XML parseado exitosamente - UUID: {}", parsedXml.getTimbreFiscalDigital().getUuid());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_PARSEAR_XML.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "XML de pago parseado exitosamente",
                    "UUID: " + parsedXml.getTimbreFiscalDigital().getUuid() + ", Serie: " + parsedXml.getSerie() + ", Folio: " + parsedXml.getFolio(),
                    Map.of("uuid", parsedXml.getTimbreFiscalDigital().getUuid(),
                            "serie", String.valueOf(parsedXml.getSerie()),
                            "folio", String.valueOf(parsedXml.getFolio()),
                            "rfcEmisor", String.valueOf(parsedXml.getEmisorRfc()),
                            "rfcReceptor", String.valueOf(parsedXml.getReceptorRfc())), null);

            // === PASO 5: VALIDAR TIPO DE COMPROBANTE ===
            log.info("Paso 5: Validando tipo de comprobante (debe ser P)");
            validationService.validateComprobanteType(parsedXml.getTipoDeComprobante());
            log.info("Tipo de comprobante validado: {}", parsedXml.getTipoDeComprobante());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_TIPO_COMPROBANTE.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Tipo de comprobante validado: P",
                    "TipoDeComprobante: " + parsedXml.getTipoDeComprobante(), null, null);

            // === PASO 6: VALIDAR NO DUPLICADO ===
            log.info("Paso 6: Validando que no exista duplicado por UUID fiscal");
            UUID fiscalUuid = UUID.fromString(parsedXml.getTimbreFiscalDigital().getUuid());
            validationService.validateNoDuplicate(fiscalUuid);
            log.info("Complemento no duplicado. UUID: {}", fiscalUuid);
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_DUPLICADO.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Complemento no duplicado, UUID unico",
                    "UUID fiscal: " + fiscalUuid, null, null);

            // === PASO 7: VALIDAR RECEPTOR AUTORIZADO ===
            log.info("Paso 7: Validando receptor autorizado");
            validationService.validateAuthorizedReceiver(parsedXml.getReceptorRfc());
            log.info("Receptor autorizado: {}", parsedXml.getReceptorRfc());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_RECEPTOR.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Receptor autorizado y vigente",
                    "RFC: " + parsedXml.getReceptorRfc(), null, null);

            // === PASO 8: VALIDAR VERSIÓN VIGENTE ===
            log.info("Paso 8: Validando version del complemento de pago");
            validationService.validatePaymentVersion("2.0", "P");
            log.info("Version del complemento validada: 2.0");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_VERSION.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Version Pagos 2.0 validada correctamente",
                    "Version: 2.0, TipoComprobante: P", null, null);

            // === PASO 9: VALIDAR CON SAT VÍA MULTIPAC ===
            // TODO: Implementar validación SAT mediante multipac (Detecno) cuando esté disponible
            log.info("Paso 9: Validacion SAT omitida (pendiente de implementar via multipac)");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_VALIDAR_SAT.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Validacion SAT omitida (pendiente de implementar via multipac)",
                    "Este paso se habilitara cuando se integre el servicio multipac (Detecno)", null, null);

            // === PASO 10: REGISTRAR EN BASE DE DATOS ===
            log.info("Paso 10: Registrando complemento de pago en base de datos");
            PaymentRegistrationResponse response = savePaymentToDatabase(
                    parsedXml,
                    request,
                    fileName,
                    xmlContent,
                    fiscalUuid
            );
            log.info("Complemento persistido exitosamente. UUID: {}", response.getPaymentsUuid());
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_PERSISTIR_BD.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Complemento de pago persistido exitosamente",
                    "Payments UUID: " + response.getPaymentsUuid(),
                    Map.of("paymentsUuid", response.getPaymentsUuid().toString(),
                            "fiscalUuid", fiscalUuid.toString()), null);

            // === PASO 11: REGISTRAR ARCHIVO PROCESADO ===
            log.info("Paso 11: Registrando archivo procesado");
            saveFileRegistry(fileName, response.getPaymentsUuid(), "SUCCESS", null, null, request);
            log.info("Registro de archivo completado");
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_REGISTRO_ARCHIVO.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Archivo procesado registrado exitosamente",
                    "Archivo: " + fileName + ", Status: SUCCESS", null, null);

            long duration = System.currentTimeMillis() - startTime;
            log.info("========================================");
            log.info("COMPLEMENTO DE PAGO REGISTRADO EXITOSAMENTE");
            log.info("========================================");
            log.info("UUID: {}", response.getPaymentsUuid());
            log.info("Duracion: {} ms", duration);

            // Registrar response exitoso en bitácora (STM-272)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_REGISTRO_RESPONSE.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), false, "Registro de complemento de pago completado exitosamente",
                    "UUID: " + response.getPaymentsUuid() + ", Serie: " + response.getSerie() + ", Folio: " + response.getFolio(),
                    Map.of("paymentsUuid", response.getPaymentsUuid().toString(),
                            "serie", String.valueOf(response.getSerie()),
                            "folio", String.valueOf(response.getFolio()),
                            "rfcEmisor", String.valueOf(response.getRfcEmisor()),
                            "rfcReceptor", String.valueOf(response.getRfcReceptor())), duration);

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error registrando complemento de pago: {}", e.getMessage(), e);
            log.error("========================================");
            log.error("REGISTRO FALLIDO - ERROR");
            log.error("========================================");

            // Registrar error en bitácora (STM-272)
            auditoriaApiService.logActivity(idTransaccion, AuditAction.PAGO_REGISTRO_ERROR.getCode(), SERVICE_NAME,
                    String.valueOf(request.getIdUsuario()), true, "Error en registro de complemento de pago: " + e.getMessage(),
                    e.getClass().getName() + ": " + e.getMessage(),
                    Map.of("idProveedor", request.getIdProveedor(), "fileName", fileName), duration);

            // Registrar archivo con error
            try {
                saveFileRegistry(fileName, null, "FAILED", "700", e.getMessage(), request);
            } catch (Exception ex) {
                log.error("Error guardando registro de archivo fallido", ex);
            }

            messageCatalog.throwError(FiscalMessageCode.ERR003, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí por el throw anterior
    }

    // ========== MÉTODOS PRIVADOS ==========

    /**
     * Lee el contenido del archivo XML.
     */
    private String readXmlFile(PaymentRegistrationRequest request) {
        try {
            StringBuilder xmlBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(request.getXmlFile().getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    xmlBuilder.append(line);
                }
            }
            return xmlBuilder.toString();

        } catch (Exception e) {
            log.error("Error leyendo archivo XML", e);
            messageCatalog.throwError(FiscalMessageCode.ERR005, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí por el throw anterior
    }

    /**
     * Valida el complemento de pago con el SAT vía multipac (Detecno).
     */
    private ResponseEntity<Object> validateWithSat(String xmlContent, PaymentRegistrationRequest request) {
        try {
            log.debug("Iniciando validación con SAT vía multipac");

            // Obtener PAC configurado (Detecno)
            PacCatalogDto pac = pacCatalogService.findById(1L)
                    .orElseThrow(() -> new FiscalException(FiscalMessageCode.ERR003));

            // Convertir XML a Document
            Document xmlDocument = xmlParserService.xmlStringToDocument(xmlContent);

            // Validar con servicio multipac
            ResponseEntity<Object> response = pacService.validaXml(
                    xmlDocument,
                    xmlContent,
                    pac,
                    null,  // strXmlJson - no es necesario por ahora
                    null   // xmlFiscalDto - se extrae internamente
            );

            // Verificar respuesta
            if (response.getStatusCode().is2xxSuccessful()) {
                Object body = response.getBody();
                if (body instanceof ResponseValidationDetecnoDto) {
                    ResponseValidationDetecnoDto validationDto = (ResponseValidationDetecnoDto) body;

                    // Verificar que el status sea válido
                    if (!"Vigente".equalsIgnoreCase(validationDto.getStatus()) &&
                        !"0".equals(validationDto.getErrorCode())) {

                        String additionalInfo = String.format("Código SAT: %s, Mensaje: %s",
                                validationDto.getErrorCode(),
                                validationDto.getErrorMessage());
                        messageCatalog.throwError(FiscalMessageCode.BUS030, additionalInfo);
                    }
                }
                log.info("Complemento válido ante el SAT");
                return response;
            } else {
                String additionalInfo = "Status code: " + response.getStatusCode();
                messageCatalog.throwError(FiscalMessageCode.BUS030, additionalInfo);
            }

        } catch (Exception e) {
            log.error("Error validando con SAT", e);
            messageCatalog.throwError(FiscalMessageCode.BUS030, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí por los throws anteriores
    }

    /**
     * Guarda el complemento de pago en la base de datos.
     */
    private PaymentRegistrationResponse savePaymentToDatabase(
            ParsedPaymentXmlDto parsedXml,
            PaymentRegistrationRequest request,
            String fileName,
            String xmlContent,
            UUID fiscalUuid) {

        log.info("Guardando complemento de pago en base de datos");

        try {
            // 1. Guardar/obtener Emisor
            IssuerEntity issuer = getOrCreateIssuer(parsedXml);

            // 2. Guardar/obtener Receptor
            ReceiverEntity receiver = getOrCreateReceiver(parsedXml);

            // 3. Crear y guardar Payments
            PaymentsEntity payments = createPaymentsEntity(parsedXml, issuer, receiver, xmlContent, fiscalUuid, request);
            payments = paymentsRepository.save(payments);
            log.info("Payments guardado con UUID: {}", payments.getPaymentsUuid());

            // 4. Dispersar pagos y documentos relacionados
            savePaymentsAndRelatedDocuments(payments, parsedXml, request);

            // 5. Crear y guardar Addenda
            createAndSaveAddenda(payments, parsedXml, request);

            // 6. Crear respuesta
            return PaymentRegistrationResponse.builder()
                    .paymentsUuid(payments.getPaymentsUuid())
                    .fileName(fileName)
                    .processingStatus("SUCCESS")
                    .responseCode("200")
                    .message("Complemento de pago registrado exitosamente")
                    .folio(payments.getFolio())
                    .serie(payments.getSeries())
                    .rfcEmisor(parsedXml.getEmisorRfc())
                    .rfcReceptor(parsedXml.getReceptorRfc())
                    .fechaRegistro(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error guardando en base de datos", e);
            messageCatalog.throwError(FiscalMessageCode.ERR003, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí por el throw anterior
    }

    /**
     * Obtiene o crea el emisor.
     * Busca por RFC, si no existe lo crea.
     */
    private IssuerEntity getOrCreateIssuer(ParsedPaymentXmlDto parsedXml) {
        return issuerService.getOrCreate(
                parsedXml.getEmisorRfc(),
                parsedXml.getEmisorNombre(),
                parsedXml.getEmisorRegimenFiscal()
        );
    }

    /**
     * Obtiene o crea el receptor.
     * Busca por RFC, si no existe lo crea.
     */
    private ReceiverEntity getOrCreateReceiver(ParsedPaymentXmlDto parsedXml) {
        return receiverService.getOrCreate(
                parsedXml.getReceptorRfc(),
                parsedXml.getReceptorNombre(),
                parsedXml.getReceptorRegimenFiscal()
        );
    }

    /**
     * Crea la entidad Payments.
     */
    private PaymentsEntity createPaymentsEntity(
            ParsedPaymentXmlDto parsedXml,
            IssuerEntity issuer,
            ReceiverEntity receiver,
            String xmlContent,
            UUID fiscalUuid,
            PaymentRegistrationRequest request) {

        PaymentsEntity payments = new PaymentsEntity();
        payments.setFiscalUuid(fiscalUuid);  // UUID del TimbreFiscalDigital del SAT
        payments.setVersion(java.math.BigDecimal.valueOf(2.0));
        payments.setPaymentDate(LocalDate.parse(parsedXml.getFecha().substring(0, 10)));
        payments.setIssuerUuid(issuer.getIssuerUuid());
        payments.setReceiverUuid(receiver.getReceiverUuid());
        payments.setFolio(parsedXml.getFolio());
        payments.setSeries(parsedXml.getSerie());
        payments.setXmlContent(xmlContent);
        payments.setStatus(1); // Vigente
        payments.setCertificationDate(parsedXml.getTimbreFiscalDigital() != null ?
                LocalDateTime.parse(parsedXml.getTimbreFiscalDigital().getFechaTimbrado().substring(0, 19)) : null);
        payments.setCreatedBy(request.getIdUsuario());

        return payments;
    }

    /**
     * Dispersa los pagos individuales y sus documentos relacionados.
     * Parsea cada nodo Pago del complemento y lo persiste en la tabla payment,
     * luego cada DoctoRelacionado en la tabla related_documents.
     */
    private void savePaymentsAndRelatedDocuments(
            PaymentsEntity payments,
            ParsedPaymentXmlDto parsedXml,
            PaymentRegistrationRequest request) {

        if (parsedXml.getPagos() == null || parsedXml.getPagos().getPagos() == null) {
            log.warn("No se encontraron pagos para dispersar");
            return;
        }

        for (PagoDto pagoDto : parsedXml.getPagos().getPagos()) {
            // Crear PaymentEntity
            PaymentEntity payment = new PaymentEntity();
            payment.setPaymentsUuid(payments.getPaymentsUuid());
            payment.setPaymentDate(LocalDate.parse(pagoDto.getFechaPago().substring(0, 10)));
            payment.setPaymentMethod(pagoDto.getFormaDePagoP());
            payment.setCurrency(pagoDto.getMonedaP() != null ? pagoDto.getMonedaP() : "MXN");
            payment.setAmount(new BigDecimal(pagoDto.getMonto()));
            payment.setOperationNumber(pagoDto.getNumOperacion());
            payment.setExchangeRate(pagoDto.getTipoCambioP() != null && !pagoDto.getTipoCambioP().isEmpty()
                    ? new BigDecimal(pagoDto.getTipoCambioP()) : BigDecimal.ONE);
            payment.setPayerBankRfc(pagoDto.getRfcEmisorCtaOrd());
            payment.setPayerAccount(pagoDto.getCtaOrdenante());
            payment.setBeneficiaryBankRfc(pagoDto.getRfcEmisorCtaBen());
            payment.setBeneficiaryAccount(pagoDto.getCtaBeneficiario());
            payment.setCreatedBy(request.getIdUsuario());

            payment = paymentRepository.save(payment);
            log.debug("Payment guardado: UUID={}, monto={}", payment.getPaymentUuid(), payment.getAmount());

            // Dispersar documentos relacionados de este pago
            if (pagoDto.getDoctosRelacionados() != null) {
                for (DoctoRelacionadoDto docto : pagoDto.getDoctosRelacionados()) {
                    RelatedDocumentsEntity relDoc = new RelatedDocumentsEntity();
                    relDoc.setPaymentUuid(payment.getPaymentUuid());
                    relDoc.setDocumentUuid(UUID.fromString(docto.getIdDocumento()));
                    relDoc.setAmountPaid(new BigDecimal(docto.getImpPagado()));
                    relDoc.setPreviousBalance(new BigDecimal(docto.getImpSaldoAnt()));
                    relDoc.setRemainingBalance(new BigDecimal(docto.getImpSaldoInsoluto()));
                    relDoc.setInstallmentNumber(docto.getNumParcialidad() != null && !docto.getNumParcialidad().isEmpty()
                            ? new BigDecimal(docto.getNumParcialidad()) : null);
                    relDoc.setSeries(docto.getSerie());
                    relDoc.setFolio(docto.getFolio());
                    relDoc.setCurrency(docto.getMonedaDR() != null ? docto.getMonedaDR() : "MXN");
                    relDoc.setExchangeRate(docto.getEquivalenciaDR() != null && !docto.getEquivalenciaDR().isEmpty()
                            ? new BigDecimal(docto.getEquivalenciaDR()) : BigDecimal.ONE);
                    relDoc.setCreatedBy(request.getIdUsuario());

                    relatedDocumentsRepository.save(relDoc);
                    log.debug("RelatedDocument guardado: docUuid={}, pagado={}, saldo={}",
                            docto.getIdDocumento(), docto.getImpPagado(), docto.getImpSaldoInsoluto());
                }
            }
        }

        log.info("Dispersión completada: {} pagos procesados", parsedXml.getPagos().getPagos().size());
    }

    /**
     * Crea y guarda la addenda.
     */
    private void createAndSaveAddenda(
            PaymentsEntity payments,
            ParsedPaymentXmlDto parsedXml,
            PaymentRegistrationRequest request) {

        AddendumEntity addendum = new AddendumEntity();
        addendum.setPaymentsUuid(payments.getPaymentsUuid());
        addendum.setSupplierNumber(java.math.BigDecimal.valueOf(request.getIdProveedor()));
        addendum.setReceptionNumber(null);
        addendum.setPurchaseOrderNumber(null);
        addendum.setShippingGuideNumber(null);
        addendum.setAddendaType(request.getTipoAddenda()); // 5
        addendum.setSupplierType(request.getTipoProveedor());
        addendum.setUserId(request.getIdUsuario());
        addendum.setAddendumContent(parsedXml.getAddendaContent());
        addendum.setUpdateDate(null);
        addendum.setCreatedBy(request.getIdUsuario());

        addendumRepository.save(addendum);
        log.debug("Addenda guardada");
    }

    /**
     * Guarda el registro del archivo procesado.
     */
    private void saveFileRegistry(
            String fileName,
            UUID paymentsUuid,
            String processingStatus,
            String errorCode,
            String errorMessage,
            PaymentRegistrationRequest request) {

        PaymentFileRegistryEntity fileRegistry = new PaymentFileRegistryEntity();
        fileRegistry.setFileName(fileName);
        fileRegistry.setPaymentsUuid(paymentsUuid);
        fileRegistry.setProcessingStatus(processingStatus);
        fileRegistry.setErrorCode(errorCode);
        fileRegistry.setErrorMessage(errorMessage);
        fileRegistry.setSupplierId(request.getIdProveedor());
        fileRegistry.setUserId(request.getIdUsuario());
        fileRegistry.setCreatedBy(request.getIdUsuario());

        fileRegistryRepository.save(fileRegistry);
        log.debug("Registro de archivo guardado: {}", fileName);
    }
}
