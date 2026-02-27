-- ============================================================================
-- STM-305: Datos de Prueba para Estado de Cuenta
-- Esquema: tenant_finance
-- Fecha: 2026-02-06
--
-- Escenarios de prueba:
-- - 3 proveedores (1001, 1002, 1003)
-- - Estados de cuenta de Enero 2026
-- - Proveedor 1001: Estado publicado con todos los tipos de detalle
-- - Proveedor 1002: Estado generado (pendiente de publicar)
-- - Proveedor 1003: Estado con reproceso (2 versiones)
-- ============================================================================

SET search_path TO tenant_finance;

-- ============================================================================
-- 1. ESTADOS DE CUENTA (tabla principal)
-- ============================================================================

-- Proveedor 1001: Estado de cuenta Enero 2026 - PUBLICADO
INSERT INTO tenant_finance.account_statement (
    vendor_number, year, month, version, status,
    initial_balance, final_balance,
    process_date, issue_date, period_start_date, period_end_date,
    created_by, created_at
) VALUES (
    1001, 2026, 1, 1, 2,  -- status 2 = Publicado
    50000.00, 65500.00,
    '2026-02-01 08:00:00', '2026-02-01 10:00:00', '2026-01-01', '2026-01-31',
    1, NOW()
);

-- Proveedor 1002: Estado de cuenta Enero 2026 - GENERADO
INSERT INTO tenant_finance.account_statement (
    vendor_number, year, month, version, status,
    initial_balance, final_balance,
    process_date, period_start_date, period_end_date,
    created_by, created_at
) VALUES (
    1002, 2026, 1, 1, 1,  -- status 1 = Generado
    0.00, 25000.00,
    '2026-02-01 09:00:00', '2026-01-01', '2026-01-31',
    1, NOW()
);

-- Proveedor 1003: Estado de cuenta Enero 2026 v1 - REPROCESADO
INSERT INTO tenant_finance.account_statement (
    vendor_number, year, month, version, status,
    initial_balance, final_balance,
    process_date, issue_date, period_start_date, period_end_date,
    created_by, created_at
) VALUES (
    1003, 2026, 1, 1, 5,  -- status 5 = Reprocesado
    10000.00, 35000.00,
    '2026-02-01 08:30:00', '2026-02-01 11:00:00', '2026-01-01', '2026-01-31',
    1, NOW()
);

-- Proveedor 1003: Estado de cuenta Enero 2026 v2 - PUBLICADO (correccion)
INSERT INTO tenant_finance.account_statement (
    vendor_number, year, month, version, status,
    initial_balance, final_balance,
    process_date, issue_date, period_start_date, period_end_date,
    previous_statement_uuid,
    created_by, created_at
) VALUES (
    1003, 2026, 1, 2, 2,  -- status 2 = Publicado
    10000.00, 32000.00,  -- monto corregido
    '2026-02-03 10:00:00', '2026-02-03 12:00:00', '2026-01-01', '2026-01-31',
    (SELECT account_statement_uuid FROM tenant_finance.account_statement WHERE vendor_number = 1003 AND version = 1),
    1, NOW()
);


-- ============================================================================
-- 2. FACTURAS (Proveedor 1001)
-- ============================================================================

-- Obtener el UUID del estado de cuenta del proveedor 1001
DO $$
DECLARE
    v_statement_uuid UUID;
BEGIN
    SELECT account_statement_uuid INTO v_statement_uuid
    FROM tenant_finance.account_statement
    WHERE vendor_number = 1001 AND year = 2026 AND month = 1 AND version = 1;

    -- Factura pendiente 1 (MXN)
    INSERT INTO tenant_finance.account_statement_invoice (
        account_statement_uuid, invoice_type, series, folio, uuid,
        stamp_date, accounting_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        invoice_status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 'PENDING', 'A', '1001', 'UUID-FAC-001-2026',
        '2026-01-15 10:00:00', '2026-01-16 08:00:00',
        'MXN', 15000.00, 1, 'MXN', 15000.00,
        'Pendiente de Pago', 1, NOW()
    );

    -- Factura pendiente 2 (USD)
    INSERT INTO tenant_finance.account_statement_invoice (
        account_statement_uuid, invoice_type, series, folio, uuid,
        stamp_date, accounting_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        invoice_status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 'PENDING', 'B', '2001', 'UUID-FAC-002-2026',
        '2026-01-20 14:00:00', '2026-01-21 09:00:00',
        'USD', 1000.00, 17.50, 'MXN', 17500.00,
        'Pendiente de Pago', 1, NOW()
    );

    -- Factura pagada
    INSERT INTO tenant_finance.account_statement_invoice (
        account_statement_uuid, invoice_type, series, folio, uuid,
        stamp_date, accounting_date, payment_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        invoice_status, payment_id, created_by, created_at
    ) VALUES (
        v_statement_uuid, 'PAID', 'A', '0998', 'UUID-FAC-003-2026',
        '2026-01-05 09:00:00', '2026-01-06 08:00:00', '2026-01-25 16:00:00',
        'MXN', 8000.00, 1, 'MXN', 8000.00,
        'Pagado', 5001, 1, NOW()
    );

