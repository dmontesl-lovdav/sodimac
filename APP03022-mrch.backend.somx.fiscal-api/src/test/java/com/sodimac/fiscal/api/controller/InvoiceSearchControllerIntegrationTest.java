package com.sodimac.fiscal.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.fiscal.api.model.dto.InvoiceSearchRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceSearchResponse;
import com.sodimac.fiscal.api.model.entity.*;
import com.sodimac.fiscal.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para búsqueda de facturas y notas de crédito (STM-338).
 *
 * Estas pruebas validan el flujo completo de búsqueda incluyendo:
 * - Llamadas REST al endpoint POST /api/invoices/search
 * - Filtros obligatorios y opcionales
 * - Paginación y ordenamiento
 * - Estructura de respuesta con Comprobante, Emisor, Receptor, Addenda
 * - JPA Criteria API (Specifications) para consultas dinámicas
 *
 * DESHABILITADO: Se habilitará cuando sea necesario para STM-338.
 * Por ahora usar Postman collection para pruebas.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.junit.jupiter.api.Disabled("Deshabilitado - habilitar cuando se pruebe STM-338")
public class InvoiceSearchControllerIntegrationTest {

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
    private AddendumRepository addendumRepository;

    // Test data
    private IssuerEntity testIssuer1;
    private IssuerEntity testIssuer2;
    private ReceiverEntity testReceiver;
    private InvoiceEntity testInvoice1;
    private InvoiceEntity testInvoice2;
    private InvoiceEntity testInvoice3;

    @BeforeEach
    void setUp() {
        // Limpiar datos de tests anteriores
        addendumRepository.deleteAll();
        invoiceRepository.deleteAll();
        issuerRepository.deleteAll();
        receiverRepository.deleteAll();

        // Crear datos de prueba para búsqueda
        setupTestData();
    }

