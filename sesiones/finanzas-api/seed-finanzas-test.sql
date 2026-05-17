-- =============================================================================
-- SEED: Datos de prueba para STM-321, STM-1461, STM-1524
-- DB: b2b_portal, schema: tenant_finance
-- Vendors: 11111 (USR_FERNANDO), 22222 (USR_JOSE), -1 = acceso total
-- =============================================================================
-- SET search_path TO tenant_finance, public;

-- =============================================================================
-- THREE_WAY_MATCH (STM-321)
-- vendor_number es VARCHAR(50) → sin problemas de tipo
-- tipoFecha=fechaRecepcion → reception_date debe estar en rango de prueba
-- =============================================================================
INSERT INTO tenant_finance.three_way_match (
    vendor_number, purchase_order_number, purchase_order_date, purchase_order_amount,
    reception_number, reception_date, reception_amount,
    invoice_folio, invoice_uuid, invoice_stamp_date, invoice_amount,
    sap_document, accounting_date, accounting_amount,
    payment_reference, payment_date, payment_amount,
    currency, status, created_by
) VALUES
-- Vendor 11111 - 3 registros
('11111', 'PO-11111-001', '2024-06-01', 50000.00, 'REC-11111-001', '2024-06-15', 50000.00,
 'A001', gen_random_uuid(), '2024-06-20', 50000.00,
 'SAP-11111-001', '2024-07-01', 50000.00, 'PAY-11111-001', '2024-07-15', 50000.00,
 'MXN', 5, 1),
('11111', 'PO-11111-002', '2024-07-01', 30000.00, 'REC-11111-002', '2024-07-10', 30000.00,
 'A002', gen_random_uuid(), '2024-07-15', 30000.00,
 'SAP-11111-002', '2024-08-01', 30000.00, NULL, NULL, NULL,
 'MXN', 3, 1),
('11111', 'PO-11111-003', '2025-01-01', 80000.00, 'REC-11111-003', '2025-01-20', 80000.00,
 'B001', gen_random_uuid(), '2025-01-25', 80000.00,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'MXN', 1, 1),
-- Vendor 22222 - 2 registros
('22222', 'PO-22222-001', '2024-09-01', 45000.00, 'REC-22222-001', '2024-09-15', 45000.00,
 'C001', gen_random_uuid(), '2024-09-20', 45000.00,
 'SAP-22222-001', '2024-10-01', 45000.00, 'PAY-22222-001', '2024-10-15', 45000.00,
 'MXN', 5, 1),
('22222', 'PO-22222-002', '2025-02-01', 20000.00, 'REC-22222-002', '2025-02-10', 20000.00,
 'C002', gen_random_uuid(), '2025-02-15', 20000.00,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'MXN', 1, 1),
-- Vendor 33333 - 1 registro (para probar que -1 devuelve más que filtro por vendor)
('33333', 'PO-33333-001', '2025-03-01', 15000.00, 'REC-33333-001', '2025-03-10', 15000.00,
 'D001', gen_random_uuid(), '2025-03-15', 15000.00,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'MXN', 1, 1);

-- Totales esperados después del seed:
-- vendor 11111 → 3 registros
-- vendor 22222 → 2 registros
-- vendor 11111 + 22222 → 5 registros
-- total (acceso -1) → 6 registros


-- =============================================================================
-- ACCOUNT_STATEMENT (STM-1524)
-- vendor_number es BIGINT, year mínimo 2026
-- =============================================================================
INSERT INTO tenant_finance.account_statement (
    vendor_number, year, month, version, status,
    initial_balance, final_balance,
    period_start_date, period_end_date,
    process_date, created_by, created_at
) VALUES
-- Vendor 11111 - 3 meses
(11111, 2026, 1, 1, 1, 100000.00, 95000.00, '2026-01-01', '2026-01-31', '2026-02-01', 1, NOW()),
(11111, 2026, 2, 1, 1, 95000.00,  88000.00, '2026-02-01', '2026-02-28', '2026-03-01', 1, NOW()),
(11111, 2026, 3, 1, 2, 88000.00,  82000.00, '2026-03-01', '2026-03-31', '2026-04-01', 1, NOW()),
-- Vendor 22222 - 2 meses
(22222, 2026, 1, 1, 1, 200000.00, 185000.00, '2026-01-01', '2026-01-31', '2026-02-01', 1, NOW()),
(22222, 2026, 2, 1, 1, 185000.00, 170000.00, '2026-02-01', '2026-02-28', '2026-03-01', 1, NOW()),
-- Vendor 33333 - 1 mes
(33333, 2026, 1, 1, 1, 50000.00,  47000.00, '2026-01-01', '2026-01-31', '2026-02-01', 1, NOW());

-- Totales esperados después del seed:
-- vendor 11111 → 3 registros
-- vendor 22222 → 2 registros
-- vendor 11111 + 22222 → 5 registros
-- total (acceso -1, year=2026) → 6 registros


-- =============================================================================
-- SHIPPING_GUIDE (STM-1461)
-- vendor_number es BIGINT, source_id=1 (origen existente en migration)
-- delivery_type=1, status=3 (valores de migración inicial)
-- =============================================================================
INSERT INTO tenant_finance.shipping_guide (
    guide_number, vendor_number, truck_plate, driver_name,
    source_id, delivery_type, status,
    delivery_date, created_by
) VALUES
-- Vendor 11111 - 2 guias
('SG-11111-001', 11111, 'AAA-111-MX', 'Conductor A', 1, 1, 3, '2024-06-15 10:00:00', 1),
('SG-11111-002', 11111, 'BBB-111-MX', 'Conductor B', 1, 1, 1, '2025-01-20 09:00:00', 1),
-- Vendor 22222 - 2 guias
('SG-22222-001', 22222, 'CCC-222-MX', 'Conductor C', 1, 1, 3, '2024-09-15 10:00:00', 1),
('SG-22222-002', 22222, 'DDD-222-MX', 'Conductor D', 1, 1, 1, '2025-02-10 09:00:00', 1),
-- Vendor 33333 - 1 guia
('SG-33333-001', 33333, 'EEE-333-MX', 'Conductor E', 1, 1, 1, '2025-03-10 09:00:00', 1);

-- Totales esperados después del seed:
-- vendor 11111 → 2 guias
-- vendor 22222 → 2 guias
-- vendor 11111 + 22222 → 4 guias
-- total (acceso -1) → 5 guias (+ los 2 que ya tenía la migración con 1001 y 1002)


-- =============================================================================
-- VERIFICAR
-- =============================================================================
SELECT 'three_way_match' AS tabla,
       CAST(vendor_number AS TEXT) AS vendor,
       COUNT(*) AS total
FROM tenant_finance.three_way_match GROUP BY vendor_number ORDER BY vendor_number;

SELECT 'account_statement' AS tabla,
       CAST(vendor_number AS TEXT) AS vendor,
       year, COUNT(*) AS total
FROM tenant_finance.account_statement WHERE year = 2026
GROUP BY vendor_number, year ORDER BY vendor_number;

SELECT 'shipping_guide' AS tabla,
       CAST(vendor_number AS TEXT) AS vendor,
       COUNT(*) AS total
FROM tenant_finance.shipping_guide GROUP BY vendor_number ORDER BY vendor_number;
