-- ============================================================================
-- STM-1378: Datos de Prueba para Three Way Match
-- Esquema: tenant_finance
-- Fecha: 2026-02-11
-- ============================================================================

SET search_path TO tenant_finance;

-- ============================================================================
-- 1. PROVEEDOR 100001: Caso perfecto (Conciliado)
-- ============================================================================

INSERT INTO tenant_finance.three_way_match (
    vendor_number,
    purchase_order_number, purchase_order_date, purchase_order_amount,
    reception_number, reception_date, reception_amount,
    invoice_series, invoice_folio, invoice_uuid, invoice_stamp_date, invoice_amount,
    document_number, sap_document, accounting_date, accounting_amount,
    payment_reference, payment_date, payment_amount,
    currency, exchange_rate,
    status, created_by
) VALUES
(
    100001,
    'OC-2026-00001', '2026-01-15', 50000.00,
    'REC-2026-00001', '2026-01-20', 50000.00,
    'A', '1001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '2026-01-25 10:30:00', 50000.00,
    'DOC-100001', 'SAP-500001', '2026-01-26', 50000.00,
    'PAY-2026-00001', '2026-02-01', 50000.00,
    'MXN', 1.000000,
    3, 1  -- Conciliado
);


-- ============================================================================
-- 2. PROVEEDOR 100001: Pendiente (sin recepcion ni factura)
-- ============================================================================

INSERT INTO tenant_finance.three_way_match (
    vendor_number,
    purchase_order_number, purchase_order_date, purchase_order_amount,
    currency, exchange_rate,
    status, created_by
) VALUES
(
    100001,
    'OC-2026-00002', '2026-02-01', 75000.00,
    'MXN', 1.000000,
    1, 1  -- Pendiente
);


-- ============================================================================
-- 3. PROVEEDOR 100002: Discrepancia (OC > Recepcion)
-- ============================================================================

INSERT INTO tenant_finance.three_way_match (
    vendor_number,
    purchase_order_number, purchase_order_date, purchase_order_amount,
    reception_number, reception_date, reception_amount,
    invoice_series, invoice_folio, invoice_uuid, invoice_stamp_date, invoice_amount,
    currency, exchange_rate,
    status, created_by
) VALUES
(
    100002,
    'OC-2026-00003', '2026-01-10', 100000.00,
    'REC-2026-00003', '2026-01-18', 80000.00,
    'B', '2001', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', '2026-01-20 14:00:00', 80000.00,
    'MXN', 1.000000,
    4, 1  -- Discrepancia
);


-- ============================================================================
-- 4. PROVEEDOR 100002: Parcial (OC + Recepcion, sin factura)
-- ============================================================================

INSERT INTO tenant_finance.three_way_match (
    vendor_number,
    purchase_order_number, purchase_order_date, purchase_order_amount,
    reception_number, reception_date, reception_amount,
    currency, exchange_rate,
    status, created_by
) VALUES
(
    100002,
    'OC-2026-00004', '2026-01-25', 30000.00,
    'REC-2026-00004', '2026-01-28', 30000.00,
    'MXN', 1.000000,
    2, 1  -- Parcial
);


-- ============================================================================
-- 5. PROVEEDOR 100003: En dolares (USD)
-- ============================================================================

INSERT INTO tenant_finance.three_way_match (
    vendor_number,
    purchase_order_number, purchase_order_date, purchase_order_amount,
    reception_number, reception_date, reception_amount,
    invoice_series, invoice_folio, invoice_uuid, invoice_stamp_date, invoice_amount,
    document_number, sap_document, accounting_date, accounting_amount,
    payment_reference, payment_date, payment_amount,
    currency, exchange_rate,
    status, created_by
) VALUES
(
    100003,
    'OC-2026-00005', '2026-01-05', 5000.00,
    'REC-2026-00005', '2026-01-12', 5000.00,
    'C', '3001', 'c3d4e5f6-a7b8-9012-cdef-345678901234', '2026-01-15 09:00:00', 5000.00,
    'DOC-100003', 'SAP-500003', '2026-01-16', 100000.00,
    'PAY-2026-00003', '2026-01-30', 5000.00,
    'USD', 20.000000,
    3, 1  -- Conciliado
);


-- ============================================================================
-- 6. PROVEEDOR 100003: Cerrado (con nota de credito)
-- ============================================================================

INSERT INTO tenant_finance.three_way_match (
    vendor_number,
    purchase_order_number, purchase_order_date, purchase_order_amount,
    reception_number, reception_date, reception_amount,
    invoice_series, invoice_folio, invoice_uuid, invoice_stamp_date, invoice_amount,
    credit_note_number, credit_note_amount,
    currency, exchange_rate,
    status, created_by
) VALUES
(
    100003,
    'OC-2026-00006', '2025-12-01', 15000.00,
    'REC-2025-00006', '2025-12-10', 15000.00,
    'D', '4001', 'd4e5f6a7-b8c9-0123-defa-456789012345', '2025-12-15 11:00:00', 15000.00,
    'NC-2025-00001', 15000.00,
    'MXN', 1.000000,
    5, 1  -- Cerrado
);


-- ============================================================================
-- VERIFICACION
-- ============================================================================

SELECT
    vendor_number,
    purchase_order_number,
    reception_number,
    CASE status
        WHEN 1 THEN 'Pendiente'
        WHEN 2 THEN 'Parcial'
        WHEN 3 THEN 'Conciliado'
        WHEN 4 THEN 'Discrepancia'
        WHEN 5 THEN 'Cerrado'
    END AS estatus,
    purchase_order_amount AS monto_oc,
    reception_amount AS monto_recep,
    invoice_amount AS monto_fact,
    currency
FROM tenant_finance.three_way_match
ORDER BY vendor_number, created_at;


-- ============================================================================
-- FIN
-- ============================================================================
