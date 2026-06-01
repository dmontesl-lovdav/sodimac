package com.invoicesync.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class BatchDatabaseMigration {

    @Value("${batch.datasource.url}")
    private String batchUrl;

    @Value("${batch.datasource.username}")
    private String batchUsername;

    @Value("${batch.datasource.password}")
    private String batchPassword;

    /**
     * DataSource para la base de datos de control batch (SODIMAC_BATCH_DEV)
     */
    @Bean(name = "batchDataSource")
    public DataSource batchDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        // Conectarse directamente a SODIMAC_BATCH_DEV
        dataSource.setUrl(batchUrl);
        dataSource.setUsername(batchUsername);
        dataSource.setPassword(batchPassword);
        return dataSource;
    }


    /**
     * JdbcTemplate para ejecutar scripts en la base de datos batch
     */
    @Bean(name = "batchJdbcTemplate")
    public JdbcTemplate batchJdbcTemplate(@Qualifier("batchDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Migración de base de datos batch (solo en perfil dev)
     * Se ejecuta al iniciar la aplicación una sola vez
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner migrateBatchDatabase(@Qualifier("batchJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                log.info("===========================================");
                log.info("Iniciando migración de SODIMAC_BATCH_DEV");
                log.info("===========================================");

                // Crear conexión temporal a master para verificar/crear la base de datos
                String masterUrl = batchUrl.replace("databaseName=SODIMAC_BATCH_DEV", "databaseName=master");
                DriverManagerDataSource masterDataSource = new DriverManagerDataSource();
                masterDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                masterDataSource.setUrl(masterUrl);
                masterDataSource.setUsername(batchUsername);
                masterDataSource.setPassword(batchPassword);
                JdbcTemplate masterJdbc = new JdbcTemplate(masterDataSource);

                // Verificar si la base de datos ya existe
                String checkDbQuery = "SELECT COUNT(*) FROM sys.databases WHERE name = 'SODIMAC_BATCH_DEV'";
                Integer dbExists = masterJdbc.queryForObject(checkDbQuery, Integer.class);

                boolean needsCreation = false;

                if (dbExists != null && dbExists > 0) {
                    log.info("✓ Base de datos SODIMAC_BATCH_DEV ya existe");

                    // Verificar si existen las tablas (usando el jdbcTemplate que ya está conectado a SODIMAC_BATCH_DEV)
                    String checkTablesQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'";
                    Integer tableCount = jdbcTemplate.queryForObject(checkTablesQuery, Integer.class);

                    if (tableCount != null && tableCount > 0) {
                        log.info("✓ La base de datos ya tiene {} tablas. Saltando migración.", tableCount);
                        return;
                    } else {
                        log.info("⚠ La base de datos existe pero NO tiene tablas. Creando estructura...");
                        needsCreation = true;
                    }
                } else {
                    log.info("Base de datos SODIMAC_BATCH_DEV no existe. Creando...");
                    // Ejecutar scripts en orden (versiones compatibles con JDBC) usando la conexión a master
                    executeScript(masterJdbc, "database/01_create_database_jdbc.sql", "Crear base de datos");
                    needsCreation = true;
                }

                if (needsCreation) {
                    // Usar el jdbcTemplate que ya está conectado a SODIMAC_BATCH_DEV
                    executeScript(jdbcTemplate, "database/02_create_tables_jdbc.sql", "Crear tablas");
                    executeScript(jdbcTemplate, "database/03_seed_data_jdbc.sql", "Insertar datos iniciales");

                    log.info("===========================================");
                    log.info("✅ Migración completada exitosamente");
                    log.info("===========================================");

                    // Ejecutar verificación
                    verifyMigration(jdbcTemplate);
                }

            } catch (Exception e) {
                log.error("Error durante la migración de SODIMAC_BATCH_DEV", e);
                log.error("La aplicación continuará, pero es posible que algunas funcionalidades no estén disponibles.");
            }
        };
    }

    private void executeScript(JdbcTemplate jdbcTemplate, String scriptPath, String description) {
        try {
            log.info("Ejecutando: {}", description);

            ClassPathResource resource = new ClassPathResource(scriptPath);
            String script = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            // Dividir por bloques BEGIN/END para ejecutar cada uno por separado
            String[] blocks = script.split("(?<=END)\\s*(?=--\\s*Tabla:|IF NOT EXISTS)");

            int executedBlocks = 0;
            for (String block : blocks) {
                // Limpiar comentarios de línea completa
                String[] lines = block.split("\\r?\\n");
                StringBuilder cleanedBlock = new StringBuilder();

                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.startsWith("--") && !trimmedLine.isEmpty()) {
                        cleanedBlock.append(line).append("\n");
                    }
                }

                String finalBlock = cleanedBlock.toString().trim();

                if (!finalBlock.isEmpty()) {
                    try {
                        jdbcTemplate.execute(finalBlock);
                        executedBlocks++;
                    } catch (Exception e) {
                        log.error("Error ejecutando bloque {}: {}", executedBlocks + 1, e.getMessage());
                        throw e;
                    }
                }
            }

            log.info("✓ {} completado ({} bloques ejecutados)", description, executedBlocks);

        } catch (Exception e) {
            log.error("Error en migración de {}: {}", description, e.getMessage());
            throw new RuntimeException("Error en migración: " + description, e);
        }
    }

    private void verifyMigration(JdbcTemplate jdbcTemplate) {
        try {
            log.info("Verificando migración...");

            // Verificar tablas
            String countTablesQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'";
            Integer tableCount = jdbcTemplate.queryForObject(countTablesQuery, Integer.class);
            log.info("✓ Tablas creadas: {}", tableCount);

            // Verificar catálogos
            String countCatalogosQuery = "SELECT COUNT(*) FROM catCatalogo";
            Integer catalogoCount = jdbcTemplate.queryForObject(countCatalogosQuery, Integer.class);
            log.info("✓ Catálogos insertados: {}", catalogoCount);

            log.info("✓ Verificación completada exitosamente");

        } catch (Exception e) {
            log.warn("No se pudo verificar la migración completamente: {}", e.getMessage());
        }
    }
}
