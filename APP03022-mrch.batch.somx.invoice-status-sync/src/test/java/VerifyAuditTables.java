import java.sql.*;

/**
 * Verificar registros en tablas de auditoría
 */
public class VerifyAuditTables {

    public static void main(String[] args) {
        String connectionUrl = "jdbc:sqlserver://10.138.153.10:1433;databaseName=SODIMAC_BATCH_DEV;encrypt=true;trustServerCertificate=true";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("✓ Conectado a SODIMAC_BATCH_DEV");
            System.out.println("\n=== VERIFICACIÓN DE REGISTROS DE AUDITORÍA ===\n");

            // 1. CtrlEnlace (Ejecuciones del batch)
            System.out.println("1. CtrlEnlace (Ejecuciones del batch):");
            System.out.println("---------------------------------------");
            String query1 = "SELECT TOP 3 execution_id, start_time, total_processed, success_count, error_count, skipped_count FROM CtrlEnlace ORDER BY start_time DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query1)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  Ejecución " + count + ":");
                    System.out.println("    Execution ID: " + rs.getString("execution_id"));
                    System.out.println("    Hora: " + rs.getTimestamp("start_time"));
                    System.out.println("    Procesados: " + rs.getInt("total_processed"));
                    System.out.println("    Exitosos: " + rs.getInt("success_count"));
                    System.out.println("    Errores: " + rs.getInt("error_count"));
                    System.out.println("    Omitidos: " + rs.getInt("skipped_count"));
                    System.out.println();
                }
                if (count == 0) {
                    System.out.println("  (Sin registros)\n");
                } else {
                    System.out.println("  Total de ejecuciones registradas: " + getCount(conn, "CtrlEnlace") + "\n");
                }
            }

            // 2. CtrlEnlace_CifrasControl (Fotografía antes/después)
            System.out.println("2. CtrlEnlace_CifrasControl (Cifras antes/después):");
            System.out.println("---------------------------------------------------");
            String query2 = "SELECT TOP 3 execution_id, phase, total_invoices, pending_sync, synced, failed FROM CtrlEnlace_CifrasControl ORDER BY captured_at DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query2)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  Registro " + count + ":");
                    System.out.println("    Execution ID: " + rs.getString("execution_id"));
                    System.out.println("    Fase: " + rs.getString("phase"));
                    System.out.println("    Total facturas: " + rs.getInt("total_invoices"));
                    System.out.println("    Pendientes: " + rs.getInt("pending_sync"));
                    System.out.println("    Sincronizadas: " + rs.getInt("synced"));
                    System.out.println("    Fallidas: " + rs.getInt("failed"));
                    System.out.println();
                }
                if (count == 0) {
                    System.out.println("  (Sin registros)\n");
                } else {
                    System.out.println("  Total de registros: " + getCount(conn, "CtrlEnlace_CifrasControl") + "\n");
                }
            }

            // 3. CtrlEnlace_Comprobante_ControlDocumento (Detalle de transiciones)
            System.out.println("3. CtrlEnlace_Comprobante_ControlDocumento (Transiciones):");
            System.out.println("----------------------------------------------------------");
            String query3 = "SELECT TOP 5 execution_id, id_proveedor, document_number, previous_status, new_status, success, message FROM CtrlEnlace_Comprobante_ControlDocumento ORDER BY processed_at DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query3)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  Transición " + count + ":");
                    System.out.println("    Execution ID: " + rs.getString("execution_id"));
                    System.out.println("    Proveedor: " + rs.getString("id_proveedor"));
                    System.out.println("    Documento: " + rs.getString("document_number"));
                    System.out.println("    Estado anterior: " + rs.getInt("previous_status"));
                    System.out.println("    Estado nuevo: " + rs.getInt("new_status"));
                    System.out.println("    Éxito: " + (rs.getBoolean("success") ? "Sí" : "No"));
                    System.out.println("    Mensaje: " + rs.getString("message"));
                    System.out.println();
                }
                if (count == 0) {
                    System.out.println("  (Sin registros)\n");
                } else {
                    System.out.println("  Total de transiciones: " + getCount(conn, "CtrlEnlace_Comprobante_ControlDocumento") + "\n");
                }
            }

            // 4. CtrlEnlaceLog (Errores)
            System.out.println("4. CtrlEnlaceLog (Registro de errores):");
            System.out.println("----------------------------------------");
            String query4 = "SELECT TOP 3 execution_id, log_level, message FROM CtrlEnlaceLog ORDER BY logged_at DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query4)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  Error " + count + ":");
                    System.out.println("    Execution ID: " + rs.getString("execution_id"));
                    System.out.println("    Nivel: " + rs.getString("log_level"));
                    System.out.println("    Mensaje: " + rs.getString("message"));
                    System.out.println();
                }
                if (count == 0) {
                    System.out.println("  (Sin registros)\n");
                } else {
                    System.out.println("  Total de errores: " + getCount(conn, "CtrlEnlaceLog") + "\n");
                }
            }

            System.out.println("=== RESUMEN ===");
            System.out.println("✓ Ejecuciones: " + getCount(conn, "CtrlEnlace"));
            System.out.println("✓ Cifras: " + getCount(conn, "CtrlEnlace_CifrasControl"));
            System.out.println("✓ Transiciones: " + getCount(conn, "CtrlEnlace_Comprobante_ControlDocumento"));
            System.out.println("✓ Errores: " + getCount(conn, "CtrlEnlaceLog"));

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getCount(Connection conn, String tableName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
