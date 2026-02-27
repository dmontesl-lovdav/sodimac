package com.sodimac.fiscal.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.fiscal.api.model.dto.InvoiceRegistrationResponse;
import com.sodimac.fiscal.api.model.entity.*;
import com.sodimac.fiscal.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para el controlador de registro de facturas y notas de crédito (STM-337).
 *
 * Estas pruebas validan el flujo completo HTTP incluyendo:
 * - Llamadas REST al endpoint POST /api/invoices/register
 * - Validaciones de negocio (RFC receptor, versión CFDI, addenda)
 * - Persistencia en base de datos (invoice, issuer, receiver, addenda)
 * - Códigos de respuesta HTTP y estructura JSON de respuesta
 *
 * DESHABILITADO: Pruebas se ejecutan desde Postman Collection.
 * Ver: postman/STM-337-Invoice-Registration.postman_collection.json
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.junit.jupiter.api.Disabled("Deshabilitado - usar Postman collection para pruebas")
public class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private IssuerRepository issuerRepository;

    @Autowired
    private ReceiverRepository receiverRepository;

    @Autowired
    private AuthorizedReceiverCatalogRepository receiverCatalogRepository;

    @Autowired
    private VersionCatalogRepository versionCatalogRepository;

    @Autowired
    private AddendumRepository addendumRepository;

    @BeforeEach
    void setUp() {
        // Limpiar solo los datos de transacción (no los catálogos)
        addendumRepository.deleteAll();
        invoiceRepository.deleteAll();

        // Los datos de catálogo deben existir en la base de datos:
        // - authorized_receiver_catalog debe tener RFC CSD161207R2A
        // - version_catalog debe tener versiones 4.0 para tipos I y E
    }

    // ==================================================================================
    // TESTS PARA FACTURA (TIPO I)
    // ==================================================================================

    @Test
    @DisplayName("Controller: Debe registrar factura exitosamente sin addenda y retornar HTTP 200 con BUS1002")
    void testRegisterFacturaSinAddenda() throws Exception {
        // Given: XML de factura sin addenda Sodimac
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso2s.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso2.xml",
                "application/xml",
                xmlContent
        );

        // When: Se hace POST al endpoint
        MvcResult result = mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("BUS1002"))
                .andExpect(jsonPath("$.message").value("Factura registrada exitosamente - Pendiente de Addenda"))
                .andExpect(jsonPath("$.pendingAddenda").value(true))
                .andExpect(jsonPath("$.hasAddenda").value(false))
                .andExpect(jsonPath("$.documentType").value("I"))
                .andExpect(jsonPath("$.invoiceUuid").exists())
                .andExpect(jsonPath("$.fiscalUuid").exists())
                .andReturn();

        // Then: Verifica respuesta y persistencia
        String responseBody = result.getResponse().getContentAsString();
        InvoiceRegistrationResponse response = objectMapper.readValue(responseBody, InvoiceRegistrationResponse.class);

        // Verifica persistencia en BD
        Optional<InvoiceEntity> savedInvoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid());
        assertThat(savedInvoice).isPresent();
        assertThat(savedInvoice.get().getDocumentType()).isEqualTo("I");
        assertThat(savedInvoice.get().getSeries()).isEqualTo("FF");
        assertThat(savedInvoice.get().getFolio()).isEqualTo("027720");
        assertThat(savedInvoice.get().getTotal()).isEqualByComparingTo(new BigDecimal("31360.00"));
    }

    @Test
    @DisplayName("Controller: Debe rechazar factura con RFC receptor no autorizado y retornar HTTP 400 con BUS2002")
    void testRechazarFacturaRfcNoAutorizado() throws Exception {
        // Given: Eliminar RFC autorizado del catálogo
        receiverCatalogRepository.deleteAll();

        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // When & Then: Se hace POST al endpoint
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS2002"))
                .andExpect(jsonPath("$.message").value("El RFC CSD161207R2A no acepta documentos fiscales"));

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("Controller: Debe rechazar factura duplicada por UUID y retornar HTTP 400 con BUS2601")
    void testRechazarFacturaDuplicada() throws Exception {
        // Given: Registrar una factura primero
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // Primer registro exitoso
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk());

        // When: Intentar registrar la misma factura nuevamente
        MockMultipartFile fileDuplicate = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        mockMvc.perform(multipart("/api/invoices/register")
                        .file(fileDuplicate))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS2601"))
                .andExpect(jsonPath("$.message").value("El comprobante fiscal con UUID EC9E24C1-18EE-D34E-BFDE-48A4D9103027 ya se encuentra registrado"));

        // Verifica que solo hay 1 registro en BD
        assertThat(invoiceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller: Debe rechazar factura con versión CFDI no vigente y retornar HTTP 400 con BUS2202")
    void testRechazarFacturaVersionNoVigente() throws Exception {
        // Given: Eliminar la versión vigente
        versionCatalogRepository.deleteAll();

        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS2202"))
                .andExpect(jsonPath("$.message").value("La version 4.0 no es vigente. Actualice a version 4.0"));

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("Controller: Debe validar que se guardan emisor y receptor correctamente")
    void testGuardarEmisorYReceptor() throws Exception {
        // Given
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // When
        MvcResult result = mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        InvoiceRegistrationResponse response = objectMapper.readValue(responseBody, InvoiceRegistrationResponse.class);

        // Then: Verifica que se guardó el emisor
        Optional<IssuerEntity> issuer = issuerRepository.findByRfc("HTR060210FJ9");
        assertThat(issuer).isPresent();
        assertThat(issuer.get().getName()).isEqualTo("H.H. TRANSPORTES");
        assertThat(issuer.get().getTaxRegime()).isEqualTo("624");

        // Verifica que se guardó el receptor
        Optional<ReceiverEntity> receiver = receiverRepository.findByRfc("CSD161207R2A");
        assertThat(receiver).isPresent();
        assertThat(receiver.get().getName()).isEqualTo("COMERCIALIZADORA SDMHC");
        assertThat(receiver.get().getTaxRegime()).isEqualTo("601");

        // Verifica que la factura está vinculada correctamente
        InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid()).get();
        assertThat(invoice.getIssuerUuid()).isEqualTo(issuer.get().getIssuerUuid());
        assertThat(invoice.getReceiverUuid()).isEqualTo(receiver.get().getReceiverUuid());
    }

    @Test
    @DisplayName("Controller: Debe rechazar archivo vacío con HTTP 400")
    void testRechazarArchivoVacio() throws Exception {
        // Given: Archivo vacío
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vacio.xml",
                "application/xml",
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FISCAL-ERR-100"))
                .andExpect(jsonPath("$.message").value("El archivo esta vacio"));
    }

    @Test
    @DisplayName("Controller: Debe rechazar archivo con extensión inválida con HTTP 400")
    void testRechazarArchivoExtensionInvalida() throws Exception {
        // Given: Archivo con extensión incorrecta
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "factura.pdf",
                "application/pdf",
                "contenido".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FISCAL-ERR-101"))
                .andExpect(jsonPath("$.message").value("El archivo debe tener extension .xml"));
    }

    @Test
    @DisplayName("Controller: Debe procesar correctamente los totales y montos de la factura")
    void testValidarTotalesYMontos() throws Exception {
        // Given
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // When
        MvcResult result = mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        InvoiceRegistrationResponse response = objectMapper.readValue(responseBody, InvoiceRegistrationResponse.class);

        // Then: Verifica que los montos se guardaron correctamente
        InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid()).get();

        assertThat(invoice.getTotal()).isEqualByComparingTo(new BigDecimal("31360.00"));
        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("28000.00"));
        assertThat(invoice.getCurrency()).isEqualTo("MXN");
        assertThat(invoice.getExchangeRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(invoice.getPaymentMethod()).isEqualTo("PPD");
        assertThat(invoice.getPaymentForm()).isEqualTo("99");
    }

    @Test
    @DisplayName("Controller: Debe validar que no se guarda addenda cuando no existe estructura Sodimac")
    void testNoGuardarAddendaSinEstructuraSodimac() throws Exception {
        // Given: El archivo FacturaIngreso.xml tiene addenda pero no estructura Sodimac
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // When
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.pendingAddenda").value(true));

        // Then: No debe haber addenda guardada
        assertThat(addendumRepository.count()).isZero();
    }

    // ==================================================================================
    // TESTS PARA NOTA DE CRÉDITO (TIPO E)
    // ==================================================================================

    @Test
    @DisplayName("Controller NC: Debe registrar nota de crédito exitosamente sin addenda y retornar HTTP 200 con BUS1004")
    void testRegisterNotaCreditoSinAddenda() throws Exception {
        // Given: XML de nota de crédito sin addenda Sodimac
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/NotaCredito.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        // When: Se hace POST al endpoint
        MvcResult result = mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("BUS1004"))
                .andExpect(jsonPath("$.message").value("Nota de Crédito registrada exitosamente - Pendiente de Addenda"))
                .andExpect(jsonPath("$.pendingAddenda").value(true))
                .andExpect(jsonPath("$.hasAddenda").value(false))
                .andExpect(jsonPath("$.documentType").value("E"))
                .andExpect(jsonPath("$.invoiceUuid").exists())
                .andExpect(jsonPath("$.fiscalUuid").value("A12B2040-D8F8-4FCE-AB9D-37A636F8E59E"))
                .andReturn();

        // Then: Verifica persistencia
        String responseBody = result.getResponse().getContentAsString();
        InvoiceRegistrationResponse response = objectMapper.readValue(responseBody, InvoiceRegistrationResponse.class);

        Optional<InvoiceEntity> savedInvoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid());
        assertThat(savedInvoice).isPresent();
        assertThat(savedInvoice.get().getDocumentType()).isEqualTo("E");
        assertThat(savedInvoice.get().getSeries()).isEqualTo("MC");
        assertThat(savedInvoice.get().getFolio()).isEqualTo("00444871");
        assertThat(savedInvoice.get().getTotal()).isEqualByComparingTo(new BigDecimal("613.41"));
        assertThat(savedInvoice.get().getSubtotal()).isEqualByComparingTo(new BigDecimal("528.80"));
    }

    @Test
    @DisplayName("Controller NC: Debe rechazar nota de crédito con RFC receptor no autorizado y retornar HTTP 400 con BUS2002")
    void testRechazarNotaCreditoRfcNoAutorizado() throws Exception {
        // Given: Eliminar RFC autorizado del catálogo
        receiverCatalogRepository.deleteAll();

        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/NotaCredito.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS2002"))
                .andExpect(jsonPath("$.message").value("El RFC CSD161207R2A no acepta documentos fiscales"));

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("Controller NC: Debe rechazar nota de crédito duplicada por UUID y retornar HTTP 400 con BUS2601")
    void testRechazarNotaCreditoDuplicada() throws Exception {
        // Given: Registrar una nota de crédito primero
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/NotaCredito.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        // Primer registro exitoso
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BUS1004"));

        // When: Intentar registrar la misma nota de crédito nuevamente
        MockMultipartFile fileDuplicate = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        mockMvc.perform(multipart("/api/invoices/register")
                        .file(fileDuplicate))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS2601"))
                .andExpect(jsonPath("$.message").value("El comprobante fiscal con UUID A12B2040-D8F8-4FCE-AB9D-37A636F8E59E ya se encuentra registrado"));

        // Verifica que solo hay 1 registro en BD
        assertThat(invoiceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller NC: Debe rechazar nota de crédito con versión CFDI no vigente y retornar HTTP 400 con BUS2202")
    void testRechazarNotaCreditoVersionNoVigente() throws Exception {
        // Given: Eliminar la versión vigente para tipo E
        versionCatalogRepository.deleteAll();

        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/NotaCredito.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS2202"))
                .andExpect(jsonPath("$.message").value("La version 4.0 no es vigente. Actualice a version 4.0"));

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("Controller NC: Debe validar que se guardan emisor (SC JOHNSON) y receptor correctamente")
    void testGuardarEmisorYReceptorNotaCredito() throws Exception {
        // Given
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/NotaCredito.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        // When
        MvcResult result = mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        InvoiceRegistrationResponse response = objectMapper.readValue(responseBody, InvoiceRegistrationResponse.class);

        // Then: Verifica que se guardó el emisor (SC JOHNSON)
        Optional<IssuerEntity> issuer = issuerRepository.findByRfc("JOH120507FU9");
        assertThat(issuer).isPresent();
        assertThat(issuer.get().getName()).isEqualTo("SC JOHNSON");
        assertThat(issuer.get().getTaxRegime()).isEqualTo("601");

        // Verifica que se guardó el receptor
        Optional<ReceiverEntity> receiver = receiverRepository.findByRfc("CSD161207R2A");
        assertThat(receiver).isPresent();
        assertThat(receiver.get().getName()).isEqualTo("COMERCIALIZADORA SDMHC");
        assertThat(receiver.get().getTaxRegime()).isEqualTo("601");

        // Verifica que la NC está vinculada correctamente
        InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid()).get();
        assertThat(invoice.getIssuerUuid()).isEqualTo(issuer.get().getIssuerUuid());
        assertThat(invoice.getReceiverUuid()).isEqualTo(receiver.get().getReceiverUuid());
        assertThat(invoice.getDocumentType()).isEqualTo("E");
    }

    @Test
    @DisplayName("Controller NC: Debe procesar correctamente los totales y montos de la nota de crédito")
    void testValidarTotalesYMontosNotaCredito() throws Exception {
        // Given
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/NotaCredito.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        // When
        MvcResult result = mockMvc.perform(multipart("/api/invoices/register")
                        .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        InvoiceRegistrationResponse response = objectMapper.readValue(responseBody, InvoiceRegistrationResponse.class);

        // Then: Verifica que los montos se guardaron correctamente
        InvoiceEntity invoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid()).get();

        assertThat(invoice.getTotal()).isEqualByComparingTo(new BigDecimal("613.41"));
        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("528.80"));
        assertThat(invoice.getCurrency()).isEqualTo("MXN");
        assertThat(invoice.getExchangeRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(invoice.getPaymentMethod()).isEqualTo("PUE");
        assertThat(invoice.getPaymentForm()).isEqualTo("99");
        assertThat(invoice.getDocumentType()).isEqualTo("E");
    }
}