END $$;


-- ============================================================================
-- 3. DESCUENTOS (Proveedor 1001)
-- ============================================================================

DO $$
DECLARE
    v_statement_uuid UUID;
BEGIN
    SELECT account_statement_uuid INTO v_statement_uuid
    FROM tenant_finance.account_statement
    WHERE vendor_number = 1001 AND year = 2026 AND month = 1 AND version = 1;

    INSERT INTO tenant_finance.account_statement_discount (
        account_statement_uuid, document_number, reference_number,
        discount_date, accounting_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 'DESC-001', 'PROMO-ENE-2026',
        '2026-01-28 12:00:00', '2026-01-29 08:00:00',
        'MXN', 2000.00, 1, 'MXN', 2000.00,
        'Aplicado', 1, NOW()
    );

END $$;


-- ============================================================================
-- 4. NOTAS DE CREDITO (Proveedor 1001)
-- ============================================================================

DO $$
DECLARE
    v_statement_uuid UUID;
BEGIN
    SELECT account_statement_uuid INTO v_statement_uuid
    FROM tenant_finance.account_statement
    WHERE vendor_number = 1001 AND year = 2026 AND month = 1 AND version = 1;

    INSERT INTO tenant_finance.account_statement_credit_note (
        account_statement_uuid, document_number, series, folio, uuid,
        issue_date, accounting_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 'NC-001', 'NC', '0001', 'UUID-NC-001-2026',
        '2026-01-22 11:00:00', '2026-01-23 08:00:00',
        'MXN', 3000.00, 1, 'MXN', 3000.00,
        'Aplicada', 1, NOW()
    );

END $$;


-- ============================================================================
-- 5. PAGOS (Proveedor 1001)
-- ============================================================================

DO $$
DECLARE
    v_statement_uuid UUID;
BEGIN
    SELECT account_statement_uuid INTO v_statement_uuid
    FROM tenant_finance.account_statement
    WHERE vendor_number = 1001 AND year = 2026 AND month = 1 AND version = 1;

    INSERT INTO tenant_finance.account_statement_payment (
        account_statement_uuid, payment_id, document_number, reference_number,
        payment_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 5001, 'PAG-001', 'TRANSF-001',
        '2026-01-25 16:00:00',
        'MXN', 8000.00, 1, 'MXN', 8000.00,
        'Aplicado', 1, NOW()
    );

    INSERT INTO tenant_finance.account_statement_payment (
        account_statement_uuid, payment_id, document_number, reference_number,
        payment_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 5002, 'PAG-002', 'TRANSF-002',
        '2026-01-30 14:00:00',
        'MXN', 2000.00, 1, 'MXN', 2000.00,
        'Aplicado', 1, NOW()
    );

END $$;


-- ============================================================================
-- 6. ORDENES DE COMPRA (Proveedor 1001)
-- ============================================================================

DO $$
DECLARE
    v_statement_uuid UUID;
BEGIN
    SELECT account_statement_uuid INTO v_statement_uuid
    FROM tenant_finance.account_statement
    WHERE vendor_number = 1001 AND year = 2026 AND month = 1 AND version = 1;

    INSERT INTO tenant_finance.account_statement_purchase_order (
        account_statement_uuid, order_number, document_date, due_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 900001, '2026-01-10', '2026-02-10',
        'MXN', 25000.00, 1, 'MXN', 25000.00,
        'Recibida', 1, NOW()
    );

    INSERT INTO tenant_finance.account_statement_purchase_order (
        account_statement_uuid, order_number, document_date, due_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 900002, '2026-01-18', '2026-02-18',
        'USD', 500.00, 17.50, 'MXN', 8750.00,
        'Pendiente', 1, NOW()
    );

END $$;


-- ============================================================================
-- 7. RECEPCIONES (Proveedor 1001)
-- ============================================================================

DO $$
DECLARE
    v_statement_uuid UUID;
