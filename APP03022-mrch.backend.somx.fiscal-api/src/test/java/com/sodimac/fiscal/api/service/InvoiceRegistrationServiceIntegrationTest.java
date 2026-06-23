package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.InvoiceRegistrationResponse;
import com.sodimac.fiscal.api.model.entity.*;
import com.sodimac.fiscal.api.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración para el servicio de registro de facturas y notas de crédito (STM-337).
 *
 * Estas pruebas validan el flujo completo incluyendo:
 * - Lectura y parseo del XML
 * - Validaciones de negocio (RFC receptor, versión CFDI, addenda)
 * - Persistencia en base de datos (invoice, issuer, receiver, addenda)
 * - Respuestas con códigos de negocio (BUS1xxx, BUS2xxx)
 *
 * IMPORTANTE: Los tests NO usan @Transactional para simular comportamiento real (como Postman).
 * Los datos se persisten realmente en la BD y se limpian manualmente en @BeforeEach.
 *
 * DESHABILITADO: Pruebas se ejecutan desde Postman Collection.
 * Ver: postman/STM-337-Invoice-Registration.postman_collection.json
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@SpringBootTest
@org.junit.jupiter.api.Disabled("Deshabilitado - usar Postman collection para pruebas")
public class InvoiceRegistrationServiceIntegrationTest {

    @Autowired
    private InvoiceRegistrationService invoiceRegistrationService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private IssuerRepository issuerRepository;

    @Autowired
    private ReceiverRepository receiverRepository;

    @Autowired
    private VersionCatalogRepository versionCatalogRepository;

    @Autowired
    private AddendumRepository addendumRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Limpiar datos de prueba usando SQL nativo para evitar problemas con FK constraints
        // IMPORTANTE: Limpiar en orden inverso a las dependencias FK

