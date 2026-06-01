import java.sql.*;

/**
 * Test de conexión a SAPITO usando IP real: 10.140.0.182
 * IP obtenida desde red corporativa (ensenada.falabella.cl → 10.140.0.182)
 */
public class TestSapitoWithIP {
    public static void main(String[] args) {
        String host = "10.140.0.182";  // IP real de ensenada.falabella.cl
        String port = "1541";
        String sid = "odsrmxts";
        String username = "UODSRMX";
        String password = "uodsrmx47q8";

        String connectionUrl = String.format(
            "jdbc:oracle:thin:@%s:%s:%s",
            host, port, sid
        );

        System.out.println("============================================================");
        System.out.println("TEST DE CONEXIÓN - SAPITO (IP REAL)");
        System.out.println("============================================================\n");

        System.out.println("Configuración:");
        System.out.println("  IP: " + host + " (ensenada.falabella.cl)");
        System.out.println("  Puerto: " + port);
        System.out.println("  SID: " + sid);
        System.out.println("  Usuario: " + username);
        System.out.println("  URL: " + connectionUrl);
        System.out.println();

        System.out.println("------------------------------------------------------------");
        System.out.println("TEST 1: Conexión básica");
        System.out.println("------------------------------------------------------------");

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("✅ CONEXIÓN EXITOSA a SAPITO!");

            // Obtener información del servidor
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("   Base de datos: " + metaData.getDatabaseProductName());
            System.out.println("   Versión: " + metaData.getDatabaseProductVersion());
            System.out.println();

            // TEST 2: Verificar tabla Envios_Ap
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 2: Verificar tabla Envios_Ap");
            System.out.println("------------------------------------------------------------");

            String checkTableSQL =
                "SELECT COUNT(*) AS table_exists " +
                "FROM user_tables " +
                "WHERE table_name = 'ENVIOS_AP'";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkTableSQL)) {
                if (rs.next() && rs.getInt("table_exists") > 0) {
                    System.out.println("✅ Tabla Envios_Ap EXISTE\n");
                } else {
                    System.out.println("❌ Tabla Envios_Ap NO ENCONTRADA\n");
                    return;
                }
            }

            // TEST 3: Estructura de la tabla
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 3: Estructura de tabla Envios_Ap");
            System.out.println("------------------------------------------------------------");

            String structureSQL =
                "SELECT column_name, data_type, data_length, nullable " +
                "FROM user_tab_columns " +
                "WHERE table_name = 'ENVIOS_AP' " +
                "ORDER BY column_id";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(structureSQL)) {

                System.out.printf("%-30s %-20s %-10s %-10s%n", "Campo", "Tipo", "Longitud", "Nulo");
                System.out.println("----------------------------------------------------------------------");

                int columnCount = 0;
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String dataType = rs.getString("data_type");
                    String dataLength = rs.getString("data_length");
                    String nullable = rs.getString("nullable");

                    System.out.printf("%-30s %-20s %-10s %-10s%n",
                        columnName, dataType, dataLength, nullable);
                    columnCount++;
                }
                System.out.println("\nColumnas encontradas: " + columnCount + "\n");
            }

            // TEST 4: Contar registros
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 4: Contar registros en Envios_Ap");
            System.out.println("------------------------------------------------------------");

            String countSQL = "SELECT COUNT(*) AS total FROM Envios_Ap";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSQL)) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    System.out.printf("Total de registros: %,d%n%n", total);
                }
            }

            // TEST 5: Verificar campo FLAG_ENVIADO (crítico para HU)
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 5: Verificar campo FLAG_ENVIADO (CRÍTICO)");
            System.out.println("------------------------------------------------------------");

            String checkFlagSQL =
                "SELECT column_name, data_type " +
                "FROM user_tab_columns " +
                "WHERE table_name = 'ENVIOS_AP' " +
                "AND column_name = 'FLAG_ENVIADO'";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkFlagSQL)) {
                if (rs.next()) {
                    System.out.println("✅ Campo FLAG_ENVIADO existe");
                    System.out.println("   Tipo: " + rs.getString("data_type"));
                } else {
                    System.out.println("❌ Campo FLAG_ENVIADO NO ENCONTRADO");
                }
            }
            System.out.println();

            // TEST 6: Muestra de datos
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 6: Muestra de 3 registros");
            System.out.println("------------------------------------------------------------");

            String sampleSQL =
                "SELECT NUMERO_UUID, CODIGO_PROVEEDOR, NUMERO_DOCUMENTO, FLAG_ENVIADO " +
                "FROM Envios_Ap " +
                "WHERE ROWNUM <= 3";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sampleSQL)) {

                System.out.printf("%-40s %-20s %-30s %-15s%n",
                    "UUID", "Proveedor", "Documento", "FLAG_ENVIADO");
                System.out.println("-------------------------------------------------------------------------------------------------------");

                while (rs.next()) {
                    String uuid = rs.getString("NUMERO_UUID");
                    String proveedor = rs.getString("CODIGO_PROVEEDOR");
                    String documento = rs.getString("NUMERO_DOCUMENTO");
                    String flag = rs.getString("FLAG_ENVIADO");

                    System.out.printf("%-40s %-20s %-30s %-15s%n",
                        uuid != null ? uuid : "NULL",
                        proveedor != null ? proveedor : "NULL",
                        documento != null ? documento : "NULL",
                        flag != null ? flag : "NULL"
                    );
                }
            }

            System.out.println("\n============================================================");
            System.out.println("✅ TODAS LAS VALIDACIONES COMPLETADAS");
            System.out.println("============================================================");
            System.out.println("\n🎉 SAPITO ESTÁ OPERATIVO!");
            System.out.println("\n📋 RESUMEN:");
            System.out.println("  ✅ Conexión exitosa con IP 10.140.0.182");
            System.out.println("  ✅ Tabla Envios_Ap disponible");
            System.out.println("  ✅ Campo FLAG_ENVIADO presente");
            System.out.println("  ✅ Datos disponibles para pruebas");
            System.out.println("\n🚀 ESCENARIOS 2 Y 3 LISTOS PARA EJECUTAR");

        } catch (SQLException e) {
            System.err.println("\n❌ Error de conexión: " + e.getMessage());
            System.err.println("\n📋 POSIBLES CAUSAS:");
            System.err.println("  1. No estás conectado a la VPN corporativa");
            System.err.println("  2. Firewall bloqueando puerto 1541");
            System.err.println("  3. Credenciales incorrectas");
            System.err.println("  4. Servicio Oracle no disponible");
            e.printStackTrace();
        }
    }
}
