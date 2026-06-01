package com.rebatesync.infrastructure.config;

/**
 * Este archivo se mantiene para documentación pero no tiene beans activos.
 *
 * Configuración de datasources:
 * - SODIMAC_REBATES_DEV (principal): Configurado automáticamente por Spring Boot
 *   a través de spring.datasource.* en application.yml
 * - SODIMAC_BATCH_DEV (batch): Configurado en BatchDatabaseMigration.java
 *   para tablas de control (ctrlProcesoCab, ctrlProcesoDet, ctrlProcesoElemento, ctrlLog)
 */
public class DataSourceConfig {
    // La configuración principal del datasource la hace Spring Boot automáticamente
}
