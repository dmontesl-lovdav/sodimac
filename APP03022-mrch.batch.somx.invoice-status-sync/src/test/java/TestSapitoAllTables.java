import java.sql.*;

/**
 * Buscar tabla Envios_Ap en todos los esquemas accesibles
 */
public class TestSapitoAllTables {
    public static void main(String[] args) {
        String host = "10.140.0.182";
        String port = "1541";
        String sid = "odsrmxts";
        String username = "UODSRMX";
        String password = "uodsrmx47q8";

        String connectionUrl = String.format(
            "jdbc:oracle:thin:@%s:%s:%s",
            host, port, sid
        );

        System.out.println("============================================================");
        System.out.println("BUSCAR TABLA ENVIOS_AP EN SAPITO");
        System.out.println("============================================================\n");

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("✅ Conectado a SAPITO\n");

            // Buscar en todas las tablas accesibles
            System.out.println("------------------------------------------------------------");
            System.out.println("Buscando 'ENVIOS_AP' en todos los esquemas:");
            System.out.println("------------------------------------------------------------");

            String searchSQL =
                "SELECT owner, table_name, num_rows " +
                "FROM all_tables " +
                "WHERE UPPER(table_name) LIKE '%ENVIO%' " +
                "ORDER BY owner, table_name";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(searchSQL)) {

                System.out.printf("%-30s %-40s %15s%n", "Esquema", "Tabla", "Num Rows");
                System.out.println("-----------------------------------------------------------------------------------");

                boolean found = false;
                while (rs.next()) {
                    String owner = rs.getString("owner");
                    String tableName = rs.getString("table_name");
                    String numRows = rs.getString("num_rows");
                    System.out.printf("%-30s %-40s %15s%n",
                        owner, tableName, numRows != null ? numRows : "N/A");
                    found = true;
                }

                if (!found) {
                    System.out.println("(No se encontraron tablas con 'ENVIO')");
                }
                System.out.println();
            }

            // Buscar sinónimos
            System.out.println("------------------------------------------------------------");
            System.out.println("Buscando sinónimos de tablas con 'ENVIO':");
            System.out.println("------------------------------------------------------------");

            String synonymSQL =
                "SELECT synonym_name, table_owner, table_name " +
                "FROM all_synonyms " +
                "WHERE UPPER(synonym_name) LIKE '%ENVIO%' " +
                "   OR UPPER(table_name) LIKE '%ENVIO%'";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(synonymSQL)) {

                System.out.printf("%-30s %-30s %-30s%n", "Sinónimo", "Esquema", "Tabla Real");
                System.out.println("-----------------------------------------------------------------------------------");

                boolean found = false;
                while (rs.next()) {
                    String synonymName = rs.getString("synonym_name");
                    String tableOwner = rs.getString("table_owner");
                    String tableName = rs.getString("table_name");
                    System.out.printf("%-30s %-30s %-30s%n", synonymName, tableOwner, tableName);
                    found = true;
                }

                if (!found) {
                    System.out.println("(No se encontraron sinónimos)");
                }
                System.out.println();
            }

            // Intentar acceso directo a diferentes variaciones
            System.out.println("------------------------------------------------------------");
            System.out.println("Probando acceso directo a tabla:");
            System.out.println("------------------------------------------------------------");

            String[] tableVariations = {
                "Envios_Ap",
                "ENVIOS_AP",
                "envios_ap",
                "Envios_AP"
            };

            for (String table : tableVariations) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM " + table)) {
                    if (rs.next()) {
                        int count = rs.getInt("cnt");
                        System.out.println("✅ Tabla '" + table + "' encontrada con " + count + " registros");

                        // Mostrar estructura si se encontró
                        System.out.println("\n------------------------------------------------------------");
                        System.out.println("Estructura de " + table + ":");
                        System.out.println("------------------------------------------------------------");

                        DatabaseMetaData metaData = conn.getMetaData();
                        try (ResultSet columns = metaData.getColumns(null, null, table.toUpperCase(), null)) {
                            System.out.printf("%-30s %-20s%n", "Columna", "Tipo");
                            System.out.println("----------------------------------------------------");
                            while (columns.next()) {
                                String columnName = columns.getString("COLUMN_NAME");
                                String columnType = columns.getString("TYPE_NAME");
                                System.out.printf("%-30s %-20s%n", columnName, columnType);
                            }
                        }
                        break;
                    }
                } catch (SQLException e) {
                    System.out.println("❌ Tabla '" + table + "' no accesible: " + e.getMessage().split("\n")[0]);
                }
            }

            System.out.println("\n============================================================");
            System.out.println("✅ BÚSQUEDA COMPLETADA");
            System.out.println("============================================================");

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
