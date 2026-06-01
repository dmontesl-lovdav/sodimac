import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test de conexión a SODIMAC_SAP_DEV
 *
 * Compile: javac TestSodimacSapConnection.java
 * Run: java -cp ".:target/classes:$HOME/.m2/repository/com/microsoft/sqlserver/mssql-jdbc/12.4.2.jre11/mssql-jdbc-12.4.2.jre11.jar" TestSodimacSapConnection
 */
public class TestSodimacSapConnection {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("TEST DE CONEXIÓN - SODIMAC_SAP_DEV");
        System.out.println("============================================================");
        System.out.println();

        String server = "10.138.153.10";
        String database = "SODIMAC_SAP_DEV";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        System.out.println("Configuración:");
        System.out.println("  Servidor: " + server);
        System.out.println("  Base de datos: " + database);
        System.out.println("  Usuario: " + username);
        System.out.println();

        String connectionUrl = String.format(
            "jdbc:sqlserver://%s:1433;databaseName=%s;user=%s;password=%s;encrypt=false;trustServerCertificate=true;loginTimeout=10",
            server, database, username, password
        );

        try {
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 1: Conexión básica");
            System.out.println("------------------------------------------------------------");

            Connection conn = DriverManager.getConnection(connectionUrl);
            System.out.println("✅ Conexión exitosa a SODIMAC_SAP_DEV");

            Statement stmt = conn.createStatement();

            // Versión del servidor
            ResultSet rs = stmt.executeQuery("SELECT @@VERSION AS Version");
            if (rs.next()) {
                String version = rs.getString("Version").split("\n")[0];
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
                "      SELECT 1 FROM INFORMATION_SCHEMA.TABLES " +
                "      WHERE TABLE_NAME = 'Envios_Ap'" +
                "    ) THEN 'SI' " +
                "    ELSE 'NO' " +
                "  END AS Existe";

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
                        "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_NAME LIKE '%Envio%' OR TABLE_NAME LIKE '%AP%' " +
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
                    "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE " +
                    "FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 'Envios_Ap' " +
                    "ORDER BY ORDINAL_POSITION"
                );

                int columnCount = 0;
                System.out.printf("%-30s %-20s %-10s %-10s%n", "Campo", "Tipo", "Longitud", "Nulo");
                System.out.println("----------------------------------------------------------------------");

                while (rs.next()) {
                    columnCount++;
                    String nombre = rs.getString("COLUMN_NAME");
                    String tipo = rs.getString("DATA_TYPE");
                    String longitud = rs.getString("CHARACTER_MAXIMUM_LENGTH");
                    String nulable = rs.getString("IS_NULLABLE");

                    System.out.printf("%-30s %-20s %-10s %-10s%n",
                        nombre, tipo,
                        longitud != null ? longitud : "-",
                        nulable
                    );
                }
                rs.close();
                System.out.println();
                System.out.println("Columnas encontradas: " + columnCount);
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
            }

            // Test 5: Permisos
            System.out.println("------------------------------------------------------------");
            System.out.println("TEST 5: Permisos del usuario");
            System.out.println("------------------------------------------------------------");

            if (tableExists) {
                rs = stmt.executeQuery(
                    "SELECT " +
                    "  CASE " +
                    "    WHEN HAS_PERMS_BY_NAME('Envios_Ap', 'OBJECT', 'SELECT') = 1 " +
                    "    THEN 'SI' " +
                    "    ELSE 'NO' " +
                    "  END AS Permiso_SELECT"
                );

                if (rs.next()) {
                    String permiso = rs.getString("Permiso_SELECT");
                    if ("SI".equals(permiso)) {
                        System.out.println("✅ Usuario tiene permiso SELECT en Envios_Ap");
                    } else {
                        System.out.println("❌ Usuario NO tiene permiso SELECT en Envios_Ap");
                    }
                }
                rs.close();
            } else {
                System.out.println("⚠️ No se puede verificar permisos (tabla no existe)");
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
            System.out.println("  3. Base de datos no existe");
            System.out.println("  4. Driver JDBC no disponible");
            System.out.println();
            e.printStackTrace();
            System.exit(1);
        }
    }
}
