import java.sql.*;
import java.io.*;
import java.nio.file.*;

/**
 * Crear tablas de auditoría en SODIMAC_BATCH_DEV
 */
public class CreateBatchTables {

    public static void main(String[] args) {
        String connectionUrl = "jdbc:sqlserver://10.138.153.10:1433;databaseName=SODIMAC_BATCH_DEV;encrypt=true;trustServerCertificate=true";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("✓ Conectado a SODIMAC_BATCH_DEV");

            // Leer script SQL
            String scriptPath = "src/main/resources/database/02_create_tables_jdbc.sql";
            String script = new String(Files.readAllBytes(Paths.get(scriptPath)));

            System.out.println("\nEjecutando script de creación de tablas...");

            // Dividir por bloques BEGIN/END
            String[] blocks = script.split("(?<=END)\\s*(?=--|IF NOT EXISTS)");

            int executedBlocks = 0;
            int skippedBlocks = 0;

            for (String block : blocks) {
                // Limpiar comentarios
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
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(finalBlock);
                        executedBlocks++;

                        // Detectar qué tabla se creó
                        if (finalBlock.contains("CtrlEnlace_Comprobante")) {
                            System.out.println("  ✓ CtrlEnlace_Comprobante_ControlDocumento");
                        } else if (finalBlock.contains("CtrlEnlace_CifrasControl")) {
                            System.out.println("  ✓ CtrlEnlace_CifrasControl");
                        } else if (finalBlock.contains("CtrlEnlaceLog") && !finalBlock.contains("CifrasControl")) {
                            System.out.println("  ✓ CtrlEnlaceLog");
                        } else if (finalBlock.contains("CREATE TABLE CtrlEnlace") && !finalBlock.contains("Log") && !finalBlock.contains("Cifras") && !finalBlock.contains("Comprobante")) {
                            System.out.println("  ✓ CtrlEnlace");
                        } else if (finalBlock.contains("catCatalogo") && !finalBlock.contains("admin")) {
                            System.out.println("  ✓ catCatalogo (existente)");
                        } else if (finalBlock.contains("adminCatalogo")) {
                            System.out.println("  ✓ adminCatalogo (existente)");
                        } else if (finalBlock.contains("ctrlProcesoCab")) {
                            System.out.println("  ✓ ctrlProcesoCab (existente)");
                        } else if (finalBlock.contains("ctrlProcesoDet")) {
                            System.out.println("  ✓ ctrlProcesoDet (existente)");
                        } else if (finalBlock.contains("ctrlProcesoElemento")) {
                            System.out.println("  ✓ ctrlProcesoElemento (existente)");
                        } else if (finalBlock.contains("ctrlLog")) {
                            System.out.println("  ✓ ctrlLog (existente)");
                        }
                    } catch (SQLException e) {
                        if (e.getMessage().contains("already an object")) {
                            skippedBlocks++;
                        } else {
                            System.err.println("Error ejecutando bloque " + (executedBlocks + 1) + ": " + e.getMessage());
                            throw e;
                        }
                    }
                }
            }

            System.out.println("\n✓ Script ejecutado: " + executedBlocks + " bloques ejecutados, " + skippedBlocks + " omitidos");

            // Verificar tablas creadas
            System.out.println("\nVerificando tablas requeridas:");
            String[] requiredTables = {
                "CtrlEnlace",
                "CtrlEnlace_CifrasControl",
                "CtrlEnlace_Comprobante_ControlDocumento",
                "CtrlEnlaceLog"
            };

            for (String table : requiredTables) {
                String checkQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                    pstmt.setString(1, table);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("  ✓ " + table);
                        } else {
                            System.out.println("  ✗ " + table + " - NO CREADA");
                        }
                    }
                }
            }

            System.out.println("\n✅ Proceso completado exitosamente");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