        // Usar SQL nativo para DELETE en lugar de JPA deleteAll() para evitar lazy loading issues
        entityManager.createNativeQuery("DELETE FROM tenant_fiscal.addendum").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tenant_fiscal.related_cfdi").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tenant_fiscal.invoice").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tenant_fiscal.issuer").executeUpdate();

        // Limpiar cache de Hibernate para evitar referencias obsoletas
        entityManager.clear();

        // NO eliminar receiverCatalogRepository ni versionCatalogRepository (son catálogos del sistema)

        // Configurar datos necesarios para las pruebas
        setupTestData();
    }

    /**
     * Configura datos de catálogos necesarios para las pruebas
     */
    private void setupTestData() {
        // 1. RFC receptor autorizado: ya no se usa la tabla authorized_receiver_catalog; la
        // validación lee el catálogo CATRFCRECEPTOR (shared_catalogs). Test @Disabled.

        // 2. Crear versión CFDI vigente para facturas
        if (versionCatalogRepository.findByVersionAndDocumentTypeAndStatus(
                new BigDecimal("4.0"), "I", 1).isEmpty()) {
            VersionCatalogEntity version = new VersionCatalogEntity();
            version.setVersion(new BigDecimal("4.0"));
            version.setDocumentType("I");
            version.setStatus(1);
            version.setDescription("CFDI 4.0 - Factura");
            versionCatalogRepository.save(version);
        }

        // 3. Crear versión CFDI vigente para notas de crédito
        if (versionCatalogRepository.findByVersionAndDocumentTypeAndStatus(
                new BigDecimal("4.0"), "E", 1).isEmpty()) {
            VersionCatalogEntity versionNC = new VersionCatalogEntity();
            versionNC.setVersion(new BigDecimal("4.0"));
            versionNC.setDocumentType("E");
            versionNC.setStatus(1);
            versionNC.setDescription("CFDI 4.0 - Nota de Crédito");
            versionCatalogRepository.save(versionNC);
        }
    }

    @Test
    @DisplayName("Debe registrar factura exitosamente sin addenda y retornar BUS1002")
    void testRegisterFacturaSinAddenda() throws IOException {
        // Given: XML de factura sin addenda Sodimac
        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // When: Se registra la factura
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica respuesta de éxito pendiente de addenda
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo("BUS1002");
        assertThat(response.getMessage()).contains("Pendiente de Addenda");
        assertThat(response.isPendingAddenda()).isTrue();
        assertThat(response.isHasAddenda()).isFalse();
        assertThat(response.getInvoiceUuid()).isNotNull();
        assertThat(response.getFiscalUuid()).isNotNull();
        assertThat(response.getDocumentType()).isEqualTo("I");

        // Verifica persistencia en BD
        Optional<InvoiceEntity> savedInvoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid());
        assertThat(savedInvoice).isPresent();
        assertThat(savedInvoice.get().getDocumentType()).isEqualTo("I");
        assertThat(savedInvoice.get().getSeries()).isEqualTo("FF");
        assertThat(savedInvoice.get().getFolio()).isEqualTo("027720");
        assertThat(savedInvoice.get().getTotal()).isEqualByComparingTo(new BigDecimal("31360.00"));
    }

    @Test
    @DisplayName("Debe rechazar factura con RFC receptor no autorizado y retornar BUS2002")
    void testRechazarFacturaRfcNoAutorizado() throws IOException {
        // Given: RFC receptor no autorizado. La validación usa el catálogo CATRFCRECEPTOR.
        // (Test @Disabled; se valida por Postman.)

        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/FacturaIngreso.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "FacturaIngreso.xml",
                "application/xml",
                xmlContent
        );

        // When: Se intenta registrar la factura
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica rechazo por RFC no autorizado
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("BUS2002");
        assertThat(response.getMessage()).contains("no acepta documentos fiscales");

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("Debe rechazar factura duplicada por UUID y retornar BUS2601")
    void testRechazarFacturaDuplicada() throws IOException {
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
        InvoiceRegistrationResponse firstResponse = invoiceRegistrationService.registerInvoice(file);
        assertThat(firstResponse.isSuccess()).isTrue();

        // When: Intentar registrar la misma factura nuevamente
        InvoiceRegistrationResponse secondResponse = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica rechazo por duplicado
        assertThat(secondResponse).isNotNull();
        assertThat(secondResponse.isSuccess()).isFalse();
        assertThat(secondResponse.getCode()).isEqualTo("BUS2601");
        assertThat(secondResponse.getMessage()).contains("ya se encuentra registrado");

        // Verifica que solo hay 1 registro en BD
        assertThat(invoiceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe rechazar factura con versión CFDI no vigente y retornar BUS2202")
    void testRechazarFacturaVersionNoVigente() throws IOException {
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

        // When: Se intenta registrar la factura
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica rechazo por versión no vigente
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("BUS2202");
        assertThat(response.getMessage()).contains("no vigente");

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("Debe validar que se guardan emisor y receptor correctamente")
    void testGuardarEmisorYReceptor() throws IOException {
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
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

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
    @DisplayName("Debe rechazar archivo vacío con error técnico")
    void testRechazarArchivoVacio() {
        // Given: Archivo vacío
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vacio.xml",
                "application/xml",
                new byte[0]
        );

        // When
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).contains("ERR");
        assertThat(response.getMessage()).contains("vacio");
    }

    @Test
    @DisplayName("Debe procesar correctamente los totales y montos de la factura")
    void testValidarTotalesYMontos() throws IOException {
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
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

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
    @DisplayName("Debe validar que no se guarda addenda cuando no existe estructura Sodimac")
    void testNoGuardarAddendaSinEstructuraSodimac() throws IOException {
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
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica que NO se guardó addenda
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isPendingAddenda()).isTrue();

        // No debe haber addenda guardada para este invoice
        assertThat(addendumRepository.count()).isZero();
    }

    // ==================================================================================
    // TESTS PARA NOTA DE CRÉDITO (TIPO E)
    // ==================================================================================

    @Test
    @DisplayName("NC: Debe registrar nota de crédito exitosamente sin addenda y retornar BUS1004")
    void testRegisterNotaCreditoSinAddenda() throws IOException {
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

        // When: Se registra la nota de crédito
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica respuesta de éxito pendiente de addenda para NC
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo("BUS1004");
        assertThat(response.getMessage()).contains("Nota de Crédito registrada");
        assertThat(response.getMessage()).contains("Pendiente de Addenda");
        assertThat(response.isPendingAddenda()).isTrue();
        assertThat(response.isHasAddenda()).isFalse();
        assertThat(response.getInvoiceUuid()).isNotNull();
        assertThat(response.getFiscalUuid()).isEqualTo("A12B2040-D8F8-4FCE-AB9D-37A636F8E59E");
        assertThat(response.getDocumentType()).isEqualTo("E");

        // Verifica persistencia en BD
        Optional<InvoiceEntity> savedInvoice = invoiceRepository.findByFiscalUuid(response.getFiscalUuid());
        assertThat(savedInvoice).isPresent();
        assertThat(savedInvoice.get().getDocumentType()).isEqualTo("E");
        assertThat(savedInvoice.get().getSeries()).isEqualTo("MC");
        assertThat(savedInvoice.get().getFolio()).isEqualTo("00444871");
        assertThat(savedInvoice.get().getTotal()).isEqualByComparingTo(new BigDecimal("613.41"));
        assertThat(savedInvoice.get().getSubtotal()).isEqualByComparingTo(new BigDecimal("528.80"));
    }

    @Test
    @DisplayName("NC: Debe rechazar nota de crédito con RFC receptor no autorizado y retornar BUS2002")
    void testRechazarNotaCreditoRfcNoAutorizado() throws IOException {
        // Given: RFC receptor no autorizado (catálogo CATRFCRECEPTOR). Test @Disabled.

        byte[] xmlContent = Files.readAllBytes(
                Paths.get("src/main/resources/invoice/NotaCredito.xml")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "NotaCredito.xml",
                "application/xml",
                xmlContent
        );

        // When: Se intenta registrar la nota de crédito
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica rechazo por RFC no autorizado
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("BUS2002");
        assertThat(response.getMessage()).contains("no acepta documentos fiscales");

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("NC: Debe rechazar nota de crédito duplicada por UUID y retornar BUS2601")
    void testRechazarNotaCreditoDuplicada() throws IOException {
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
        InvoiceRegistrationResponse firstResponse = invoiceRegistrationService.registerInvoice(file);
        assertThat(firstResponse.isSuccess()).isTrue();
        assertThat(firstResponse.getCode()).isEqualTo("BUS1004");

        // When: Intentar registrar la misma nota de crédito nuevamente
        InvoiceRegistrationResponse secondResponse = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica rechazo por duplicado
        assertThat(secondResponse).isNotNull();
        assertThat(secondResponse.isSuccess()).isFalse();
        assertThat(secondResponse.getCode()).isEqualTo("BUS2601");
        assertThat(secondResponse.getMessage()).contains("ya se encuentra registrado");

        // Verifica que solo hay 1 registro en BD
        assertThat(invoiceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("NC: Debe rechazar nota de crédito con versión CFDI no vigente y retornar BUS2202")
    void testRechazarNotaCreditoVersionNoVigente() throws IOException {
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

        // When: Se intenta registrar la nota de crédito
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

        // Then: Verifica rechazo por versión no vigente
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("BUS2202");
        assertThat(response.getMessage()).contains("no vigente");

        // Verifica que NO se guardó en BD
        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    @DisplayName("NC: Debe validar que se guardan emisor (SC JOHNSON) y receptor correctamente")
    void testGuardarEmisorYReceptorNotaCredito() throws IOException {
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
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

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
    @DisplayName("NC: Debe procesar correctamente los totales y montos de la nota de crédito")
    void testValidarTotalesYMontosNotaCredito() throws IOException {
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
        InvoiceRegistrationResponse response = invoiceRegistrationService.registerInvoice(file);

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
