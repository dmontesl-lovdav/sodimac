import java.sql.*;

String url = "jdbc:postgresql://localhost:5434/b2b_portal?currentSchema=tenant_fiscal";
String user = "wwwb2bportal";
String pass = "b8@qU0YM1HU>";

try (Connection conn = DriverManager.getConnection(url, user, pass)) {
    conn.setAutoCommit(false);
    Statement st = conn.createStatement();

    // RECEIVERS
    st.executeUpdate("INSERT INTO receiver (receiver_uuid, name, rfc, tax_regime, created_by, created_at) VALUES ('aaaa0001-0001-0001-0001-000000000001', 'SODIMAC HOMECENTER MEXICO S.A. DE C.V.', 'SHM130515QR8', '601', 1, NOW()), ('aaaa0002-0002-0002-0002-000000000002', 'FALABELLA RETAIL MEXICO S.A. DE C.V.', 'FRM140820LP3', '601', 1, NOW()) ON CONFLICT DO NOTHING");
    System.out.println("OK: Receivers");

    // FACTURAS estatus 12 (No valido fiscal)
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at) VALUES ('f0000001-0001-4001-a001-000000000001', 'f0000001-a001-4001-b001-000000000001', 'I', 25000.00, 21551.72, 'MXN', 1, 'FA', '10001', 4.0, 12, '2025-06-15', '2025-06-15 10:30:00', 'PPD', '99', 'NET 30', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '88888888-8888-8888-8888-888888888888', 1, '2025-06-15 10:30:00'), ('f0000002-0002-4002-a002-000000000002', 'f0000002-a002-4002-b002-000000000002', 'I', 18500.50, 15948.71, 'MXN', 1, 'A', '20001', 4.0, 12, '2025-07-20', '2025-07-20 14:00:00', 'PUE', '01', 'CONTADO', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '55555555-5555-5555-5555-555555555555', 1, '2025-07-20 14:00:00')");
    System.out.println("OK: Facturas estatus 12");

    // FACTURAS estatus 1
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at) VALUES ('f0000003-0003-4003-a003-000000000003', 'f0000003-a003-4003-b003-000000000003', 'I', 45000.00, 38793.10, 'MXN', 1, 'B', '30001', 4.0, 1, '2025-08-10', '2025-08-10 09:15:00', 'PPD', '99', 'NET 30', '06600', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 1, '2025-08-10 09:15:00'), ('f0000004-0004-4004-a004-000000000004', 'f0000004-a004-4004-b004-000000000004', 'I', 5200.00, 4482.76, 'USD', 17.50, 'INV', '40001', 4.0, 1, '2025-09-05', '2025-09-05 16:45:00', 'PPD', '99', 'NET 60', '06600', '55555555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 1, '2025-09-05 16:45:00')");
    System.out.println("OK: Facturas estatus 1");

    // FACTURA estatus 2
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at) VALUES ('f0000005-0005-4005-a005-000000000005', 'f0000005-a005-4005-b005-000000000005', 'I', 150000.00, 129310.34, 'MXN', 1, 'FA', '50001', 4.0, 2, '2025-10-01', '2025-10-01 08:00:00', 'PPD', '99', 'NET 30', '06600', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 1, '2025-10-01 08:00:00')");
    System.out.println("OK: Factura estatus 2");

    // FACTURAS estatus 3
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at) VALUES ('f0000006-0006-4006-a006-000000000006', 'f0000006-a006-4006-b006-000000000006', 'I', 32000.00, 27586.21, 'MXN', 1, 'MC', '60001', 4.0, 3, '2025-10-15', '2025-10-15 11:20:00', 'PPD', '99', 'NET 30', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '55555555-5555-5555-5555-555555555555', 1, '2025-10-15 11:20:00'), ('f0000007-0007-4007-a007-000000000007', 'f0000007-a007-4007-b007-000000000007', 'I', 78500.00, 67672.41, 'MXN', 1, 'FA', '70001', 4.0, 3, '2025-11-01', '2025-11-01 13:30:00', 'PPD', '99', 'NET 45', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '88888888-8888-8888-8888-888888888888', 1, '2025-11-01 13:30:00')");
    System.out.println("OK: Facturas estatus 3");

    // FACTURAS estatus 7
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at, accounting_date) VALUES ('f0000008-0008-4008-a008-000000000008', 'f0000008-a008-4008-b008-000000000008', 'I', 55000.00, 47413.79, 'MXN', 1, 'B', '80001', 4.0, 7, '2025-11-10', '2025-11-10 09:00:00', 'PPD', '99', 'NET 30', '06600', '33333333-3333-3333-3333-333333333333', '55555555-5555-5555-5555-555555555555', 1, '2025-11-10 09:00:00', '2025-12-01'), ('f0000009-0009-4009-a009-000000000009', 'f0000009-a009-4009-b009-000000000009', 'I', 12800.00, 11034.48, 'USD', 17.25, 'INV', '90001', 4.0, 7, '2025-11-20', '2025-11-20 15:00:00', 'PPD', '99', 'NET 60', '06600', '55555555-5555-5555-5555-555555555555', '88888888-8888-8888-8888-888888888888', 1, '2025-11-20 15:00:00', '2025-12-15')");
    System.out.println("OK: Facturas estatus 7");

    // FACTURAS estatus 8
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at, accounting_date) VALUES ('f0000010-0010-4010-a010-000000000010', 'f0000010-a010-4010-b010-000000000010', 'I', 95000.00, 81896.55, 'MXN', 1, 'FA', '10002', 4.0, 8, '2025-09-01', '2025-09-01 10:00:00', 'PPD', '99', 'NET 30', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '55555555-5555-5555-5555-555555555555', 1, '2025-09-01 10:00:00', '2025-09-20'), ('f0000011-0011-4011-a011-000000000011', 'f0000011-a011-4011-b011-000000000011', 'I', 42000.00, 36206.90, 'MXN', 1, 'MC', '10003', 4.0, 8, '2025-08-15', '2025-08-15 12:00:00', 'PUE', '01', 'CONTADO', '06600', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 1, '2025-08-15 12:00:00', '2025-09-01')");
    System.out.println("OK: Facturas estatus 8");

    // FACTURA estatus 10
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at, accounting_date) VALUES ('f0000012-0012-4012-a012-000000000012', 'f0000012-a012-4012-b012-000000000012', 'I', 200000.00, 172413.79, 'MXN', 1, 'FA', '10004', 4.0, 10, '2025-06-01', '2025-06-01 08:00:00', 'PPD', '99', 'NET 30', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '55555555-5555-5555-5555-555555555555', 1, '2025-06-01 08:00:00', '2025-07-01')");
    System.out.println("OK: Factura estatus 10");

    // FACTURA estatus 11
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at) VALUES ('f0000013-0013-4013-a013-000000000013', 'f0000013-a013-4013-b013-000000000013', 'I', 67000.00, 57758.62, 'MXN', 1, 'B', '10005', 4.0, 11, '2025-12-01', '2025-12-01 10:00:00', 'PPD', '99', 'NET 30', '06600', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 1, '2025-12-01 10:00:00')");
    System.out.println("OK: Factura estatus 11");

    // NOTAS DE CREDITO
    st.executeUpdate("INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at) VALUES ('e0000001-0001-4001-e001-000000000001', 'e0000001-a001-4001-e001-000000000001', 'E', 15000.00, 12931.03, 'MXN', 1, 'NC', '50001', 4.0, 3, '2025-09-15', '2025-09-15 11:00:00', 'PUE', '01', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '55555555-5555-5555-5555-555555555555', 1, '2025-09-15 11:00:00'), ('e0000002-0002-4002-e002-000000000002', 'e0000002-a002-4002-e002-000000000002', 'E', 8500.00, 7327.59, 'MXN', 1, 'NC', '50002', 4.0, 7, '2025-09-01', '2025-09-01 14:00:00', 'PUE', '01', '06600', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 1, '2025-09-01 14:00:00'), ('e0000003-0003-4003-e003-000000000003', 'e0000003-a003-4003-e003-000000000003', 'E', 50000.00, 43103.45, 'MXN', 1, 'NC', '50003', 4.0, 10, '2025-07-01', '2025-07-01 09:00:00', 'PUE', '01', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '55555555-5555-5555-5555-555555555555', 1, '2025-07-01 09:00:00'), ('e0000004-0004-4004-e004-000000000004', 'e0000004-a004-4004-e004-000000000004', 'E', 3200.00, 2758.62, 'MXN', 1, 'NC', '50004', 4.0, 1, '2025-12-10', '2025-12-10 16:00:00', 'PUE', '01', '06600', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 1, '2025-12-10 16:00:00')");
    System.out.println("OK: Notas de Credito");

    // ADDENDAS
    st.executeUpdate("INSERT INTO addendum (addendum_uuid, invoice_uuid, supplier_number, reception_number, purchase_order_number, shipping_guide_number, supplier_type, addenda_type, created_by, created_at) VALUES ('add00005-0005-4005-a005-000000000005', 'f0000005-0005-4005-a005-000000000005', 67890, 'REC-2025-050', 'OC-2025-1001', 'GE-050', 'SODIMAC', 1, 1, '2025-10-02 08:00:00'), ('add00006-0006-4006-a006-000000000006', 'f0000006-0006-4006-a006-000000000006', 12345, 'REC-2025-060', 'OC-2025-1002', 'GE-060', 'SODIMAC', 1, 1, '2025-10-16 08:00:00'), ('add00007-0007-4007-a007-000000000007', 'f0000007-0007-4007-a007-000000000007', 54321, 'REC-2025-070', 'OC-2025-1003', 'GE-070', 'SODIMAC', 1, 1, '2025-11-02 08:00:00'), ('add00008-0008-4008-a008-000000000008', 'f0000008-0008-4008-a008-000000000008', 11111, 'REC-2025-080', 'OC-2025-1004', 'GE-080', 'SODIMAC', 1, 1, '2025-11-11 08:00:00'), ('add00009-0009-4009-a009-000000000009', 'f0000009-0009-4009-a009-000000000009', 22222, 'REC-2025-090', 'OC-2025-1005', NULL, 'TOTTUS', 2, 1, '2025-11-21 08:00:00'), ('add00010-0010-4010-a010-000000000010', 'f0000010-0010-4010-a010-000000000010', 12345, 'REC-2025-100', 'OC-2025-1006', 'GE-100', 'SODIMAC', 1, 1, '2025-09-02 08:00:00'), ('add00011-0011-4011-a011-000000000011', 'f0000011-0011-4011-a011-000000000011', 67890, 'REC-2025-110', 'OC-2025-1007', 'GE-110', 'SODIMAC', 1, 1, '2025-08-16 08:00:00'), ('add00012-0012-4012-a012-000000000012', 'f0000012-0012-4012-a012-000000000012', 54321, 'REC-2025-120', 'OC-2025-1008', 'GE-120', 'SODIMAC', 1, 1, '2025-06-02 08:00:00'), ('add00013-0013-4013-a013-000000000013', 'f0000013-0013-4013-a013-000000000013', 11111, 'REC-2025-130', 'OC-2025-1009', 'GE-130', 'SODIMAC', 1, 1, '2025-12-02 08:00:00'), ('add0e001-e001-4001-e001-000000000001', 'e0000001-0001-4001-e001-000000000001', 12345, 'REC-2025-NC1', 'OC-2025-1006', NULL, 'SODIMAC', 1, 1, '2025-09-16 08:00:00'), ('add0e002-e002-4002-e002-000000000002', 'e0000002-0002-4002-e002-000000000002', 67890, 'REC-2025-NC2', 'OC-2025-1007', NULL, 'SODIMAC', 1, 1, '2025-09-02 08:00:00'), ('add0e003-e003-4003-e003-000000000003', 'e0000003-0003-4003-e003-000000000003', 54321, 'REC-2025-NC3', 'OC-2025-1008', NULL, 'SODIMAC', 1, 1, '2025-07-02 08:00:00')");
    System.out.println("OK: Addendas");

    // RELATED CFDI
    st.executeUpdate("INSERT INTO related_cfdi (related_cfdi_uuid, invoice_uuid, related_invoice_uuid, relation_type, created_by, created_at) VALUES ('ce100001-0001-4001-a001-000000000001', 'e0000001-0001-4001-e001-000000000001', 'f0000010-0010-4010-a010-000000000010', '01', 1, '2025-09-15 11:00:00'), ('ce200002-0002-4002-a002-000000000002', 'e0000002-0002-4002-e002-000000000002', 'f0000011-0011-4011-a011-000000000011', '01', 1, '2025-09-01 14:00:00'), ('ce300003-0003-4003-a003-000000000003', 'e0000003-0003-4003-e003-000000000003', 'f0000012-0012-4012-a012-000000000012', '01', 1, '2025-07-01 09:00:00')");
    System.out.println("OK: Related CFDI");

    conn.commit();
    System.out.println("\n=== COMMIT EXITOSO ===");

    // Resumen
    ResultSet rs = conn.createStatement().executeQuery("SELECT document_type, status, COUNT(*) FROM invoice GROUP BY document_type, status ORDER BY document_type, status");
    System.out.println("\nResumen por tipo y estatus:");
    while (rs.next()) {
        System.out.printf("Tipo: %s | Estatus: %2d | Cantidad: %d%n", rs.getString(1), rs.getInt(2), rs.getInt(3));
    }

    rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM addendum");
    rs.next();
    System.out.printf("\nTotal addendas: %d%n", rs.getInt(1));

    rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM related_cfdi");
    rs.next();
    System.out.printf("Total relaciones CFDI: %d%n", rs.getInt(1));

} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
}

/exit