    /**
     * Crea datos de prueba en la base de datos para los tests de búsqueda.
     */
    private void setupTestData() {
        // === CREAR EMISORES ===
        testIssuer1 = new IssuerEntity();
        testIssuer1.setRfc("AAA010101AAA");
        testIssuer1.setName("EMISOR DE PRUEBA 1 SA DE CV");
        testIssuer1.setTaxRegime("601");
        testIssuer1 = issuerRepository.save(testIssuer1);

        testIssuer2 = new IssuerEntity();
        testIssuer2.setRfc("BBB020202BBB");
        testIssuer2.setName("EMISOR DE PRUEBA 2 SA DE CV");
        testIssuer2.setTaxRegime("601");
        testIssuer2 = issuerRepository.save(testIssuer2);

        // === CREAR RECEPTOR ===
        testReceiver = new ReceiverEntity();
        testReceiver.setRfc("CSD161207R2A");
        testReceiver.setName("COMERCIALIZADORA SDMHC");
        testReceiver.setTaxRegime("601");
        testReceiver = receiverRepository.save(testReceiver);

        // === CREAR FACTURAS DE PRUEBA ===

        // Factura 1: Emisor AAA, Tipo I, Serie A, Fecha 2025-01-15
        testInvoice1 = new InvoiceEntity();
        testInvoice1.setFiscalUuid(UUID.randomUUID());
        testInvoice1.setDocumentType("I");
        testInvoice1.setSeries("A");
        testInvoice1.setFolio("12345");
        testInvoice1.setVersion(new BigDecimal("4.0"));
        testInvoice1.setTotal(new BigDecimal("10000.00")); // total = subtotal - discount (10000 - 0 = 10000)
        testInvoice1.setSubtotal(new BigDecimal("10000.00"));
        testInvoice1.setCurrency("MXN");
        testInvoice1.setPaymentMethod("PUE");
        testInvoice1.setIssueDate(LocalDate.of(2025, 1, 15));
        testInvoice1.setCertificationDate(LocalDateTime.of(2025, 1, 15, 10, 30));
        testInvoice1.setStatus(1);
        testInvoice1.setXmlContent("<xml>test1</xml>");
        testInvoice1.setIssuerUuid(testIssuer1.getIssuerUuid());
        testInvoice1.setReceiverUuid(testReceiver.getReceiverUuid());
        testInvoice1 = invoiceRepository.save(testInvoice1);

        // Factura 2: Emisor AAA, Tipo I, Serie B, Fecha 2025-01-20
        testInvoice2 = new InvoiceEntity();
        testInvoice2.setFiscalUuid(UUID.randomUUID());
        testInvoice2.setDocumentType("I");
        testInvoice2.setSeries("B");
        testInvoice2.setFolio("67890");
        testInvoice2.setVersion(new BigDecimal("4.0"));
        testInvoice2.setTotal(new BigDecimal("5000.00")); // total = subtotal - discount (5000 - 0 = 5000)
        testInvoice2.setSubtotal(new BigDecimal("5000.00"));
        testInvoice2.setCurrency("MXN");
        testInvoice2.setPaymentMethod("PPD");
        testInvoice2.setIssueDate(LocalDate.of(2025, 1, 20));
        testInvoice2.setCertificationDate(LocalDateTime.of(2025, 1, 20, 14, 15));
        testInvoice2.setStatus(2);
        testInvoice2.setXmlContent("<xml>test2</xml>");
        testInvoice2.setIssuerUuid(testIssuer1.getIssuerUuid());
        testInvoice2.setReceiverUuid(testReceiver.getReceiverUuid());
        testInvoice2 = invoiceRepository.save(testInvoice2);

        // Nota de Crédito 3: Emisor BBB, Tipo E, Serie C, Fecha 2025-01-25
        testInvoice3 = new InvoiceEntity();
        testInvoice3.setFiscalUuid(UUID.randomUUID());
        testInvoice3.setDocumentType("E");
        testInvoice3.setSeries("C");
        testInvoice3.setFolio("11111");
        testInvoice3.setVersion(new BigDecimal("4.0"));
        testInvoice3.setTotal(new BigDecimal("2000.00")); // total = subtotal - discount (2000 - 0 = 2000)
        testInvoice3.setSubtotal(new BigDecimal("2000.00"));
        testInvoice3.setCurrency("MXN");
        testInvoice3.setPaymentMethod("PUE");
        testInvoice3.setIssueDate(LocalDate.of(2025, 1, 25));
        testInvoice3.setCertificationDate(LocalDateTime.of(2025, 1, 25, 16, 45));
        testInvoice3.setStatus(1);
        testInvoice3.setXmlContent("<xml>test3</xml>");
        testInvoice3.setIssuerUuid(testIssuer2.getIssuerUuid());
        testInvoice3.setReceiverUuid(testReceiver.getReceiverUuid());
        testInvoice3 = invoiceRepository.save(testInvoice3);

        // Crear addenda para testInvoice1
        AddendumEntity addendum = new AddendumEntity();
        addendum.setInvoiceUuid(testInvoice1.getInvoiceUuid());
        addendum.setAddendaType(5);
        addendum.setAddendumContent("<addenda>test</addenda>");
        addendumRepository.save(addendum);
    }

    // ==================================================================================
    // TESTS DE BÚSQUEDA CON FILTROS OBLIGATORIOS
    // ==================================================================================

