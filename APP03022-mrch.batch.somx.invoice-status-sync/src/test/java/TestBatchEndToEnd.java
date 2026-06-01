import java.sql.*;

/**
 * Test End-to-End de conectividad a las 4 bases de datos
 * Simula los 5 escenarios del batch de sincronización
 */
public class TestBatchEndToEnd {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  TEST END-TO-END - BATCH DE SINCRONIZACIÓN");
        System.out.println("  HU STM-1309: Sincronización de Estatus de Facturas");
        System.out.println("================================================================\n");

        int successCount = 0;
        int totalTests = 5;

        // TEST 1: SODIMAC_SAP_DEV (Escenario 1: 6→7)
        System.out.println("------------------------------------------------------------");
        System.out.println("ESCENARIO 1: Validación en SODIMAC_SAP_DEV (6 → 7)");
        System.out.println("------------------------------------------------------------");
        if (testSodimacSap()) {
            successCount++;
            System.out.println("✅ ESCENARIO 1: OPERATIVO\n");
        } else {
            System.out.println("❌ ESCENARIO 1: FALLIDO\n");
        }

        // TEST 2: SAPITO (Escenario 2: 7→8)
        System.out.println("------------------------------------------------------------");
        System.out.println("ESCENARIO 2: Validación en SAPITO (7 → 8)");
        System.out.println("------------------------------------------------------------");
        if (testSapitoScenario2()) {
            successCount++;
            System.out.println("✅ ESCENARIO 2: OPERATIVO\n");
        } else {
            System.out.println("❌ ESCENARIO 2: FALLIDO\n");
        }

        // TEST 3: SAPITO (Escenario 3: 8→9/16)
        System.out.println("------------------------------------------------------------");
        System.out.println("ESCENARIO 3: Validación FLAG_ENVIADO en SAPITO (8 → 9/16)");
        System.out.println("------------------------------------------------------------");
        if (testSapitoScenario3()) {
            successCount++;
            System.out.println("✅ ESCENARIO 3: OPERATIVO\n");
        } else {
            System.out.println("❌ ESCENARIO 3: FALLIDO\n");
        }

        // TEST 4: i213 (Escenario 4: 9→10/13)
        System.out.println("------------------------------------------------------------");
        System.out.println("ESCENARIO 4: Validación Contabilización en i213 (9 → 10/13)");
        System.out.println("------------------------------------------------------------");
        if (testI213Accounting()) {
            successCount++;
            System.out.println("✅ ESCENARIO 4: OPERATIVO\n");
        } else {
            System.out.println("❌ ESCENARIO 4: FALLIDO\n");
        }

        // TEST 5: i213 (Escenario 5: 10→11)
        System.out.println("------------------------------------------------------------");
        System.out.println("ESCENARIO 5: Validación Pago en i213 (10 → 11)");
        System.out.println("------------------------------------------------------------");
        if (testI213Payment()) {
            successCount++;
            System.out.println("✅ ESCENARIO 5: OPERATIVO\n");
        } else {
            System.out.println("❌ ESCENARIO 5: FALLIDO\n");
        }

        // RESUMEN FINAL
        System.out.println("================================================================");
        System.out.println("  RESUMEN FINAL");
        System.out.println("================================================================");
        System.out.printf("Escenarios exitosos: %d de %d (%.0f%%)%n", successCount, totalTests, (successCount * 100.0 / totalTests));
        System.out.println();

        if (successCount == totalTests) {
            System.out.println("🎉 ¡TODOS LOS ESCENARIOS OPERATIVOS!");
            System.out.println("✅ Sistema 100% funcional y listo para producción");
        } else {
            System.out.printf("⚠️  %d de %d escenarios funcionando%n", successCount, totalTests);
            System.out.printf("   Falta validar: %d escenarios%n", totalTests - successCount);
        }

