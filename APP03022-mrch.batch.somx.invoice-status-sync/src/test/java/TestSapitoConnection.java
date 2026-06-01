import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test de conexión a SAPITO (Oracle)
 *
 * Run: mvn test-compile exec:java -Dexec.mainClass="TestSapitoConnection" -Dexec.classpathScope=test -q
 */
public class TestSapitoConnection {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("TEST DE CONEXIÓN - SAPITO (Oracle)");
        System.out.println("============================================================");
        System.out.println();

        String host = "ensenada";
        String port = "1541";
        String sid = "odsrmxts";
        String username = "UODSRMX";
        String password = "uodsrmx47q8";

        System.out.println("Configuración:");
        System.out.println("  Host: " + host);
        System.out.println("  Puerto: " + port);
        System.out.println("  SID: " + sid);
        System.out.println("  Usuario: " + username);
        System.out.println();

        String connectionUrl = String.format(
            "jdbc:oracle:thin:@%s:%s:%s",
            host, port, sid
        );

        try {
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 1: Conexión básica");
            System.out.println("------------------------------------------------------------");

            Connection conn = DriverManager.getConnection(connectionUrl, username, password);
            System.out.println("✅ Conexión exitosa a SAPITO (Oracle)");

            Statement stmt = conn.createStatement();

            // Versión del servidor
            ResultSet rs = stmt.executeQuery("SELECT * FROM V$VERSION WHERE ROWNUM = 1");
            if (rs.next()) {
                String version = rs.getString(1);
                System.out.println("   Versión: " + version);
            }
            rs.close();
            System.out.println();

            // Test 2: Verificar tabla Envios_Ap
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 2: Verificar tabla Envios_Ap");
            System.out.println("------------------------------------------------------------");

            String checkTableSql =
                "SELECT " +
                "  CASE " +
                "    WHEN EXISTS (" +
                "      SELECT 1 FROM USER_TABLES " +
                "      WHERE TABLE_NAME = 'Envios_Ap'" +
                "    ) THEN 'SI' " +
                "    ELSE 'NO' " +
                "  END AS Existe " +
                "FROM DUAL";

            rs = stmt.executeQuery(checkTableSql);
            boolean tableExists = false;
            if (rs.next()) {
                String existe = rs.getString("Existe");
                tableExists = "SI".equals(existe);

                if (tableExists) {
                    System.out.println("✅ Tabla Envios_Ap EXISTE");
                } else {
                    System.out.println("❌ Tabla Envios_Ap NO EXISTE");
                    System.out.println();
                    System.out.println("Buscando tablas similares...");

                    ResultSet tables = stmt.executeQuery(
                        "SELECT TABLE_NAME FROM USER_TABLES " +
                        "WHERE TABLE_NAME LIKE '%ENVIO%' OR TABLE_NAME LIKE '%AP%' " +
                        "ORDER BY TABLE_NAME"
                    );

                    boolean found = false;
                    while (tables.next()) {
                        if (!found) {
                            System.out.println("Tablas encontradas:");
                            found = true;
                        }
                        System.out.println("  - " + tables.getString("TABLE_NAME"));
                    }
                    if (!found) {
                        System.out.println("  No se encontraron tablas similares");
                    }
                    tables.close();
                }
            }
            rs.close();
            System.out.println();

            // Test 3: Estructura de tabla (solo si existe)
            if (tableExists) {
                System.out.println("------------------------------------------------------------");
                System.out.println("TEST 3: Estructura de tabla Envios_Ap");
                System.out.println("------------------------------------------------------------");

                rs = stmt.executeQuery(
                    "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE " +
                    "FROM USER_TAB_COLUMNS " +
                    "WHERE TABLE_NAME = 'Envios_Ap' " +
                    "ORDER BY COLUMN_ID"
                );

                int columnCount = 0;
                System.out.printf("%-30s %-20s %-10s %-10s%n", "Campo", "Tipo", "Longitud", "Nulo");
                System.out.println("----------------------------------------------------------------------");

                boolean hasFlagEnviado = false;
                while (rs.next()) {
                    columnCount++;
                    String nombre = rs.getString("COLUMN_NAME");
                    String tipo = rs.getString("DATA_TYPE");
                    String longitud = rs.getString("DATA_LENGTH");
                    String nullable = rs.getString("NULLABLE");

                    if ("FLAG_ENVIADO".equalsIgnoreCase(nombre)) {
                        hasFlagEnviado = true;
                    }

                    System.out.printf("%-30s %-20s %-10s %-10s%n",
                        nombre, tipo,
                        longitud != null ? longitud : "-",
                        nullable
                    );
                }
                rs.close();
                System.out.println();
                System.out.println("Columnas encontradas: " + columnCount);

                if (hasFlagEnviado) {
                    System.out.println("✅ Campo FLAG_ENVIADO encontrado (CRÍTICO para HU)");
                } else {
                    System.out.println("⚠️ Campo FLAG_ENVIADO NO encontrado");
                }
                System.out.println();

                // Test 4: Contar registros
                System.out.println("------------------------------------------------------------");
                System.out.println("TEST 4: Contar registros en Envios_Ap");
                System.out.println("------------------------------------------------------------");

                rs = stmt.executeQuery("SELECT COUNT(*) AS Total FROM Envios_Ap");
                if (rs.next()) {
                    int total = rs.getInt("Total");
                    System.out.println("Total de registros: " + String.format("%,d", total));
                }
                rs.close();
                System.out.println();

                // Test 5: Validar FLAG_ENVIADO
                if (hasFlagEnviado) {
                    System.out.println("------------------------------------------------------------");
                    System.out.println("TEST 5: Análisis del campo FLAG_ENVIADO");
                    System.out.println("------------------------------------------------------------");

                    rs = stmt.executeQuery(
                        "SELECT FLAG_ENVIADO, COUNT(*) AS Total " +
                        "FROM Envios_Ap " +
                        "WHERE FLAG_ENVIADO IS NOT NULL " +
                        "GROUP BY FLAG_ENVIADO " +
                        "ORDER BY FLAG_ENVIADO"
                    );

                    System.out.println("Valores distintos de FLAG_ENVIADO:");
                    System.out.printf("%-15s %s%n", "Valor", "Cantidad");
                    System.out.println("-------------------------------");

                    while (rs.next()) {
                        String valor = rs.getString("FLAG_ENVIADO");
                        int cantidad = rs.getInt("Total");
                        System.out.printf("%-15s %,d%n",
                            valor != null ? "'" + valor + "'" : "NULL",
                            cantidad
                        );
                    }
                    rs.close();
                    System.out.println();
                }

                // Test 6: Validar campos requeridos por HU
                System.out.println("------------------------------------------------------------");
                System.out.println("TEST 6: Campos requeridos por HU STM-1309");
                System.out.println("------------------------------------------------------------");

                String[] requiredFields = {
                    "NUMERO_UUID",
                    "CODIGO_PROVEEDOR", "IdProveedor", "ID_PROVEEDOR",
                    "NUMERO_DOCUMENTO", "NumeroDocumento", "NUMERO_DOC",
                    "FLAG_ENVIADO"
                };

                System.out.println("Verificando campos críticos:");
                for (String field : requiredFields) {
                    rs = stmt.executeQuery(
                        "SELECT COUNT(*) AS Exists " +
                        "FROM USER_TAB_COLUMNS " +
                        "WHERE TABLE_NAME = 'Envios_Ap' " +
                        "AND COLUMN_NAME = '" + field + "'"
                    );

                    if (rs.next() && rs.getInt("Exists") > 0) {
                        System.out.println("  ✅ " + field);
                    }
                    rs.close();
                }
            }

            System.out.println();
            System.out.println("============================================================");
            System.out.println("✅ VALIDACIÓN COMPLETADA");
            System.out.println("============================================================");

            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println();
            System.out.println("============================================================");
            System.out.println("❌ ERROR DE CONEXIÓN");
            System.out.println("============================================================");
            System.out.println();
            System.out.println("Error: " + e.getMessage());
            System.out.println();
            System.out.println("Posibles causas:");
            System.out.println("  1. Servidor no accesible (firewall, VPN)");
            System.out.println("  2. Credenciales incorrectas");
            System.out.println("  3. SID incorrecto");
            System.out.println("  4. Driver Oracle JDBC no disponible");
            System.out.println();
            e.printStackTrace();
            System.exit(1);
        }
    }
}
