import java.sql.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

String url = "jdbc:postgresql://localhost:5434/b2b_portal?currentSchema=tenant_fiscal";
String user = "wwwb2bportal";
String pass = "b8@qU0YM1HU>";

try (Connection conn = DriverManager.getConnection(url, user, pass)) {
    conn.setAutoCommit(false);

    // Datos para variar
    String[] issuerUuids = {
        "50f67897-f28f-492c-a80c-0f8523f1837d",  // SC JOHNSON
        "e5eebf2e-2bdd-4061-aa66-21403b849bbe",  // MUELLER
        "33333333-3333-3333-3333-333333333333",  // FERRETERIA INDUSTRIAL
        "55555555-5555-5555-5555-555555555555",  // DISTRIBUIDORA ELECTRICA
        "66666666-6666-6666-6666-666666666666",  // PINTURAS PREMIUM
        "11111111-1111-1111-1111-111111111111"   // SODIMAC
    };
    String[] receiverUuids = {
        "88888888-8888-8888-8888-888888888888",  // COMERCIALIZADORA SDMHC
        "55555555-5555-5555-5555-555555555555",  // SODIMAC MEXICO
        "33333333-3333-3333-3333-333333333333",  // CLIENTE GENERICO
        "aaaa0001-0001-0001-0001-000000000001"   // SODIMAC HOMECENTER
    };
    String[] series = {"FA", "MC", "B", "INV", "A", "FC", "FE", "SER"};
    String[] currencies = {"MXN", "MXN", "MXN", "MXN", "USD"};  // 80% MXN
    int[] statuses = {1, 1, 2, 2, 3, 3, 3, 7, 7, 8, 8, 10, 11, 12, 13};  // distribucion variada
    String[] supplierTypes = {"SODIMAC", "SODIMAC", "SODIMAC", "TOTTUS"};
    int[] supplierNumbers = {12345, 54321, 67890, 11111, 22222, 33333, 44444, 55555};

    Random rnd = new Random(42);
    int invoiceCount = 0;
    int addendumCount = 0;

    String invoiceSql = "INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at, accounting_date) VALUES (?, ?, 'I', ?, ?, ?, ?, ?, ?, 4.0, ?, ?, ?, ?, ?, ?, '06600', ?, ?, 1, ?, ?)";
    String addendumSql = "INSERT INTO addendum (addendum_uuid, invoice_uuid, supplier_number, reception_number, purchase_order_number, shipping_guide_number, supplier_type, addenda_type, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 1, ?)";

    PreparedStatement psInv = conn.prepareStatement(invoiceSql);
    PreparedStatement psAdd = conn.prepareStatement(addendumSql);

    for (int i = 1; i <= 100; i++) {
        String invUuid = String.format("a%07d-b%03d-4c%02d-d%03d-%012d", i, i % 999, i % 99, i % 999, i);
        String fiscUuid = String.format("f%07d-a%03d-4b%02d-c%03d-%012d", i, i % 999, i % 99, i % 999, i);

        int status = statuses[rnd.nextInt(statuses.length)];
        String issuer = issuerUuids[rnd.nextInt(issuerUuids.length)];
        String receiver = receiverUuids[rnd.nextInt(receiverUuids.length)];
        String serie = series[rnd.nextInt(series.length)];
        String currency = currencies[rnd.nextInt(currencies.length)];
        double exchangeRate = currency.equals("USD") ? 17.0 + rnd.nextDouble() * 3.0 : 1.0;
        double subtotal = 5000 + rnd.nextDouble() * 495000;  // 5k a 500k
        double total = subtotal * 1.16;  // con IVA
        int folio = 100000 + i;
        String paymentMethod = rnd.nextBoolean() ? "PPD" : "PUE";
        String paymentForm = paymentMethod.equals("PPD") ? "99" : "01";
        String paymentConditions = paymentMethod.equals("PPD") ? (rnd.nextBoolean() ? "NET 30" : "NET 60") : "CONTADO";

        // Fecha entre 2025-01-01 y 2026-01-31
        int dayOffset = rnd.nextInt(396);  // ~13 meses
        LocalDate issueDate = LocalDate.of(2025, 1, 1).plusDays(dayOffset);
        LocalDateTime certDate = issueDate.atTime(8 + rnd.nextInt(10), rnd.nextInt(60));
        LocalDateTime createdAt = certDate.plusMinutes(rnd.nextInt(60));

        LocalDate accountingDate = null;
        if (status >= 7) {
            accountingDate = issueDate.plusDays(15 + rnd.nextInt(30));
        }

        psInv.setObject(1, UUID.fromString(invUuid));
        psInv.setObject(2, UUID.fromString(fiscUuid));
        psInv.setDouble(3, Math.round(total * 100.0) / 100.0);
        psInv.setDouble(4, Math.round(subtotal * 100.0) / 100.0);
        psInv.setString(5, currency);
        psInv.setDouble(6, Math.round(exchangeRate * 1000000.0) / 1000000.0);
        psInv.setString(7, serie);
        psInv.setString(8, String.valueOf(folio));
        psInv.setInt(9, status);
        psInv.setDate(10, java.sql.Date.valueOf(issueDate));
        psInv.setTimestamp(11, Timestamp.valueOf(certDate));
        psInv.setString(12, paymentMethod);
        psInv.setString(13, paymentForm);
        psInv.setString(14, paymentConditions);
        psInv.setObject(15, UUID.fromString(issuer));
        psInv.setObject(16, UUID.fromString(receiver));
        psInv.setTimestamp(17, Timestamp.valueOf(createdAt));
        if (accountingDate != null) {
            psInv.setDate(18, java.sql.Date.valueOf(accountingDate));
        } else {
            psInv.setNull(18, Types.DATE);
        }
        psInv.executeUpdate();
        invoiceCount++;

        // Addenda para facturas con estatus >= 2
        if (status >= 2) {
            String addUuid = String.format("ad%06d-a%03d-4c%02d-d%03d-%012d", i, i % 999, i % 99, i % 999, i);
            int supplierNum = supplierNumbers[rnd.nextInt(supplierNumbers.length)];
            String recNumber = String.format("REC-2025-%05d", i);
            String poNumber = String.format("OC-2025-%05d", 2000 + i);
            String guideNumber = rnd.nextBoolean() ? String.format("GE-%05d", i) : null;
            String supplierType = supplierTypes[rnd.nextInt(supplierTypes.length)];

            psAdd.setObject(1, UUID.fromString(addUuid));
            psAdd.setObject(2, UUID.fromString(invUuid));
            psAdd.setInt(3, supplierNum);
            psAdd.setString(4, recNumber);
            psAdd.setString(5, poNumber);
            psAdd.setString(6, guideNumber);
            psAdd.setString(7, supplierType);
            psAdd.setTimestamp(8, Timestamp.valueOf(createdAt.plusHours(1)));
            psAdd.executeUpdate();
            addendumCount++;
        }
    }

    conn.commit();
    System.out.println("=== COMMIT EXITOSO ===");
    System.out.println("Facturas insertadas: " + invoiceCount);
    System.out.println("Addendas insertadas: " + addendumCount);

    // Resumen total
    ResultSet rs = conn.createStatement().executeQuery("SELECT document_type, status, COUNT(*) FROM invoice GROUP BY document_type, status ORDER BY document_type, status");
    System.out.println("\nResumen total por tipo y estatus:");
    int total = 0;
    while (rs.next()) {
        int cnt = rs.getInt(3);
        total += cnt;
        System.out.printf("Tipo: %s | Estatus: %2d | Cantidad: %d%n", rs.getString(1), rs.getInt(2), cnt);
    }
    System.out.println("Total documentos: " + total);

    rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM addendum");
    rs.next();
    System.out.printf("Total addendas: %d%n", rs.getInt(1));

} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
    e.printStackTrace();
}

/exit