    @Test
    @DisplayName("Search: Debe buscar facturas con todos los filtros obligatorios y retornar HTTP 200")
    void testSearchWithMandatoryFiltersOnly() throws Exception {
        // Given: Request con solo filtros obligatorios
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2)) // Debe encontrar 2 facturas del emisor AAA
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andReturn();

        // Then: Validar estructura de respuesta
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(2);

        // Validar que ambas facturas son del emisor AAA y tipo I
        content.forEach(invoice -> {
            assertThat(invoice.get("documentType")).isEqualTo("I");
            assertThat(invoice.get("emisorRfc")).isEqualTo("AAA010101AAA");
        });
    }

    @Test
    @DisplayName("Search: Debe buscar notas de crédito (tipo E) y retornar HTTP 200")
    void testSearchCreditNotes() throws Exception {
        // Given: Request para buscar notas de crédito del emisor BBB
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("BBB020202BBB");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("E");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1)) // Debe encontrar 1 NC
                .andReturn();

        // Then: Validar que es nota de crédito
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("documentType")).isEqualTo("E");
        assertThat(content.get(0).get("emisorRfc")).isEqualTo("BBB020202BBB");
    }

    @Test
    @DisplayName("Search: Debe retornar lista vacía cuando no hay resultados")
    void testSearchWithNoResults() throws Exception {
        // Given: Request con RFC emisor que no existe
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("XXX999999XXX");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");

        // When: Se hace POST al endpoint de búsqueda
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    // ==================================================================================
    // TESTS CON FILTROS OPCIONALES
    // ==================================================================================

    @Test
    @DisplayName("Search: Debe filtrar por serie específica")
    void testSearchBySeries() throws Exception {
        // Given: Request con filtro de serie "A"
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setSerie("A");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1)) // Solo testInvoice1 tiene serie A
                .andReturn();

        // Then: Validar que el resultado tiene serie A
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("series")).isEqualTo("A");
        assertThat(content.get(0).get("folio")).isEqualTo("12345");
    }

    @Test
    @DisplayName("Search: Debe filtrar por folio específico")
    void testSearchByFolio() throws Exception {
        // Given: Request con filtro de folio "67890"
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setFolio("67890");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1)) // Solo testInvoice2 tiene folio 67890
                .andReturn();

        // Then: Validar que el resultado tiene folio 67890
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("folio")).isEqualTo("67890");
        assertThat(content.get(0).get("series")).isEqualTo("B");
    }

    @Test
    @DisplayName("Search: Debe filtrar por UUID fiscal específico")
    void testSearchByUuid() throws Exception {
        // Given: Request con filtro de UUID fiscal
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setUuid(testInvoice1.getFiscalUuid());

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andReturn();

        // Then: Validar que el resultado es la factura correcta
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("fiscalUuid")).isEqualTo(testInvoice1.getFiscalUuid().toString());
    }

    @Test
    @DisplayName("Search: Debe filtrar por RFC receptor")
    void testSearchByRfcReceptor() throws Exception {
        // Given: Request con filtro de RFC receptor
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setRfcReceptor("CSD161207R2A");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andReturn();

        // Then: Validar que todos los resultados tienen el receptor correcto
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        content.forEach(invoice -> {
            assertThat(invoice.get("receptorRfc")).isEqualTo("CSD161207R2A");
        });
    }

    // ==================================================================================
    // TESTS DE ESTRUCTURA DE RESPUESTA
    // ==================================================================================

    @Test
    @DisplayName("Search: Debe retornar estructura completa con Comprobante, Emisor, Receptor, Addenda")
    void testResponseStructure() throws Exception {
        // Given: Request de búsqueda
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setSerie("A");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                // Validar campos de Comprobante
                .andExpect(jsonPath("$.content[0].invoiceUuid").exists())
                .andExpect(jsonPath("$.content[0].fiscalUuid").exists())
                .andExpect(jsonPath("$.content[0].documentType").exists())
                .andExpect(jsonPath("$.content[0].series").exists())
                .andExpect(jsonPath("$.content[0].folio").exists())
                .andExpect(jsonPath("$.content[0].total").exists())
                .andExpect(jsonPath("$.content[0].subtotal").exists())
                .andExpect(jsonPath("$.content[0].currency").exists())
                .andExpect(jsonPath("$.content[0].status").exists())
                // Validar campos de Emisor
                .andExpect(jsonPath("$.content[0].emisorRfc").exists())
                .andExpect(jsonPath("$.content[0].emisorName").exists())
                .andExpect(jsonPath("$.content[0].emisorTaxRegime").exists())
                // Validar campos de Receptor
                .andExpect(jsonPath("$.content[0].receptorRfc").exists())
                .andExpect(jsonPath("$.content[0].receptorName").exists())
                .andExpect(jsonPath("$.content[0].receptorTaxRegime").exists())
                // Validar campos de Addenda
                .andExpect(jsonPath("$.content[0].hasAddenda").exists())
                .andReturn();

        // Then: Validar valores específicos de las 4 secciones
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        Map<String, Object> invoice = content.get(0);

        // COMPROBANTE
        assertThat(invoice.get("documentType")).isEqualTo("I");
        assertThat(invoice.get("series")).isEqualTo("A");
        assertThat(invoice.get("folio")).isEqualTo("12345");
        assertThat(invoice.get("currency")).isEqualTo("MXN");

        // EMISOR
        assertThat(invoice.get("emisorRfc")).isEqualTo("AAA010101AAA");
        assertThat(invoice.get("emisorName")).isEqualTo("EMISOR DE PRUEBA 1 SA DE CV");
        assertThat(invoice.get("emisorTaxRegime")).isEqualTo("601");

        // RECEPTOR
        assertThat(invoice.get("receptorRfc")).isEqualTo("CSD161207R2A");
        assertThat(invoice.get("receptorName")).isEqualTo("COMERCIALIZADORA SDMHC");
        assertThat(invoice.get("receptorTaxRegime")).isEqualTo("601");

        // ADDENDA (testInvoice1 tiene addenda)
        assertThat(invoice.get("hasAddenda")).isEqualTo(true);
        assertThat(invoice.get("addendaUuid")).isNotNull();
        assertThat(invoice.get("addendaType")).isEqualTo(5);
    }

    @Test
    @DisplayName("Search: Debe indicar cuando no hay addenda asociada")
    void testResponseWithoutAddenda() throws Exception {
        // Given: Request para buscar testInvoice2 que no tiene addenda
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setSerie("B");

        // When: Se hace POST al endpoint de búsqueda
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].hasAddenda").value(false))
                .andExpect(jsonPath("$.content[0].addendaUuid").isEmpty())
                .andReturn();
    }

    // ==================================================================================
    // TESTS DE PAGINACIÓN Y ORDENAMIENTO
    // ==================================================================================

    @Test
    @DisplayName("Search: Debe paginar resultados correctamente")
    void testPagination() throws Exception {
        // Given: Request con paginación de 1 elemento por página
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setPage(0);
        searchRequest.setSize(1);

        // When: Se hace POST al endpoint de búsqueda
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.number").value(0)); // Página actual
    }

    @Test
    @DisplayName("Search: Debe ordenar por createdAt DESC por defecto")
    void testDefaultSorting() throws Exception {
        // Given: Request sin especificar ordenamiento
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: Verificar que los resultados están ordenados por fecha DESC
        // (testInvoice2 fue creado después que testInvoice1, debe aparecer primero)
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(2);
        // El primer resultado debe ser testInvoice2 (creado más recientemente)
        assertThat(content.get(0).get("series")).isEqualTo("B");
        assertThat(content.get(1).get("series")).isEqualTo("A");
    }

    @Test
    @DisplayName("Search: Debe ordenar por total ASC cuando se especifica")
    void testCustomSorting() throws Exception {
        // Given: Request con ordenamiento por total ASC
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setSortBy("total");
        searchRequest.setSortDirection("ASC");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: Verificar que los resultados están ordenados por total ASC
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(2);
        // El primer resultado debe tener el total menor (5800.00)
        assertThat(content.get(0).get("total")).isEqualTo(5800.00);
        assertThat(content.get(1).get("total")).isEqualTo(11600.00);
    }

    // ==================================================================================
    // TESTS DE VALIDACIÓN
    // ==================================================================================

    @Test
    @DisplayName("Search: Debe retornar 400 cuando falta RFC Emisor")
    void testValidationMissingRfcEmisor() throws Exception {
        // Given: Request sin RFC Emisor
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        // searchRequest.setRfcEmisor("AAA010101AAA"); // OMITIDO
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");

        // When & Then: Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Search: Debe retornar 400 cuando falta Fecha Inicio")
    void testValidationMissingFechaInicio() throws Exception {
        // Given: Request sin Fecha Inicio
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        // searchRequest.setFechaInicioRecepcion(...); // OMITIDO
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");

        // When & Then: Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Search: Debe retornar 400 cuando falta Tipo Documento")
    void testValidationMissingTipoDocumento() throws Exception {
        // Given: Request sin Tipo Documento
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        // searchRequest.setTipoDocumento("I"); // OMITIDO

        // When & Then: Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================================================================================
    // TESTS DE RANGO DE FECHAS
    // ==================================================================================

    @Test
    @DisplayName("Search: Debe filtrar por rango de fechas correctamente")
    void testDateRangeFilter() throws Exception {
        // Given: Request con rango de fechas que solo incluye testInvoice1 (2025-01-15)
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 10));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 17));
        searchRequest.setTipoDocumento("I");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andReturn();

        // Then: Validar que solo se encontró testInvoice1
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("series")).isEqualTo("A");
    }

    @Test
    @DisplayName("Search: Debe retornar vacío cuando el rango de fechas no contiene documentos")
    void testDateRangeNoResults() throws Exception {
        // Given: Request con rango de fechas sin documentos
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2024, 12, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2024, 12, 31));
        searchRequest.setTipoDocumento("I");

        // When & Then: Debe retornar lista vacía
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ==================================================================================
    // TESTS PARA STM-771: FILTRO DE ESTATUS Y XML CONTENT
    // ==================================================================================

    @Test
    @DisplayName("STM-771: Debe filtrar por estatus específico")
    void testSearchByStatus() throws Exception {
        // Given: Request con filtro de estatus 1 (Pendiente Addenda)
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setEstatus(1); // Solo facturas con estatus 1

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1)) // Solo testInvoice1 tiene estatus 1
                .andReturn();

        // Then: Validar que el resultado tiene estatus 1
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("status")).isEqualTo(1);
        assertThat(content.get(0).get("series")).isEqualTo("A");
    }

    @Test
    @DisplayName("STM-771: Debe filtrar por estatus 2 (Recibido Parcial)")
    void testSearchByStatusRecibidoParcial() throws Exception {
        // Given: Request con filtro de estatus 2
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setEstatus(2); // Solo facturas con estatus 2

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1)) // Solo testInvoice2 tiene estatus 2
                .andReturn();

        // Then: Validar que el resultado tiene estatus 2
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("status")).isEqualTo(2);
        assertThat(content.get(0).get("series")).isEqualTo("B");
        assertThat(content.get(0).get("statusName")).isEqualTo("Recibido Parcial");
    }

    @Test
    @DisplayName("STM-771: Debe incluir xmlContent en la respuesta")
    void testXmlContentInResponse() throws Exception {
        // Given: Request de búsqueda
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setSerie("A");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].xmlContent").exists())
                .andReturn();

        // Then: Validar que xmlContent existe y no está vacío
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);

        String xmlContent = (String) content.get(0).get("xmlContent");
        assertThat(xmlContent).isNotNull();
        assertThat(xmlContent).isNotEmpty();
        assertThat(xmlContent).contains("<xml>"); // Verificar que es un XML válido
    }

    @Test
    @DisplayName("STM-771: Debe combinar filtro de estatus con otros filtros")
    void testSearchWithStatusAndOtherFilters() throws Exception {
        // Given: Request con múltiples filtros incluyendo estatus
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setRfcReceptor("CSD161207R2A");
        searchRequest.setEstatus(1);

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andReturn();

        // Then: Validar que se aplicaron todos los filtros
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        assertThat(content).hasSize(1);
        Map<String, Object> invoice = content.get(0);
        assertThat(invoice.get("status")).isEqualTo(1);
        assertThat(invoice.get("receptorRfc")).isEqualTo("CSD161207R2A");
        assertThat(invoice.get("emisorRfc")).isEqualTo("AAA010101AAA");
    }

    @Test
    @DisplayName("STM-771: Debe retornar vacío cuando no hay facturas con el estatus buscado")
    void testSearchByStatusNoResults() throws Exception {
        // Given: Request con estatus que no existe en los datos de prueba
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setEstatus(8); // Estatus 8 (Completado) no existe en datos de prueba

        // When & Then: Debe retornar lista vacía
        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("STM-771: xmlContent debe contener el XML completo del documento")
    void testXmlContentIsComplete() throws Exception {
        // Given: Request para obtener una factura específica
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setRfcEmisor("AAA010101AAA");
        searchRequest.setFechaInicioRecepcion(LocalDate.of(2025, 1, 1));
        searchRequest.setFechaFinalRecepcion(LocalDate.of(2025, 1, 31));
        searchRequest.setTipoDocumento("I");
        searchRequest.setSerie("A");
        searchRequest.setFolio("12345");

        // When: Se hace POST al endpoint de búsqueda
        MvcResult result = mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].xmlContent").exists())
                .andReturn();

        // Then: Validar estructura del xmlContent
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        String xmlContent = (String) content.get(0).get("xmlContent");

        // Verificar que el XML contiene elementos esperados
        assertThat(xmlContent).isNotNull();
        assertThat(xmlContent).isNotEmpty();
        assertThat(xmlContent).contains("<xml>");
        assertThat(xmlContent).contains("test1"); // Contenido específico del testInvoice1
    }
}
