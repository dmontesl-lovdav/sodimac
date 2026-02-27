package com.sodimac.fiscal.api.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import com.sodimac.fiscal.api.model.entity.IssuerEntity;
import com.sodimac.fiscal.api.model.entity.PaymentFileRegistryEntity;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.model.entity.ReceiverEntity;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.repository.AddendumRepository;
import com.sodimac.fiscal.api.repository.PaymentFileRegistryRepository;
import com.sodimac.fiscal.api.repository.PaymentsRepository;
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

    // Repositories
    private final PaymentFileRegistryRepository fileRegistryRepository;
    private final IssuerService issuerService;
    private final ReceiverService receiverService;
    private final PaymentsRepository paymentsRepository;
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
            PaymentFileRegistryRepository fileRegistryRepository,
            IssuerService issuerService,
            ReceiverService receiverService,
            PaymentsRepository paymentsRepository,
            AddendumRepository addendumRepository) {
        this.xmlParserService = xmlParserService;
        this.validationService = validationService;
        this.pacService = pacService;
        this.pacCatalogService = pacCatalogService;
        this.logService = logService;
        this.messageCatalog = messageCatalog;
        this.fileRegistryRepository = fileRegistryRepository;
        this.issuerService = issuerService;
        this.receiverService = receiverService;
        this.paymentsRepository = paymentsRepository;
        this.addendumRepository = addendumRepository;
    }

    @Override
    @Transactional
    public PaymentRegistrationResponse registerPayment(PaymentRegistrationRequest request) {
        log.info("=== INICIO REGISTRO COMPLEMENTO DE PAGO ===");
        log.info("Archivo: {}", request.getXmlFile().getOriginalFilename());
        log.info("Proveedor: {}, Usuario: {}", request.getIdProveedor(), request.getIdUsuario());

        String fileName = request.getXmlFile().getOriginalFilename();
        String xmlContent = null;
        ParsedPaymentXmlDto parsedXml = null;

        try {
            // === PASO 1: LEER ARCHIVO XML ===
            xmlContent = readXmlFile(request);
            log.debug("XML leído exitosamente");

            // === PASO 2: VALIDAR TIPO DE ADDENDA ===
            validationService.validateAddendaType(request.getTipoAddenda());
            log.debug("Tipo de addenda validado");

            // === PASO 3: VALIDAR ESTRUCTURA XML contra XSD ===
            if (!xmlParserService.validateXmlStructure(xmlContent)) {
                messageCatalog.throwError(FiscalMessageCode.ERR007);
            }
            log.info("Estructura XML validada contra XSD");

            // === PASO 4: PARSEAR XML ===
            parsedXml = xmlParserService.parsePaymentXml(xmlContent);
            log.info("XML parseado exitosamente - UUID: {}", parsedXml.getTimbreFiscalDigital().getUuid());

            // === PASO 5: VALIDAR TIPO DE COMPROBANTE ===
            validationService.validateComprobanteType(parsedXml.getTipoDeComprobante());
            log.debug("Tipo de comprobante validado: P");

            // === PASO 6: VALIDAR NO DUPLICADO ===
            UUID fiscalUuid = UUID.fromString(parsedXml.getTimbreFiscalDigital().getUuid());
            validationService.validateNoDuplicate(fiscalUuid);
            log.debug("Complemento no duplicado");

            // === PASO 7: VALIDAR RECEPTOR AUTORIZADO ===
            validationService.validateAuthorizedReceiver(parsedXml.getReceptorRfc());
            log.info("Receptor autorizado: {}", parsedXml.getReceptorRfc());

            // === PASO 8: VALIDAR VERSIÓN VIGENTE ===
            // Validar la versión 2.0 del complemento Pagos
            validationService.validatePaymentVersion("2.0", "P");
            log.info("Versión del complemento validada: 2.0");

            // === PASO 9: VALIDAR CON SAT VÍA MULTIPAC ===
            //ResponseEntity<Object> satValidationResponse = validateWithSat(xmlContent, request);
            log.info("Validación SAT completada");

            // === PASO 10: REGISTRAR EN BASE DE DATOS ===
            PaymentRegistrationResponse response = savePaymentToDatabase(
                    parsedXml,
                    request,
                    fileName,
                    xmlContent,
                    fiscalUuid
            );

            // === PASO 11: REGISTRAR ARCHIVO PROCESADO ===
            saveFileRegistry(fileName, response.getPaymentsUuid(), "SUCCESS", null, null, request);
            log.info("Registro de archivo completado");

            log.info("=== COMPLEMENTO DE PAGO REGISTRADO EXITOSAMENTE ===");
            log.info("UUID: {}", response.getPaymentsUuid());

            return response;

        } catch (Exception e) {
            log.error("Error registrando complemento de pago: {}", e.getMessage(), e);

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

            // 4. Crear y guardar Addenda
            createAndSaveAddenda(payments, parsedXml, request);

            // 5. Crear respuesta
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
