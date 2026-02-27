package com.sodimac.fiscal.api.controller;

import com.sodimac.fiscal.api.model.dto.response.PaymentRegistrationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración para PaymentRegistrationController.
 *
 * Valida los dos escenarios principales:
 * 1. Registro exitoso de complemento de pago con XML válido
 * 2. Rechazo de complemento de pago con RFC no autorizado
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Tests de Registro de Complemento de Pago")
class PaymentRegistrationControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * ESCENARIO EXITOSO
     *
     * Valida que un complemento de pago con datos correctos se registre exitosamente.
     *
     * Precondiciones:
     * - XML válido según XSD Pagos 2.0
     * - RFC emisor y receptor válidos
     * - RFC receptor autorizado en el catálogo (CGE990101GHI, SOD970101ABC)
     * - Datos completos de pago
     *
     * Resultado esperado:
     * - HTTP 200 OK
     * - processingStatus = "SUCCESS"
     * - paymentsUuid generado
     * - fileName con nombre del archivo
     */
    // @Test
    @DisplayName("Caso exitoso: Registro de complemento de pago válido")
    void testRegistrarComplementoPagoExitoso() throws Exception {
        // Arrange - Preparar datos de entrada
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("idProveedor", "12345");
        body.add("tipoAddenda", "5");
        body.add("tipoProveedor", "SODIMAC");
        body.add("idUsuario", "1");
        body.add("xmlFile", new ClassPathResource("xml-samples/cfdi-pagos-sample.xml"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Act - Ejecutar registro
        ResponseEntity<PaymentRegistrationResponse> response = restTemplate.postForEntity(
                "/api/fiscal/complementos-pago/registrar",
                requestEntity,
                PaymentRegistrationResponse.class
        );

        // Assert - Verificar respuesta exitosa
        System.out.println("\n========== ESCENARIO EXITOSO ==========");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("========================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProcessingStatus()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getPaymentsUuid()).isNotNull();
        assertThat(response.getBody().getFileName()).contains("cfdi-pagos-sample.xml");
        assertThat(response.getBody().getMessage()).contains("exitosamente");
    }

    /**
     * ESCENARIO FALLIDO
     *
     * Valida que un complemento de pago con RFC no autorizado sea rechazado.
     *
     * Precondiciones:
     * - XML estructuralmente válido
     * - RFC receptor NO está en el catálogo de receptores autorizados
     * - Debe fallar en la validación de negocio
     *
     * Resultado esperado:
     * - HTTP 422 UNPROCESSABLE_ENTITY
     * - processingStatus = "FAILED"
     * - message con descripción del error
     */
    // @Test
    @DisplayName("Caso fallido: RFC receptor no autorizado")
    void testRegistrarComplementoPagoRfcNoAutorizado() throws Exception {
        // Arrange - Preparar datos con RFC no autorizado
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("idProveedor", "12345");
        body.add("tipoAddenda", "5");
        body.add("tipoProveedor", "SODIMAC");
        body.add("idUsuario", "1");
        body.add("xmlFile", new ClassPathResource("xml-samples/cfdi-pagos-invalid-rfc.xml"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Act - Ejecutar registro
        ResponseEntity<PaymentRegistrationResponse> response = restTemplate.postForEntity(
                "/api/fiscal/complementos-pago/registrar",
                requestEntity,
                PaymentRegistrationResponse.class
        );

        // Assert - Verificar rechazo
        System.out.println("\n========== ESCENARIO FALLIDO ==========");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("========================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProcessingStatus()).isEqualTo("FAILED");
        assertThat(response.getBody().getMessage()).containsAnyOf(
                "no autorizado",
                "no está autorizado",
                "receptor no válido",
                "RFC no autorizado"
        );
    }    
}
