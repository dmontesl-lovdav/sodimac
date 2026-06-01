package com.rebatesync.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Componente que ejecuta la optimización de índices al iniciar la aplicación.
 * Solo se ejecuta una vez y luego debe ser deshabilitado manualmente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexOptimizer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    // Cambiar a false después de ejecutar la primera vez
    // NOTA: Deshabilitado porque necesitamos primero tener datos en la tabla
    // Los índices se crearán manualmente después del primer sync exitoso
    private static final boolean ENABLED = false;

    @Override
    public void run(String... args) throws Exception {
        if (!ENABLED) {
            log.info("IndexOptimizer está deshabilitado. No se ejecutará la optimización.");
            return;
        }

        log.info("========================================");
        log.info("Iniciando optimización de índices...");
        log.info("========================================");

        try {
            int successCount = 0;
            int errorCount = 0;

            // Lista de comandos SQL a ejecutar
            String[] sqlCommands = {
                // 1. Índice compuesto principal
                """
                CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Proveedor_Acuerdo
                ON RebateAcuerdosTemp(NumeroProveedor, NumeroAcuerdo)
                WITH (FILLFACTOR = 90, ONLINE = ON)
                """,

                // 2. Índice para Familia con INCLUDE
                """
                CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Familia
                ON RebateAcuerdosTemp(Familia)
                INCLUDE (NumeroProveedor, NumeroAcuerdo, RazonSocial)
                WITH (FILLFACTOR = 90, ONLINE = ON)
                """,

                // 3. Índice para TipoAcuerdo con INCLUDE
                """
                CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_TipoAcuerdo
                ON RebateAcuerdosTemp(TipoAcuerdo)
                INCLUDE (NumeroProveedor, NumeroAcuerdo, Moneda, Valor)
                WITH (FILLFACTOR = 90, ONLINE = ON)
                """,

                // 4. Índice para Estado
                """
                CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Estado
                ON RebateAcuerdosTemp(Estado)
                WITH (FILLFACTOR = 90, ONLINE = ON)
                """,

                // 5. Actualizar estadísticas
                "UPDATE STATISTICS RebateAcuerdosTemp WITH FULLSCAN"
            };

            String[] descriptions = {
                "Índice compuesto Proveedor+Acuerdo (FILLFACTOR 90)",
                "Índice Familia con INCLUDE (covering index)",
                "Índice TipoAcuerdo con INCLUDE (covering index)",
                "Índice Estado",
                "Actualizar estadísticas"
            };

            log.info("Ejecutando {} comandos de optimización...", sqlCommands.length);
            log.info("");

            for (int i = 0; i < sqlCommands.length; i++) {
                String sql = sqlCommands[i].trim();
                String description = descriptions[i];

                log.info("{}. {}", i + 1, description);

                try {
                    // Primero intentar eliminar el índice si existe (excepto UPDATE STATISTICS)
                    if (sql.toUpperCase().contains("CREATE") && sql.contains("IX_")) {
                        String indexName = sql.substring(
                                sql.indexOf("IX_"),
                                sql.indexOf("\n", sql.indexOf("IX_"))
                        ).trim();

                        try {
                            String dropSql = "DROP INDEX " + indexName + " ON RebateAcuerdosTemp";
                            jdbcTemplate.execute(dropSql);
                            log.info("   • Índice anterior eliminado");
                        } catch (Exception e) {
                            log.debug("   • Índice no existía previamente");
                        }
                    }

                    // Ejecutar el comando principal
                    jdbcTemplate.execute(sql);
                    log.info("   ✓ Ejecutado exitosamente");
                    successCount++;

                } catch (Exception e) {
                    log.error("   ❌ Error: {}", e.getMessage());
                    errorCount++;
                }

                log.info("");
            }

            log.info("Resumen de ejecución:");
            log.info("  • Comandos exitosos: {}", successCount);
            log.info("  • Errores: {}", errorCount);

            log.info("========================================");
            log.info("✅ Optimización de índices completada");
            log.info("========================================");
            log.info("IMPORTANTE: Cambiar ENABLED = false en IndexOptimizer.java");
            log.info("para evitar que se ejecute en el próximo arranque.");

        } catch (Exception e) {
            log.error("❌ Error durante la optimización de índices", e);
            throw e;
        }
    }
}
