import java.sql.*;

/**
 * Test para verificar tablas en SODIMAC_BATCH_DEV
 */
public class TestBatchDatabase {

    public static void main(String[] args) {
        String connectionUrl = "jdbc:sqlserver://10.138.153.10:1433;databaseName=SODIMAC_BATCH_DEV;encrypt=true;trustServerCertificate=true";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("✓ Conectado a SODIMAC_BATCH_DEV");

            // Listar todas las tablas
            String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("\nTablas existentes:");
                System.out.println("==================");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println(count + ". " + rs.getString("TABLE_NAME"));
                }
                System.out.println("\nTotal de tablas: " + count);
            }

            // Verificar tablas específicas requeridas
            String[] requiredTables = {
                "CtrlEnlace",
                "CtrlEnlace_CifrasControl",
                "CtrlEnlace_Comprobante_ControlDocumento",
                "CtrlEnlaceLog",
                "catCatalogo"
            };

            System.out.println("\nVerificación de tablas requeridas:");
            System.out.println("===================================");

            for (String table : requiredTables) {
                String checkQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                    pstmt.setString(1, table);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("✓ " + table + " - EXISTE");
                        } else {
                            System.out.println("✗ " + table + " - NO EXISTE");
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
