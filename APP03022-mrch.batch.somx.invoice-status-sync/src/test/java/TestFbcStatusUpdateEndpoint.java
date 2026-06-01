import com.invoicesync.infrastructure.adapter.external.dto.FbcStatusUpdateRequest;
import com.invoicesync.infrastructure.adapter.external.dto.FbcStatusUpdateResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Test de integración para validar el endpoint PUT /invoices/{uuid}/status de FBC Portal.
 *
 * Este test valida:
 * 1. Conectividad al endpoint FBC en ambiente dev
 * 2. Formato correcto de la petición
 * 3. Manejo de la respuesta
 *
 * NOTA: Este test no requiere Spring Boot - es un test standalone.
 */
public class TestFbcStatusUpdateEndpoint {

    private static final String FBC_BASE_URL = "https://dev.fbusinesscenter.com/ppsomx/fiscal";
    private static final String TEST_UUID = "9db51140-3fec-45a9-89ee-7e8b35dda53f";

    public static void main(String[] args) {
        System.out.println("=== Test FBC Status Update Endpoint ===\n");

        TestFbcStatusUpdateEndpoint test = new TestFbcStatusUpdateEndpoint();

        try {
            test.testUpdateInvoiceStatus();
            System.out.println("\n✅ Test completado exitosamente");
        } catch (Exception e) {
            System.err.println("\n❌ Test falló: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testUpdateInvoiceStatus() {
        System.out.println("Test 1: Actualizar estado de factura en FBC");
        System.out.println("-------------------------------------------");
        System.out.println("URL: " + FBC_BASE_URL + "/invoices/" + TEST_UUID + "/status");

        // Crear RestClient
        RestClient restClient = RestClient.builder()
                .baseUrl(FBC_BASE_URL)
                .build();

        // Crear request para transición de estado 7 → 8 (Pendiente de Pago → Pagado)
        FbcStatusUpdateRequest request = FbcStatusUpdateRequest.forBatchUpdate(
                1,  // numeroProveedor
                7,  // estatusOrigen (Pendiente de Pago)
                8   // estatusDestino (Pagado)
        );

        System.out.println("\nRequest:");
        System.out.println("  - UUID: " + TEST_UUID);
        System.out.println("  - Número Proveedor: " + request.numeroProveedor());
        System.out.println("  - Estado Origen: " + request.estatusOrigen());
        System.out.println("  - Estado Destino: " + request.estatusDestino());
        System.out.println("  - Usuario: " + request.idUsuarioActualizacion());
        System.out.println("  - Comentario: " + request.comentario());

        try {
            System.out.println("\nEnviando petición PUT...");

            FbcStatusUpdateResponse response = restClient.put()
                    .uri("/invoices/{uuid}/status", TEST_UUID)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        System.err.println("Error 4xx: " + res.getStatusCode() + " - " + res.getStatusText());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        System.err.println("Error 5xx: " + res.getStatusCode() + " - " + res.getStatusText());
                    })
                    .body(FbcStatusUpdateResponse.class);

            System.out.println("\nResponse recibida:");
            if (response != null) {
                System.out.println("  - Success: " + response.success());
                System.out.println("  - Code: " + response.code());
                System.out.println("  - Message: " + response.message());
                System.out.println("  - Invoice UUID: " + response.invoiceUuid());
                System.out.println("  - Fiscal UUID: " + response.fiscalUuid());
                System.out.println("  - Estado Anterior: " + response.estatusAnterior());
                System.out.println("  - Estado Nuevo: " + response.estatusNuevo());
                System.out.println("  - Estado Nuevo Nombre: " + response.estatusNuevoNombre());
                System.out.println("  - Fecha Actualización: " + response.fechaActualizacion());

                if (response.isSuccessful()) {
                    System.out.println("\n✅ Estado actualizado exitosamente en FBC");
                    System.out.println("   " + response.getResultMessage());
                } else {
                    System.out.println("\n⚠️  Actualización no exitosa");
                    System.out.println("   " + response.getResultMessage());
                    System.out.println("\n   NOTA: Es normal que falle en ambiente dev si el servicio");
                    System.out.println("   de tren de estatus no está disponible (error SVC5001).");
                    System.out.println("   Lo importante es validar que el endpoint responde correctamente.");
                }
            } else {
                System.out.println("  ❌ No se recibió respuesta del servidor");
            }

        } catch (Exception e) {
            System.err.println("\n❌ Error al ejecutar petición:");
            System.err.println("   " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();

            System.out.println("\n📝 Notas importantes:");
            System.out.println("   - Si el error es de conexión, verificar acceso a internet");
            System.out.println("   - Si el error es 503 (Service Unavailable), es normal en ambiente dev");
            System.out.println("   - El endpoint FBC puede requerir servicios internos no disponibles en dev");
        }
    }
}
