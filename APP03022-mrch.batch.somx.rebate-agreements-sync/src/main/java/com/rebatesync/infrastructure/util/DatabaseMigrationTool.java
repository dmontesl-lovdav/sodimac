package com.rebatesync.infrastructure.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Herramienta independiente para ejecutar la migración de SODIMAC_BATCH_DEV
 *
 * Ejecutar con:
 * mvn exec:java -Dexec.mainClass="com.rebatesync.infrastructure.util.DatabaseMigrationTool"
 */
public class DatabaseMigrationTool {

    private static final String SERVER = "10.138.153.10";
    private static final String PORT = "1433";
    private static final String USERNAME = "SodimacETLUSR";
    private static final String PASSWORD = "$DEV50dimac2026";

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("HERRAMIENTA DE MIGRACIÓN - SODIMAC_BATCH_DEV");
        System.out.println("===========================================");
        System.out.println();

        try {
            // Paso 1: Conectar a master para verificar/crear la base de datos
            String masterUrl = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=master;encrypt=true;trustServerCertificate=true",
                SERVER, PORT
            );

            System.out.println("Conectando a master...");
            try (Connection conn = DriverManager.getConnection(masterUrl, USERNAME, PASSWORD)) {
                System.out.println("✓ Conectado exitosamente");

                // Verificar si la base de datos existe
                String checkDbSql = "SELECT COUNT(*) FROM sys.databases WHERE name = 'SODIMAC_BATCH_DEV'";
                try (Statement stmt = conn.createStatement();
                     var rs = stmt.executeQuery(checkDbSql)) {
                    rs.next();
                    int count = rs.getInt(1);

                    if (count > 0) {
                        System.out.println();
                        System.out.println("⚠️  Base de datos SODIMAC_BATCH_DEV ya existe");
                        System.out.println("La migración se saltará para evitar duplicación");
                        System.out.println("Si necesita recrear la base de datos, elimínela manualmente primero");
                        return;
                    }
                }

                // Crear la base de datos
                System.out.println();
                System.out.println("Creando base de datos SODIMAC_BATCH_DEV...");
                String createDbSql = "CREATE DATABASE SODIMAC_BATCH_DEV";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createDbSql);
                    System.out.println("✓ Base de datos creada");
                }
            }

            // Paso 2: Conectar a SODIMAC_BATCH_DEV para crear tablas
            String batchUrl = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=SODIMAC_BATCH_DEV;encrypt=true;trustServerCertificate=true",
                SERVER, PORT
            );

            System.out.println();
            System.out.println("Conectando a SODIMAC_BATCH_DEV...");
            try (Connection conn = DriverManager.getConnection(batchUrl, USERNAME, PASSWORD)) {
                System.out.println("✓ Conectado exitosamente");

                // Ejecutar script de creación de tablas
                System.out.println();
                System.out.println("Ejecutando script de creación de tablas...");
                String createTablesSql = loadScript("database/02_create_tables_jdbc.sql");
                executeScript(conn, createTablesSql, "Creación de tablas");

                // Ejecutar script de datos iniciales
                System.out.println();
                System.out.println("Ejecutando script de datos iniciales...");
                String seedDataSql = loadScript("database/03_seed_data_jdbc.sql");
                executeScript(conn, seedDataSql, "Datos iniciales");

                // Verificación
                System.out.println();
                System.out.println("Verificando migración...");
                verifyMigration(conn);
            }

            System.out.println();
            System.out.println("===========================================");
            System.out.println("✓ MIGRACIÓN COMPLETADA EXITOSAMENTE");
            System.out.println("===========================================");

        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ ERROR EN LA MIGRACIÓN:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String loadScript(String relativePath) throws IOException {
        // Intentar cargar desde resources o desde el directorio del proyecto
        String basePath = System.getProperty("user.dir");
        String fullPath = Paths.get(basePath, "src/main/resources", relativePath).toString();

        if (!Files.exists(Paths.get(fullPath))) {
            throw new IOException("No se encontró el archivo: " + fullPath);
        }

        return Files.readString(Paths.get(fullPath));
    }

    private static void executeScript(Connection conn, String sql, String description) throws Exception {
        // Limpiar comentarios y líneas vacías
        String[] lines = sql.split("\\r?\\n");
        StringBuilder cleanedSql = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("--") && !trimmed.isEmpty()) {
                cleanedSql.append(line).append("\n");
            }
        }

        String finalSql = cleanedSql.toString().trim();

        if (!finalSql.isEmpty()) {
            try (Statement stmt = conn.createStatement()) {
                // Dividir por BEGIN/END para ejecutar bloques separados
                String[] batches = finalSql.split("(?i)\\bGO\\b");

                for (String batch : batches) {
                    String trimmedBatch = batch.trim();
                    if (!trimmedBatch.isEmpty()) {
                        stmt.execute(trimmedBatch);
                    }
                }

                System.out.println("✓ " + description + " completado");
            }
        }
    }

    private static void verifyMigration(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // Contar tablas
            var rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'"
            );
            rs.next();
            System.out.println("  • Tablas creadas: " + rs.getInt(1));

            // Contar catálogos
            rs = stmt.executeQuery("SELECT COUNT(*) FROM catCatalogo");
            rs.next();
            System.out.println("  • Catálogos insertados: " + rs.getInt(1));

            // Contar elementos de catálogos
            rs = stmt.executeQuery("SELECT COUNT(*) FROM adminCatalogo");
            rs.next();
            System.out.println("  • Elementos de catálogo insertados: " + rs.getInt(1));
        }
    }
}