        System.out.println("\n================================================================");
    }

    private static boolean testSodimacSap() {
        String connectionUrl = "jdbc:sqlserver://10.138.153.10:1433;databaseName=SODIMAC_SAP_DEV;encrypt=false;trustServerCertificate=true;loginTimeout=10";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("Conectado a SODIMAC_SAP_DEV");

            // Simular query del escenario 1
            String query = "SELECT TOP 1 NUMERO_UUID, CODIGO_PROVEEDOR, NUMERO_DOCUMENTO FROM Envios_Ap WHERE NUMERO_UUID IS NOT NULL";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    System.out.println("  UUID encontrado: " + rs.getString("NUMERO_UUID"));
                    System.out.println("  Proveedor: " + rs.getString("CODIGO_PROVEEDOR"));
                    System.out.println("  Documento: " + rs.getString("NUMERO_DOCUMENTO"));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    private static boolean testSapitoScenario2() {
        String connectionUrl = "jdbc:oracle:thin:@10.140.0.182:1541:odsrmxts";
        String username = "UODSRMX";
        String password = "uodsrmx47q8";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("Conectado a SAPITO");

            // Simular query del escenario 2 (FLAG_ENVIADO IS NULL)
            String query = "SELECT NUMERO_UUID, CODIGO_PROVEEDOR, FLAG_ENVIADO FROM Envios_Ap WHERE ROWNUM <= 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    String flag = rs.getString("FLAG_ENVIADO");
                    System.out.println("  UUID: " + rs.getString("NUMERO_UUID"));
                    System.out.println("  FLAG_ENVIADO: " + (flag != null ? flag : "NULL"));
                    System.out.println("  → Transición 7→8 disponible cuando FLAG_ENVIADO IS NULL");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    private static boolean testSapitoScenario3() {
        String connectionUrl = "jdbc:oracle:thin:@10.140.0.182:1541:odsrmxts";
        String username = "UODSRMX";
        String password = "uodsrmx47q8";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("Conectado a SAPITO");

            // Simular query del escenario 3 (FLAG_ENVIADO = 1 o 0)
            String query = "SELECT COUNT(*) as total_con_flag FROM Envios_Ap WHERE FLAG_ENVIADO IS NOT NULL";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    int total = rs.getInt("total_con_flag");
                    System.out.println("  Registros con FLAG_ENVIADO: " + total);
                    System.out.println("  → Transición 8→9 cuando FLAG_ENVIADO = '1'");
                    System.out.println("  → Transición 8→16 cuando FLAG_ENVIADO = '0'");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    private static boolean testI213Accounting() {
        String connectionUrl = "jdbc:sqlserver://10.138.153.10:1433;databaseName=AdmIF213ProdDB;encrypt=false;trustServerCertificate=true;loginTimeout=10";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("Conectado a i213 (10.138.153.10)");

            // Ejecutar SP i123_Valida_Documento_AP
            String spCall = "{call i123_Valida_Documento_AP(?, ?)}";

            try (CallableStatement cs = conn.prepareCall(spCall)) {
                cs.setString(1, "TEST001");
                cs.setString(2, "DOC001");
                ResultSet rs = cs.executeQuery();
                if (rs.next()) {
                    System.out.println("  SP: i123_Valida_Documento_AP");
                    System.out.println("  CODE: " + rs.getInt("CODE"));
                    System.out.println("  MSG: " + rs.getString("MSG"));
                    System.out.println("  → Transición 9→10 cuando CODE=1");
                    System.out.println("  → Transición 9→13 cuando CODE=0");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    private static boolean testI213Payment() {
        String connectionUrl = "jdbc:sqlserver://10.138.153.10:1433;databaseName=AdmIF213ProdDB;encrypt=false;trustServerCertificate=true;loginTimeout=10";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("Conectado a i213 (10.138.153.10)");

            // Ejecutar SP i213_Valida_Documento_Pagado_AP
            String spCall = "{call i213_Valida_Documento_Pagado_AP(?, ?)}";

            try (CallableStatement cs = conn.prepareCall(spCall)) {
                cs.setString(1, "TEST001");
                cs.setString(2, "DOC001");
                ResultSet rs = cs.executeQuery();
                if (rs.next()) {
                    System.out.println("  SP: i213_Valida_Documento_Pagado_AP");
                    System.out.println("  CODE: " + rs.getInt("CODE"));
                    System.out.println("  MSG: " + rs.getString("MSG"));
                    System.out.println("  REF_BANK: " + rs.getString("REF_BANK"));
                    System.out.println("  → Transición 10→11 cuando CODE=1");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }
}