BEGIN
    SELECT account_statement_uuid INTO v_statement_uuid
    FROM tenant_finance.account_statement
    WHERE vendor_number = 1001 AND year = 2026 AND month = 1 AND version = 1;

    INSERT INTO tenant_finance.account_statement_reception (
        account_statement_uuid, reception_number, order_number,
        document_date, reception_date, due_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 800001, 900001,
        '2026-01-10', '2026-01-12', '2026-02-12',
        'MXN', 25000.00, 1, 'MXN', 25000.00,
        'Completa', 1, NOW()
    );

END $$;


-- ============================================================================
-- 8. DATOS PARA PROVEEDOR 1002 (estado generado simple)
-- ============================================================================

DO $$
DECLARE
    v_statement_uuid UUID;
BEGIN
    SELECT account_statement_uuid INTO v_statement_uuid
    FROM tenant_finance.account_statement
    WHERE vendor_number = 1002 AND year = 2026 AND month = 1 AND version = 1;

    -- Una factura pendiente
    INSERT INTO tenant_finance.account_statement_invoice (
        account_statement_uuid, invoice_type, series, folio, uuid,
        stamp_date,
        currency, amount, exchange_rate, base_currency, base_amount,
        invoice_status, created_by, created_at
    ) VALUES (
        v_statement_uuid, 'PENDING', 'C', '3001', 'UUID-FAC-1002-001',
        '2026-01-25 15:00:00',
        'MXN', 25000.00, 1, 'MXN', 25000.00,
        'Pendiente de Pago', 1, NOW()
    );

END $$;


-- ============================================================================
-- 9. VERIFICACION DE DATOS
-- ============================================================================

SELECT '=== ESTADOS DE CUENTA INSERTADOS ===' as info;

SELECT
    as_tbl.account_statement_uuid,
    as_tbl.vendor_number,
    as_tbl.year,
    as_tbl.month,
    as_tbl.version,
    as_tbl.status,
    CASE as_tbl.status
        WHEN 1 THEN 'Generado'
        WHEN 2 THEN 'Publicado'
        WHEN 3 THEN 'Revisado'
        WHEN 4 THEN 'Rechazado'
        WHEN 5 THEN 'Reprocesado'
    END as status_desc,
    as_tbl.initial_balance,
    as_tbl.final_balance
FROM tenant_finance.account_statement as_tbl
ORDER BY as_tbl.vendor_number, as_tbl.version;

SELECT '=== RESUMEN DE DETALLES (Proveedor 1001) ===' as info;

SELECT
    'Invoices' as type,
    COUNT(*) as count,
    SUM(base_amount) as total_mxn
FROM tenant_finance.account_statement_invoice
WHERE account_statement_uuid = (SELECT account_statement_uuid FROM tenant_finance.account_statement WHERE vendor_number = 1001 AND version = 1)
UNION ALL
SELECT
    'Discounts',
    COUNT(*),
    SUM(base_amount)
FROM tenant_finance.account_statement_discount
WHERE account_statement_uuid = (SELECT account_statement_uuid FROM tenant_finance.account_statement WHERE vendor_number = 1001 AND version = 1)
UNION ALL
SELECT
    'Credit Notes',
    COUNT(*),
    SUM(base_amount)
FROM tenant_finance.account_statement_credit_note
WHERE account_statement_uuid = (SELECT account_statement_uuid FROM tenant_finance.account_statement WHERE vendor_number = 1001 AND version = 1)
UNION ALL
SELECT
    'Payments',
    COUNT(*),
    SUM(base_amount)
FROM tenant_finance.account_statement_payment
WHERE account_statement_uuid = (SELECT account_statement_uuid FROM tenant_finance.account_statement WHERE vendor_number = 1001 AND version = 1)
UNION ALL
SELECT
    'Purchase Orders',
    COUNT(*),
    SUM(base_amount)
FROM tenant_finance.account_statement_purchase_order
WHERE account_statement_uuid = (SELECT account_statement_uuid FROM tenant_finance.account_statement WHERE vendor_number = 1001 AND version = 1)
UNION ALL
SELECT
    'Receptions',
    COUNT(*),
    SUM(base_amount)
FROM tenant_finance.account_statement_reception
WHERE account_statement_uuid = (SELECT account_statement_uuid FROM tenant_finance.account_statement WHERE vendor_number = 1001 AND version = 1);


-- ============================================================================
-- FIN DEL SCRIPT DE DATOS DE PRUEBA
-- ============================================================================
