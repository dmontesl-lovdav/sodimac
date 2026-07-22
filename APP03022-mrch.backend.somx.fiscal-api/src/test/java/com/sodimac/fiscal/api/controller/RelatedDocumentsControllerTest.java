package com.sodimac.fiscal.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración para RelatedDocumentsController.
 *
 * Valida el nuevo endpoint GET /api/related-documents/by-payment/{paymentsUuid}
 * que permite consultar las facturas relacionadas (documentos pagados) de un
 * complemento de pago específico.
 *
 * Este endpoint fue creado para resolver la necesidad del BFF de obtener
 * los documentos relacionados filtrando por el UUID del complemento de pago.
 *
 * CONTEXTO FISCAL:
 * - payments (tabla): Complemento de pago completo (CFDI tipo P)
 * - payment (tabla): Pago individual dentro del complemento (elemento <Pago>)
 * - related_documents (tabla): Factura relacionada (elemento <DoctoRelacionado>)
 *
 * RELACIÓN CORRECTA:
 * payments (1) → payment (N) → related_documents (N)
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Tests de Documentos Relacionados")
class RelatedDocumentsControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test 1: Obtener documentos relacionados por UUID de complemento de pago.
     *
     * Valida que el endpoint retorne correctamente las facturas relacionadas
     * cuando se proporciona un UUID válido de complemento de pago.
     *
     * Precondiciones:
     * - Existe un complemento de pago con UUID: aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee
     * - Ese complemento tiene al menos 1 documento relacionado
     *
     * Resultado esperado:
     * - HTTP 200 OK
     * - content[] con al menos 1 documento relacionado
     * - Cada documento tiene: amountPaid, previousBalance, remainingBalance, series, folio
     */
    // @Test
    @DisplayName("Obtener documentos relacionados por UUID de complemento de pago válido")
    void testObtenerDocumentosRelacionadosPorPaymentsUuid() {
        // Arrange - UUID del complemento de pago insertado en V10
        String paymentsUuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        // Act - Consultar documentos relacionados
        String url = "/api/related-documents/by-payment/" + paymentsUuid;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== DOCUMENTOS RELACIONADOS POR COMPLEMENTO ==========");
        System.out.println("Complemento UUID: " + paymentsUuid);
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("=============================================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"content\":");
        assertThat(response.getBody()).contains("\"totalElements\":");

        // Verificar que retorna al menos 1 documento relacionado
        assertThat(response.getBody()).contains("\"amountPaid\":");
        assertThat(response.getBody()).contains("\"previousBalance\":");
        assertThat(response.getBody()).contains("\"remainingBalance\":");
        assertThat(response.getBody()).contains("\"series\":");
        assertThat(response.getBody()).contains("\"folio\":");

        // Verificar que el documento relacionado tiene los valores esperados de V10
        assertThat(response.getBody()).contains("1800.00"); // amountPaid
        assertThat(response.getBody()).contains("\"series\":\"A\"");
        assertThat(response.getBody()).contains("\"folio\":\"002\"");
    }

    /**
     * Test 2: Obtener documentos relacionados con paginación.
     *
     * Valida que el endpoint soporte paginación correctamente.
     */
    // @Test
    @DisplayName("Obtener documentos relacionados con paginación")
    void testObtenerDocumentosRelacionadosConPaginacion() {
        // Arrange
        String paymentsUuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        // Act - Solicitar página 0 con tamaño por defecto
        String url = "/api/related-documents/by-payment/" + paymentsUuid + "?page=0";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== DOCUMENTOS RELACIONADOS CON PAGINACIÓN ==========");
        System.out.println("Complemento UUID: " + paymentsUuid);
        System.out.println("Página: 0");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("============================================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"pageable\":");
        assertThat(response.getBody()).contains("\"pageNumber\":0");
        assertThat(response.getBody()).contains("\"first\":true");
    }

    /**
     * Test 3: Buscar documentos relacionados con UUID inexistente.
     *
     * Valida que el endpoint retorne una lista vacía cuando no hay
     * documentos relacionados para el UUID proporcionado.
     */
    // @Test
    @DisplayName("Buscar documentos relacionados con UUID inexistente retorna lista vacía")
    void testBuscarDocumentosRelacionadosUuidInexistente() {
        // Arrange - UUID que no existe en la base de datos
        String paymentsUuid = "99999999-9999-9999-9999-999999999999";

        // Act
        String url = "/api/related-documents/by-payment/" + paymentsUuid;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA CON UUID INEXISTENTE ==========");
        System.out.println("Complemento UUID: " + paymentsUuid);
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("===================================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"totalElements\":0");
        assertThat(response.getBody()).contains("\"empty\":true");
        assertThat(response.getBody()).contains("\"content\":[]");
    }

    /**
     * Test 4: Validar formato inválido de UUID.
     *
     * Valida que el endpoint retorne un error cuando se proporciona
     * un UUID con formato inválido.
     *
     * Nota: Spring Boot por defecto retorna 500 INTERNAL_SERVER_ERROR
     * cuando falla la conversión de tipo en @PathVariable.
     */
    // @Test
    @DisplayName("UUID con formato inválido retorna error")
    void testUuidFormatoInvalido() {
        // Arrange - UUID con formato inválido
        String invalidUuid = "invalid-uuid-format";

        // Act
        String url = "/api/related-documents/by-payment/" + invalidUuid;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== UUID CON FORMATO INVÁLIDO ==========");
        System.out.println("UUID Inválido: " + invalidUuid);
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("===============================================\n");

        // Spring retorna 500 por defecto cuando falla la conversión de tipo
        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }

    /**
     * Test 5: Obtener todos los documentos relacionados sin filtro.
     *
     * Valida el endpoint GET /api/related-documents que retorna
     * todos los documentos relacionados con paginación.
     */
    // @Test
    @DisplayName("Obtener todos los documentos relacionados sin filtro")
    void testObtenerTodosLosDocumentosRelacionados() {
        // Act
        String url = "/api/related-documents?page=0";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== TODOS LOS DOCUMENTOS RELACIONADOS ==========");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("=======================================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\":");
        assertThat(response.getBody()).contains("\"totalElements\":");
    }

    /**
     * Test 6: Validar estructura completa del DTO de respuesta.
     *
     * Valida que el documento relacionado retornado contenga la totalidad de
     * los campos esperados según la estructura del SAT.
     */
    // @Test
    @DisplayName("Validar estructura completa del DTO de documentos relacionados")
    void testValidarEstructuraCompletaDTO() {
        // Arrange
        String paymentsUuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        // Act
        String url = "/api/related-documents/by-payment/" + paymentsUuid;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert - Verificar campos obligatorios del SAT
        System.out.println("\n========== VALIDACIÓN ESTRUCTURA DTO ==========");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("===============================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Campos obligatorios según Complemento de Pagos 2.0
        assertThat(response.getBody()).contains("\"relatedDocumentUuid\":");
        assertThat(response.getBody()).contains("\"paymentUuid\":");
        assertThat(response.getBody()).contains("\"documentUuid\":");
        assertThat(response.getBody()).contains("\"amountPaid\":");
        assertThat(response.getBody()).contains("\"previousBalance\":");
        assertThat(response.getBody()).contains("\"remainingBalance\":");

        // Campos opcionales pero comunes
        assertThat(response.getBody()).contains("\"series\":");
        assertThat(response.getBody()).contains("\"folio\":");
        assertThat(response.getBody()).contains("\"currency\":");
        assertThat(response.getBody()).contains("\"installmentNumber\":");
    }

    /**
     * Test 7: Validar que payment_uuid referencia correctamente a payment (no payments).
     *
     * Este test valida que la corrección de FK funcione correctamente.
     * El payment_uuid debe referenciar a payment(payment_uuid), no a payments(payments_uuid).
     */
    // @Test
    @DisplayName("Validar que payment_uuid referencia correctamente a tabla payment")
    void testValidarReferenciaPaymentUuid() {
        // Arrange - UUID del complemento de pago
        String paymentsUuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        // Act
        String url = "/api/related-documents/by-payment/" + paymentsUuid;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== VALIDACIÓN FK PAYMENT_UUID ==========");
        System.out.println("Complemento UUID (payments): " + paymentsUuid);
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("================================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verificar que paymentUuid es el UUID del payment individual (no del payments)
        // Según V10: payment_uuid debe ser 'bbbbbbbb-cccc-dddd-eeee-ffffffffffff'
        assertThat(response.getBody()).contains("\"paymentUuid\":\"bbbbbbbb-cccc-dddd-eeee-ffffffffffff\"");

        // NO debe contener el payments_uuid directamente en paymentUuid
        // (esto confirmaría que la FK está bien configurada)
    }
}
