package com.sodimac.fiscal.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración para la búsqueda de complementos de pago.
 *
 * Valida el endpoint GET /api/fiscal/complementos-pago/buscar
 * con diferentes combinaciones de filtros.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Tests de Búsqueda de Complementos de Pago")
class PaymentSearchControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test 1: Búsqueda sin filtros (debe retornar todos los complementos con paginación).
   
     */
    // @Test
    @DisplayName("Búsqueda sin filtros - Retorna todos los complementos paginados")
    void testBuscarSinFiltros() {
        // Act - Búsqueda sin parámetros
        String url = "/api/fiscal/complementos-pago/buscar";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA SIN FILTROS ==========");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("==========================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\":");
        assertThat(response.getBody()).contains("\"totalElements\":");
    }

    /**
     * Test 2: Búsqueda por RFC emisor.
     */
    // @Test
    @DisplayName("Búsqueda por RFC emisor - Filtra por proveedor")
    void testBuscarPorRfcEmisor() {
        // Arrange - RFC del emisor de prueba insertado en V10
        String rfcEmisor = "EKU9003173C9";

        // Act
        String url = "/api/fiscal/complementos-pago/buscar?rfcEmisor=" + rfcEmisor;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA POR RFC EMISOR ==========");
        System.out.println("RFC Emisor: " + rfcEmisor);
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("=============================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(rfcEmisor);
    }

    /**
     * Test 3: Búsqueda por rango de fechas.
     */
    // @Test
    @DisplayName("Búsqueda por rango de fechas - Filtra entre dos fechas")
    void testBuscarPorRangoFechas() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().minusDays(30);
        LocalDate fechaFin = LocalDate.now();

        // Act
        String url = String.format("/api/fiscal/complementos-pago/buscar?fechaPagoInicio=%s&fechaPagoFin=%s",
                fechaInicio, fechaFin);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA POR RANGO DE FECHAS ==========");
        System.out.println("Fecha Inicio: " + fechaInicio);
        System.out.println("Fecha Fin: " + fechaFin);
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("==================================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\":");
    }

    /**
     * Test 4: Búsqueda por folio y serie.
     */
    // @Test
    @DisplayName("Búsqueda por folio y serie - Encuentra complemento específico")
    void testBuscarPorFolioYSerie() {
        // Arrange - Datos de prueba de V10
        String folio = "001";
        String serie = "P";

        // Act
        String url = String.format("/api/fiscal/complementos-pago/buscar?folio=%s&serie=%s", folio, serie);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA POR FOLIO Y SERIE ==========");
        System.out.println("Folio: " + folio + ", Serie: " + serie);
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("================================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test 5: Búsqueda con paginación.
     */
    // @Test
    @DisplayName("Búsqueda con paginación - Retorna página específica")
    void testBuscarConPaginacion() {
        // Act - Solicitar página 0 con tamaño 5
        String url = "/api/fiscal/complementos-pago/buscar?page=0&size=5";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA CON PAGINACIÓN ==========");
        System.out.println("Página: 0, Tamaño: 5");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("=============================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"pageable\":");
        assertThat(response.getBody()).contains("\"size\":5");
    }

    /**
     * Test 6: Búsqueda por status (vigente).
     */
    // @Test
    @DisplayName("Búsqueda por status - Filtra complementos vigentes")
    void testBuscarPorStatus() {
        // Act - Buscar solo complementos vigentes (status=1)
        String url = "/api/fiscal/complementos-pago/buscar?status=1";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA POR STATUS ==========");
        System.out.println("Status: 1 (Vigente)");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("=========================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":1");
    }

    /**
     * Test 7: Búsqueda con ordenamiento.
     */
    // @Test
    @DisplayName("Búsqueda con ordenamiento - Ordena por fecha descendente")
    void testBuscarConOrdenamiento() {
        // Act - Ordenar por fecha de pago descendente
        String url = "/api/fiscal/complementos-pago/buscar?sortBy=paymentDate&sortDirection=DESC";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA CON ORDENAMIENTO ==========");
        System.out.println("Ordenar por: paymentDate DESC");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("===============================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\":");
    }

    /**
     * Test 8: Búsqueda sin resultados.
     */
    // @Test
    @DisplayName("Búsqueda sin resultados - RFC inexistente retorna lista vacía")
    void testBuscarSinResultados() {
        // Act - Buscar con RFC que no existe
        String url = "/api/fiscal/complementos-pago/buscar?rfcEmisor=NOEXISTE999999";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        System.out.println("\n========== BÚSQUEDA SIN RESULTADOS ==========");
        System.out.println("RFC Emisor: NOEXISTE999999");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        System.out.println("=============================================\n");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"totalElements\":0");
        assertThat(response.getBody()).contains("\"empty\":true");
    }
}
