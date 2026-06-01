import java.sql.*;

/**
 * Listar todas las tablas disponibles en SAPITO
 */
public class TestSapitoListTables {
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
        System.out.println("LISTAR TABLAS DISPONIBLES EN SAPITO");
        System.out.println("============================================================\n");

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            System.out.println("✅ Conectado a SAPITO\n");

            // Listar todas las tablas del usuario
            System.out.println("------------------------------------------------------------");
            System.out.println("Tablas del usuario UODSRMX:");
            System.out.println("------------------------------------------------------------");

            String listTablesSQL =
                "SELECT table_name, num_rows " +
                "FROM user_tables " +
                "ORDER BY table_name";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(listTablesSQL)) {

                System.out.printf("%-50s %15s%n", "Tabla", "Num Rows");
                System.out.println("-------------------------------------------------------------------");

                int count = 0;
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    String numRows = rs.getString("num_rows");
                    System.out.printf("%-50s %15s%n", tableName, numRows != null ? numRows : "N/A");
                    count++;
                }
                System.out.println("\nTotal de tablas: " + count);
            }

            System.out.println("\n------------------------------------------------------------");
            System.out.println("Buscando tablas que contengan 'ENVIO':");
            System.out.println("------------------------------------------------------------");

            String searchSQL =
                "SELECT table_name " +
                "FROM user_tables " +
                "WHERE UPPER(table_name) LIKE '%ENVIO%'";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(searchSQL)) {

                boolean found = false;
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("table_name"));
                    found = true;
                }
                if (!found) {
                    System.out.println("  (No se encontraron tablas con 'ENVIO')");
                }
            }

            System.out.println("\n============================================================");
            System.out.println("✅ LISTADO COMPLETADO");
            System.out.println("============================================================");

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
