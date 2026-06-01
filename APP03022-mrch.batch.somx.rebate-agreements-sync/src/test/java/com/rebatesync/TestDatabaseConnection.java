package com.rebatesync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseConnection {

    public static void main(String[] args) {
        String url = "jdbc:sqlserver://10.138.153.10:1433;databaseName=SODIMAC_REBATES_DEV;encrypt=true;trustServerCertificate=true";
        String username = "SodimacETLUSR";
        String password = "$DEV50dimac2026";

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  VALIDANDO CONEXIÓN A SODIMAC_REBATES_DEV                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Servidor: 10.138.153.10:1433");
        System.out.println("Base de Datos: SODIMAC_REBATES_DEV");
        System.out.println("Usuario: SodimacETLUSR");
        System.out.println();

        Connection connection = null;
        Statement statement = null;

        try {
            System.out.println("PASO 1: Intentando conectar al servidor...");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Conexión establecida exitosamente");
            System.out.println();

            statement = connection.createStatement();

            // Verificar base de datos actual
            System.out.println("PASO 2: Verificando base de datos...");
            ResultSet rsDb = statement.executeQuery("SELECT DB_NAME() AS DatabaseName");
            if (rsDb.next()) {
                System.out.println("✓ Base de datos actual: " + rsDb.getString("DatabaseName"));
            }
            rsDb.close();
            System.out.println();

            // Verificar que la tabla RebateAcuerdosTemp existe
            System.out.println("PASO 3: Verificando tabla RebateAcuerdosTemp...");
            ResultSet rsTable = statement.executeQuery(
                "SELECT name FROM sys.tables WHERE name = 'RebateAcuerdosTemp'"
            );
            if (rsTable.next()) {
                System.out.println("✓ Tabla RebateAcuerdosTemp EXISTE");

                // Contar registros
                ResultSet rsCount = statement.executeQuery(
                    "SELECT COUNT(*) AS TotalRegistros FROM RebateAcuerdosTemp"
                );
                if (rsCount.next()) {
                    int total = rsCount.getInt("TotalRegistros");
                    System.out.println("  Total de registros: " + total);
                }
                rsCount.close();

                // Mostrar estructura
                System.out.println();
                System.out.println("PASO 4: Estructura de la tabla:");
                System.out.println("  ─────────────────────────────────────────────");
                ResultSet rsColumns = statement.executeQuery(
                    "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                    "FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 'RebateAcuerdosTemp' " +
                    "ORDER BY ORDINAL_POSITION"
                );
                while (rsColumns.next()) {
                    System.out.printf("  %-30s %-15s %s%n",
                        rsColumns.getString("COLUMN_NAME"),
                        rsColumns.getString("DATA_TYPE"),
                        rsColumns.getString("IS_NULLABLE").equals("YES") ? "NULL" : "NOT NULL"
                    );
                }
                rsColumns.close();

                // Mostrar primeros 5 registros si hay datos
                System.out.println();
                System.out.println("PASO 5: Verificando datos en la tabla...");
                System.out.println("  ─────────────────────────────────────────────");

                ResultSet rsCount2 = statement.executeQuery(
                    "SELECT COUNT(*) AS Total FROM RebateAcuerdosTemp"
                );
                if (rsCount2.next() && rsCount2.getInt("Total") > 0) {
                    ResultSet rsData = statement.executeQuery(
                        "SELECT TOP 5 NumeroProveedor, RazonSocial, NumeroAcuerdo, " +
                        "TipoAcuerdo, Valor, Moneda " +
                        "FROM RebateAcuerdosTemp"
                    );

                    while (rsData.next()) {
                        System.out.println("  Proveedor: " + rsData.getString("NumeroProveedor") +
                                         " - " + rsData.getString("RazonSocial"));
                        System.out.println("  Acuerdo: " + rsData.getString("NumeroAcuerdo") +
                                         " - " + rsData.getString("TipoAcuerdo"));
                        System.out.println("  Valor: " + rsData.getString("Valor") +
                                         " " + rsData.getString("Moneda"));
                        System.out.println("  ─────────────────────────────────────────────");
                    }
                    rsData.close();
                } else {
                    System.out.println("  ⚠ La tabla está vacía (0 registros)");
                    System.out.println("  → La tabla está lista para recibir datos");
                    System.out.println("  → Ejecutar POST /api/sync/full para sincronizar");
                }
                rsCount2.close();
            } else {
                System.out.println("✗ Tabla RebateAcuerdosTemp NO EXISTE");
                System.out.println("  Acción requerida: Crear la tabla");
            }
            rsTable.close();

            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ VALIDACIÓN EXITOSA                                        ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("✓ La aplicación puede conectarse a SODIMAC_REBATES_DEV");
            System.out.println("✓ La tabla RebateAcuerdosTemp está lista para usar");
            System.out.println();

        } catch (Exception e) {
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║  ❌ ERROR DE CONEXIÓN                                         ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Error: " + e.getMessage());
            System.out.println();

            if (e.getMessage().contains("timed out") || e.getMessage().contains("refused")) {
                System.out.println("Causa probable:");
                System.out.println("  ❌ No hay conectividad de red al servidor 10.138.153.10");
                System.out.println("  ❌ El servidor puede estar detrás de un firewall");
                System.out.println("  ❌ Puede requerir VPN o conexión interna");
                System.out.println();
                System.out.println("Solución:");
                System.out.println("  → Conectar a VPN corporativa");
                System.out.println("  → Ejecutar desde un servidor con acceso a la red interna");
            } else if (e.getMessage().contains("Login failed")) {
                System.out.println("Causa probable:");
                System.out.println("  ❌ Credenciales incorrectas");
                System.out.println();
                System.out.println("Solución:");
                System.out.println("  → Verificar usuario y contraseña");
            }

            System.out.println();
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
