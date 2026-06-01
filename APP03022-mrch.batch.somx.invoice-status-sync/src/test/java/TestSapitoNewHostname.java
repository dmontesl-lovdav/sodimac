import java.sql.*;

/**
 * Test de conexión a SAPITO con hostname completo ensenada.falabella.cl
 */
public class TestSapitoNewHostname {
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("TEST DE CONEXIÓN - SAPITO (ensenada.falabella.cl)");
        System.out.println("============================================================\n");

        String[] hostnames = {
            "ensenada.falabella.cl",
            "ensenada.falabella.com",
            "ensenada.sodimac.cl",
            "ensenada.sodimac.com.mx",
            "ensenada"
        };

        String port = "1541";
        String sid = "odsrmxts";
        String username = "UODSRMX";
        String password = "uodsrmx47q8";

        for (String host : hostnames) {
            System.out.println("------------------------------------------------------------");
            System.out.println("Probando: " + host);
            System.out.println("------------------------------------------------------------");

            String connectionUrl = String.format(
                "jdbc:oracle:thin:@%s:%s:%s",
                host, port, sid
            );

            System.out.println("URL: " + connectionUrl);
            System.out.println("Usuario: " + username);

            try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
                System.out.println("✅ CONEXIÓN EXITOSA!");

                // Obtener información del servidor
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("Base de datos: " + metaData.getDatabaseProductName());
                System.out.println("Versión: " + metaData.getDatabaseProductVersion());

                // Intentar listar tablas
                System.out.println("\nProbando query básico...");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM Envios_Ap")) {
                    if (rs.next()) {
                        System.out.println("Registros en Envios_Ap: " + rs.getInt("total"));
                    }
                }

                System.out.println("\n🎉 ¡SAPITO CONECTADO CON HOSTNAME: " + host + "!");
                return; // Salir si la conexión es exitosa

            } catch (SQLException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
            System.out.println();
        }

        System.out.println("============================================================");
        System.out.println("❌ NO SE PUDO CONECTAR CON NINGÚN HOSTNAME");
        System.out.println("============================================================");
        System.out.println("\n📋 CONCLUSIÓN:");
        System.out.println("  - 'ensenada' es un servidor DNS interno");
        System.out.println("  - Requiere conexión a VPN corporativa");
        System.out.println("  - O necesita la IP real del servidor");
        System.out.println("\n💡 SOLUCIÓN:");
        System.out.println("  1. Obtener IP del servidor SAPITO del equipo de infraestructura");
        System.out.println("  2. O configurar DNS corporativo en VPN");
        System.out.println("  3. O validar desde servidor con acceso a red interna");
    }
}
