package com.invoicesync.infrastructure.adapter.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller para verificar el estado de las conexiones a bases de datos
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final JdbcTemplate sodimacSapJdbcTemplate;
    private final JdbcTemplate sapitoJdbcTemplate;
    private final JdbcTemplate i213JdbcTemplate;

    public HealthController(
            @Qualifier("sodimacSapJdbcTemplate") JdbcTemplate sodimacSapJdbcTemplate,
            @Qualifier("sapitoJdbcTemplate") JdbcTemplate sapitoJdbcTemplate,
            @Qualifier("i213JdbcTemplate") JdbcTemplate i213JdbcTemplate) {
        this.sodimacSapJdbcTemplate = sodimacSapJdbcTemplate;
        this.sapitoJdbcTemplate = sapitoJdbcTemplate;
        this.i213JdbcTemplate = i213JdbcTemplate;
    }

    /**
     * Verifica todas las conexiones a bases de datos externas
     */
    @GetMapping("/databases")
    public ResponseEntity<Map<String, Object>> checkAllDatabases() {
        log.info("Iniciando verificación de conexiones a bases de datos...");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());

        Map<String, Object> databases = new LinkedHashMap<>();
        int totalDatabases = 3;
        int healthyDatabases = 0;

        // Verificar Sodimac SAP
        Map<String, Object> sapStatus = checkSodimacSap();
        databases.put("sodimac_sap", sapStatus);
        if ("UP".equals(sapStatus.get("status"))) healthyDatabases++;

        // Verificar SAPITO
        Map<String, Object> sapitoStatus = checkSapito();
        databases.put("sapito", sapitoStatus);
        if ("UP".equals(sapitoStatus.get("status"))) healthyDatabases++;

        // Verificar i213
        Map<String, Object> i213Status = checkI213();
        databases.put("i213", i213Status);
        if ("UP".equals(i213Status.get("status"))) healthyDatabases++;

        response.put("databases", databases);
        response.put("summary", Map.of(
                "total", totalDatabases,
                "healthy", healthyDatabases,
                "unhealthy", totalDatabases - healthyDatabases,
                "overallStatus", healthyDatabases == totalDatabases ? "ALL_HEALTHY" :
                        healthyDatabases == 0 ? "ALL_DOWN" : "PARTIAL"
        ));

        log.info("Verificación completada: {}/{} bases de datos saludables", healthyDatabases, totalDatabases);

        if (healthyDatabases == totalDatabases) {
            return ResponseEntity.ok(response);
        } else if (healthyDatabases > 0) {
            return ResponseEntity.status(207).body(response); // Multi-Status
        } else {
            return ResponseEntity.status(503).body(response); // Service Unavailable
        }
    }

    /**
     * Verifica solo la conexión a Sodimac SAP
     */
    @GetMapping("/databases/sodimac-sap")
    public ResponseEntity<Map<String, Object>> checkSodimacSapEndpoint() {
        Map<String, Object> status = checkSodimacSap();
        return "UP".equals(status.get("status")) ?
                ResponseEntity.ok(status) :
                ResponseEntity.status(503).body(status);
    }

    /**
     * Verifica solo la conexión a SAPITO
     */
    @GetMapping("/databases/sapito")
    public ResponseEntity<Map<String, Object>> checkSapitoEndpoint() {
        Map<String, Object> status = checkSapito();
        return "UP".equals(status.get("status")) ?
                ResponseEntity.ok(status) :
                ResponseEntity.status(503).body(status);
    }

    /**
     * Verifica solo la conexión a i213
     */
    @GetMapping("/databases/i213")
    public ResponseEntity<Map<String, Object>> checkI213Endpoint() {
        Map<String, Object> status = checkI213();
        return "UP".equals(status.get("status")) ?
                ResponseEntity.ok(status) :
                ResponseEntity.status(503).body(status);
    }

    private Map<String, Object> checkSodimacSap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "Sodimac SAP");
        result.put("type", "SQL Server");
        result.put("server", "10.138.153.10:1433");
        result.put("database", "SODIMAC_SAP_DEV");

        try {
            long startTime = System.currentTimeMillis();
            Integer queryResult = sodimacSapJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;

            if (queryResult != null && queryResult == 1) {
                result.put("status", "UP");
                result.put("responseTimeMs", responseTime);
                result.put("message", "Conexión exitosa");
                log.info("✓ Sodimac SAP: Conexión exitosa ({}ms)", responseTime);
            } else {
                result.put("status", "DOWN");
                result.put("message", "Query de validación falló");
                log.warn("✗ Sodimac SAP: Query de validación falló");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            log.error("✗ Sodimac SAP: Error de conexión - {}", e.getMessage());
        }

        return result;
    }

    private Map<String, Object> checkSapito() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "SAPITO");
        result.put("type", "Oracle");
        result.put("server", "ensenada:1541");
        result.put("database", "odsrmxts");

        try {
            long startTime = System.currentTimeMillis();
            Integer queryResult = sapitoJdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;

            if (queryResult != null && queryResult == 1) {
                result.put("status", "UP");
                result.put("responseTimeMs", responseTime);
                result.put("message", "Conexión exitosa");
                log.info("✓ SAPITO: Conexión exitosa ({}ms)", responseTime);
            } else {
                result.put("status", "DOWN");
                result.put("message", "Query de validación falló");
                log.warn("✗ SAPITO: Query de validación falló");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            log.error("✗ SAPITO: Error de conexión - {}", e.getMessage());
        }

        return result;
    }

    private Map<String, Object> checkI213() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "Interfase i213");
        result.put("type", "SQL Server");
        result.put("server", "10.138.153.10:1433");
        result.put("database", "AdmIF213ProdDB");

        try {
            long startTime = System.currentTimeMillis();
            Integer queryResult = i213JdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;

            if (queryResult != null && queryResult == 1) {
                result.put("status", "UP");
                result.put("responseTimeMs", responseTime);
                result.put("message", "Conexión exitosa");
                log.info("✓ i213: Conexión exitosa ({}ms)", responseTime);
            } else {
                result.put("status", "DOWN");
                result.put("message", "Query de validación falló");
                log.warn("✗ i213: Query de validación falló");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            log.error("✗ i213: Error de conexión - {}", e.getMessage());
        }

        return result;
    }
}
